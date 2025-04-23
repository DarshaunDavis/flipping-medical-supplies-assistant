// app/src/main/java/com/tundynamcorp/flippingmedicalsuppliesassistant/ui/admin/AdminScreen.kt
package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: AdminViewModel = viewModel()
) {
    // 1) Observe margins
    val margins by viewModel.margins.collectAsState()

    // 2) Static categories
    val categories = listOf("Test Strips", "Devices", "Inhalers", "Insulin")

    // dropdown state
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    // input state
    var input by remember { mutableStateOf("") }

    // to clear focus (dismiss keyboard)
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Top: display current margins
        Text("Current Profit Margins", style = MaterialTheme.typography.titleLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            margins.forEach { (cat, pct) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(cat, style = MaterialTheme.typography.bodyMedium)
                    Text("${pct.toInt()}%", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        // Bottom: form
        Text("Set Profit Margin (%)", style = MaterialTheme.typography.titleMedium)

        // ExposedDropdownMenuBox anchors the menu to the TextField
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                // hide keyboard if we open dropdown
                if (!expanded) focusManager.clearFocus()
                expanded = !expanded
            }
        ) {
            TextField(
                value = selectedCategory.orEmpty(),
                onValueChange = { /* readOnly */ },
                readOnly = true,
                placeholder = { Text("Select Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()      // <— this anchors the menu correctly
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat) },
                        onClick = {
                            selectedCategory = cat
                            expanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = input,
            onValueChange = { input = it.filter(Char::isDigit) },
            label = { Text("Enter Profit Margin") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                // ensure we have a category and a double
                val pct = input.toDoubleOrNull() ?: return@Button
                selectedCategory?.let { viewModel.updateMargin(it, pct) }
                // clear for next time
                input = ""
                selectedCategory = null
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Submit")
        }
    }
}
