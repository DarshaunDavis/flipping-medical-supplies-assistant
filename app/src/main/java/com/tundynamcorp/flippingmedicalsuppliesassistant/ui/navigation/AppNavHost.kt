package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tundynamcorp.flippingmedicalsuppliesassistant.R
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.AdminViewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.HomeViewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.Product
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.admin.AdminScreen
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home.BottomNavigationBar
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home.HomeScreen
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home.PriceHistoryDialog
import kotlin.math.roundToInt

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    // view-models
    val homeVm: HomeViewModel = viewModel()
    val adminVm: AdminViewModel = viewModel()

    // dialog state for the “home” screen
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    val ph by homeVm.priceHistory.collectAsState()
    val margins by adminVm.margins.collectAsState()

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
                        val query by homeVm.query.collectAsState()
                        val products by homeVm.filteredProducts.collectAsState()

                        HomeScreen(
                            products = products,
                            query = query,
                            onQueryChange = { homeVm.onQueryChanged(it) },
                            onProductClick = { product ->
                                // load data and open our dialog
                                homeVm.loadPriceHistory(product.category, product.barcode)
                                selectedProduct = product
                            }
                        )
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

        selectedProduct
            ?.takeIf { ph != null }
            ?.let { product ->
                PriceHistoryDialog(
                    title = product.description,
                    lastUpdated = ph!!.lastUpdated,
                    prices = ph!!.prices.map { raw ->
                        // apply your margin here if you like
                        ((raw * (1 - (margins[product.category] ?: 0.0) / 100.0))
                            .roundToInt()).toFloat()
                    },
                    onDismiss = {
                        homeVm.clearPriceHistory()
                        selectedProduct = null
                    }
                )
            }
    }
}
