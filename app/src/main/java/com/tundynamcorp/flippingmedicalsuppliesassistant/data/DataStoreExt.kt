package com.tundynamcorp.flippingmedicalsuppliesassistant.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.Preferences

// backs overrides DataStore under the name "override_prefs"
internal val Context.overrideStore: androidx.datastore.core.DataStore<Preferences> by preferencesDataStore(
    name = "override_prefs"
)
