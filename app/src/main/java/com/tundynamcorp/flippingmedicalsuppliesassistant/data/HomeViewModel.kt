// app/src/main/java/com/tundynamcorp/flippingmedicalsuppliesassistant/data/HomeViewModel.kt
package com.tundynamcorp.flippingmedicalsuppliesassistant.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlin.math.roundToInt

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val settingsRepo = SettingsRepository(app)
    private val repo         = HomeRepository()
    private val adminRepo    = AdminRepository(app)
    private val overrideRepo = PriceOverrideRepository(app)

    // ─── Raw products & search query ─────────────────────────────────────
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    private val _query    = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    // ─── ➊ Selected‐buyer map (backed by DataStore) ───────────────────────
    val selectedBuyerMap: StateFlow<Map<String, String?>> =
        settingsRepo.selectedBuyersMapFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun setSelectedBuyer(category: String, buyer: String?) {
        viewModelScope.launch { settingsRepo.saveSelectedBuyer(category, buyer) }
    }

    // ─── ➋ Index: category → ( buyer → [barcodes] ) ──────────────────────
    private val barcodesByCategoryAndBuyer: StateFlow<Map<String, Map<String, List<String>>>> =
        repo.getBarcodesByCategoryAndBuyer()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    // ─── ➌ Flatten to simple buyer lists for the spinner ────────────────
    val buyersByCategory: StateFlow<Map<String, List<String>>> =
        barcodesByCategoryAndBuyer
            .map { idx -> idx.mapValues { it.value.keys.sorted() } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    // ─── ➍ Combined + filtered products ─────────────────────────────────
    val filteredProducts: StateFlow<List<Product>> = combine(
        _products,
        _query,
        adminRepo.visibilityFlow,
        selectedBuyerMap,
        barcodesByCategoryAndBuyer
    ) { products, q, visMap, selBuyerMap, index ->
        products
            .filter { prod ->
                val cat = prod.category

                // 1) must have at least one buyer for this category
                val hasBuyers = index[cat]?.isNotEmpty() == true
                if (!hasBuyers) return@filter false

                // 2) must be visible per the toggle
                val isVisible = visMap[cat] ?: false
                if (!isVisible) return@filter false

                // 3) must match the selected buyer’s bucket
                val buyer = selBuyerMap[cat]
                val inBucket = buyer
                    ?.let { b -> index[cat]?.get(b)?.contains(prod.barcode) == true }
                    ?: true
                inBucket
            }
            .let { list ->
                if (q.isBlank()) list
                else list.filter { it.description.startsWith(q, ignoreCase = true) }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        // 1) Fetch flat product list
        viewModelScope.launch {
            repo.getAllProducts()
                .catch { /* log or handle */ }
                .collect { _products.value = it }
        }

        // 2) Default buyer for key categories on first run
        viewModelScope.launch {
            selectedBuyerMap.first().also { m ->
                listOf("Test Strips", "Devices").forEach { cat ->
                    if (m[cat].isNullOrBlank()) {
                        setSelectedBuyer(cat, "Strip Flip")
                    }
                }
            }
        }
    }

    /** Search box text changes */
    fun onQueryChanged(new: String) {
        _query.value = new
    }

    // ─── Price‐history plumbing ──────────────────────────────────────────
    private val _priceHistory = MutableStateFlow<PriceHistory?>(null)
    val priceHistory: StateFlow<PriceHistory?> = _priceHistory.asStateFlow()

    fun loadPriceHistory(category: String, barcode: String) {
        viewModelScope.launch {
            // ▶ pull the *selected* buyer for this category
            val buyer = selectedBuyerMap.value[category] ?: return@launch
            val raw   = repo.getPriceHistory(category, barcode, buyer)
            val overridesForBar = overrideRepo.overridesFlow.first()[barcode] ?: emptyMap()
            val marginPct      = adminRepo.marginsFlow.first()[category] ?: 0.0

            val finalPrices = raw.prices.mapIndexed { idx, dbPrice ->
                overridesForBar[idx]?.toFloat()
                    ?: ((dbPrice * (1 - marginPct / 100))
                        .roundToInt()
                        .toFloat())
            }
            _priceHistory.value = raw.copy(prices = finalPrices)
        }
    }

    fun clearPriceHistory() {
        _priceHistory.value = null
    }

    fun overridePrice(barcode: String, index: Int, newPrice: Int) {
        viewModelScope.launch {
            overrideRepo.setOverride(barcode, index, newPrice)
            _priceHistory.value = _priceHistory.value
                ?.copy(prices = _priceHistory.value!!.prices
                    .mapIndexed { i, v -> if (i == index) newPrice.toFloat() else v })
        }
    }

    fun resetOverrides(category: String, barcode: String) {
        viewModelScope.launch {
            overrideRepo.clearOverrides(barcode)
            loadPriceHistory(category, barcode)
        }
    }
}
