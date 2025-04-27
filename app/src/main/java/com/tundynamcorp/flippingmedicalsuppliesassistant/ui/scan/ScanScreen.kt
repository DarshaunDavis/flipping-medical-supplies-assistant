// app/src/main/java/com/tundynamcorp/flippingmedicalsuppliesassistant/ui/scan/ScanScreen.kt
package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.scan

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.*
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.HomeRepository
import kotlinx.coroutines.flow.first

@Composable
fun ScanScreen() {
    var isScanning by remember { mutableStateOf(false) }
    var scannedCode by remember { mutableStateOf<String?>(null) }
    var scannedDesc by remember { mutableStateOf<String?>(null) }

    // ZXing view reference so we can pause/resume
    var barcodeView: DecoratedBarcodeView? by remember { mutableStateOf(null) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val repo = remember { HomeRepository() }

    // Continuous callback: on first non-blank scan, save code & stop
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

    // Pause/resume ZXing view with lifecycle
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

    // Lookup product description once a code arrives
    LaunchedEffect(scannedCode) {
        scannedCode?.let { code ->
            // fetch flat list and find matching barcode
            val all = repo.getAllProducts().first()
            scannedDesc = all.firstOrNull { it.barcode == code }
                ?.description
                ?: "Product not found"
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
            // 1) Scan button at the top
            Button(onClick = {
                scannedCode = null
                scannedDesc = null
                isScanning = true
            }) {
                Text(if (scannedCode == null) "Scan Barcode" else "Scan Again")
            }

            Spacer(Modifier.height(24.dp))

            // 2) Show looked-up description
            scannedDesc?.let { desc ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(desc, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}
