package com.tundynamcorp.flippingmedicalsuppliesassistant.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class AdminRepository(private val context: Context) {
    // Define your app’s categories (adjust if you ever add/remove)
    private val categories = listOf("Test Strips", "Devices", "Inhalers", "Insulin")

    // Preference key for a given category
    private fun marginKey(category: String) =
        doublePreferencesKey("profit_margin_${category.replace(" ", "_")}")

    /** Emits a map of category → stored percent (defaults to 0.0) */
    val marginsFlow: Flow<Map<String, Double>> = context.dataStore.data
        .catch { e ->
            // on error (e.g. read failure), emit empty prefs
            if (e is IOException) emit(emptyPreferences())
            else throw e
        }
        .map { prefs: Preferences ->
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