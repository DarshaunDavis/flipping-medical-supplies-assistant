package com.lislal.flippingmedicalsuppliesassistant.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

// backs your Preferences DataStore under the name "admin_prefs"
val Context.dataStore by preferencesDataStore(name = "admin_prefs")
