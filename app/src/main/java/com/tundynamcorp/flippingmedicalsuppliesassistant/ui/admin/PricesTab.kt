package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.HomeViewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home.PriceHistoryDialog
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home.HomeScreen
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricesTab(
    homeViewModel: HomeViewModel = viewModel()
) {
    // 1) Observe query + filtered products
    val query by homeViewModel.query.collectAsState()
    val products by homeViewModel.filteredProducts.collectAsState()

    // 2) Local UI state
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    val ph by homeViewModel.priceHistory.collectAsState()
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var overrideInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Adjust or Customize Prices", style = MaterialTheme.typography.titleLarge)
        Text("Tap a price below to override it, or reset all.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))

        // 3) Use HomeScreen with onProductClick wired
        HomeScreen(
            products      = products,
            query         = query,
            onQueryChange = { homeViewModel.onQueryChanged(it) },
            onProductClick = { prod ->
                // load history then open dialog
                homeViewModel.loadPriceHistory(prod.category, prod.barcode)
                selectedProduct = prod
            }
        )
    }

    // 4) Main price‐history dialog (editable!)
    if (selectedProduct != null && ph != null) {
        PriceHistoryDialog(
            title       = selectedProduct!!.description,
            lastUpdated = ph!!.lastUpdated,
            prices      = ph!!.prices,
            editable    = true,
            onPriceClick = { idx ->
                editingIndex = idx
                overrideInput = ph!!.prices[idx].toInt().toString()
            },
            onReset = {
                homeViewModel.resetOverrides(
                    selectedProduct!!.category,
                    selectedProduct!!.barcode
                )
            },
            onDismiss = {
                homeViewModel.clearPriceHistory()
                selectedProduct = null
            }
        )
    }

    // 5) Per‐price override input dialog
    editingIndex?.let { idx ->
        AlertDialog(
            onDismissRequest = { editingIndex = null },
            title   = { Text("Override Price") },
            text    = {
                Column {
                    Text("Enter new price for month #${idx + 1}:")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value         = overrideInput,
                        onValueChange = { overrideInput = it.filter(Char::isDigit) },
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    overrideInput.toIntOrNull()?.let { newP ->
                        homeViewModel.overridePrice(
                            selectedProduct!!.category,
                            selectedProduct!!.barcode,
                            idx,
                            newP
                        )
                        editingIndex = null
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingIndex = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
