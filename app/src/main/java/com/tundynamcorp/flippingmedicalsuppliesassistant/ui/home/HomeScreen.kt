package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.HomeViewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.PriceHistory
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.AdminViewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.Product
import java.math.RoundingMode
import kotlin.math.roundToInt

/**
 * Displays the Home screen with a search filter, product list, and profit-adjusted price popup.
 */
@Composable
fun HomeScreen(
    products: List<Product>,
    query: String,
    onQueryChange: (String) -> Unit
) {
    // 1) ViewModels & state
    val homeVm: HomeViewModel = viewModel()
    val adminVm: AdminViewModel = viewModel()

    val ph: PriceHistory? by homeVm.priceHistory.collectAsState()
    val margins by adminVm.margins.collectAsState()

    // 2) Local UI state
    var selectedProduct by remember { mutableStateOf<Product?>(null) }

    // 3) Filter logic
    val filtered = if (query.isBlank()) products
    else products.filter {
        it.description.startsWith(query, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search field
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

        // Product list
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(filtered) { product ->
                Text(
                    text = product.description,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedProduct = product
                            homeVm.loadPriceHistory(product.category, product.barcode)
                        }
                        .padding(16.dp)
                )
                HorizontalDivider()
            }
        }
    }

    // 4) Popup when an item is clicked AND history loaded
    selectedProduct?.let { product ->
        ph?.let { history ->
            // pull margin% for this category (default 0)
            val marginPct = margins[product.category] ?: 0.0
            // apply margin to each raw price, round-half-up to nearest dollar
            val adjusted = history.prices.map { raw ->
                ((raw * (1 - marginPct / 100))
                    .roundToInt()).toFloat()
            }

            PriceHistoryDialog(
                title       = product.description,
                lastUpdated = history.lastUpdated,
                prices      = adjusted,
                onDismiss   = {
                    homeVm.clearPriceHistory()
                    selectedProduct = null
                }
            )
        }
    }
}
