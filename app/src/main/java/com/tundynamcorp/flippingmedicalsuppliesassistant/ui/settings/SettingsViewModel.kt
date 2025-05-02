package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.SettingsRepository
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.invoice.SellerInfo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SettingsRepository(app)
    private val auth = FirebaseAuth.getInstance()
    private val db   = Firebase.database.reference

    /** DataStore-backed flow of SellerInfo (defaults to blank fields) */
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

    /** Persist the profile into DataStore */
    fun updateProfile(new: SellerInfo) {
        viewModelScope.launch {
            repo.saveProfile(new)
        }
    }
}
