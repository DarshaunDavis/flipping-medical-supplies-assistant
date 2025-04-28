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
    import androidx.compose.ui.platform.LocalLifecycleOwner
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.viewinterop.AndroidView
    import androidx.lifecycle.DefaultLifecycleObserver
    import androidx.lifecycle.LifecycleOwner
    import androidx.lifecycle.viewmodel.compose.viewModel
    import com.google.zxing.ResultPoint
    import com.journeyapps.barcodescanner.*
    import com.tundynamcorp.flippingmedicalsuppliesassistant.data.HomeRepository
    import com.tundynamcorp.flippingmedicalsuppliesassistant.data.HomeViewModel
    import com.tundynamcorp.flippingmedicalsuppliesassistant.data.PriceHistory
    import com.tundynamcorp.flippingmedicalsuppliesassistant.data.Product
    import java.text.SimpleDateFormat
    import java.util.*
    import kotlinx.coroutines.flow.first
    import kotlinx.coroutines.launch
    import kotlinx.coroutines.delay

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
                // pull full list once
                val all = HomeRepository()
                    .getAllProducts()
                    .first()  // suspend, safe in LaunchedEffect
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
            // CAMERA PREVIEW
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

                // MAIN UI
            } else {
                // a) Scan / Rescan button
                Button(onClick = {
                    scannedCode = null
                    matchedProduct = null
                    isScanning = true
                }) {
                    Text(if (scannedCode == null) "Scan Barcode" else "Scan Again")
                }

                Spacer(Modifier.height(24.dp))

                // b) If scanned but no match
                if (scannedCode != null && matchedProduct == null) {
                    Text(
                        "We don’t accept that product.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // c) If match found
                matchedProduct?.let { prod ->
                    // show product name
                    Text(
                        prod.description,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(Modifier.height(16.dp))

                    // --- Expiration Date Picker ---
                    selectedDate?.let {
                        Text("Expiration Date:", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(4.dp))
                    }
                    TextButton(onClick = { datePickerVisible = true }) {
                        Text(selectedDate ?: "Select Expiration Date")
                    }
                    if (datePickerVisible) {
                        showDatePicker(
                            context,
                            onDateSelected = { y, m, d ->
                                selectedDate = "${m + 1}/$d/$y"
                                datePickerVisible = false
                            },
                            onDismiss = { datePickerVisible = false }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // --- Condition Spinner ---
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
                                .menuAnchor()
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

                    // d) Submit button (only until we submit once)
                    if (!submitted && selectedDate != null && !selectedCondition.isNullOrBlank()) {
                        Button(onClick = {
                            // compute month diff
                            val inputFmt = SimpleDateFormat("M/d/yyyy", Locale.US)
                            val selDate = inputFmt.parse(selectedDate!!)
                            val baseDate = SimpleDateFormat("M/d/yyyy", Locale.US)
                                .parse(ph!!.lastUpdated)
                            val selCal = Calendar.getInstance().apply { time = selDate!! }
                            val baseCal = Calendar.getInstance().apply { time = baseDate!! }
                            val yearDiff = selCal.get(Calendar.YEAR) - baseCal.get(Calendar.YEAR)
                            val monDiff = selCal.get(Calendar.MONTH) - baseCal.get(Calendar.MONTH)
                            val diff = yearDiff * 12 + monDiff

                            if (diff < 2) {
                                // too close or past
                                reject = true
                            } else {
                                // pick index: if beyond furthest, use price1 (idx=0)
                                val idx = when {
                                    diff > 11 -> 0
                                    else      -> 11 - diff
                                }
                                resultPrice = ph!!.prices[idx]
                            }
                            submitted = true
                        }) {
                            Text("Submit")
                        }
                    }

                    // e) After submit: show result or rejection
                    if (submitted) {
                        Spacer(Modifier.height(24.dp))
                        if (reject) {
                            Text(
                                "We cannot accept that product.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            // final price with override+margin baked in by VM
                            Text(
                                text = "$${resultPrice?.toInt()}",
                                style = MaterialTheme.typography.titleLarge
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
    private fun showDatePicker(
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

