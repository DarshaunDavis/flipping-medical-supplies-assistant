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
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.settings.SettingsViewModel
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsTab(
    homeViewModel: HomeViewModel = viewModel(),
    adminViewModel: AdminViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    // 1) Raw maps from your view models
    val buyersByCategory by homeViewModel.buyersByCategory.collectAsState(initial = emptyMap())
    val visibilityMap    by adminViewModel.visibility.collectAsState(initial = emptyMap())
    val selectedBuyerMap by homeViewModel.selectedBuyerMap.collectAsState(initial = emptyMap())

    // 2) Live list of buyer names from RTDB
    val validBuyerNames by settingsViewModel.buyerList
        .map { it.map { bi -> bi.name } }
        .collectAsState(initial = emptyList())

    val categories = listOf("Test Strips", "Devices", "Inhalers", "Insulin")
    var expandedCategory by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        categories.forEach { category ->
            // 3️⃣ Raw buyer‐keys under this category
            val rawBuyers = buyersByCategory[category].orEmpty()
            // 4️⃣ Keep only those that exist in /buyers.json
            val filteredBuyers = rawBuyers.filter { it in validBuyerNames }

            // 5️⃣ Default‐always categories get only “Strip Flip” if present
            val defaultCats = setOf("Test Strips", "Devices")
            val defaultBuyer = "Strip Flip"
            val buyersForUi: List<String> = when {
                category in defaultCats && filteredBuyers.contains(defaultBuyer) ->
                    listOf("Select Buyer", defaultBuyer)
                filteredBuyers.isNotEmpty() ->
                    listOf("Select Buyer") + filteredBuyers
                else ->
                    emptyList()
            }

            val hasBuyers = buyersForUi.isNotEmpty()
            val visible   = visibilityMap[category] ?: false

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
                        onExpandedChange = {
                            // whenever we open, re-fetch any new buyers
                            if (it) settingsViewModel.refreshBuyers()
                            expandedCategory = if (it) category else null
                        }
                    ) {
                        TextField(
                            value = selectedBuyerMap[category]
                                ?.takeIf { it in buyersForUi }
                                .orEmpty(),
                            onValueChange = {},
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
                                buyersForUi.forEach { buyerName ->
                                    DropdownMenuItem(
                                        text = { Text(buyerName) },
                                        onClick = {
                                            homeViewModel.setSelectedBuyer(category, buyerName)
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