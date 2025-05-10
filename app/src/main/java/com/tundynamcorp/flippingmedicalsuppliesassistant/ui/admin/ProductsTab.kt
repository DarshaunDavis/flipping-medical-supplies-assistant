package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.HomeViewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsTab(
    homeViewModel: HomeViewModel = viewModel(),
    adminViewModel: AdminViewModel = viewModel()
) {
    // 1) Observe persisted maps
    val buyersByCategory by homeViewModel.buyersByCategory.collectAsState(initial = emptyMap())
    val visibilityMap    by adminViewModel.visibility.collectAsState(initial = emptyMap())
    // 2) Observe the VM’s buyer-selection state
    val selectedBuyerMap by homeViewModel.selectedBuyerMap.collectAsState(initial = emptyMap())

    val categories = listOf("Test Strips", "Devices", "Inhalers", "Insulin")
    // 3) Local UI state for which dropdown is open
    var expandedCategory by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        categories.forEach { category ->
            // find all buyers for this category
            val buyers = buyersByCategory[category].orEmpty()
            val hasBuyers = buyers.isNotEmpty()
            // find current toggle state
            val visible = visibilityMap[category] ?: true

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ── Header + toggle ──
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(category, style = MaterialTheme.typography.titleMedium)
                        // disable switch if no buyers exist
                        Switch(
                            checked = visible,
                            onCheckedChange = { adminViewModel.setVisibility(category, it) },
                            enabled = hasBuyers
                        )
                    }

                    // ── Status text ──
                    Text(
                        text = if (visible) "Status: Displayed" else "Status: Hidden",
                        style = MaterialTheme.typography.bodySmall
                    )

                    // ── Buyer dropdown ──
                    ExposedDropdownMenuBox(
                        expanded = expandedCategory == category,
                        onExpandedChange = { expandedCategory = if (it) category else null }
                    ) {
                        TextField(
                            value = selectedBuyerMap[category].orEmpty(),
                            onValueChange = { /* read-only */ },
                            readOnly = true,
                            placeholder = {
                                Text(
                                    if (!hasBuyers) "No buyer available"
                                    else "Select Buyer"
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expandedCategory == category)
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
                            if (!hasBuyers) {
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
                                            homeViewModel.setSelectedBuyer(category, buyer)
                                            expandedCategory = null
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // ── Currently selected buyer ──
                    Text(
                        text = "Using: ${selectedBuyerMap[category] ?: "—"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
