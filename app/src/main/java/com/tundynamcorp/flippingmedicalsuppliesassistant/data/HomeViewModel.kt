package com.tundynamcorp.flippingmedicalsuppliesassistant.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.HomeRepository
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.AdminRepository
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.PriceOverrideRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo         = HomeRepository()
    private val adminRepo    = AdminRepository(app)
    private val overrideRepo = PriceOverrideRepository(app)

    // Remember what product we last loaded, so reset can re-fetch it
    private var lastCategory: String? = null
    private var lastBarcode:  String? = null

    /** Products & Search **/
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    private val _query    = MutableStateFlow("")
    val query: StateFlow<String> = _query

    val filteredProducts: StateFlow<List<Product>> = combine(
        _products,
        _query,
        adminRepo.visibilityFlow
    ) { products, q, visMap ->
        products
            .filter { visMap[it.category] ?: true }
            .let { list ->
                if (q.isBlank()) list
                else list.filter { it.description.startsWith(q, ignoreCase = true) }
            }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            repo.getAllProducts()
                .catch { /* log or handle */ }
                .collect { _products.value = it }
        }
    }

    fun onQueryChanged(new: String) {
        _query.value = new
    }

    /** Price-History (with overrides applied) **/
    private val _priceHistory = MutableStateFlow<PriceHistory?>(null)
    val priceHistory: StateFlow<PriceHistory?> = _priceHistory

    fun loadPriceHistory(category: String, barcode: String) {
        lastCategory = category
        lastBarcode  = barcode

        viewModelScope.launch {
            // 1) Fetch raw history
            val raw = repo.getPriceHistory(category, barcode)

            // 2) Grab the single-shot snapshot of all overrides
            val allOverrides    = overrideRepo.overridesFlow.first()
            val overridesForBar = allOverrides[barcode] ?: emptyMap()

            // 3) Build a patched list: use override if present, else raw
            val patchedPrices = raw.prices.mapIndexed { idx, original ->
                overridesForBar[idx]?.toFloat() ?: original
            }

            // 4) Emit a copy with overrides applied
            _priceHistory.value = raw.copy(prices = patchedPrices)
        }
    }

    fun clearPriceHistory() {
        _priceHistory.value = null
    }

    /** Per-price override APIs **/
    fun overridePrice(category: String, barcode: String, index: Int, newPrice: Int) {
        viewModelScope.launch {
            // Persist the override
            overrideRepo.setOverride(barcode, index, newPrice)

            // Immediately patch the in-memory dialog
            _priceHistory.value = _priceHistory.value
                ?.copy(prices = _priceHistory.value!!.prices.mapIndexed { i, v ->
                    if (i == index) newPrice.toFloat() else v
                })
        }
    }

    fun resetOverrides(category: String, barcode: String) {
        lastCategory = category
        lastBarcode  = barcode

        viewModelScope.launch {
            // Clear persisted overrides
            overrideRepo.clearOverrides(barcode)
            // Re-fetch raw history (no overrides now)
            lastCategory?.let { cat ->
                lastBarcode?.let { code ->
                    _priceHistory.value = repo.getPriceHistory(cat, code)
                }
            }
        }
    }

    /** Buyers by Category **/
    val buyersByCategory: StateFlow<Map<String, List<String>>> =
        repo.getBuyersByCategory()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())
}
