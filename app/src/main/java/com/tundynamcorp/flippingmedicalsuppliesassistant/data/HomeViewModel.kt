package com.tundynamcorp.flippingmedicalsuppliesassistant.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = HomeRepository()

    /** Product list & search **/
    private val _products = MutableStateFlow<List<Product>>(emptyList())

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    val filteredProducts: StateFlow<List<Product>> =
        combine(_products, _query) { list, q ->
            if (q.isBlank()) list
            else list.filter { it.description.startsWith(q, ignoreCase = true) }
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repo.getAllProducts()
                .catch { /* log or handle */ }
                .collect { list -> _products.value = list }
        }
    }

    fun onQueryChanged(new: String) {
        _query.value = new
    }

    /** Price‑history state **/
    private val _priceHistory = MutableStateFlow<PriceHistory?>(null)
    val priceHistory: StateFlow<PriceHistory?> = _priceHistory

    /** Kick off loading the history for one product */
    fun loadPriceHistory(category: String, barcode: String) {
        viewModelScope.launch {
            // Assuming you’ll add this to your repo:
            // suspend fun getPriceHistory(category: String, barcode: String): PriceHistory
            val ph = repo.getPriceHistory(category, barcode)
            _priceHistory.value = ph
        }
    }

    /** Clear the dialog data */
    fun clearPriceHistory() {
        _priceHistory.value = null
    }
}
