package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.scan

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.*
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.HomeRepository
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.HomeViewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.Product
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

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

    // result state
    var submitted by remember { mutableStateOf(false) }
    var resultPrice by remember { mutableStateOf<Float?>(null) }
    var reject by remember { mutableStateOf(false) }

    // camera ref
    var barcodeView: DecoratedBarcodeView? by remember { mutableStateOf(null) }

    // lifecycle pause/resume
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

    // scan callback
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

    // lookup + load history
    LaunchedEffect(scannedCode) {
        matchedProduct = null
        submitted = false
        resultPrice = null
        reject = false
        selectedDate = null
        selectedCondition = null
        if (scannedCode != null) {
            val all = HomeRepository().getAllProducts().first()
            matchedProduct = all.firstOrNull { it.barcode == scannedCode }
            matchedProduct?.let { homeVm.loadPriceHistory(it.category, it.barcode) }
        }
    }

    // auto‐submit when date + condition + history ready
    LaunchedEffect(selectedDate, selectedCondition, ph) {
        if (!submitted && selectedDate != null && selectedCondition != null && ph != null && matchedProduct != null) {
            val prod = matchedProduct!!
            try {
                val fmt = SimpleDateFormat("M/d/yyyy", Locale.US)
                val sel = fmt.parse(selectedDate!!)!!
                val base = fmt.parse(ph!!.lastUpdated)!!
                val selCal = Calendar.getInstance().apply { time = sel }
                val baseCal = Calendar.getInstance().apply { time = base }
                val diffMonths = (selCal.get(Calendar.YEAR) - baseCal.get(Calendar.YEAR)) * 12 +
                        (selCal.get(Calendar.MONTH) - baseCal.get(Calendar.MONTH))
                if (diffMonths < 2) {
                    reject = true
                } else {
                    val idx = (11 - diffMonths).coerceIn(0, 11)
                    val basePrice = ph!!.prices.getOrNull(idx) ?: ph!!.prices.lastOrNull() ?: 0f
                    resultPrice = when (selectedCondition) {
                        "Dinged"  -> when {
                            prod.category.equals("Test Strips", true) -> basePrice - 3f
                            prod.category.equals("Devices", true) &&
                                    prod.description.contains("Dexcom G6", true) -> basePrice - 15f
                            prod.category.equals("Devices", true) -> basePrice - 5f
                            else -> basePrice
                        }
                        "Damaged" -> when {
                            prod.category.equals("Test Strips", true) && idx in 0..3 ->
                                ph!!.prices.getOrElse(4) { basePrice }
                            prod.category.equals("Devices", true) &&
                                    prod.description.contains("Dexcom G6", true) -> basePrice - 15f
                            prod.category.equals("Devices", true) -> basePrice - 5f
                            else -> basePrice
                        }
                        else -> basePrice
                    }
                }
            } catch (_: Exception) {
                reject = true
            }
            submitted = true
        }
    }

    // ───── UI ─────
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
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                scannedCode = null
                matchedProduct = null
                isScanning = true
            }) {
                Text(if (scannedCode == null) "Scan Barcode" else "Scan Again")
            }

            Spacer(Modifier.height(24.dp))

            if (scannedCode != null && matchedProduct == null) {
                Text("We don’t accept that product.", style = MaterialTheme.typography.bodyMedium)
            }

            matchedProduct?.let { prod ->
                Text(prod.description, style = MaterialTheme.typography.titleLarge)

                prod.imageUrl?.takeIf(String::isNotBlank)?.let { url ->
                    Spacer(Modifier.height(12.dp))
                    AsyncImage(
                        model = url,
                        contentDescription = prod.description,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // only show form until auto‐submit
                if (!submitted) {
                    // Expiration Date
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

                    // Condition
                    selectedCondition?.let {
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
                                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
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
                }

                // after auto‐submit: show result
                if (submitted) {
                    Spacer(Modifier.height(24.dp))
                    if (reject) {
                        Text("We cannot accept that product.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Text(
                            "$${resultPrice?.toInt()}",
                            style = MaterialTheme.typography.displayLarge
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
