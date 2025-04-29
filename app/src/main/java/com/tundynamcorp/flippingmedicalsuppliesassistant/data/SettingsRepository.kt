package com.tundynamcorp.flippingmedicalsuppliesassistant.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
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
    // 2️⃣ Preference keys
    private val NAME_KEY      = stringPreferencesKey("profile_name")
    private val DBA_KEY       = stringPreferencesKey("profile_dba")
    private val ADDR1_KEY     = stringPreferencesKey("profile_addr1")
    private val ADDR2_KEY     = stringPreferencesKey("profile_addr2")
    private val CITY_KEY      = stringPreferencesKey("profile_city")
    private val STATE_KEY     = stringPreferencesKey("profile_state")
    private val ZIP_KEY       = stringPreferencesKey("profile_zip")
    private val PHONE_KEY     = stringPreferencesKey("profile_phone")
    private val EMAIL_KEY     = stringPreferencesKey("profile_email")

    /** Stream that emits the saved SellerInfo whenever it changes */
    val profileFlow: Flow<SellerInfo> = context.settingsStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences())
            else throw e
        }
        .map { prefs ->
            SellerInfo(
                name     = prefs[NAME_KEY] ?: "",
                dba      = prefs[DBA_KEY],
                address1 = prefs[ADDR1_KEY] ?: "",
                address2 = prefs[ADDR2_KEY],
                city     = prefs[CITY_KEY] ?: "",
                state    = prefs[STATE_KEY] ?: "",
                zip      = prefs[ZIP_KEY] ?: "",
                phone    = prefs[PHONE_KEY] ?: "",
                email    = prefs[EMAIL_KEY]
            )
        }

    /** Persist a full SellerInfo */
    suspend fun saveProfile(info: SellerInfo) {
        context.settingsStore.edit { prefs ->
            prefs[NAME_KEY]  = info.name
            prefs[DBA_KEY]   = info.dba.orEmpty()
            prefs[ADDR1_KEY] = info.address1
            prefs[ADDR2_KEY] = info.address2.orEmpty()
            prefs[CITY_KEY]  = info.city
            prefs[STATE_KEY] = info.state
            prefs[ZIP_KEY]   = info.zip
            prefs[PHONE_KEY] = info.phone
            prefs[EMAIL_KEY] = info.email.orEmpty()
        }
    }
}
