// app/src/main/java/com/tundynamcorp/flippingmedicalsuppliesassistant/ui/invoice/InvoiceScreen.kt
package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.invoice

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.Product
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    settingsViewModel: SettingsViewModel = viewModel()
) {
    // Persisted profile for Step 1
    val profile by settingsViewModel.profileInfo.collectAsState()

    // Which step: 1=Seller, 2=Client, 3=Item, 4=Review
    var step by rememberSaveable { mutableStateOf(1) }

    // Collected data
    var sellerInfo  by remember { mutableStateOf<SellerInfo?>(null) }
    var invoiceMeta by remember { mutableStateOf<InvoiceMeta?>(null) }

    // Step 3 results
    var lineProduct   by remember { mutableStateOf<Product?>(null) }
    var lineExpiry    by remember { mutableStateOf<String?>(null) }
    var lineQuantity  by remember { mutableStateOf(0) }
    var lineTotal     by remember { mutableStateOf(0f) }

    Surface(Modifier.fillMaxSize()) {
        when (step) {
            // Step 1: Seller Info
            1 -> InvoiceStep1Screen(onNext = { info ->
                settingsViewModel.updateProfile(info)
                sellerInfo = info
                step = 2
            })

            // Step 2: Client / Invoice Details
            2 -> sellerInfo?.let { s ->
                InvoiceStep2Screen(
                    initial    = invoiceMeta,
                    sellerInfo = s,
                    onBack     = { step = 1 },
                    onNext     = { meta ->
                        invoiceMeta = meta
                        step = 3
                    }
                )
            }

            // Step 3: Item & Quantity
            3 -> {
                val s = sellerInfo; val m = invoiceMeta
                if (s != null && m != null) {
                    InvoiceStep3Screen(
                        sellerInfo  = s,
                        invoiceMeta = m,
                        onBack      = { step = 2 },
                        onNext      = { prod, exp, qty, total ->
                            lineProduct  = prod
                            lineExpiry   = exp
                            lineQuantity = qty
                            lineTotal    = total
                            step = 4
                        }
                    )
                }
            }

            // Step 4: Final Review
            4 -> Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Review Invoice", style = MaterialTheme.typography.titleLarge)

                // Seller
                sellerInfo?.let { s ->
                    Text("Seller: ${s.name}")
                    Text("Address: ${s.address1}${s.address2?.let { ", $it" } ?: ""}")
                    Text("Location: ${s.city}, ${s.state} ${s.zip}")
                }

                // Client
                invoiceMeta?.let { m ->
                    Spacer(Modifier.height(8.dp))
                    Text("Client: ${m.clientName}")
                    Text("Address: ${m.clientAddress1}${m.clientAddress2?.let { ", $it" } ?: ""}")
                    Text("Location: ${m.clientCity}, ${m.clientState} ${m.clientZip}")
                }

                // Line‐item
                if (lineProduct != null && lineExpiry != null && lineQuantity > 0) {
                    Spacer(Modifier.height(8.dp))
                    Text("Product: ${lineProduct!!.description}")
                    Text("Expires: $lineExpiry")
                    Text("Quantity: $lineQuantity")
                    Text("Line Total: $${lineTotal.toInt()}", style = MaterialTheme.typography.titleLarge)
                }

                Spacer(Modifier.weight(1f))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = { step = 3 }) { Text("Back") }
                    Button(onClick = { /* TODO: generate/export invoice */ }) {
                        Text("Finish")
                    }
                }
            }
        }
    }
}
