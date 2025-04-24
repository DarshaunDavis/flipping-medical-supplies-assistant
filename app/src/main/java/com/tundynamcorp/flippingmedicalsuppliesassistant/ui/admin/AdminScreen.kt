package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.AdminViewModel
import androidx.compose.material3.MenuAnchorType

@Composable
fun AdminScreen(
    viewModel: AdminViewModel = viewModel()
) {
    val margins by viewModel.margins.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Profit Margin", "Prices", "Products")

    Column(modifier = Modifier.fillMaxSize()) {
        // --- Tab row ---
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick  = { selectedTab = index },
                    text     = { Text(title) }
                )
            }
        }

        // --- Tab content ---
        when (selectedTab) {
            0 -> ProfitMarginTab(
                margins   = margins,
                onSubmit  = { cat, pct -> viewModel.updateMargin(cat, pct) }
            )
            1 -> PricesTab()
            2 -> ProductsTab()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfitMarginTab(
    margins: Map<String, Double>,
    onSubmit: (category: String, percent: Double) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val categories   = listOf("Test Strips", "Devices", "Inhalers", "Insulin")

    var expanded          by remember { mutableStateOf(false) }
    var selectedCategory  by remember { mutableStateOf<String?>(null) }
    var input             by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Current margins row
        Text("Current Profit Margins", style = MaterialTheme.typography.titleLarge)
        Row(
            modifier             = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            margins.forEach { (category, pct) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(category, style = MaterialTheme.typography.bodyMedium)
                    Text("${pct.toInt()}%", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        // Form heading
        Text("Set Profit Margin (%)", style = MaterialTheme.typography.titleMedium)

        // Category dropdown
        ExposedDropdownMenuBox(
            expanded         = expanded,
            onExpandedChange = {
                if (!expanded) focusManager.clearFocus()
                expanded = !expanded
            }
        ) {
            TextField(
                value         = selectedCategory.orEmpty(),
                onValueChange = { /* read-only */ },
                readOnly      = true,
                placeholder   = { Text("Select Category") },
                trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier      = Modifier
                    .fillMaxWidth()
                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable) // ← updated
            )
            ExposedDropdownMenu(
                expanded         = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { cat ->
                    DropdownMenuItem(
                        text    = { Text(cat) },
                        onClick = {
                            selectedCategory = cat
                            expanded         = false
                        }
                    )
                }
            }
        }

        // Profit input
        OutlinedTextField(
            value         = input,
            onValueChange = { input = it.filter(Char::isDigit) },
            label         = { Text("Enter Profit Margin") },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth()
        )

        // Submit button
        Button(
            onClick = {
                selectedCategory
                    ?.takeIf { it.isNotBlank() }
                    ?.let { cat ->
                        input.toDoubleOrNull()?.let { pct ->
                            onSubmit(cat, pct)
                            selectedCategory = null
                            input            = ""
                        }
                    }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Submit")
        }
    }
}

@Composable private fun PricesTab()    { /* … */ }
@Composable private fun ProductsTab()  { /* … */ }