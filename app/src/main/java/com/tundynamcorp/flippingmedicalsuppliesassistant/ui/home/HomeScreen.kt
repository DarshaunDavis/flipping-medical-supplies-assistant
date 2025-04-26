package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.AdminViewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.HomeViewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.PriceHistory
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.Product
import kotlin.math.roundToInt

/**
 * Displays the Home screen with a search filter, product list,
 * and profit-adjusted price popup.
 *
 * @param onProductClick invoked when a row is tapped; default = no-op
 */
@Composable
fun HomeScreen(
    products: List<Product>,
    query: String,
    onQueryChange: (String) -> Unit,
    onProductClick: (Product) -> Unit = {}
) {
    val homeVm: HomeViewModel = viewModel()
    val adminVm: AdminViewModel = viewModel()

    val ph: PriceHistory? by homeVm.priceHistory.collectAsState()
    val margins by adminVm.margins.collectAsState()

    // track which product was tapped here (for the Home tab)
    var localSelection by remember { mutableStateOf<Product?>(null) }

    // filter logic
    val filtered = if (query.isBlank()) products
    else products.filter { it.description.startsWith(query, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search products…") },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }
            }
        )

        Spacer(Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(filtered) { product ->
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // let the parent handle it...
                            onProductClick(product)
                            // ...and also our local Home-dialog
                            localSelection = product
                            homeVm.loadPriceHistory(product.category, product.barcode)
                        }
                        .padding(16.dp)
                )
                Divider()
            }
        }
    }

    // If nobody else handled the click, show our built-in Home dialog:
    localSelection?.let { product ->
        ph?.let { history ->
            val marginPct = margins[product.category] ?: 0.0
            val adjusted = history.prices.map { raw ->
                ((raw * (1 - marginPct / 100)).roundToInt()).toFloat()
            }
            PriceHistoryDialog(
                title       = product.description,
                lastUpdated = history.lastUpdated,
                prices      = adjusted,
                onDismiss   = {
                    homeVm.clearPriceHistory()
                    localSelection = null
                }
            )
        }
    }
}
