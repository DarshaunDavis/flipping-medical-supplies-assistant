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

    /** Expose the category→margin map to Compose */
    val margins: StateFlow<Map<String, Double>> =
        repo.marginsFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyMap()
            )

    /** Expose the on/off visibility map */
    val visibility: StateFlow<Map<String, Boolean>> = repo.visibilityFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = emptyMap())

    /** Update one category’s margin */
    fun updateMargin(category: String, percent: Double) {
        viewModelScope.launch {
            repo.setMargin(category, percent)
        }
    }

    fun setVisibility(category: String, visible: Boolean) {
        viewModelScope.launch { repo.setVisibility(category, visible) }
    }
}
