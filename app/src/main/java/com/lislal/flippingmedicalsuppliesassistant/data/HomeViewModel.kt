package com.lislal.flippingmedicalsuppliesassistant.data

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

    // ─── ➊ Selected‐wholesaler map (backed by DataStore) ───────────────────────
    val selectedWholesalerMap: StateFlow<Map<String, String?>> =
        settingsRepo.selectedWholesalersMapFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun setSelectedWholesaler(category: String, wholesaler: String?) {
        viewModelScope.launch { settingsRepo.saveSelectedWholesaler(category, wholesaler) }
    }

    // ─── barcodesByCategoryAndWholesaler & wholesalersByCategory unchanged…
    private val barcodesByCategoryAndWholesaler: StateFlow<Map<String, Map<String, List<String>>>> =
        repo.getBarcodesByCategoryAndWholesaler()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val wholesalersByCategory: StateFlow<Map<String, List<String>>> =
        barcodesByCategoryAndWholesaler
            .map { idx -> idx.mapValues { it.value.keys.sorted() } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    // ─── Combined + filtered products unchanged…
    val filteredProducts: StateFlow<List<Product>> = combine(
        _products,
        _query,
        adminRepo.visibilityFlow,
        selectedWholesalerMap,
        barcodesByCategoryAndWholesaler
    ) { products, q, visMap, selWholesalerMap, index ->
        products
            .filter { prod ->
                val cat = prod.category
                val hasWholesalers = index[cat]?.isNotEmpty() == true
                if (!hasWholesalers) return@filter false
                if (visMap[cat] != true) return@filter false

                // keep everything if no wholesaler selected, else filter by bucket
                selWholesalerMap[cat]?.let { b ->
                    index[cat]?.get(b)?.contains(prod.barcode) == true
                } ?: true
            }
            .let { list ->
                if (q.isBlank()) list
                else list.filter { it.description.startsWith(q, ignoreCase = true) }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        // 📦 load products
        viewModelScope.launch {
            repo.getAllProducts()
                .catch { /* log or handle */ }
                .collect { _products.value = it }
        }

        // 💾 default wholesaler seeding unchanged…
        viewModelScope.launch {
            settingsRepo.selectedWholesalersMapFlow
                .first()
                .also { m ->
                    listOf("Test Strips", "Devices").forEach { cat ->
                        if (m[cat].isNullOrBlank()) {
                            setSelectedWholesaler(cat, "Strip Flip")
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
            // ▶ pull the *selected* wholesaler, or FALL BACK to the default
            val wholesaler = selectedWholesalerMap.value[category]
                .takeUnless { it.isNullOrBlank() }
                ?: "Strip Flip"

            // now we always fetch, even if the user never explicitly selected one
            val raw = repo.getPriceHistory(category, barcode, wholesaler)
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
