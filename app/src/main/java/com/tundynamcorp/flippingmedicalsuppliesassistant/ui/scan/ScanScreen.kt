package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.scan

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.*
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.HomeRepository
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.HomeViewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.Product
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.flow.first
import androidx.compose.material3.MenuAnchorType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val homeVm: HomeViewModel = viewModel()
    val ph by homeVm.priceHistory.collectAsState()

    // scanning state
    var isScanning by remember { mutableStateOf(false) }
    var scannedCode by remember { mutableStateOf<String?>(null) }
    var matchedProduct by remember { mutableStateOf<Product?>(null) }

    // form state
    var selectedDate by remember { mutableStateOf<String?>(null) }
    var datePickerVisible by remember { mutableStateOf(false) }
    var selectedCondition by remember { mutableStateOf<String?>(null) }
    var condDropdownExpanded by remember { mutableStateOf(false) }

    // submit/result state
    var submitted by remember { mutableStateOf(false) }
    var resultPrice by remember { mutableStateOf<Float?>(null) }
    var reject by remember { mutableStateOf(false) }

    // camera view ref
    var barcodeView: DecoratedBarcodeView? by remember { mutableStateOf(null) }

    // 1) Pause/resume camera with lifecycle
    DisposableEffect(lifecycleOwner) {
        val obs = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                if (isScanning) barcodeView?.resume()
            }
            override fun onPause(owner: LifecycleOwner) {
                barcodeView?.pause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    // 2) Handle scan results
    val callback = remember {
        object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                result?.text?.takeIf { it.isNotBlank() }?.let { code ->
                    if (scannedCode == null) {
                        scannedCode = code
                        isScanning = false
                        barcodeView?.pause()
                    }
                }
            }
            override fun possibleResultPoints(points: MutableList<ResultPoint>?) = Unit
        }
    }

    // 3) Lookup product + kick off history load
    LaunchedEffect(scannedCode) {
        matchedProduct = null
        submitted = false
        resultPrice = null
        reject = false
        selectedDate = null
        selectedCondition = null
        if (scannedCode != null) {
            val all = HomeRepository()
                .getAllProducts()
                .first()
            matchedProduct = all.firstOrNull { it.barcode == scannedCode }
            matchedProduct?.let { prod ->
                homeVm.loadPriceHistory(prod.category, prod.barcode)
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isScanning) {
            AndroidView(
                factory = { ctx ->
                    DecoratedBarcodeView(ctx).apply {
                        initializeFromIntent(Intent())
                        decodeContinuous(callback)
                        resume()
                        barcodeView = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Button(onClick = {
                scannedCode = null
                matchedProduct = null
                isScanning = true
            }) {
                Text(if (scannedCode == null) "Scan Barcode" else "Scan Again")
            }

            Spacer(Modifier.height(24.dp))

            if (scannedCode != null && matchedProduct == null) {
                Text(
                    "We don’t accept that product.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            matchedProduct?.let { prod ->
                Text(
                    prod.description,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.height(16.dp))

                // --- Only show form BEFORE submit! ---
                if (!submitted) {
                    // Expiration Date Picker
                    selectedDate?.let {
                        Text("Expiration Date:", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(4.dp))
                    }
                    TextButton(onClick = { datePickerVisible = true }) {
                        Text(selectedDate ?: "Select Expiration Date")
                    }
                    if (datePickerVisible) {
                        ShowDatePicker(
                            context,
                            onDateSelected = { y, m, d ->
                                selectedDate = "${m+1}/$d/$y"
                                datePickerVisible = false
                            },
                            onDismiss = { datePickerVisible = false }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Condition Spinner
                    selectedCondition?.takeIf { it.isNotBlank() }?.let {
                        Text("Product Condition:", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(4.dp))
                    }
                    ExposedDropdownMenuBox(
                        expanded = condDropdownExpanded,
                        onExpandedChange = { condDropdownExpanded = it }
                    ) {
                        TextField(
                            value = selectedCondition ?: "Select Condition",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(condDropdownExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = condDropdownExpanded,
                            onDismissRequest = { condDropdownExpanded = false }
                        ) {
                            listOf("New", "Dinged", "Damaged").forEach { cond ->
                                DropdownMenuItem(
                                    text = { Text(cond) },
                                    onClick = {
                                        selectedCondition = cond
                                        condDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Submit button
                    if (selectedDate != null && !selectedCondition.isNullOrBlank()) {
                        Button(onClick = {
                            // compute month diff
                            val inputFmt = SimpleDateFormat("M/d/yyyy", Locale.US)
                            val selDate = inputFmt.parse(selectedDate!!)
                            val baseDate = SimpleDateFormat("M/d/yyyy", Locale.US)
                                .parse(ph!!.lastUpdated)
                            val selCal = Calendar.getInstance().apply { time = selDate!! }
                            val baseCal = Calendar.getInstance().apply { time = baseDate!! }
                            val yearDiff = selCal.get(Calendar.YEAR) - baseCal.get(Calendar.YEAR)
                            val monDiff  = selCal.get(Calendar.MONTH) - baseCal.get(Calendar.MONTH)
                            val diff     = yearDiff * 12 + monDiff

                            if (diff < 2) {
                                reject = true
                            } else {
                                // pick historical price index
                                val idx = if (diff > 11) 0 else 11 - diff
                                val basePrice = ph!!.prices[idx]

                                // apply condition-based adjustment:
                                val cat = prod.category.lowercase(Locale.US)
                                val desc = prod.description
                                resultPrice = when (selectedCondition) {
                                    "Dinged" -> {
                                        when {
                                            cat == "test strips" ->
                                                basePrice - 3f
                                            cat == "devices" ->
                                                if (desc.contains("Dexcom G6", true))
                                                    basePrice - 15f
                                                else basePrice - 5f
                                            else -> basePrice
                                        }
                                    }
                                    "Damaged" -> {
                                        if (cat == "test strips") {
                                            // if within the first 4 months use month-5 price
                                            if (idx in 0..3)
                                                ph!!.prices.getOrElse(4) { basePrice }
                                            else basePrice
                                        } else if (cat == "devices") {
                                            // same % reduction for any damage on devices
                                            if (desc.contains("Dexcom G6", true))
                                                basePrice - 15f
                                            else basePrice - 5f
                                        } else basePrice
                                    }
                                    else -> basePrice // "New" or any other
                                }
                            }
                            datePickerVisible = false
                            condDropdownExpanded = false
                            submitted = true
                        }) {
                            Text("Submit")
                        }
                    }
                }

                // --- After submit: show result or rejection ---
                if (submitted) {
                    Spacer(Modifier.height(24.dp))
                    if (reject) {
                        Text(
                            "We cannot accept that product.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = "$${resultPrice?.toInt()}",
                            style = MaterialTheme.typography.displayLarge,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("Expiration Date: $selectedDate")
                        Text("Product Condition: $selectedCondition")
                    }
                }
            }
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