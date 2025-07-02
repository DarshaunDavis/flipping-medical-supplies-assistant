package com.lislal.flippingmedicalsuppliesassistant.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.lislal.flippingmedicalsuppliesassistant.data.UserRole

@Composable
fun BottomNavigationBar(
    navController: NavController,
    currentRole: UserRole,
    hasFullAccess: Boolean
) {
    val items = listOf(
        Triple("home",    Icons.Filled.Home,                      UserRole.Guest),
        Triple("scan",    Icons.Filled.QrCodeScanner,             UserRole.Subscriber),
        Triple("invoice", Icons.AutoMirrored.Filled.ReceiptLong, UserRole.User),
        Triple("admin",   Icons.Outlined.AdminPanelSettings,      UserRole.Admin),
        Triple("settings",Icons.Filled.Settings,                  UserRole.Subscriber)
    )

    val effectiveRole = if (hasFullAccess) UserRole.Subscriber else currentRole

    NavigationBar {
        items.forEach { (route, icon, minRole) ->
            val enabled = effectiveRole.ordinal >= minRole.ordinal

            NavigationBarItem(
                icon = { Icon(icon, contentDescription = route) },
                selected = false,
                enabled = enabled,
                onClick = {
                    if (enabled) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    } else {
                        // TODO: Show upgrade prompt
                    }
                }
            )
        }
    }
}
