// app/src/main/java/com/tundynamcorp/flippingmedicalsuppliesassistant/ui/home/BottomNavigationBar.kt
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
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.UserRole

/**
 * A bottom navigation bar with role-based enabled/disabled items.
 */
@Composable
fun BottomNavigationBar(
    navController: NavController,
    currentRole: UserRole,
    isTrialActive: Boolean
) {
    val effectiveOrdinal = when {
        currentRole == UserRole.User && isTrialActive -> UserRole.Subscriber.ordinal
        else                                           -> currentRole.ordinal
    }

    // route, icon, minimum role required to enable
    val items = listOf(
        Triple("home",    Icons.Filled.Home,                   UserRole.Guest),
        Triple("scan",    Icons.Filled.QrCodeScanner,          UserRole.Subscriber),
        Triple("invoice", Icons.AutoMirrored.Filled.ReceiptLong, UserRole.User),
        Triple("admin",   Icons.Outlined.AdminPanelSettings,   UserRole.Guest),
        Triple("settings",Icons.Filled.Settings,               UserRole.Subscriber)
    )

    NavigationBar {
        items.forEach { (route, icon, minRole) ->
            // compare by ordinal since enums are final Comparable
            val enabled = effectiveOrdinal >= minRole.ordinal
            NavigationBarItem(
                icon      = { Icon(icon, contentDescription = route) },
                selected  = false, // wire up selection logic as needed
                enabled   = enabled,
                onClick   = {
                    if (enabled) navController.navigate(route)
                    else {
                        // TODO: show upgrade prompt
                    }
                }
            )
        }
    }
}
