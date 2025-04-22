package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.HomeViewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.PriceHistory
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.Product

@Composable
fun HomeScreen(
    products: List<Product>,
    query: String,
    onQueryChange: (String) -> Unit
) {
    // 1) ViewModel & state
    val vm: HomeViewModel = viewModel()
    val ph: PriceHistory? by vm.priceHistory.collectAsState()

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
                            vm.loadPriceHistory(product.category, product.barcode)
                        }
                        .padding(16.dp)
                )
                Divider()
            }
        }
    }

    // 4) Popup when both a product is selected and history has loaded
    selectedProduct?.let { product ->
        ph?.let { history ->
            PriceHistoryDialog(
                title       = product.description,
                lastUpdated = history.lastUpdated,
                prices      = history.prices,
                onDismiss   = {
                    vm.clearPriceHistory()
                    selectedProduct = null
                }
            )
        }
    }
}
