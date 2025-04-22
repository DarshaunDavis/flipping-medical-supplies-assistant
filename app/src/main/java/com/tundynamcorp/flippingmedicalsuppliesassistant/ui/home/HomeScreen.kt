package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.Product

/**
 * Displays the Home screen with a logo, search filter, and product list.
 * Tapping a product opens a placeholder popup.
 */
@Composable
fun HomeScreen(
    products: List<Product>,
    query: String,
    onQueryChange: (String) -> Unit
) {
    // Local state to track which product was tapped
    var selectedProduct by remember { mutableStateOf<Product?>(null) }

    // Filter products based on query
    val filtered = if (query.isBlank()) products
    else products.filter { it.description.startsWith(query, ignoreCase = true) }

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
                        .clickable { selectedProduct = product }
                        .padding(16.dp)
                )
                HorizontalDivider()
            }
        }
    }

    // Popup dialog when an item is clicked
    selectedProduct?.let { product ->
        AlertDialog(
            onDismissRequest = { selectedProduct = null },
            title = { Text(text = product.description) },
            text = { Text("Price chart will go here…") },
            confirmButton = {
                TextButton(onClick = { selectedProduct = null }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
@Preview(showBackground = true, widthDp = 320, heightDp = 480)
fun HomeScreenPreview() {
    val sampleProducts = listOf(
        Product("301937080500","Contour 50 count","Test Strips"),
        Product("365702408104","AccuChek Aviva Plus 100 count","Test Strips"),
        Product("086270077010","Dexcom G7 Sensor","Devices")
    )
    var q by remember { mutableStateOf("") }
    HomeScreen(
        products = sampleProducts,
        query = q,
        onQueryChange = { q = it }
    )
}
