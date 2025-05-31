package com.lislal.flippingmedicalsuppliesassistant.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lislal.flippingmedicalsuppliesassistant.ui.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfitMarginTab(
    margins: Map<String, Double>,
    settingsViewModel: SettingsViewModel = viewModel(),
    onSubmit: (category: String, percent: Double) -> Unit
) {
    val focusManager = LocalFocusManager.current

    // Fetch & collect remote categories
    LaunchedEffect(Unit) { settingsViewModel.refreshCategories() }
    val remoteCats by settingsViewModel.categoryList.collectAsState(initial = emptyList())
    val categories = remoteCats.ifEmpty { listOf("Test Strips", "Devices", "Inhalers", "Insulin") }

    var expanded    by remember { mutableStateOf(false) }
    var selectedCat by remember { mutableStateOf<String?>(null) }
    var input       by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // ── Current Profit Margins ───────────────────────────
        Text("Current Profit Margins", style = MaterialTheme.typography.titleLarge)

        // Two rows of three equal-weight columns
        categories.chunked(3).forEach { rowCats ->
            Row(Modifier.fillMaxWidth()) {
                rowCats.forEach { cat ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(cat, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "${margins[cat]?.toInt() ?: 0}%",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                // Fill remaining slots if rowCats.size < 3
                repeat(3 - rowCats.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // ── Set Profit Margin Form ────────────────────────────
        Text("Set Profit Margin (%)", style = MaterialTheme.typography.titleMedium)

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                if (!expanded) focusManager.clearFocus()
                expanded = !expanded
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value         = selectedCat.orEmpty(),
                onValueChange = {},
                readOnly      = true,
                placeholder   = { Text("Select Category") },
                trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier      = Modifier
                    .fillMaxWidth()
                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded         = expanded,
                onDismissRequest = { expanded = false }
            ) {
                // Disabled prompt
                DropdownMenuItem(
                    text    = { Text("Select Category") },
                    onClick = { /* no-op */ },
                    enabled = false
                )
                categories.forEach { cat ->
                    DropdownMenuItem(
                        text    = { Text(cat) },
                        onClick = {
                            selectedCat = cat
                            expanded    = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value         = input,
            onValueChange = { input = it.filter(Char::isDigit) },
            label         = { Text("Enter Profit Margin") },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                selectedCat?.let { cat ->
                    input.toDoubleOrNull()?.let { pct ->
                        onSubmit(cat, pct)
                        selectedCat = null
                        input       = ""
                    }
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Submit")
        }
    }
}
