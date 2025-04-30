package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.invoice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tundynamcorp.flippingmedicalsuppliesassistant.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceStep2Screen(
    initial: InvoiceMeta? = null,
    sellerInfo: SellerInfo,
    onBack: () -> Unit,
    onNext: (InvoiceMeta) -> Unit
) {
    val focusManager = LocalFocusManager.current

    // Initialize each field from `initial` if provided, otherwise blank
    var clientName     by rememberSaveable { mutableStateOf(initial?.clientName     ?: "") }
    var clientAddress1 by rememberSaveable { mutableStateOf(initial?.clientAddress1 ?: "") }
    var clientAddress2 by rememberSaveable { mutableStateOf(initial?.clientAddress2.orEmpty()) }
    var clientCity     by rememberSaveable { mutableStateOf(initial?.clientCity     ?: "") }
    var clientState    by rememberSaveable { mutableStateOf(initial?.clientState    ?: "") }
    var clientZip      by rememberSaveable { mutableStateOf(initial?.clientZip      ?: "") }
    var invoiceNum     by rememberSaveable { mutableStateOf(initial?.invoiceNumber.orEmpty()) }

    // “Payable To” derived from sellerInfo
    val payableTo = remember(sellerInfo) {
        sellerInfo.dba ?: sellerInfo.name
    }

    // Dropdown and scroll state
    val statesList = stringArrayResource(id = R.array.states).toList()
    var stateDropdownExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Step 2: Invoice Details", style = MaterialTheme.typography.titleLarge)

        // Client Name
        OutlinedTextField(
            value = clientName,
            onValueChange = { clientName = it },
            label = { Text("Client Name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Client Address 1
        OutlinedTextField(
            value = clientAddress1,
            onValueChange = { clientAddress1 = it },
            label = { Text("Client Address 1") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Client Address 2 (optional)
        OutlinedTextField(
            value = clientAddress2,
            onValueChange = { clientAddress2 = it },
            label = { Text("Client Address 2 (optional)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // City
        OutlinedTextField(
            value = clientCity,
            onValueChange = { clientCity = it },
            label = { Text("City") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
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
                singleLine = true,
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
                            focusManager.moveFocus(FocusDirection.Down)
                        }
                    )
                }
            }
        }

        // Zip Code
        OutlinedTextField(
            value = clientZip,
            onValueChange = { clientZip = it.filter(Char::isDigit) },
            label = { Text("Zip Code") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Payable To (read-only)
        OutlinedTextField(
            value = payableTo,
            onValueChange = {},
            label = { Text("Payable To") },
            readOnly = true,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Invoice # (optional)
        OutlinedTextField(
            value = invoiceNum,
            onValueChange = { invoiceNum = it.filter(Char::isDigit) },
            label = { Text("Invoice # (optional)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        // Back & Next buttons
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
                enabled = listOf(
                    clientName,
                    clientAddress1,
                    clientCity,
                    clientState,
                    clientZip
                ).all { it.isNotBlank() }
            ) {
                Text("Next")
            }
        }
    }
}
