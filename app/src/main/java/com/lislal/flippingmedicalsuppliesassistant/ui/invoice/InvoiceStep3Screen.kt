package com.lislal.flippingmedicalsuppliesassistant.ui.invoice

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lislal.flippingmedicalsuppliesassistant.data.HomeRepository
import com.lislal.flippingmedicalsuppliesassistant.data.PriceHistory
import com.lislal.flippingmedicalsuppliesassistant.data.Product
import com.lislal.flippingmedicalsuppliesassistant.ui.settings.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceStep3Screen(
    existingLines: List<InvoiceLine>,
    onBack: () -> Unit,
    onAddLine: (InvoiceLine) -> Unit,
    onDone: (hideLogo: Boolean) -> Unit,
    isSubscriber: Boolean,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val repo = remember { HomeRepository() }

    val selectedWholesalersMap by settingsViewModel.selectedWholesalersMap.collectAsState()

    val categories = listOf("Test Strips", "Devices", "Inhalers", "Insulin")
    var catExpanded   by rememberSaveable { mutableStateOf(false) }
    var selectedCat   by rememberSaveable { mutableStateOf<String?>(null) }
    var prodExpanded  by rememberSaveable { mutableStateOf(false) }
    var selectedProd  by rememberSaveable { mutableStateOf<Product?>(null) }

    var expDate          by rememberSaveable { mutableStateOf<String?>(null) }
    var datePickerVisible by rememberSaveable { mutableStateOf(false) }

    var selectedCondition by rememberSaveable { mutableStateOf<String?>(null) }
    var condExpanded      by remember { mutableStateOf(false) }
    val conditionOptions  = listOf("New", "Dinged", "Damaged")

    var quantityStr by rememberSaveable { mutableStateOf("1") }
    val quantity   = quantityStr.toIntOrNull() ?: 1

    var rawHistory by remember { mutableStateOf<PriceHistory?>(null) }
    LaunchedEffect(selectedCat, selectedProd) {
        if (selectedCat != null && selectedProd != null) {
            val wholesalerKey = selectedWholesalersMap[selectedCat].orEmpty()
            rawHistory = repo.getPriceHistory(selectedCat!!, selectedProd!!.barcode, wholesalerKey)
            expDate = null
            selectedCondition = null
        } else {
            rawHistory = null
        }
    }

    var unitPrice by rememberSaveable { mutableStateOf<Float?>(null) }
    LaunchedEffect(expDate, rawHistory, selectedCondition) {
        val history = rawHistory
        val dateStr = expDate
        if (history != null && dateStr != null) {
            try {
                val fmt      = SimpleDateFormat("M/d/yyyy", Locale.US)
                val baseDate = fmt.parse(history.lastUpdated)!!
                val selDate  = fmt.parse(dateStr)!!
                val calSel   = Calendar.getInstance().apply { time = selDate }
                val calBase  = Calendar.getInstance().apply { time = baseDate }
                val diffMonths =
                    (calSel.get(Calendar.YEAR) - calBase.get(Calendar.YEAR)) * 12 +
                            (calSel.get(Calendar.MONTH) - calBase.get(Calendar.MONTH))
                val idx       = if (diffMonths > 11) 0 else (11 - diffMonths).coerceAtLeast(0)
                val prices    = history.prices
                val basePrice = prices.getOrNull(idx) ?: prices.last()

                unitPrice = when (selectedCondition) {
                    "Dinged"  -> when (selectedCat) {
                        "Test Strips" -> basePrice - 3f
                        "Devices" ->
                            if (selectedProd!!.description.contains("DexcomG6", true))
                                basePrice - 15f else basePrice - 5f
                        else -> basePrice
                    }
                    "Damaged" -> when (selectedCat) {
                        "Test Strips" ->
                            if (idx in 0..3) prices.getOrElse(4) { basePrice }
                            else basePrice
                        "Devices" ->
                            if (selectedProd!!.description.contains("DexcomG6", true))
                                basePrice - 15f else basePrice - 5f
                        else -> basePrice
                    }
                    else      -> basePrice
                }
            } catch (_: Exception) {
                unitPrice = null
            }
        } else {
            unitPrice = null
        }
    }

    val allProducts by repo.getAllProducts().collectAsState(emptyList())
    val prodsForCat = selectedCat
        ?.let { cat -> allProducts.filter { it.category == cat } }
        .orEmpty()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (existingLines.isNotEmpty()) {
            Text(
                "Items added: ${existingLines.size}   Total: $${existingLines.sumOf { it.lineTotal.toDouble() }.toInt()}",
                style = MaterialTheme.typography.bodyLarge
            )
            HorizontalDivider()
        }

        Text("Step 3: Add Item", style = MaterialTheme.typography.titleLarge)

        // Category dropdown
        ExposedDropdownMenuBox(catExpanded, onExpandedChange = { catExpanded = it }) {
            TextField(
                value = selectedCat ?: "Select Category",
                onValueChange = {}, readOnly = true, label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catExpanded) },
                modifier = Modifier.fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(catExpanded, onDismissRequest = { catExpanded = false }) {
                categories.forEach { c ->
                    DropdownMenuItem(
                        text = { Text(c) },
                        onClick = {
                            selectedCat = c
                            catExpanded = false
                            selectedProd = null
                        }
                    )
                }
            }
        }

        // Product dropdown
        ExposedDropdownMenuBox(prodExpanded, onExpandedChange = { prodExpanded = it }) {
            TextField(
                value = selectedProd?.description ?: "Select Product",
                onValueChange = {}, readOnly = true, label = { Text("Product") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(prodExpanded) },
                modifier = Modifier.fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(prodExpanded, onDismissRequest = { prodExpanded = false }) {
                prodsForCat.forEach { p ->
                    DropdownMenuItem(
                        text = { Text(p.description) },
                        onClick = {
                            selectedProd = p
                            prodExpanded = false
                        }
                    )
                }
            }
        }

        // Expiration Date
        Text("Expiration Date", style = MaterialTheme.typography.bodyMedium)
        Button(onClick = { datePickerVisible = true }) {
            Text(expDate ?: "Select Date")
        }
        if (datePickerVisible) {
            ShowDatePicker(context,
                onDateSelected = { y, m, d ->
                    expDate = "${m + 1}/$d/$y"
                    datePickerVisible = false
                },
                onDismiss = { datePickerVisible = false }
            )
        }

        // Condition dropdown
        selectedCondition?.let {
            Text("Product Condition", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
        }
        ExposedDropdownMenuBox(condExpanded, onExpandedChange = { condExpanded = it }) {
            TextField(
                value = selectedCondition ?: "Select Condition",
                onValueChange = {}, readOnly = true, singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(condExpanded) },
                modifier = Modifier.fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(condExpanded, onDismissRequest = { condExpanded = false }) {
                conditionOptions.forEach { cond ->
                    DropdownMenuItem(
                        text = { Text(cond) },
                        onClick = {
                            selectedCondition = cond
                            condExpanded = false
                        }
                    )
                }
            }
        }

        // Quantity
        OutlinedTextField(
            value = quantityStr,
            onValueChange = { quantityStr = it.filter(Char::isDigit) },
            label = { Text("Quantity") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        // Price preview
        unitPrice?.let { price ->
            Text("Unit Price: $${price.toInt()}", style = MaterialTheme.typography.bodyLarge)
            Text(
                "Line Total: $${(price * quantity).toInt()}",
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(Modifier.height(24.dp))

        // Actions
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onBack) { Text("Back") }
            Button(
                onClick = {
                    val prod = selectedProd!!
                    onAddLine(InvoiceLine(prod.description, expDate!!, unitPrice!!, quantity))
                    selectedCat = null; selectedProd = null
                    expDate = null; selectedCondition = null
                    quantityStr = "1"; unitPrice = null
                },
                enabled = selectedProd != null && unitPrice != null && selectedCondition != null
            ) { Text("Add Another") }
            Button(
                onClick = {
                    selectedProd?.let { p ->
                        onAddLine(InvoiceLine(p.description, expDate!!, unitPrice!!, quantity))
                    }
                    onDone(!isSubscriber)
                },
                enabled = existingLines.isNotEmpty()
                        || (selectedProd != null && unitPrice != null && selectedCondition != null)
            ) { Text("Done") }
        }
    }
}

@Composable
private fun ShowDatePicker(
    context: Context,
    onDateSelected: (year: Int, month: Int, day: Int) -> Unit,
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) {
        val now = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, y, m, d -> onDateSelected(y, m, d) },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnCancelListener { onDismiss() }
            show()
        }
    }
}
