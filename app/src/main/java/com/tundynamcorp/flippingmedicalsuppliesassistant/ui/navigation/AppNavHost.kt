package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home.HomeScreen
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home.BottomNavigationBar

@Composable
fun AppNavHost() {
    // Create the NavController
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    products = emptyList(),            // placeholder until we wire Firebase
                    onProductClick = { /* … */ }
                )
            }
            composable("scan") {
                /* TODO: ScanScreen(navController) */
            }
            composable("invoice") {
                /* TODO: InvoiceScreen(navController) */
            }
            composable("admin") {
                /* TODO: AdminScreen(navController) */
            }
            composable("settings") {
                /* TODO: SettingsScreen(navController) */
            }
        }
    }
}
