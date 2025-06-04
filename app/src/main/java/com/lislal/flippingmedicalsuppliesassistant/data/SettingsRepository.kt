package com.lislal.flippingmedicalsuppliesassistant.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.lislal.flippingmedicalsuppliesassistant.ui.invoice.SellerInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// Use a single DataStore for both profile and wholesaler selections
private const val SETTINGS_NAME = "settings_prefs"
private val Context.settingsStore by preferencesDataStore(name = SETTINGS_NAME)

class SettingsRepository(private val context: Context) {
    // ─── Profile keys (unchanged) ─────────────────────────────────────────
    private val nameKey   = stringPreferencesKey("profile_name")
    private val dbaKey    = stringPreferencesKey("profile_dba")
    private val addr1Key  = stringPreferencesKey("profile_addr1")
    private val addr2Key  = stringPreferencesKey("profile_addr2")
    private val cityKey   = stringPreferencesKey("profile_city")
    private val stateKey  = stringPreferencesKey("profile_state")
    private val zipKey    = stringPreferencesKey("profile_zip")
    private val phoneKey  = stringPreferencesKey("profile_phone")
    private val emailKey  = stringPreferencesKey("profile_email")

    /** Flow of SellerInfo from DataStore **/
    val profileFlow: Flow<SellerInfo> = context.settingsStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences())
            else throw e
        }
        .map { prefs ->
            SellerInfo(
                name     = prefs[nameKey]   ?: "",
                dba      = prefs[dbaKey]?.takeIf { it.isNotBlank() },
                address1 = prefs[addr1Key] ?: "",
                address2 = prefs[addr2Key]?.takeIf { it.isNotBlank() },
                city     = prefs[cityKey] ?: "",
                state    = prefs[stateKey] ?: "",
                zip      = prefs[zipKey] ?: "",
                phone    = prefs[phoneKey] ?: "",
                email    = prefs[emailKey]?.takeIf { it.isNotBlank() }
            )
        }

    /** Persist full SellerInfo **/
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

    // ─── Wholesaler-selection keys ────────────────────────────────────────────
    private val wholesalerKeyPrefix = "selected_wholesaler_"
    private val categories = listOf("Test Strips", "Devices", "Inhalers", "Insulin")

    /**
     * Flow that emits a Map<Category, SelectedWholesaler?>
     * reading keys like "selected_wholesaler_Test_Strips" from DataStore
     */
    val selectedWholesalersMapFlow: Flow<Map<String, String?>> =
        context.settingsStore.data
            .catch { e ->
                if (e is IOException) emit(emptyPreferences())
                else throw e
            }
            .map { prefs ->
                categories.associateWith { category ->
                    prefs[stringPreferencesKey(
                        "$wholesalerKeyPrefix${category.replace(" ", "_")}"
                    )]
                }
            }

    /**
     * Persist a single selection.
     * If `wholesaler` is null, removes that preference key.
     */
    suspend fun saveSelectedWholesaler(category: String, wholesaler: String?) {
        val key = stringPreferencesKey(
            "$wholesalerKeyPrefix${category.replace(" ", "_")}"
        )
        context.settingsStore.edit { prefs ->
            if (wholesaler != null) prefs[key] = wholesaler
            else prefs.remove(key)
        }
    }
}
