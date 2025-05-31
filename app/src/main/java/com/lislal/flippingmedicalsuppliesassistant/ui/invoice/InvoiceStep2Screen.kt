package com.lislal.flippingmedicalsuppliesassistant.ui.invoice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lislal.flippingmedicalsuppliesassistant.R
import com.lislal.flippingmedicalsuppliesassistant.ui.settings.SettingsViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceStep2Screen(
    initial: InvoiceMeta? = null,
    sellerInfo: SellerInfo,
    onBack: () -> Unit,
    onNext: (InvoiceMeta) -> Unit,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val focusManager = LocalFocusManager.current
    val locale = Locale.getDefault()
    fun String.normalize() = split(' ')
        .joinToString(" ") { word ->
            word.lowercase(locale).replaceFirstChar { it.uppercase(locale) }
        }

    // 1) Pull live buyer list from RTDB
    val buyers by settingsViewModel.buyerList.collectAsState()
    val buyerOptions = remember(buyers) {
        listOf("Select Existing Buyer") + buyers.map { it.name }
    }
    var buyerExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedBuyer by rememberSaveable { mutableStateOf(buyerOptions[0]) }

    // 2) Form fields
    var clientName     by rememberSaveable { mutableStateOf(initial?.clientName ?: "") }
    var clientAddress1 by rememberSaveable { mutableStateOf(initial?.clientAddress1 ?: "") }
    var clientAddress2 by rememberSaveable { mutableStateOf(initial?.clientAddress2.orEmpty()) }
    var clientCity     by rememberSaveable { mutableStateOf(initial?.clientCity ?: "") }
    var clientState    by rememberSaveable { mutableStateOf(initial?.clientState ?: "") }
    var clientZip      by rememberSaveable { mutableStateOf(initial?.clientZip ?: "") }
    var invoiceNum     by rememberSaveable { mutableStateOf(initial?.invoiceNumber.orEmpty()) }

    // Payment details
    val payableTo = remember(sellerInfo) { sellerInfo.dba ?: sellerInfo.name }
    // Pull states from arrays.xml
    val statesList = stringArrayResource(id = R.array.states).toList()

    // FocusRequesters
    val stateRequester      = remember { FocusRequester() }
    val invoiceNumRequester = remember { FocusRequester() }
    val scrollState         = rememberScrollState()

    // State dropdown control
    var stateDropdownExpanded by rememberSaveable { mutableStateOf(false) }

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
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(buyerExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = buyerExpanded,
                onDismissRequest = { buyerExpanded = false }
            ) {
                buyerOptions.forEachIndexed { index, opt ->
                    DropdownMenuItem(
                        text = { Text(opt) },
                        onClick = {
                            selectedBuyer = opt
                            buyerExpanded = false
                            if (index == 0) {
                                // Manual entry
                                clientName = ""
                                clientAddress1 = ""
                                clientAddress2 = ""
                                clientCity = ""
                                clientState = ""
                                clientZip = ""
                            } else {
                                // Populate from RTDB buyer info
                                val info = buyers[index - 1]
                                clientName     = info.name
                                clientAddress1 = info.address1
                                clientAddress2 = info.address2.orEmpty()
                                clientCity     = info.city
                                clientState    = info.state
                                clientZip      = info.zip
                            }
                            focusManager.clearFocus()
                        }
                    )
                }
            }
        }

        // ── Client Name ──
        OutlinedTextField(
            value = clientName,
            onValueChange = { clientName = it.normalize() },
            label = { Text("Client Name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // ── Address Line 1 ──
        OutlinedTextField(
            value = clientAddress1,
            onValueChange = { clientAddress1 = it.normalize() },
            label = { Text("Address Line 1") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // ── Address Line 2 ──
        OutlinedTextField(
            value = clientAddress2,
            onValueChange = { clientAddress2 = it.normalize() },
            label = { Text("Address Line 2 (optional)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // ── City ──
        OutlinedTextField(
            value = clientCity,
            onValueChange = { clientCity = it.normalize() },
            label = { Text("City") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    stateDropdownExpanded = true
                    stateRequester.requestFocus()
                }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // ── State spinner ──
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
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(stateDropdownExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(stateRequester)
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
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

        // ── Zip Code ──
        OutlinedTextField(
            value = clientZip,
            onValueChange = { clientZip = it.filter(Char::isDigit).take(5) },
            label = { Text("Zip Code") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { invoiceNumRequester.requestFocus() }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // ── Payable To ──
        OutlinedTextField(
            value = payableTo,
            onValueChange = {},
            label = { Text("Payable To") },
            readOnly = true,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // ── Invoice # ──
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
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(invoiceNumRequester)
        )

        Spacer(Modifier.height(24.dp))

        // ── Navigation buttons ──
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
                enabled = listOf(
                    clientName, clientAddress1,
                    clientCity, clientState, clientZip
                ).all { it.isNotBlank() }
            ) {
                Text("Next")
            }
        }
    }
}
