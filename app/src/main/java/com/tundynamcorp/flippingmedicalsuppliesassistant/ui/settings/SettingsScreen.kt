// File: SettingsScreen.kt
package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.invoice.SellerInfo

/**
 * SettingsHost: two tabs, delegating to ProfileTab and AccountTab.
 * Manages local profileInfo state for editing.
 */
@Composable
fun SettingsScreen() {
    val tabs = listOf("Profile", "Account")
    var selectedTab by remember { mutableIntStateOf(0) }

    // Local profile info state, initialized with empty/defaults
    var profileInfo by remember {
        mutableStateOf(
            SellerInfo(
                name = "",
                dba = null,
                address1 = "",
                address2 = null,
                city = "",
                state = "",
                zip = "",
                phone = "",
                email = null
            )
        )
    }

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
                onSave = { updated -> profileInfo = updated }
            )
            1 -> AccountTab()
        }
    }
}
