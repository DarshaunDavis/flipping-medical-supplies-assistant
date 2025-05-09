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

    private var lastCategory: String? = null
    private var lastBarcode:  String? = null

    /** Products & Search **/
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    private val _query    = MutableStateFlow("")
    val query: StateFlow<String> = _query

    // ➊ Keep track of which buyer (if any) is selected per category:
    private val _selectedBuyerMap =
        MutableStateFlow<Map<String, String?>>(emptyMap())
    val selectedBuyerMap: StateFlow<Map<String, String?>> =
        _selectedBuyerMap.asStateFlow()

    fun setSelectedBuyer(category: String, buyer: String?) {
        _selectedBuyerMap.update { old ->
            old.toMutableMap().apply { put(category, buyer) }
        }
    }

    // ➋ Load the JSON index: category → (buyer → list of barcodes)
    private val barcodesByCategoryAndBuyer = repo
        .getBarcodesByCategoryAndBuyer()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyMap()
        )


    val filteredProducts: StateFlow<List<Product>> = combine(
        _products,
        _query,
        adminRepo.visibilityFlow,
        selectedBuyerMap,
        barcodesByCategoryAndBuyer
    ) { products, q, visMap, buyerMap, bcIndex ->
        products
            // only categories whose toggle is ON
            .filter { prod ->
                (visMap[prod.category] ?: false)
                        // AND if a buyer is selected, the barcode must live under that buyer
                        && (buyerMap[prod.category]?.let { buyer ->
                    bcIndex[prod.category]
                        ?.get(buyer)
                        ?.contains(prod.barcode) == true
                } ?: true)
            }
            // then your existing search
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
            // raw prices
            val raw = repo.getPriceHistory(category, barcode)

            // apply any stored overrides
            val allOverrides    = overrideRepo.overridesFlow.first()
            val overridesForBar = allOverrides[barcode] ?: emptyMap()

            // apply category margin
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

    /** Allow per‐month overrides of a barcode’s price **/
    fun overridePrice(barcode: String, index: Int, newPrice: Int) {
        viewModelScope.launch {
            // persist override
            overrideRepo.setOverride(barcode, index, newPrice)
            // update in‐memory immediately
            _priceHistory.value = _priceHistory.value
                ?.copy(prices = _priceHistory.value!!.prices
                    .mapIndexed { i, v -> if (i == index) newPrice.toFloat() else v })
        }
    }

    /** Clear all overrides for a barcode and reload its history **/
    fun resetOverrides(category: String, barcode: String) {
        lastCategory = category
        lastBarcode  = barcode

        viewModelScope.launch {
            // remove persisted overrides
            overrideRepo.clearOverrides(barcode)
            // re‐fetch fresh history (with only category margin)
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
