package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
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
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.Product
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.UserRole
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.admin.AdminScreen
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.ads.BannerAd
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.auth.AuthViewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.auth.LoginDialog
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.auth.RegisterDialog
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.components.TrialReminderBanner
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home.BottomNavigationBar
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home.HomeScreen
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home.PriceHistoryDialog
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.invoice.InvoiceScreen
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.scan.ScanScreen
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.settings.SettingsScreen
import java.util.Calendar

@Composable
private fun ThemedAppLogo(modifier: Modifier = Modifier) {
    val logoRes = if (isSystemInDarkTheme()) R.drawable.fmsadarklogo else R.drawable.fmsalightlogo
    Image(
        painter = painterResource(logoRes),
        contentDescription = null,
        modifier = modifier
    )
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    // Auth + role + trial
    val authVm: AuthViewModel = viewModel()
    val firebaseUser by authVm.user.collectAsState()
    val dbProfile   by authVm.profileInfo.collectAsState()
    val role        by authVm.role.collectAsState()
    val trialStart  by authVm.trialStart.collectAsState()

    // Home VM + state
    val homeVm: HomeViewModel = viewModel()
    val query    by homeVm.query.collectAsState()
    val products by homeVm.filteredProducts.collectAsState()
    val ph       by homeVm.priceHistory.collectAsState()
    var selectedProduct by remember { mutableStateOf<Product?>(null) }

    // Greeting
    val rawName = dbProfile?.name.takeIf { !it.isNullOrBlank() }
        ?: firebaseUser?.displayName.orEmpty()
    val showingGreeting = rawName.isNotBlank()
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greetingWord = "Good"
    val greetingRest = when (hour) {
        in 5..11  -> "Morning"
        in 12..17 -> "Afternoon"
        else      -> "Evening"
    }

    var showLogin    by remember { mutableStateOf(false) }
    var showRegister by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar      = { BottomNavigationBar(navController, role) }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // ── Header ──
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                if (showingGreeting) {
                    Column(
                        Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 16.dp)
                    ) {
                        Text(greetingWord, style = MaterialTheme.typography.bodyLarge)
                        Text("$greetingRest,\n$rawName!", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                ThemedAppLogo(Modifier.align(Alignment.Center).size(120.dp))
                Text(
                    text = if (firebaseUser == null) "Login" else "Logout",
                    style = MaterialTheme.typography.bodyLarge
                        .copy(color = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                        .clickable {
                            if (firebaseUser == null) showLogin = true
                            else authVm.signOut()
                        }
                )
            }

            // ── Trial banner ──
            TrialReminderBanner(trialStart = trialStart) {
                // navigate to subscription screen
            }

            // ── Auth dialogs ──
            if (showLogin) {
                LoginDialog(
                    authViewModel   = authVm,
                    onDismiss       = { showLogin = false },
                    onRegisterClick = { showLogin = false; showRegister = true },
                    onLoginSuccess = { showLogin = false }
                )
            }
            if (showRegister) {
                RegisterDialog(
                    authViewModel     = authVm,
                    onDismiss         = { showRegister = false },
                    onSignInClick     = { showRegister = false; showLogin = true },
                    onRegisterSuccess = { showRegister = false }
                )
            }

            // ── Main content ──
            Box(Modifier.weight(1f)) {
                NavHost(navController, startDestination = "home", Modifier.fillMaxSize()) {
                    composable("home") {
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
                    composable("scan") {
                        RoleGate(
                            currentRole = role,
                            allowed     = setOf(UserRole.Subscriber, UserRole.Admin)
                        ) {
                            ScanScreen()
                        }
                    }
                    composable("invoice") {
                        RoleGate(
                            currentRole = role,
                            allowed     = setOf(UserRole.User, UserRole.Subscriber, UserRole.Admin)
                        ) {
                            InvoiceScreen()
                        }
                    }
                    composable("admin") {
                        AdminScreen(
                            currentRole    = role,
                            onUpgradeClick = { /* show your upgrade dialog */ }
                        )
                    }
                    composable("settings") {
                        SettingsScreen()
                    }
                }
            }

            // ── Banner Ad ──
            BannerAd(modifier = Modifier.fillMaxWidth().height(50.dp))
        }
    }
}
