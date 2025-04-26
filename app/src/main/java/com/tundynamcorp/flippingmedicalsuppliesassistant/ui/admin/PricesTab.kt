// app/src/main/java/com/tundynamcorp/flippingmedicalsuppliesassistant/ui/admin/PricesTab.kt
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
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home.PriceHistoryDialog
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.Product

@Composable
fun PricesTab(
    homeViewModel: HomeViewModel = viewModel()
) {
    // 1) Observe search & filtered list
    val query    by homeViewModel.query.collectAsState()
    val products by homeViewModel.filteredProducts.collectAsState()

    // 2) Observe the loaded history, and track which product was tapped
    val ph by homeViewModel.priceHistory.collectAsState()
    var selectedProduct by remember { mutableStateOf<Product?>(null) }

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
            text = "Tap a price below to override it, or reset all.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 3) Drive the same HomeScreen, but now hook onProductClick
        HomeScreen(
            products      = products,
            query         = query,
            onQueryChange = { homeViewModel.onQueryChanged(it) },
            onProductClick = { prod ->
                homeViewModel.loadPriceHistory(prod.category, prod.barcode)
                selectedProduct = prod
            }
        )
    }

    // 4) When a product is selected and history is loaded, show an editable dialog
    selectedProduct?.let { prod ->
        ph?.let { history ->
            PriceHistoryDialog(
                title       = prod.description,
                lastUpdated = history.lastUpdated,
                prices      = history.prices,
                editable    = true,
                onPriceClick = { idx ->
                    // TODO: show per-price input dialog
                },
                onReset      = {
                    // Call your VM method to clear any overrides for this product
                    homeViewModel.resetOverrides(prod.category, prod.barcode)
                },
                onDismiss    = {
                    homeViewModel.clearPriceHistory()
                    selectedProduct = null
                }
            )
        }
    }
}
