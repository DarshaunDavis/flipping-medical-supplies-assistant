package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableIntStateOf

/**
 * Settings screen with two tabs: Profile & Account.
 */
@Composable
fun SettingsScreen() {
    val tabs = listOf("Profile", "Account")
    var selectedTab by remember { mutableIntStateOf(0) }

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
            0 -> ProfileTab()
            1 -> AccountTab()
        }
    }
}

/**
 * Profile tab: user profile and company info fields.
 */
@Composable
fun ProfileTab() {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Profile Information", modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("Name") },
            modifier = Modifier.fillMaxSize().padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("Address") },
            modifier = Modifier.fillMaxSize().padding(bottom = 8.dp)
        )
        // TODO: add more profile fields (Phone, Email, etc.)
        Button(onClick = { /* Save profile changes */ }) {
            Text("Save")
        }
    }
}

/**
 * Account tab: password change and other account settings.
 */
@Composable
fun AccountTab() {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Account Settings", modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("Current Password") },
            modifier = Modifier.fillMaxSize().padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("New Password") },
            modifier = Modifier.fillMaxSize().padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxSize().padding(bottom = 16.dp)
        )
        Button(onClick = { /* Save password */ }) {
            Text("Change Password")
        }
    }
}
