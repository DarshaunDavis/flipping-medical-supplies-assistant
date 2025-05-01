package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
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
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.PriceHistory
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.Product
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.auth.AuthViewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.auth.LoginDialog
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.auth.RegisterDialog
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.admin.AdminScreen
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home.BottomNavigationBar
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home.HomeScreen
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home.PriceHistoryDialog
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.invoice.InvoiceScreen
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.scan.ScanScreen
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.settings.SettingsScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val authVm: AuthViewModel = viewModel()

    // Observe current Firebase user
    val user by authVm.user.collectAsState()

    // Dialog visibility state
    var showLogin by remember { mutableStateOf(false) }
    var showRegister by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top logo + login/logout link
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Centered logo
                Image(
                    painter = painterResource(R.drawable.fmsalogo),
                    contentDescription = "App logo",
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.Center)
                )

                // Conditional Login / Logout link
                Text(
                    text = if (user == null) "Login" else "Logout",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .align(Alignment.Center)
                        // shift right of logo: half logo width (60.dp) + spacing (16.dp)
                        .offset(x = 60.dp + 60.dp)
                        .clickable {
                            if (user == null) {
                                showLogin = true
                            } else {
                                authVm.signOut()
                            }
                        }
                )
            }

            // Auth dialogs
            if (showLogin) {
                LoginDialog(
                    onDismiss = { showLogin = false },
                    onRegisterClick = {
                        showLogin = false
                        showRegister = true
                    },
                    onLoginSuccess = {
                        showLogin = false
                    },
                    authViewModel = authVm
                )
            }
            if (showRegister) {
                RegisterDialog(
                    onDismiss = { showRegister = false },
                    onSignInClick = {
                        showRegister = false
                        showLogin = true
                    },
                    onRegisterSuccess = {
                        showRegister = false
                    },
                    authViewModel = authVm
                )
            }

            // Main content
            Box(Modifier.weight(1f)) {
                NavHost(navController, startDestination = "home", Modifier.fillMaxSize()) {
                    composable("home") {
                        val homeVm: HomeViewModel = viewModel()
                        val query by homeVm.query.collectAsState()
                        val products by homeVm.filteredProducts.collectAsState()
                        val ph: PriceHistory? by homeVm.priceHistory.collectAsState()
                        var selectedProduct by remember { mutableStateOf<Product?>(null) }

                        HomeScreen(
                            products = products,
                            query = query,
                            onQueryChange = { homeVm.onQueryChanged(it) },
                            onProductClick = { prod ->
                                homeVm.loadPriceHistory(prod.category, prod.barcode)
                                selectedProduct = prod
                            }
                        )

                        selectedProduct
                            ?.takeIf { ph != null }
                            ?.let { prod ->
                                PriceHistoryDialog(
                                    title = prod.description,
                                    lastUpdated = ph!!.lastUpdated,
                                    prices = ph!!.prices,
                                    onDismiss = {
                                        homeVm.clearPriceHistory()
                                        selectedProduct = null
                                    }
                                )
                            }
                    }
                    composable("scan")    { ScanScreen() }
                    composable("invoice") { InvoiceScreen() }
                    composable("admin")   { AdminScreen() }
                    composable("settings"){ SettingsScreen() }
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
