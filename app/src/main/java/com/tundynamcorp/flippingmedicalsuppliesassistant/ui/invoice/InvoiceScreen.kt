// app/src/main/java/com/tundynamcorp/flippingmedicalsuppliesassistant/ui/invoice/InvoiceScreen.kt
package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.invoice

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    settingsViewModel: SettingsViewModel = viewModel()
) {
    // ① Pull persisted profile for Step 1 autofill
    val profile by settingsViewModel.profileInfo.collectAsState()

    // ② Track current step
    var step by rememberSaveable { mutableStateOf(1) }

    // ③ Hold the SellerInfo and InvoiceMeta as we move through steps
    var sellerInfo by remember { mutableStateOf<SellerInfo?>(null) }
    var invoiceMeta by remember { mutableStateOf<InvoiceMeta?>(null) }

    Surface(modifier = Modifier.fillMaxSize()) {
        when (step) {
            // --- Step 1: Seller Info ---
            1 -> InvoiceStep1Screen(
                onNext = { info ->
                    // Persist back to profile if desired
                    settingsViewModel.updateProfile(info)
                    sellerInfo = info
                    step = 2
                }
            )

            // --- Step 2: Client / Invoice Details ---
            2 -> {
                // Only show when we have sellerInfo
                sellerInfo?.let { info ->
                    InvoiceStep2Screen(
                        initial    = invoiceMeta,
                        sellerInfo = info,
                        onBack      = { step = 1 },
                        onNext      = { meta ->
                            invoiceMeta = meta
                            step = 3
                        }
                    )
                }
            }

            // --- Step 3: Preview / Next Steps (placeholder) ---
            3 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Review", style = MaterialTheme.typography.titleLarge)
                    sellerInfo?.let { s ->
                        Text("Seller: ${s.name}")
                        Text("Seller Addr: ${s.address1}${s.address2?.let { ", $it" } ?: ""}")
                        Text("Seller Location: ${s.city}, ${s.state} ${s.zip}")
                        }
                    invoiceMeta?.let { m ->
                        Text("Client: ${m.clientName}")
                        Text("Client Addr: ${m.clientAddress1}${m.clientAddress2?.let { ", $it" } ?: ""}")
                        Text("Client Location: ${m.clientCity}, ${m.clientState} ${m.clientZip}")
                        Text("Payable To: ${m.payableTo}")
                        m.invoiceNumber?.let { Text("Invoice #: $it") }
                        }
                    Spacer(Modifier.weight(1f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(onClick = { step = 2 }) {
                            Text("Back")
                        }
                        Button(onClick = { /* TODO: finalize/invoice export */ }) {
                            Text("Finish")
                        }
                    }
                }
            }
        }
    }
}
