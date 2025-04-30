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
    // Pull persisted profile to offer autofill in Step 1
    val profile by settingsViewModel.profileInfo.collectAsState()

    // Track which step we’re on
    var step by rememberSaveable { mutableStateOf(1) }

    // Hold the data collected in each step
    var sellerInfo by remember { mutableStateOf<SellerInfo?>(null) }
    var invoiceMeta by remember { mutableStateOf<InvoiceMeta?>(null) }

    Surface(modifier = Modifier.fillMaxSize()) {
        when (step) {
            // --- Step 1: Seller Info ---
            1 -> InvoiceStep1Screen(
                onNext = { info ->
                    // optionally persist back to profile
                    settingsViewModel.updateProfile(info)
                    sellerInfo = info
                    step = 2
                }
            )

            // --- Step 2: Client / Invoice Details ---
            2 -> sellerInfo?.let { info ->
                InvoiceStep2Screen(
                    initial    = invoiceMeta,
                    sellerInfo = info,
                    onBack     = { step = 1 },
                    onNext     = { meta ->
                        invoiceMeta = meta
                        step = 3
                    }
                )
            }

            // --- Step 3: Review Preview ---
            3 -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Review", style = MaterialTheme.typography.titleLarge)

                sellerInfo?.let { s ->
                    Text("Seller: ${s.name}")
                    Text(
                        "Seller Address: ${s.address1}" +
                                (s.address2?.let { ", $it" } ?: "")
                    )
                    Text("Location: ${s.city}, ${s.state} ${s.zip}")
                    Text("Phone: ${s.phone}")
                    s.email?.let { Text("Email: $it") }
                }

                invoiceMeta?.let { m ->
                    Spacer(Modifier.height(8.dp))
                    Text("Client: ${m.clientName}")
                    Text(
                        "Client Address: ${m.clientAddress1}" +
                                (m.clientAddress2?.let { ", $it" } ?: "")
                    )
                    Text("Location: ${m.clientCity}, ${m.clientState} ${m.clientZip}")
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
                    Button(onClick = { /* TODO: finalize or export invoice */ }) {
                        Text("Finish")
                    }
                }
            }
        }
    }
}
