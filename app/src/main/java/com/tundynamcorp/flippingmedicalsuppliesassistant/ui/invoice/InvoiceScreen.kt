// app/src/main/java/com/tundynamcorp/flippingmedicalsuppliesassistant/ui/invoice/InvoiceScreen.kt
package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.invoice

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen() {
    // Track which step we're on
    var step by rememberSaveable { mutableStateOf(1) }

    // Data passed between steps
    var sellerInfo by remember { mutableStateOf<SellerInfo?>(null) }
    var invoiceMeta by remember { mutableStateOf<InvoiceMeta?>(null) }

    Surface(modifier = Modifier.fillMaxSize()) {
        when (step) {
            1 -> {
                // Step 1: collect SellerInfo
                InvoiceStep1Screen { info ->
                    sellerInfo = info
                    step = 2
                }
            }
            2 -> {
                // Step 2: collect InvoiceMeta (requires sellerInfo)
                sellerInfo?.let { info ->
                    InvoiceStep2Screen(
                        sellerInfo = info,
                        onBack = { step = 1 },
                        onNext = { meta ->
                            invoiceMeta = meta
                            // TODO: advance to step 3 for line-items
                            // For now, just log or preview:
                            step = 3
                        }
                    )
                }
            }
            3 -> {
                // Simple preview of collected data (replace with Step 3 UI later)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Review", style = MaterialTheme.typography.titleLarge)
                    sellerInfo?.let {
                        Text("Seller: ${it.name}, ${it.address1}, ${it.city}, ${it.state} ${it.zip}")
                    }
                    invoiceMeta?.let {
                        Text("Client: ${it.clientName}, ${it.clientAddress1}, ${it.clientCity}, ${it.clientState} ${it.clientZip}")
                        Text("Payable To: ${it.payableTo}")
                    }
                    Spacer(Modifier.weight(1f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(onClick = { step = 2 }) {
                            Text("Back")
                        }
                        Button(onClick = { /* TODO: finish/invoice export */ }) {
                            Text("Finish")
                        }
                    }
                }
            }
        }
    }
}
