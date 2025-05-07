// app/src/main/java/com/tundynamcorp/flippingmedicalsuppliesassistant/ui/invoice/InvoiceStep3Screen.kt
package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.invoice

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
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.HomeRepository
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.PriceHistory
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.Product
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceStep3Screen(
    existingLines: List<InvoiceLine>,
    onBack: () -> Unit,
    onAddLine: (InvoiceLine) -> Unit,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { HomeRepository() }

    // 1️⃣ Category & product selection
    val categories = listOf("Test Strips", "Devices", "Inhalers", "Insulin")
    var catExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedCat by rememberSaveable { mutableStateOf<String?>(null) }

    var prodExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedProd by rememberSaveable { mutableStateOf<Product?>(null) }

    // 2️⃣ Expiration date
    var expDate by rememberSaveable { mutableStateOf<String?>(null) }
    var datePickerVisible by rememberSaveable { mutableStateOf(false) }

    // 3️⃣ Condition
    var selectedCondition by rememberSaveable { mutableStateOf<String?>(null) }
    var condExpanded by remember { mutableStateOf(false) }
    val conditionOptions = listOf("New", "Dinged", "Damaged")

    // 4️⃣ Quantity
    var quantityStr by rememberSaveable { mutableStateOf("1") }
    val quantity = quantityStr.toIntOrNull() ?: 1

    // 5️⃣ Fetch raw price history when product changes
    var rawHistory by remember { mutableStateOf<PriceHistory?>(null) }
    LaunchedEffect(selectedProd) {
        selectedProd?.let {
            rawHistory = repo.getPriceHistory(it.category, it.barcode)
            expDate = null
            selectedCondition = null
        }
    }

    // 6️⃣ Compute unitPrice based on expDate, history & condition
    var unitPrice by rememberSaveable { mutableStateOf<Float?>(null) }
    LaunchedEffect(expDate, rawHistory, selectedCondition) {
        val history = rawHistory
        val dateStr = expDate
        if (history != null && dateStr != null) {
            try {
                val fmt = SimpleDateFormat("M/d/yyyy", Locale.US)
                val baseDate = fmt.parse(history.lastUpdated)!!
                val selDate  = fmt.parse(dateStr)!!
                val calSel = Calendar.getInstance().apply { time = selDate }
                val calBase = Calendar.getInstance().apply { time = baseDate }
                val diffMonths = (calSel.get(Calendar.YEAR) - calBase.get(Calendar.YEAR)) * 12 +
                        (calSel.get(Calendar.MONTH) - calBase.get(Calendar.MONTH))
                val idx = if (diffMonths > 11) 0 else (11 - diffMonths).coerceAtLeast(0)
                val prices = history.prices
                val basePrice = prices.getOrNull(idx) ?: prices.last()

                // apply “Dinged” / “Damaged” rules
                unitPrice = when (selectedCondition) {
                    "Dinged" -> {
                        when (selectedCat) {
                            "Test Strips" -> basePrice - 3f
                            "Devices"    ->
                                if (selectedProd!!.description.contains("DexcomG6", true))
                                    basePrice - 15f else basePrice - 5f
                            else -> basePrice
                        }
                    }
                    "Damaged" -> {
                        when (selectedCat) {
                            "Test Strips" ->
                                if (idx in 0..3) prices.getOrElse(4) { basePrice }
                                else basePrice
                            "Devices"    ->
                                if (selectedProd!!.description.contains("DexcomG6", true))
                                    basePrice - 15f else basePrice - 5f
                            else -> basePrice
                        }
                    }
                    else -> basePrice // “New” or null
                }
            } catch (_: Exception) {
                unitPrice = null
            }
        } else {
            unitPrice = null
        }
    }

    // 7️⃣ Filter products for the selected category
    val allProducts by repo.getAllProducts().collectAsState(emptyList())
    val prodsForCat = selectedCat?.let { cat ->
        allProducts.filter { it.category == cat }
    }.orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Summary of added items ---
        if (existingLines.isNotEmpty()) {
            Text(
                text = "Items added: ${existingLines.size}    " +
                        "Total: $${existingLines.sumOf { it.lineTotal.toDouble() }.toInt()}",
                style = MaterialTheme.typography.bodyLarge
            )
            HorizontalDivider()
        }

        Text("Step 3: Add Item", style = MaterialTheme.typography.titleLarge)

        // Category dropdown
        ExposedDropdownMenuBox(
            expanded = catExpanded,
            onExpandedChange = { catExpanded = it }
        ) {
            TextField(
                value = selectedCat ?: "Select Category",
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = catExpanded,
                onDismissRequest = { catExpanded = false }
            ) {
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
        ExposedDropdownMenuBox(
            expanded = prodExpanded,
            onExpandedChange = { prodExpanded = it }
        ) {
            TextField(
                value = selectedProd?.description ?: "Select Product",
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                label = { Text("Product") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(prodExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = prodExpanded,
                onDismissRequest = { prodExpanded = false }
            ) {
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

        // Expiration date picker
        Text("Expiration Date", style = MaterialTheme.typography.bodyMedium)
        Button(onClick = { datePickerVisible = true }) {
            Text(expDate ?: "Select Date")
        }
        if (datePickerVisible) {
            ShowDatePicker(
                context = context,
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
        ExposedDropdownMenuBox(
            expanded = condExpanded,
            onExpandedChange = { condExpanded = it }
        ) {
            TextField(
                value = selectedCondition ?: "Select Condition",
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(condExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = condExpanded,
                onDismissRequest = { condExpanded = false }
            ) {
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

        // Quantity field
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

        // Action buttons
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onBack) {
                Text("Back")
            }
            Button(
                onClick = {
                    val prod = selectedProd!!
                    val price = unitPrice!!
                    onAddLine(InvoiceLine(prod.description, expDate!!, price, quantity))
                    // reset fields
                    selectedCat = null
                    selectedProd = null
                    expDate = null
                    selectedCondition = null
                    quantityStr = "1"
                    unitPrice = null
                },
                enabled = selectedProd != null && unitPrice != null && selectedCondition != null
            ) {
                Text("Add Another")
            }
            Button(
                onClick = {
                    selectedProd?.let { prod ->
                        val price = unitPrice!!
                        onAddLine(InvoiceLine(prod.description, expDate!!, price, quantity))
                    }
                    onDone()
                },
                enabled = existingLines.isNotEmpty() ||
                        (selectedProd != null && unitPrice != null && selectedCondition != null)
            ) {
                Text("Done")
            }
        }
    }
}

/** Helper to show a native DatePickerDialog */
@Composable
private fun ShowDatePicker(
    context: Context,
    onDateSelected: (Int, Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) {
        val now = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, day -> onDateSelected(year, month, day) },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnCancelListener { onDismiss() }
            show()
        }
    }
}
