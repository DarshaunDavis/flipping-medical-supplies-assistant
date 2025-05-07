// app/src/main/java/com/tundynamcorp/flippingmedicalsuppliesassistant/ui/subscription/SubscriptionScreen.kt
package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.subscription

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Choose a Plan") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            PlanCard(
                title = "Monthly",
                price = "$20 / month",
                onSubscribe = { /* TODO: hook up monthly purchase */ }
            )
            PlanCard(
                title = "Annual",
                price = "$180 / year",
                onSubscribe = { /* TODO: hook up annual purchase */ }
            )
        }
    }
}

@Composable
private fun PlanCard(
    title: String,
    price: String,
    onSubscribe: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(4.dp))
                Text(price, style = MaterialTheme.typography.bodyLarge)
            }
            Button(
                onClick = onSubscribe,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Subscribe")
            }
        }
    }
}
