package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.Product

/**
 * Pure list + search. Taps go out via `onProductClick`, no internal dialogs.
 */
@Composable
fun HomeScreen(
    products: List<Product>,
    query: String,
    onQueryChange: (String) -> Unit,
    onProductClick: (Product) -> Unit
) {
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
                        Icon(Icons.Filled.Close, contentDescription = null)
                    }
                }
            }
        )
        Spacer(Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(products) { product ->
                Text(
                    text = product.description,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onProductClick(product) }
                        .padding(16.dp)
                )
                Divider()
            }
        }
    }
}