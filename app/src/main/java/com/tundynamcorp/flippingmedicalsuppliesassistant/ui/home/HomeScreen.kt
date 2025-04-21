package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tundynamcorp.flippingmedicalsuppliesassistant.R

@Composable
fun HomeScreen(
    products: List<String>,
    onProductClick: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, products) {
        products.filter { it.contains(query, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Logo at top
        Box(
            Modifier
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

        // Search bar
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Search products…") },
            leadingIcon = { Icon(Icons.Outlined.Search, null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Default.Close, null)
                    }
                }
            }
        )

        Spacer(Modifier.height(8.dp))

        // Scroll list
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(filtered) { name ->
                Text(
                    text = name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onProductClick(name) }
                        .padding(16.dp)
                )
                HorizontalDivider()
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


@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        "home" to Icons.Default.Home,
        "scan" to Icons.Default.QrCodeScanner,
        "invoice" to Icons.AutoMirrored.Filled.ReceiptLong,
        "admin" to Icons.Outlined.AdminPanelSettings,
        "settings" to Icons.Default.Settings
    )
    NavigationBar {
        items.forEach { (route, icon) ->
            NavigationBarItem(
                icon = { Icon(icon, null) },
                selected = false, // you can wire up NavBackStackEntry for real selection
                onClick = { navController.navigate(route) }
            )
        }
    }
}
