package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

/**
 * A bottom navigation bar with fixed routes.
 */
@Composable
fun BottomNavigationBar(
    navController: NavController,
    onItemSelected: (String) -> Unit = { navController.navigate(it) }
) {
    val items = listOf(
        "home" to Icons.Filled.Home,
        "scan" to Icons.Filled.QrCodeScanner,
        "invoice" to Icons.AutoMirrored.Filled.ReceiptLong,
        "admin" to Icons.Outlined.AdminPanelSettings,
        "settings" to Icons.Filled.Settings
    )
    NavigationBar {
        items.forEach { (route, icon) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = route) },
                selected = false, // update logic when implementing state
                onClick = { onItemSelected(route) }
            )
        }
    }
}
