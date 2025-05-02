package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.admin.AdminScreen
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.auth.AuthViewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.auth.LoginDialog
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.auth.RegisterDialog
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home.BottomNavigationBar
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home.HomeScreen
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home.PriceHistoryDialog
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.invoice.InvoiceScreen
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.scan.ScanScreen
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.settings.SettingsScreen
import java.util.*

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    // 1️⃣ Authentication
    val authVm: AuthViewModel = viewModel()
    val firebaseUser by authVm.user.collectAsState()
    val dbProfile   by authVm.profileInfo.collectAsState()

    // 2️⃣ Derive greeting name: prefer DB name, fallback to Auth displayName
    val rawName = dbProfile?.name.orEmpty().ifBlank { firebaseUser?.displayName.orEmpty() }
    val greetingName = remember(rawName) { rawName }

    // 3️⃣ Time of day pieces
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greetingWord = "Good"
    val greetingRest = when (hour) {
        in 5..11  -> "Morning"
        in 12..17 -> "Afternoon"
        else      -> "Evening"
    }

    // Dialog state
    var showLogin    by remember { mutableStateOf(false) }
    var showRegister by remember { mutableStateOf(false) }

    Scaffold(bottomBar = { BottomNavigationBar(navController) }) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Top Row ──
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment   = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: greeting
                if (greetingName.isNotBlank()) {
                    Column(Modifier.padding(start = 16.dp)) {
                        Text(greetingWord, style = MaterialTheme.typography.bodyLarge)
                        Text("$greetingRest,\n$greetingName!", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    Spacer(Modifier.width(16.dp))
                }

                // Center: logo
                Image(
                    painter             = painterResource(R.drawable.fmsalogo),
                    contentDescription  = "App logo",
                    modifier            = Modifier.size(120.dp)
                )

                // Right: login / logout link
                Text(
                    text  = if (firebaseUser == null) "Login" else "Logout",
                    style = MaterialTheme.typography.bodyLarge
                        .copy(color = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .clickable {
                            if (firebaseUser == null) {
                                showLogin = true
                            } else {
                                authVm.signOut()
                            }
                        }
                )
            }

            // ── Auth dialogs ──
            if (showLogin) {
                LoginDialog(
                    authViewModel   = authVm,
                    onDismiss       = { showLogin = false },
                    onRegisterClick = {
                        showLogin    = false
                        showRegister = true
                    },
                    onLoginSuccess = { showLogin = false }
                )
            }
            if (showRegister) {
                RegisterDialog(
                    authViewModel     = authVm,
                    onDismiss         = { showRegister = false },
                    onSignInClick     = {
                        showRegister = false
                        showLogin    = true
                    },
                    onRegisterSuccess = { showRegister = false }
                )
            }

            // ── Main content ──
            Box(Modifier.weight(1f)) {
                NavHost(navController, startDestination = "home", Modifier.fillMaxSize()) {
                    composable("home") {
                        val homeVm: HomeViewModel = viewModel()
                        val query by homeVm.query.collectAsState()
                        val products by homeVm.filteredProducts.collectAsState()
                        val ph: PriceHistory? by homeVm.priceHistory.collectAsState()
                        var selectedProduct by remember { mutableStateOf<Product?>(null) }

                        HomeScreen(
                            products      = products,
                            query         = query,
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
                    composable("scan")    { ScanScreen() }
                    composable("invoice") { InvoiceScreen() }
                    composable("admin")   { AdminScreen() }
                    composable("settings"){ SettingsScreen() }
                }
            }

            // ── Banner Ad ──
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
