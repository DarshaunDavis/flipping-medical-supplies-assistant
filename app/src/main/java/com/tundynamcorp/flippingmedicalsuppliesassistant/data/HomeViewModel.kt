package com.tundynamcorp.flippingmedicalsuppliesassistant.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = HomeRepository()
    private val adminRepo  = AdminRepository(app)

    /** Products & search **/
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    private val _query    = MutableStateFlow("")
    val query: StateFlow<String> = _query

    /** Combine products + query + visibility toggles */
    val filteredProducts: StateFlow<List<Product>> = combine(
        _products,
        _query,
        adminRepo.visibilityFlow
    ) { products, q, visMap ->
        products
            .filter { (visMap[it.category] ?: true) }       // respect toggles
            .let { list ->
                if (q.isBlank()) list
                else list.filter { it.description.startsWith(q, ignoreCase = true) }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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

    /** Price-history **/
    private val _priceHistory = MutableStateFlow<PriceHistory?>(null)
    val priceHistory: StateFlow<PriceHistory?> = _priceHistory

    fun loadPriceHistory(category: String, barcode: String) {
        viewModelScope.launch {
            val ph = repo.getPriceHistory(category, barcode)
            _priceHistory.value = ph
        }
    }

    fun clearPriceHistory() {
        _priceHistory.value = null
    }

    /** Buyers by category **/
    val buyersByCategory: StateFlow<Map<String, List<String>>> =
        repo.getBuyersByCategory()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())
}
