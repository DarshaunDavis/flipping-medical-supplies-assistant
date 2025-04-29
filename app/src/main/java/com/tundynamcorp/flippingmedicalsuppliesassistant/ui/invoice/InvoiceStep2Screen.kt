package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.invoice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tundynamcorp.flippingmedicalsuppliesassistant.R

/**
 * Step 2 form: Invoice details after seller info.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceStep2Screen(
    sellerInfo: SellerInfo,
    onBack: () -> Unit,
    onNext: (InvoiceMeta) -> Unit
) {
    // Local state for client fields
    var clientName by remember { mutableStateOf("") }
    var clientAddress1 by remember { mutableStateOf("") }
    var clientAddress2 by remember { mutableStateOf("") }
    var clientCity by remember { mutableStateOf("") }
    var clientState by remember { mutableStateOf("") }
    var clientZip by remember { mutableStateOf("") }
    var invoiceNum by remember { mutableStateOf("") }

    // Pre-fill “Payable To” from sellerInfo.dba or sellerInfo.name
    val payableTo = remember(sellerInfo) {
        sellerInfo.dba ?: sellerInfo.name
    }

    // States dropdown reuse
    val statesList = stringArrayResource(id = R.array.states).toList()
    var stateDropdownExpanded by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Step 2: Invoice Details", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = clientName,
            onValueChange = { clientName = it },
            label = { Text("Client Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = clientAddress1,
            onValueChange = { clientAddress1 = it },
            label = { Text("Client Address 1") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = clientAddress2,
            onValueChange = { clientAddress2 = it },
            label = { Text("Client Address 2 (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        // City
        OutlinedTextField(
            value = clientCity,
            onValueChange = { clientCity = it },
            label = { Text("City") },
            modifier = Modifier.fillMaxWidth()
        )

        // State spinner
        ExposedDropdownMenuBox(
            expanded = stateDropdownExpanded,
            onExpandedChange = { stateDropdownExpanded = it }
        ) {
            TextField(
                value = clientState.ifBlank { "Select State" },
                onValueChange = {},
                readOnly = true,
                label = { Text("State") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(stateDropdownExpanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(
                        type = MenuAnchorType.PrimaryNotEditable,
                        enabled = true
                    )
            )
            ExposedDropdownMenu(
                expanded = stateDropdownExpanded,
                onDismissRequest = { stateDropdownExpanded = false }
            ) {
                statesList.forEach { s ->
                    DropdownMenuItem(
                        text = { Text(s) },
                        onClick = {
                            clientState = s
                            stateDropdownExpanded = false
                        }
                    )
                }
            }
        }

        // Zip
        OutlinedTextField(
            value = clientZip,
            onValueChange = { clientZip = it.filter(Char::isDigit) },
            label = { Text("Zip Code") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        // Payable To (read-only)
        OutlinedTextField(
            value = payableTo,
            onValueChange = {},
            label = { Text("Payable To") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true
        )

        // Invoice #
        OutlinedTextField(
            value = invoiceNum,
            onValueChange = { invoiceNum = it.filter(Char::isDigit) },
            label = { Text("Invoice # (optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(Modifier.height(24.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onBack) {
                Text("Back")
            }
            Button(
                onClick = {
                    onNext(
                        InvoiceMeta(
                            clientName     = clientName,
                            clientAddress1 = clientAddress1,
                            clientAddress2 = clientAddress2.takeIf(String::isNotBlank),
                            clientCity     = clientCity,
                            clientState    = clientState,
                            clientZip      = clientZip,
                            payableTo      = payableTo,
                            invoiceNumber  = invoiceNum.takeIf(String::isNotBlank)
                        )
                    )
                },
                enabled = listOf(clientName, clientAddress1, clientCity, clientState, clientZip)
                    .all { it.isNotBlank() }
            ) {
                Text("Next")
            }
        }
    }
}