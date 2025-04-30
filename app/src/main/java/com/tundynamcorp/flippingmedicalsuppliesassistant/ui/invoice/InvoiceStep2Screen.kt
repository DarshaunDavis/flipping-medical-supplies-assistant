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

    // — load & parse our buyer list from strings.xml
    val rawBuyers = stringArrayResource(R.array.buyer_list).toList()
    data class BuyerInfo(
        val name: String,
        val address1: String,
        val address2: String?,
        val city: String,
        val state: String,
        val zip: String
    )
    val buyers = rawBuyers.map { item ->
        val parts = item.split("|")
        BuyerInfo(
            name     = parts.getOrNull(0).orEmpty(),
            address1 = parts.getOrNull(1).orEmpty(),
            address2 = parts.getOrNull(2)?.takeIf { it.isNotBlank() },
            city     = parts.getOrNull(3).orEmpty(),
            state    = parts.getOrNull(4).orEmpty(),
            zip      = parts.getOrNull(5).orEmpty()
        )
    }

    // spinner options = “Select…” + names + “Manual Entry”
    val buyerOptions = listOf("Select Existing Buyer") +
            buyers.map { it.name } +
            listOf("Manual Entry")

    var buyerExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedBuyer by rememberSaveable { mutableStateOf(buyerOptions[0]) }

    // form fields
    var clientName     by rememberSaveable { mutableStateOf(initial?.clientName     ?: "") }
    var clientAddress1 by rememberSaveable { mutableStateOf(initial?.clientAddress1 ?: "") }
    var clientAddress2 by rememberSaveable { mutableStateOf(initial?.clientAddress2.orEmpty()) }
    var clientCity     by rememberSaveable { mutableStateOf(initial?.clientCity     ?: "") }
    var clientState    by rememberSaveable { mutableStateOf(initial?.clientState    ?: "") }
    var clientZip      by rememberSaveable { mutableStateOf(initial?.clientZip      ?: "") }
    var invoiceNum     by rememberSaveable { mutableStateOf(initial?.invoiceNumber.orEmpty()) }

    val payableTo = remember(sellerInfo) { sellerInfo.dba ?: sellerInfo.name }
    val statesList = stringArrayResource(id = R.array.states).toList()
    var stateDropdownExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Step 2: Invoice Details", style = MaterialTheme.typography.titleLarge)

        // ── Buyer spinner ──
        ExposedDropdownMenuBox(
            expanded = buyerExpanded,
            onExpandedChange = { buyerExpanded = it }
        ) {
            TextField(
                value = selectedBuyer,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                label = { Text("Existing Buyer") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(buyerExpanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = buyerExpanded,
                onDismissRequest = { buyerExpanded = false }
            ) {
                buyerOptions.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt) },
                        onClick = {
                            selectedBuyer = opt
                            buyerExpanded = false
                            when {
                                opt == "Manual Entry" || opt == buyerOptions[0] -> {
                                    // clear for manual
                                    clientName = ""
                                    clientAddress1 = ""
                                    clientAddress2 = ""
                                    clientCity = ""
                                    clientState = ""
                                    clientZip = ""
                                }
                                else -> {
                                    // populate from buyers list
                                    val info = buyers.first { it.name == opt }
                                    clientName     = info.name
                                    clientAddress1 = info.address1
                                    clientAddress2 = info.address2.orEmpty()
                                    clientCity     = info.city
                                    clientState    = info.state
                                    clientZip      = info.zip
                                }
                            }
                            focusManager.clearFocus()
                        }
                    )
                }
            }
        }

        // ── Client fields ──
        OutlinedTextField(
            value = clientName,
            onValueChange = { clientName = it },
            label = { Text("Client Name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = clientAddress1,
            onValueChange = { clientAddress1 = it },
            label = { Text("Address Line 1") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = clientAddress2,
            onValueChange = { clientAddress2 = it },
            label = { Text("Address Line 2 (optional)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = clientCity,
            onValueChange = { clientCity = it },
            label = { Text("City") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            modifier = Modifier.fillMaxWidth()
        )

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
                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
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

        OutlinedTextField(
            value = clientZip,
            onValueChange = { clientZip = it.filter(Char::isDigit) },
            label = { Text("Zip Code") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = payableTo,
            onValueChange = {},
            label = { Text("Payable To") },
            readOnly = true,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = invoiceNum,
            onValueChange = { invoiceNum = it.filter(Char::isDigit) },
            label = { Text("Invoice # (optional)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onBack) { Text("Back") }
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

