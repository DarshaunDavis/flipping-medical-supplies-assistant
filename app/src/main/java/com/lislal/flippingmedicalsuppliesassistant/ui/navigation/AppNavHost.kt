package com.lislal.flippingmedicalsuppliesassistant.ui.navigation

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.lislal.flippingmedicalsuppliesassistant.R
import com.lislal.flippingmedicalsuppliesassistant.data.HomeViewModel
import com.lislal.flippingmedicalsuppliesassistant.data.Product
import com.lislal.flippingmedicalsuppliesassistant.ui.admin.AdminScreen
import com.lislal.flippingmedicalsuppliesassistant.ui.ads.BannerAd
import com.lislal.flippingmedicalsuppliesassistant.ui.auth.AuthViewModel
import com.lislal.flippingmedicalsuppliesassistant.ui.auth.LoginDialog
import com.lislal.flippingmedicalsuppliesassistant.ui.auth.RegisterDialog
import com.lislal.flippingmedicalsuppliesassistant.ui.components.TrialCountdown
import com.lislal.flippingmedicalsuppliesassistant.ui.components.TrialReminderBanner
import com.lislal.flippingmedicalsuppliesassistant.ui.home.BottomNavigationBar
import com.lislal.flippingmedicalsuppliesassistant.ui.home.HomeScreen
import com.lislal.flippingmedicalsuppliesassistant.ui.home.PriceHistoryDialog
import com.lislal.flippingmedicalsuppliesassistant.ui.invoice.InvoiceScreen
import com.lislal.flippingmedicalsuppliesassistant.ui.scan.ScanScreen
import com.lislal.flippingmedicalsuppliesassistant.ui.settings.SettingsScreen
import com.lislal.flippingmedicalsuppliesassistant.ui.subscription.SubscriptionScreen
import java.util.Calendar

@Composable
private fun ThemedAppLogo(modifier: Modifier = Modifier) {
    val logoRes =
        if (isSystemInDarkTheme()) R.drawable.fmsadarklogo else R.drawable.fmsalightlogo
    Image(
        painter = painterResource(logoRes),
        contentDescription = null,
        modifier = modifier
    )
}

@Composable
fun AppNavHost() {
    val context       = LocalContext.current
    val activity      = context as? Activity
    val navController = rememberNavController()

    // ── Double-back to exit ─────────────────────────────
    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    BackHandler {
        if (navController.previousBackStackEntry != null) {
            navController.popBackStack()
        } else {
            val now = System.currentTimeMillis()
            if (now - lastBackPressTime < 2_000) {
                activity?.finish()
            } else {
                lastBackPressTime = now
                Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ── Auth + Role + Trial from AuthViewModel ──
    val authVm       : AuthViewModel      = viewModel()
    val firebaseUser by authVm.user.collectAsState()
    val dbProfile    by authVm.profileInfo.collectAsState()
    val role         by authVm.role.collectAsState()
    val trialStart   by authVm.trialStart.collectAsState()
    // We'll set this flag from the Countdown callback:
    var isTrialActiveLocal by remember { mutableStateOf(false) }

    // ── Products and PriceHistory ──
    val homeVm   : HomeViewModel = viewModel()
    val query    by homeVm.query.collectAsState()
    val products by homeVm.filteredProducts.collectAsState()
    val ph       by homeVm.priceHistory.collectAsState()
    var selectedProduct by remember { mutableStateOf<Product?>(null) }

    // ── Greeting calculation ──
    val rawName = dbProfile?.name.takeIf { it?.isNotBlank() == true }
        ?: firebaseUser?.displayName.orEmpty()
    val showingGreeting = rawName.isNotBlank()
    val hour           = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greetingWord   = "Good"
    val greetingRest   = when (hour) {
        in 5..11  -> "Morning"
        in 12..17 -> "Afternoon"
        else      -> "Evening"
    }

    var showLogin    by remember { mutableStateOf(false) }
    var showRegister by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar      = {
            BottomNavigationBar(
                navController   = navController,
                currentRole     = role,
                isTrialActive   = isTrialActiveLocal
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top
        ) {
            // ── Header Row ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                // Greeting at left
                if (showingGreeting) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 16.dp)
                    ) {
                        Text(greetingWord, style = MaterialTheme.typography.bodyLarge)
                        Text("$greetingRest,\n$rawName!", style = MaterialTheme.typography.bodyLarge)
                    }
                }

                // Logo + Countdown center
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ThemedAppLogo(Modifier.size(120.dp))
                    Spacer(Modifier.height(8.dp))

                    // Show countdown only while trial is active
                    if (trialStart != null && isTrialActiveLocal) {
                        TrialCountdown(
                            trialStart      = trialStart,
                            onActiveChanged = { active -> isTrialActiveLocal = active },
                            modifier        = Modifier.clickable { navController.navigate("subscription") }
                        )
                    }
                }

                // Login/Logout at right
                Text(
                    text    = if (firebaseUser == null) "Login" else "Logout",
                    style   = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary),
                    modifier= Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                        .clickable {
                            if (firebaseUser == null) showLogin = true
                            else authVm.signOut()
                        }
                )
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
                    authViewModel      = authVm,
                    onDismiss          = { showRegister = false },
                    onSignInClick      = { showRegister = false; showLogin = true },
                    onRegisterSuccess = { showRegister = false }
                )
            }

            // ── Trial Reminder Banner (only while active) ──
            if (isTrialActiveLocal) {
                TrialReminderBanner(
                    trialStart     = trialStart,
                    onUpgradeClick = { navController.navigate("subscription") }
                )
            }

            // ── Main Navigation ──
            Box(modifier = Modifier.weight(1f)) {
                NavHost(
                    navController    = navController,
                    startDestination = "home",
                    modifier         = Modifier.fillMaxSize()
                ) {
                    composable("home") {
                        HomeScreen(
                            products       = products,
                            query          = query,
                            onQueryChange  = { homeVm.onQueryChanged(it) },
                            onProductClick = { prod ->
                                homeVm.loadPriceHistory(prod.category, prod.barcode)
                                selectedProduct = prod
                            }
                        )

                        // Price-history dialog
                        selectedProduct
                            ?.takeIf { ph != null }
                            ?.let { prod ->
                                PriceHistoryDialog(
                                    title       = prod.description,
                                    category    = prod.category,
                                    imageUrl    = prod.imageUrl,
                                    lastUpdated = ph!!.lastUpdated,
                                    prices      = ph!!.prices,
                                    onDismiss   = {
                                        homeVm.clearPriceHistory()
                                        selectedProduct = null
                                    }
                                )
                            }
                    }
                    composable("scan")   { ScanScreen() }
                    composable("invoice"){ InvoiceScreen() }
                    composable("admin")  {
                        AdminScreen(
                            homeViewModel  = homeVm,
                            currentRole    = role,
                            isTrialActive  = isTrialActiveLocal,
                            onUpgradeClick = { /* … */ }
                        )
                    }
                    composable("settings")     { SettingsScreen() }
                    composable("subscription"){ SubscriptionScreen(navController) }
                }
            }

            // ── Banner Ad ──
            BannerAd(
                modifier= Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )
        }
    }
}