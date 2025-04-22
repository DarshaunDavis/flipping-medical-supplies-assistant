package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.Product

/**
 * Displays the Home screen with a logo, search filter, and product list.
 * @param products List of products to display
 * @param query Current search query
 * @param onQueryChange Callback when the search text changes
 * @param onProductClick Callback when a product is tapped
 */
@Composable
fun HomeScreen(
    products: List<Product>,
    query: String,
    onQueryChange: (String) -> Unit,
    onProductClick: (Product) -> Unit
) {
    // Filter products based on query
    val filtered = if (query.isBlank()) products
    else products.filter { it.description.contains(query, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search field
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
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
                        .clickable { onProductClick(product) }
                        .padding(16.dp)
                )
                Spacer(Modifier.height(1.dp))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 640)
@Composable
fun HomeScreenPreview() {
    val sampleProducts = listOf(
        Product(barcode = "301937080500", description = "Contour 50 count", category = "Test Strips"),
        Product(barcode = "365702408104", description = "AccuChek Aviva Plus 100 count", category = "Test Strips"),
        Product(barcode = "086270077010", description = "Dexcom G7 Sensor", category = "Devices")
    )
    HomeScreen(
        products = sampleProducts,
        query = "",
        onQueryChange = {},
        onProductClick = {}
    )
}

