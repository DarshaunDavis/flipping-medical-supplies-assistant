package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tundynamcorp.flippingmedicalsuppliesassistant.R
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.HomeViewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.Product
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home.BottomNavigationBar
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home.HomeScreen
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home.PriceHistoryDialog
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.admin.AdminScreen
import kotlin.math.roundToInt

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

            // NavHost content
            Box(modifier = Modifier.weight(1f)) {
                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable("home") {
                        // ViewModels
                        val homeVm: HomeViewModel = viewModel()
                        val adminVm: HomeViewModel = viewModel() // assumes same scope, or use AdminViewModel if separate

                        // Observed state
                        val query    by homeVm.query.collectAsState()
                        val products by homeVm.filteredProducts.collectAsState()
                        val ph       by homeVm.priceHistory.collectAsState()
                        val margins  by viewModel<com.tundynamcorp.flippingmedicalsuppliesassistant.data.AdminViewModel>()
                            .margins.collectAsState()

                        // Local dialog control
                        var selectedProduct by remember { mutableStateOf<Product?>(null) }

                        // 1) Pure list + search
                        HomeScreen(
                            products      = products,
                            query         = query,
                            onQueryChange = { homeVm.onQueryChanged(it) },
                            onProductClick = { prod ->
                                homeVm.loadPriceHistory(prod.category, prod.barcode)
                                selectedProduct = prod
                            }
                        )

                        // 2) Only one dialog, once history arrives
                        selectedProduct
                            ?.takeIf { ph != null }
                            ?.let { prod ->
                                val adjusted = ph!!.prices.map { raw ->
                                    ((raw * (1 - (margins[prod.category] ?: 0.0) / 100))
                                        .roundToInt()).toFloat()
                                }
                                PriceHistoryDialog(
                                    title       = prod.description,
                                    lastUpdated = ph!!.lastUpdated,
                                    prices      = adjusted,
                                    onDismiss   = {
                                        homeVm.clearPriceHistory()
                                        selectedProduct = null
                                    }
                                )
                            }
                    }

                    composable("scan") {
                        /* TODO: ScanScreen(navController) */
                    }
                    composable("invoice") {
                        /* TODO: InvoiceScreen(navController) */
                    }
                    composable("admin") {
                        AdminScreen()
                    }
                    composable("settings") {
                        /* TODO: SettingsScreen(navController) */
                    }
                }
            }

            // Banner ad placeholder
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
