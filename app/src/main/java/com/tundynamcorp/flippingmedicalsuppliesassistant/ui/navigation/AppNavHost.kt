package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tundynamcorp.flippingmedicalsuppliesassistant.R
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home.HomeScreen
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home.BottomNavigationBar
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.HomeViewModel

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Persistent Logo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.fmsalogo),
                    contentDescription = "App logo",
                    modifier = Modifier.size(120.dp)
                )
            }

            // NavHost content takes up remaining space
            Box(modifier = Modifier.weight(1f)) {
                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable("home") {
                        // 1) Get your ViewModel
                        val vm: HomeViewModel = viewModel()
                        // 2) Observe the live data
                        val query by vm.query.collectAsState()
                        val products by vm.filteredProducts.collectAsState()

                        // 3) Feed into HomeScreen
                        HomeScreen(
                            products      = products,
                            query         = query,
                            onQueryChange = { vm.onQueryChanged(it) }
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

            // Banner ad stays here, outside NavHost
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Banner Ad Placeholder")
            }
        }
    }
}
