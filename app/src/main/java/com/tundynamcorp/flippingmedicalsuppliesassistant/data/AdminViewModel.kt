package com.tundynamcorp.flippingmedicalsuppliesassistant.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AdminRepository(app)

    /** Category → stored profit-margin % */
    val margins: StateFlow<Map<String, Double>> =
        repo.marginsFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyMap()
            )

    /** Category → visible? */
    val visibility: StateFlow<Map<String, Boolean>> =
        repo.visibilityFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyMap()
            )

    /** Update one category’s margin */
    fun updateMargin(category: String, percent: Double) {
        viewModelScope.launch {
            repo.setMargin(category, percent)
        }
    }

    /** Toggle one category’s on/off visibility */
    fun setVisibility(category: String, visible: Boolean) {
        viewModelScope.launch {
            repo.setVisibility(category, visible)
        }
    }
}
