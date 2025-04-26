package com.tundynamcorp.flippingmedicalsuppliesassistant.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class AdminRepository(private val context: Context) {
    private val categories = listOf("Test Strips", "Devices", "Inhalers", "Insulin")

    private fun visibilityKey(category: String) =
        booleanPreferencesKey("visibility_${category.replace(" ", "_")}")

    /** Persisted on/off per category */
    val visibilityFlow: Flow<Map<String, Boolean>> = context.dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences())
            else throw e
        }
        .map { prefs ->
            categories.associateWith { cat ->
                prefs[visibilityKey(cat)] ?: true
            }
        }

    /** Flip one category on or off */
    suspend fun setVisibility(category: String, visible: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[visibilityKey(category)] = visible
        }
    }

    private fun marginKey(category: String) =
        doublePreferencesKey("profit_margin_${category.replace(" ", "_")}")

    /** Emits a map of category → stored percent (defaults to 0.0) */
    val marginsFlow: Flow<Map<String, Double>> = context.dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences())
            else throw e
        }
        .map { prefs ->
            categories.associateWith { cat ->
                prefs[marginKey(cat)] ?: 0.0
            }
        }

    /** Persist a new percentage (coerced to 0–100) for one category */
    suspend fun setMargin(category: String, percent: Double) {
        context.dataStore.edit { prefs ->
            prefs[marginKey(category)] = percent.coerceIn(0.0, 100.0)
        }
    }
}
