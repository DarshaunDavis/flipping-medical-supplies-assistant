package com.tundynamcorp.flippingmedicalsuppliesassistant.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

// This will back your Preferences DataStore under the name "admin_prefs"
internal val Context.dataStore by preferencesDataStore(name = "admin_prefs")
