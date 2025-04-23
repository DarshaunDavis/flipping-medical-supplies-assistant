// app/src/main/java/com/tundynamcorp/flippingmedicalsuppliesassistant/data/AdminViewModel.kt
package com.tundynamcorp.flippingmedicalsuppliesassistant.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AdminRepository(app)

    /** Expose the current profit‐margin (defaults to 0.0) */
    val profitMargin: StateFlow<Double> =
        repo.profitMarginFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0.0
            )

    /** Update the stored profit margin */
    fun setProfitMargin(percent: Double) {
        viewModelScope.launch {
            repo.setProfitMargin(percent)
        }
    }
}
