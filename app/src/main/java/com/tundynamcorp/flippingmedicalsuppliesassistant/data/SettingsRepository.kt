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

// 1️⃣ Create your DataStore instance
private const val SETTINGS_NAME = "settings_prefs"
private val Context.settingsStore by preferencesDataStore(name = SETTINGS_NAME)

class SettingsRepository(private val context: Context) {
    // 2️⃣ Your existing profile‐fields keys
    private val nameKey      = stringPreferencesKey("profile_name")
    private val dbaKey       = stringPreferencesKey("profile_dba")
    private val addr1Key     = stringPreferencesKey("profile_addr1")
    private val addr2Key     = stringPreferencesKey("profile_addr2")
    private val cityKey      = stringPreferencesKey("profile_city")
    private val stateKey     = stringPreferencesKey("profile_state")
    private val zipKey       = stringPreferencesKey("profile_zip")
    private val phoneKey     = stringPreferencesKey("profile_phone")
    private val emailKey     = stringPreferencesKey("profile_email")

    // 3️⃣ NEW: key for your “category=buyer;category2=buyer2” string
    private val buyersMapKey = stringPreferencesKey("selected_buyers_map")

    /** Stream your saved SellerInfo (unchanged) */
    val profileFlow: Flow<SellerInfo> = context.settingsStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences())
            else throw e
        }
        .map { prefs ->
            SellerInfo(
                name     = prefs[nameKey] ?: "",
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

    /** Save a full SellerInfo (unchanged) */
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

    /**
     * 4️⃣ Stream your saved “which buyer for which category” as a Map<String,String?>:
     *
     *   - We store them in one semicolon-delimited string under `buyersMapKey`.
     *   - Here we split by `;`, then by `=`, safely.
     */
    val selectedBuyersMapFlow: Flow<Map<String, String?>> = context.settingsStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences())
            else throw e
        }
        .map { prefs ->
            prefs[buyersMapKey]
                // split into ["Test Strips=StripFlip", "Devices=OtherCo", …]
                ?.split(';')
                // drop any blank entries, then split each into key/value
                ?.mapNotNull { entry ->
                    val parts = entry.split('=', limit = 2)
                    val category = parts.getOrNull(0)
                    val buyer    = parts.getOrNull(1)
                    // only keep if category is non-blank
                    if (!category.isNullOrBlank()) {
                        category to buyer
                    } else null
                }
                // make a Map<String,String?> and return it
                ?.toMap()
            // fallback to empty map
                ?: emptyMap()
        }

    /**
     * 5️⃣ Save or clear one category’s buyer:
     *    - We read the old string, parse into MutableMap
     *    - Add/update or remove this category
     *    - Write back the joined `key=value;key2=value2` string
     */
    suspend fun saveSelectedBuyer(category: String, buyer: String?) {
        context.settingsStore.edit { prefs ->
            // parse existing into MutableMap
            val existing: MutableMap<String, String?> =
                prefs[buyersMapKey]
                    ?.split(';')
                    ?.mapNotNull { entry ->
                        val parts = entry.split('=', limit = 2)
                        val cat = parts.getOrNull(0)
                        val buy = parts.getOrNull(1)
                        if (!cat.isNullOrBlank()) cat to buy else null
                    }
                    ?.toMap()
                    ?.toMutableMap()
                    ?: mutableMapOf()

            // update or remove this category
            if (buyer.isNullOrBlank()) {
                existing.remove(category)
            } else {
                existing[category] = buyer
            }

            // write it back as one string
            prefs[buyersMapKey] = existing.entries
                .joinToString(";") { "${it.key}=${it.value.orEmpty()}" }
        }
    }
}
