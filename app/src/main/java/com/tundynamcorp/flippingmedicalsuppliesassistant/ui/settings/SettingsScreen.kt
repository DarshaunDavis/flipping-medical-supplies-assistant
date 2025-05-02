package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.auth.AuthViewModel

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = viewModel(),
    authViewModel: AuthViewModel       = viewModel()
) {
    val tabs = listOf("Profile", "Account")
    var selectedTab by remember { mutableIntStateOf(0) }
    val profileInfo by settingsViewModel.profileInfo.collectAsState()

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
                profileInfo = profileInfo,
                onSave = { newInfo ->
                    // 1) Persist to DataStore
                    settingsViewModel.updateProfile(newInfo)
                    // 2) Propagate the updated name into FirebaseAuth & RTDB
                    authViewModel.updateDisplayName(newInfo.name) { success, _ ->
                        // you could show a toast on failure if you like
                    }
                }
            )
            1 -> AccountTab()
        }
    }
}
