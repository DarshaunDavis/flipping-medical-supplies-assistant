package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.SettingsRepository
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.invoice.SellerInfo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SettingsRepository(app)

    /** Expose the persisted profile as a StateFlow, with an initial blank SellerInfo */
    val profileInfo: StateFlow<SellerInfo> = repo.profileFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SellerInfo(
                name     = "",
                dba      = null,
                address1 = "",
                address2 = null,
                city     = "",
                state    = "",
                zip      = "",
                phone    = "",
                email    = null
            )
        )

    /** Call from ProfileTab’s Save button — writes through DataStore */
    fun updateProfile(new: SellerInfo) {
        viewModelScope.launch {
            repo.saveProfile(new)
        }
    }
}
