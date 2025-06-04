package com.lislal.flippingmedicalsuppliesassistant.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lislal.flippingmedicalsuppliesassistant.data.HomeViewModel
import com.lislal.flippingmedicalsuppliesassistant.data.AdminViewModel
import com.lislal.flippingmedicalsuppliesassistant.ui.settings.SettingsViewModel
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsTab(
    homeViewModel: HomeViewModel = viewModel(),
    adminViewModel: AdminViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    // 1) Raw maps from your view models
    val wholesalersByCategory by homeViewModel.wholesalersByCategory.collectAsState(initial = emptyMap())
    val visibilityMap    by adminViewModel.visibility.collectAsState(initial = emptyMap())
    val selectedWholesalerMap by homeViewModel.selectedWholesalerMap.collectAsState(initial = emptyMap())

    // 2) Flat list of all valid wholesaler names
    val validWholesalerNames by settingsViewModel.wholesalerList
        .map { list -> list.map { it.name } }
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
            // 3️⃣ Raw wholesaler‐keys under this category
            val rawWholesalers = wholesalersByCategory[category].orEmpty()
            // 4️⃣ Keep only those that exist in /wholesalers.json
            val filteredWholesalers = rawWholesalers.filter { it in validWholesalerNames }

            // 5️⃣ Sort and display all wholesalers for this category
            val wholesalersForUi = filteredWholesalers.sorted()

            val hasWholesalers = wholesalersForUi.isNotEmpty()
            val visible   = visibilityMap[category] ?: false

            Card(
                modifier  = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ── Header + toggle ──
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(category, style = MaterialTheme.typography.titleMedium)
                        Switch(
                            checked = visible,
                            onCheckedChange = { adminViewModel.setVisibility(category, it) },
                            enabled = hasWholesalers
                        )
                    }

                    // ── Status text ──
                    Text(
                        text = if (visible) "Status: Displayed" else "Status: Hidden",
                        style = MaterialTheme.typography.bodySmall
                    )

                    // ── Wholesaler dropdown ──
                    ExposedDropdownMenuBox(
                        expanded = expandedCategory == category,
                        onExpandedChange = { expand ->
                            if (expand) settingsViewModel.refreshWholesalers()
                            expandedCategory = if (expand) category else null
                        }
                    ) {
                        TextField(
                            value         = selectedWholesalerMap[category]?.takeIf { it in wholesalersForUi }.orEmpty(),
                            onValueChange = {},
                            readOnly      = true,
                            placeholder   = { Text(if (!hasWholesalers) "No wholesaler available" else "Select Wholesaler") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedCategory == category) },
                            modifier      = Modifier
                                .fillMaxWidth()
                                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                        )

                        ExposedDropdownMenu(
                            expanded = expandedCategory == category,
                            onDismissRequest = { expandedCategory = null }
                        ) {
                            if (!hasWholesalers) {
                                DropdownMenuItem(
                                    text    = { Text("No wholesaler available") },
                                    onClick = { },
                                    enabled = false
                                )
                            } else {
                                // Disabled header prompt
                                DropdownMenuItem(
                                    text    = { Text("Select Wholesaler") },
                                    onClick = { /* no-op */ },
                                    enabled = false
                                )
                                wholesalersForUi.forEach { wholesalerName ->
                                    DropdownMenuItem(
                                        text = { Text(wholesalerName) },
                                        onClick = {
                                            homeViewModel.setSelectedWholesaler(category, wholesalerName)
                                            expandedCategory = null
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // ── Currently selected wholesaler ──
                    Text(
                        text = "Using: ${selectedWholesalerMap[category] ?: "—"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}