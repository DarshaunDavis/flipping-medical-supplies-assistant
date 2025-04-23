package com.tundynamcorp.flippingmedicalsuppliesassistant.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AdminRepository(private val context: Context) {

    companion object {
        // Key under which we store the profit percentage
        private val PROFIT_MARGIN_KEY = doublePreferencesKey("profit_margin")
    }

    /** A cold Flow that emits the current profit margin (defaults to 0.0 if unset) */
    val profitMarginFlow: Flow<Double> = context.dataStore.data
        .map { prefs: Preferences ->
            prefs[PROFIT_MARGIN_KEY] ?: 0.0
        }

    /** Call this to save a new profit margin */
    suspend fun setProfitMargin(margin: Double) {
        context.dataStore.edit { prefs ->
            prefs[PROFIT_MARGIN_KEY] = margin
        }
    }
}
