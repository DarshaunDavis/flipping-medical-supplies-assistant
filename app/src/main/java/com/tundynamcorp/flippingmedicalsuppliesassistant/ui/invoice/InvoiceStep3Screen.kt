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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.HomeRepository
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.PriceHistory
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.Product
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.settings.SettingsViewModel
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

    // pull the current marketplace‐buyer for each category
    val selectedBuyersMap by settingsViewModel.selectedBuyersMap.collectAsState()

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

    // 5️⃣ Fetch raw price history when product OR category changes
    var rawHistory by remember { mutableStateOf<PriceHistory?>(null) }
    LaunchedEffect(selectedProd, selectedCat, selectedBuyersMap) {
        val prod = selectedProd
        val cat = selectedCat
        if (prod != null && cat != null) {
            val buyerKey = selectedBuyersMap[cat].orEmpty()
            rawHistory = repo.getPriceHistory(cat, prod.barcode, buyerKey)
            expDate = null
            selectedCondition = null
        }
    }

    // 6️⃣ Compute unitPrice based on expDate, history & condition
    var unitPrice by rememberSaveable { mutableStateOf<Float?>(null) }
    LaunchedEffect(expDate, rawHistory, selectedCondition) {
        // ...unchanged...
    }

    // 7️⃣ Filter products for the selected category
    val allProducts by repo.getAllProducts().collectAsState(emptyList())
    val prodsForCat = selectedCat
        ?.let { cat -> allProducts.filter { it.category == cat } }
        .orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ...header, summary, etc...

        // ── Category dropdown ──
        ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = it }) {
            TextField(
                value = selectedCat ?: "Select Category",
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
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

        // ── Product dropdown ──
        ExposedDropdownMenuBox(expanded = prodExpanded, onExpandedChange = { prodExpanded = it }) {
            TextField(
                value = selectedProd?.description ?: "Select Product",
                onValueChange = {},
                readOnly = true,
                label = { Text("Product") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(prodExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(expanded = prodExpanded, onDismissRequest = { prodExpanded = false }) {
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

        // ── Condition dropdown ──
        ExposedDropdownMenuBox(expanded = condExpanded, onExpandedChange = { condExpanded = it }) {
            TextField(
                value = selectedCondition ?: "Select Condition",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(condExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(expanded = condExpanded, onDismissRequest = { condExpanded = false }) {
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

        // ...rest of form and buttons...
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
