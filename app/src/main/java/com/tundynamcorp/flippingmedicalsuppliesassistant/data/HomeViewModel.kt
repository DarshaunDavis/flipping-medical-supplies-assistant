package com.tundynamcorp.flippingmedicalsuppliesassistant.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = HomeRepository()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    // Exposed, filtered list
    val filteredProducts: StateFlow<List<Product>> =
        combine(_products, _query) { list, q ->
            if (q.isBlank()) list else list.filter {
                it.description.contains(q, ignoreCase = true)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Kick off the load once
        viewModelScope.launch {
            repo.getAllProducts()
                .catch { /* handle or log */ }
                .collect { list ->
                    _products.value = list
                }
        }
    }

    fun onQueryChanged(new: String) {
        _query.value = new
    }
}
