package com.tundynamcorp.flippingmedicalsuppliesassistant.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class PriceOverrideRepository(private val context: Context) {
    /** key = "override_<barcode>_<index>" **/
    private fun overrideKey(barcode: String, index: Int) =
        intPreferencesKey("override_${barcode}_$index")

    /**
     * Builds a map of
     *   barcode → ( index → overriddenValue )
     */
    val overridesFlow: Flow<Map<String, Map<Int, Int>>> =
        context.overrideStore.data
            .catch { e ->
                if (e is IOException) emit(emptyPreferences())
                else throw e
            }
            .map { prefs: Preferences ->
                // turn each entry ("override_xxx_3" → 42) into Triple(barcode, index, value)
                prefs.asMap().mapNotNull { (key, value) ->
                    val name = key.name
                    if (!name.startsWith("override_")) return@mapNotNull null
                    val parts = name
                        .removePrefix("override_")
                        .split("_")
                    // last part is index, rest is barcode (in case barcode itself contains underscores)
                    val idx = parts.last().toIntOrNull() ?: return@mapNotNull null
                    val barcode = parts.dropLast(1).joinToString("_")
                    // DataStore stores numbers as Int (or Long), cast safely
                    val intVal = when (value) {
                        is Int  -> value
                        is Long -> value.toInt()
                        else    -> return@mapNotNull null
                    }
                    Triple(barcode, idx, intVal)
                }
                    // group them back into a nested map
                    .groupBy(
                        keySelector   = { it.first },
                        valueTransform = { it.second to it.third }
                    ).mapValues { it.value.toMap() }
            }

    /** Save one override (index 0..9) for a given barcode **/
    suspend fun setOverride(barcode: String, index: Int, newPrice: Int) {
        context.overrideStore.edit { prefs ->
            prefs[overrideKey(barcode, index)] = newPrice
        }
    }

    /** Remove *all* overrides for a given barcode **/
    suspend fun clearOverrides(barcode: String) {
        context.overrideStore.edit { prefs ->
            // find all keys matching this barcode and remove them
            prefs.asMap().keys
                .filter { it.name.startsWith("override_${barcode}_") }
                .forEach { prefs.remove(it) }
        }
    }
}
