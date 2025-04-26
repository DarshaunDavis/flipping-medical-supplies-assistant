package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.HomeViewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home.HomeScreen

@Composable
fun PricesTab(
    homeViewModel: HomeViewModel = viewModel()
) {
    // 1) Observe the same query & filteredProducts from HomeViewModel
    val query by homeViewModel.query.collectAsState()
    val products by homeViewModel.filteredProducts.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Adjust or Customize Prices",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "Adjust or customize individual prices here",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // --- Re-use HomeScreen for search + list + popup ---
        HomeScreen(
            products      = products,
            query         = query,
            onQueryChange = { homeViewModel.onQueryChanged(it) }
        )
    }
}
