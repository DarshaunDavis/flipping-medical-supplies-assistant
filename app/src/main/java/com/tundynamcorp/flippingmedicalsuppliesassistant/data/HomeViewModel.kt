package com.tundynamcorp.flippingmedicalsuppliesassistant.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlin.math.roundToInt

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val settingsRepo = SettingsRepository(getApplication())
    private val repo         = HomeRepository()
    private val adminRepo    = AdminRepository(app)
    private val overrideRepo = PriceOverrideRepository(app)

    // — raw product list from network
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    // — search query
    private val _query    = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    /** ➊ DataStore-backed map of your chosen buyer per category */
    val selectedBuyerMap: StateFlow<Map<String, String?>> =
        settingsRepo.selectedBuyersMapFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyMap()
            )

    /** ➋ When user picks a buyer, write it out to DataStore */
    fun setSelectedBuyer(category: String, buyer: String?) {
        viewModelScope.launch {
            settingsRepo.saveSelectedBuyer(category, buyer)
        }
    }

    // ➋ Load index: category → ( buyer → list of barcodes )
    private val barcodesByCategoryAndBuyer: StateFlow<Map<String, Map<String, List<String>>>> =
        repo.getBarcodesByCategoryAndBuyer()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    /**
     * ➌ Combined, filtered list of products:
     *   • Only categories whose toggle is ON
     *   • Only if a buyer is selected for that category
     *   • Only barcodes under that buyer in the index
     *   • Then apply the text‐search filter
     */
    val filteredProducts: StateFlow<List<Product>> = combine(
        _products,
        _query,
        adminRepo.visibilityFlow,
        selectedBuyerMap,
        barcodesByCategoryAndBuyer
    ) { products, q, visMap, buyerMap, index ->
        products
            .filter { prod ->
                val cat = prod.category
                val isVisible = visMap[cat] ?: false
                val buyer     = buyerMap[cat]
                // toggle must be on, a buyer must be chosen, and this barcode in their bucket
                isVisible &&
                        buyer != null &&
                        (index[cat]?.get(buyer)?.contains(prod.barcode) == true)
            }
            .let { list ->
                if (q.isBlank()) list
                else list.filter { it.description.startsWith(q, ignoreCase = true) }
            }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            repo.getAllProducts()
                .catch { /* log or handle error */ }
                .collect { _products.value = it }
        }
    }

    fun onQueryChanged(new: String) {
        _query.value = new
    }

    // — Price history plumbing (unchanged) —
    private val _priceHistory = MutableStateFlow<PriceHistory?>(null)
    val priceHistory: StateFlow<PriceHistory?> = _priceHistory.asStateFlow()

    fun loadPriceHistory(category: String, barcode: String) {
        viewModelScope.launch {
            val raw = repo.getPriceHistory(category, barcode)
            val overridesForBar = overrideRepo.overridesFlow.first()[barcode] ?: emptyMap()
            val marginPct = adminRepo.marginsFlow.first()[category] ?: 0.0
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

    // — Buyer list for your dropdowns (unchanged) —
    val buyersByCategory: StateFlow<Map<String, List<String>>> =
        barcodesByCategoryAndBuyer
            .map { index -> index.mapValues { it.value.keys.sorted() } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())
}
