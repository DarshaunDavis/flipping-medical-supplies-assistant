package com.lislal.flippingmedicalsuppliesassistant.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lislal.flippingmedicalsuppliesassistant.ui.auth.AuthViewModel

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = viewModel(),
    authViewModel:    AuthViewModel       = viewModel()
) {
    val tabs = listOf("Profile", "Account")
    var selectedTab by remember { mutableIntStateOf(0) }

    // DataStore-backed fallback
    val dsProfile by settingsViewModel.profileInfo.collectAsState()

    // RTDB-backed profile (null if not loaded yet)
    val dbProfile by authViewModel.profileInfo.collectAsState()

    // Read-only profile: prefer DB, fallback to DataStore
    val readOnlyProfile = dbProfile ?: dsProfile

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { i, title ->
                Tab(
                    selected = selectedTab == i,
                    onClick  = { selectedTab = i },
                    text     = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> ProfileTab(
                profileInfo = readOnlyProfile,
                onSave = { newInfo ->
                    // 1️⃣ Persist into DataStore
                    settingsViewModel.updateProfile(newInfo)
                    // 2️⃣ Mirror *all* fields into RTDB & Auth profile
                    authViewModel.updateProfile(newInfo) { _, _ ->
                        // optionally show a Toast on error
                    }
                }
            )
            1 -> AccountTab()
        }
    }
}
