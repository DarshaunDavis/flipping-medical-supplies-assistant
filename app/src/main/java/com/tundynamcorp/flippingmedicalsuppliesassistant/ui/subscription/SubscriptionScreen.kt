package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.subscription

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    navController: NavController,      // used for the back arrow
    onPurchaseMonthly: () -> Unit = {},// placeholder
    onPurchaseAnnual: () -> Unit = {}  // placeholder
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose a Plan") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        val scroll = rememberScrollState()
        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ► Major benefits
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Why Upgrade?", style = MaterialTheme.typography.titleMedium)
                Text("• Real-time barcode scanning of products")
                Text("• One-tap professional invoice generation & PDF export")
                Text("• Store and reuse your profile for fast invoicing")
                Text("• Admin dashboard for profit margins & product management")
                Text("• Ad-free experience and priority support")
            }

            // ► Plan cards side-by-side
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // — Monthly plan
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Monthly", style = MaterialTheme.typography.titleMedium)
                        Text("$20 / month", style = MaterialTheme.typography.bodyLarge)
                        Button(onClick = onPurchaseMonthly) {
                            Text("Subscribe")
                        }
                    }
                }

                // — Annual plan
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Annual", style = MaterialTheme.typography.titleMedium)
                        Text("$180 / year", style = MaterialTheme.typography.bodyLarge)
                        Button(onClick = onPurchaseAnnual) {
                            Text("Subscribe")
                        }
                    }
                }
            }
        }
    }
}
