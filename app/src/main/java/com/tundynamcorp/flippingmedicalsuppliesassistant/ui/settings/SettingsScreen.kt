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

/**
 * SettingsHost: two tabs, delegating to ProfileTab and AccountTab.
 * Manages local profileInfo state for editing.
 */
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = viewModel()) {
    val tabs = listOf("Profile", "Account")
    var selectedTab by remember { mutableIntStateOf(0) }

    // Pull the saved profile out of the ViewModel
    val profileInfo by settingsViewModel.profileInfo.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> ProfileTab(
                profileInfo = profileInfo,
                onSave = { settingsViewModel.updateProfile(it) }
            )
            1 -> AccountTab()
        }
    }
}
