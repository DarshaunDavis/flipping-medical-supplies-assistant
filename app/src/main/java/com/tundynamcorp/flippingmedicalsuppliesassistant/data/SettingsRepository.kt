package com.tundynamcorp.flippingmedicalsuppliesassistant.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.invoice.SellerInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// 1️⃣ DataStore instance
private const val SETTINGS_NAME = "settings_prefs"
private val Context.settingsStore by preferencesDataStore(name = SETTINGS_NAME)

class SettingsRepository(private val context: Context) {
    // 2️⃣ Preference keys (lowerCamelCase)
    private val nameKey   = stringPreferencesKey("profile_name")
    private val dbaKey    = stringPreferencesKey("profile_dba")
    private val addr1Key  = stringPreferencesKey("profile_addr1")
    private val addr2Key  = stringPreferencesKey("profile_addr2")
    private val cityKey   = stringPreferencesKey("profile_city")
    private val stateKey  = stringPreferencesKey("profile_state")
    private val zipKey    = stringPreferencesKey("profile_zip")
    private val phoneKey  = stringPreferencesKey("profile_phone")
    private val emailKey  = stringPreferencesKey("profile_email")

    /** Stream that emits the saved SellerInfo whenever it changes */
    val profileFlow: Flow<SellerInfo> = context.settingsStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences())
            else throw e
        }
        .map { prefs ->
            SellerInfo(
                name     = prefs[nameKey]   ?: "",
                dba      = prefs[dbaKey],
                address1 = prefs[addr1Key]  ?: "",
                address2 = prefs[addr2Key],
                city     = prefs[cityKey]   ?: "",
                state    = prefs[stateKey]  ?: "",
                zip      = prefs[zipKey]    ?: "",
                phone    = prefs[phoneKey]  ?: "",
                email    = prefs[emailKey]
            )
        }

    /** Persist a full SellerInfo */
    suspend fun saveProfile(info: SellerInfo) {
        context.settingsStore.edit { prefs ->
            prefs[nameKey]  = info.name
            prefs[dbaKey]   = info.dba.orEmpty()
            prefs[addr1Key] = info.address1
            prefs[addr2Key] = info.address2.orEmpty()
            prefs[cityKey]  = info.city
            prefs[stateKey] = info.state
            prefs[zipKey]   = info.zip
            prefs[phoneKey] = info.phone
            prefs[emailKey] = info.email.orEmpty()
        }
    }
}
