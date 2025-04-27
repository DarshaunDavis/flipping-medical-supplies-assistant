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
import androidx.navigation.compose.*

import com.tundynamcorp.flippingmedicalsuppliesassistant.R
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.HomeViewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.PriceHistory
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.Product
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home.BottomNavigationBar
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home.HomeScreen
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home.PriceHistoryDialog
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.admin.AdminScreen
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.scan.ScanScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    Scaffold(bottomBar = { BottomNavigationBar(navController) }) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Persistent logo
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.fmsalogo),
                    contentDescription = "App logo",
                    modifier = Modifier.size(120.dp)
                )
            }

            // NavHost content
            Box(Modifier.weight(1f)) {
                NavHost(navController, startDestination = "home", Modifier.fillMaxSize()) {

                    composable("home") {
                        val homeVm: HomeViewModel = viewModel()
                        val query by homeVm.query.collectAsState()
                        val products by homeVm.filteredProducts.collectAsState()
                        val ph: PriceHistory? by homeVm.priceHistory.collectAsState()

                        // track which product is tapped
                        var selectedProduct by remember { mutableStateOf<Product?>(null) }

                        // 1) show pure list+search UI
                        HomeScreen(
                            products      = products,
                            query         = query,
                            onQueryChange = { homeVm.onQueryChanged(it) },
                            onProductClick = { prod ->
                                homeVm.loadPriceHistory(prod.category, prod.barcode)
                                selectedProduct = prod
                            }
                        )

                        // 2) single dialog, using exactly the VM-adjusted prices
                        selectedProduct
                            ?.takeIf { ph != null }
                            ?.let { prod ->
                                PriceHistoryDialog(
                                    title       = prod.description,
                                    lastUpdated = ph!!.lastUpdated,
                                    prices      = ph!!.prices,
                                    onDismiss   = {
                                        homeVm.clearPriceHistory()
                                        selectedProduct = null
                                    }
                                )
                            }
                    }

                    composable("scan")    {
                        ScanScreen()
                    }
                    composable("invoice") { /* TODO */ }
                    composable("admin")   { AdminScreen() }
                    composable("settings"){ /* TODO */ }
                }
            }

            // Banner ad placeholder
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Banner Ad Placeholder")
            }
        }
    }
}