package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MenuAnchorType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsTab(
    homeViewModel: HomeViewModel = viewModel()
) {
    // 1) Get dynamic buyers from the ViewModel
    val buyers by homeViewModel.buyers.collectAsState(initial = emptyList())

    // 2) Static categories
    val categories = listOf("Test Strips", "Devices", "Inhalers", "Insulin")

    // UI state holders
    val visibilityMap    = remember { mutableStateMapOf<String, Boolean>() }
    val selectedBuyerMap = remember { mutableStateMapOf<String, String?>() }
    categories.forEach {
        visibilityMap.putIfAbsent(it, true)
        selectedBuyerMap.putIfAbsent(it, null)
    }
    var expandedCategory by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        categories.forEach { category ->
            // Each category in its own Card for visual separation
            Card(
                modifier  = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier           = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Category header + switch
                    Row(
                        modifier             = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(category, style = MaterialTheme.typography.titleMedium)
                        Switch(
                            checked         = visibilityMap[category] == true,
                            onCheckedChange = { visibilityMap[category] = it }
                        )
                    }

                    // Status text
                    Text(
                        text  = if (visibilityMap[category] == true) "Status: Displayed" else "Status: Hidden",
                        style = MaterialTheme.typography.bodySmall
                    )

                    // Buyer dropdown
                    ExposedDropdownMenuBox(
                        expanded         = expandedCategory == category,
                        onExpandedChange = { expandedCategory = if (it) category else null }
                    ) {
                        TextField(
                            value         = selectedBuyerMap[category] ?: "",
                            onValueChange = { /* read-only */ },
                            readOnly      = true,
                            placeholder   = { Text("Select Buyer") },
                            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expandedCategory == category) },
                            modifier      = Modifier
                                .fillMaxWidth()
                                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                        )
                        ExposedDropdownMenu(
                            expanded         = expandedCategory == category,
                            onDismissRequest = { expandedCategory = null }
                        ) {
                            buyers.forEach { buyer ->
                                DropdownMenuItem(
                                    text    = { Text(buyer) },
                                    onClick = {
                                        selectedBuyerMap[category] = buyer
                                        expandedCategory           = null
                                    }
                                )
                            }
                        }
                    }

                    // Currently selected buyer
                    Text(
                        text  = "Using: ${selectedBuyerMap[category] ?: "—"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
