package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.AdminViewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.HomeViewModel
import androidx.compose.material3.MenuAnchorType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsTab(
    homeViewModel: HomeViewModel = viewModel(),
    adminViewModel: AdminViewModel = viewModel()
) {
    // 1) buyers grouped by category
    val buyersByCategory by homeViewModel.buyersByCategory.collectAsState(initial = emptyMap())

    // 2) persisted visibility toggles
    val visibilityMap by adminViewModel.visibility.collectAsState(initial = emptyMap())

    val categories = listOf("Test Strips", "Devices", "Inhalers", "Insulin")

    // local per-category selected buyer
    val selectedBuyerMap = remember { mutableStateMapOf<String, String?>() }
    categories.forEach { selectedBuyerMap.putIfAbsent(it, null) }

    var expandedCategory by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        categories.forEach { category ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // --- header + on/off switch ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(category, style = MaterialTheme.typography.titleMedium)
                        val visible = visibilityMap[category] ?: true
                        Switch(
                            checked = visible,
                            onCheckedChange = { adminViewModel.setVisibility(category, it) }
                        )
                    }

                    Text(
                        text = if ((visibilityMap[category] ?: true))
                            "Status: Displayed" else "Status: Hidden",
                        style = MaterialTheme.typography.bodySmall
                    )

                    // --- buyer dropdown ---
                    ExposedDropdownMenuBox(
                        expanded = expandedCategory == category,
                        onExpandedChange = {
                            expandedCategory =
                                if (it) category else null
                        }
                    ) {
                        val buyers = buyersByCategory[category].orEmpty()
                        TextField(
                            value = selectedBuyerMap[category] ?: "",
                            onValueChange = { /* read-only */ },
                            readOnly = true,
                            placeholder = {
                                Text(
                                    if (buyers.isEmpty())
                                        "No buyer available"
                                    else
                                        "Select Buyer"
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults
                                    .TrailingIcon(expandedCategory == category)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(
                                    type = MenuAnchorType.PrimaryNotEditable,
                                    enabled = true
                                )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCategory == category,
                            onDismissRequest = { expandedCategory = null }
                        ) {
                            if (buyers.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No buyer available") },
                                    onClick = { /* no-op */ },
                                    enabled = false
                                )
                            } else {
                                buyers.forEach { buyer ->
                                    DropdownMenuItem(
                                        text = { Text(buyer) },
                                        onClick = {
                                            selectedBuyerMap[category] = buyer
                                            expandedCategory = null
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // --- currently selected buyer ---
                    Text(
                        text = "Using: ${selectedBuyerMap[category] ?: "—"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
