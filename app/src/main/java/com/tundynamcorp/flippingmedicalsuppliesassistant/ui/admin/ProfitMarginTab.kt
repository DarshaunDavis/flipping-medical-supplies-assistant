package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MenuAnchorType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfitMarginTab(
    margins: Map<String, Double>,
    onSubmit: (category: String, percent: Double) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val categories   = listOf("Test Strips", "Devices", "Inhalers", "Insulin")
    var expanded     by remember { mutableStateOf(false) }
    var selectedCat  by remember { mutableStateOf<String?>(null) }
    var input        by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("Current Profit Margins", style = MaterialTheme.typography.titleLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            margins.forEach { (cat, pct) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(cat, style = MaterialTheme.typography.bodyMedium)
                    Text("${pct.toInt()}%", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        Text("Set Profit Margin (%)", style = MaterialTheme.typography.titleMedium)

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                if (!expanded) focusManager.clearFocus()
                expanded = !expanded
            }
        ) {
            TextField(
                value       = selectedCat.orEmpty(),
                onValueChange= {},
                readOnly    = true,
                placeholder = { Text("Select Category") },
                trailingIcon= { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier    = Modifier
                    .fillMaxWidth()
                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
            )
            ExposedDropdownMenu(
                expanded         = expanded,
                onDismissRequest = { expanded = false }
            ) {
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
