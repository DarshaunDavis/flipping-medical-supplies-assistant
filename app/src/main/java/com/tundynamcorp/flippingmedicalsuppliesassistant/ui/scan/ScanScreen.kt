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
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.*
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.HomeRepository
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen() {
    var isScanning by remember { mutableStateOf(false) }
    var scannedCode by remember { mutableStateOf<String?>(null) }
    var matchedDescription by remember { mutableStateOf<String?>(null) }
    var selectedDate by remember { mutableStateOf<String?>(null) }
    var selectedCondition by remember { mutableStateOf<String?>("") }
    var datePickerVisible by remember { mutableStateOf(false) }
    var condDropdownExpanded by remember { mutableStateOf(false) }

    val barcodeView = remember { mutableStateOf<DecoratedBarcodeView?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Lookup once when we get a scan
    LaunchedEffect(scannedCode) {
        matchedDescription = null
        scannedCode?.let { code ->
            HomeRepository().getAllProducts()
                .collect { list ->
                    matchedDescription = list.firstOrNull { it.barcode == code }?.description
                }
        }
    }

    // Pause/resume camera on lifecycle
    DisposableEffect(lifecycleOwner) {
        val obs = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                if (isScanning) barcodeView.value?.resume()
            }
            override fun onPause(owner: LifecycleOwner) {
                barcodeView.value?.pause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
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
                        decodeContinuous(object : BarcodeCallback {
                            override fun barcodeResult(result: BarcodeResult?) {
                                result?.text?.takeIf { it.isNotBlank() }?.let { code ->
                                    if (scannedCode == null) {
                                        scannedCode = code
                                        isScanning = false
                                        pause()
                                    }
                                }
                            }
                            override fun possibleResultPoints(points: MutableList<ResultPoint>?) = Unit
                        })
                        resume()
                        barcodeView.value = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // 1) Scan / Rescan button
            Button(onClick = {
                scannedCode = null
                matchedDescription = null
                selectedDate = null
                selectedCondition = ""
                isScanning = true
            }) {
                Text(if (scannedCode == null) "Scan Barcode" else "Scan Again")
            }

            Spacer(Modifier.height(24.dp))

            // 2) Show result card
            scannedCode?.let { code ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (matchedDescription != null) {
                            Text(
                                matchedDescription!!,
                                style = MaterialTheme.typography.titleLarge
                            )

                            Spacer(Modifier.height(16.dp))

                            // --- Expiration Date section ---
                            // Label shows only after a date is picked
                            selectedDate?.let {
                                Text("Expiration Date:", style = MaterialTheme.typography.bodySmall)
                                Spacer(Modifier.height(4.dp))
                            }
                            TextButton(onClick = { datePickerVisible = true }) {
                                Text(selectedDate ?: "Select Expiration Date")
                            }
                            if (datePickerVisible) {
                                showDatePicker(
                                    LocalContext.current,
                                    onDateSelected = { y, m, d ->
                                        selectedDate = "${m + 1}/$d/$y"
                                        datePickerVisible = false
                                    },
                                    onDismiss = { datePickerVisible = false }
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            // --- Condition dropdown section ---
                            // Label shows only after a non-empty condition is picked
                            selectedCondition.takeIf { it?.isNotBlank() == true }?.let {
                                Text("Product Condition:", style = MaterialTheme.typography.bodySmall)
                                Spacer(Modifier.height(4.dp))
                            }
                            ExposedDropdownMenuBox(
                                expanded = condDropdownExpanded,
                                onExpandedChange = { condDropdownExpanded = it }
                            ) {
                                selectedCondition?.let {
                                    TextField(
                                        value = it.ifBlank { "Select Condition" },
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(condDropdownExpanded)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor()
                                    )
                                }
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
                        } else {
                            Text(
                                "We don’t accept that product.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
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
