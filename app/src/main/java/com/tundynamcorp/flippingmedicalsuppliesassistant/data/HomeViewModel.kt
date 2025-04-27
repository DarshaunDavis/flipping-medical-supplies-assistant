package com.tundynamcorp.flippingmedicalsuppliesassistant.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlin.math.roundToInt

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

    /** Price‐History (with margin & overrides baked in) **/
    private val _priceHistory = MutableStateFlow<PriceHistory?>(null)
    val priceHistory: StateFlow<PriceHistory?> = _priceHistory

    fun loadPriceHistory(category: String, barcode: String) {
        lastCategory = category
        lastBarcode  = barcode

        viewModelScope.launch {
            // 1) get the raw monthly prices (exactly as in DB)
            val raw = repo.getPriceHistory(category, barcode)

            // 2) snapshot current overrides for this barcode
            val allOverrides    = overrideRepo.overridesFlow.first()
            val overridesForBar = allOverrides[barcode] ?: emptyMap()

            // 3) grab the current margin % for this category
            val marginPct = adminRepo
                .marginsFlow
                .first()[category]
                ?: 0.0

            // 4) build a single “final” list:
            //    if override exists use it,
            //    otherwise apply margin discount to raw
            val finalPrices = raw.prices.mapIndexed { idx, dbPrice ->
                overridesForBar[idx]?.toFloat()
                    ?: ((dbPrice * (1 - marginPct / 100))
                        .roundToInt()
                        .toFloat())
            }

            // 5) emit a cloned PriceHistory with our patched numbers
            _priceHistory.value = raw.copy(prices = finalPrices)
        }
    }

    fun clearPriceHistory() {
        _priceHistory.value = null
    }

    /** Price‐override helpers **/
    /** Now takes only barcode, index, newPrice; category was unused **/
    fun overridePrice(barcode: String, index: Int, newPrice: Int) {
        viewModelScope.launch {
            overrideRepo.setOverride(barcode, index, newPrice)
            // also update in-memory immediately
            _priceHistory.value = _priceHistory.value
                ?.copy(prices = _priceHistory.value!!.prices
                    .mapIndexed { i, v -> if (i == index) newPrice.toFloat() else v })
        }
    }

    fun resetOverrides(category: String, barcode: String) {
        lastCategory = category
        lastBarcode  = barcode

        viewModelScope.launch {
            overrideRepo.clearOverrides(barcode)
            // re-fetch raw (no overrides now, margin auto-applied)
            lastCategory?.let { cat ->
                lastBarcode?.let { code ->
                    loadPriceHistory(cat, code)
                }
            }
        }
    }

    /** Buyers by Category **/
    val buyersByCategory: StateFlow<Map<String, List<String>>> =
        repo.getBuyersByCategory()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())
}
