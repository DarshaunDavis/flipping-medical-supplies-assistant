package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.invoice

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.settings.SettingsViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current

    // Persisted seller profile for Step 1
    val profile by settingsViewModel.profileInfo.collectAsState()

    // Track which step: 1=Seller, 2=Client, 3=Items, 4=Review
    var step by rememberSaveable { mutableStateOf(1) }

    // Data carried between steps
    var sellerInfo  by remember { mutableStateOf<SellerInfo?>(null) }
    var invoiceMeta by remember { mutableStateOf<InvoiceMeta?>(null) }

    // Now hold a list of line‐items
    var lines by remember { mutableStateOf<List<InvoiceLine>>(emptyList()) }

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

            // Step 3: Add Items
            3 -> {
                val s = sellerInfo ?: return@Surface
                val m = invoiceMeta ?: return@Surface
                InvoiceStep3Screen(
                    sellerInfo    = s,
                    invoiceMeta   = m,
                    existingLines = lines,
                    onBack        = { step = 2 },
                    onAddLine     = { line -> lines = lines + line },
                    onDone        = { step = 4 }
                )
            }

            // Step 4: Review & Finish
            4 -> Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Review Invoice", style = MaterialTheme.typography.titleLarge)

                // Seller info
                sellerInfo?.let { s ->
                    Text("Seller: ${s.name}")
                    Text("Address: ${s.address1}${s.address2?.let { ", $it" } ?: ""}")
                    Text("Location: ${s.city}, ${s.state} ${s.zip}")
                    Text("Phone: ${s.phone}")
                    s.email?.let { Text("Email: $it") }
                }

                Spacer(Modifier.height(8.dp))

                // Client info
                invoiceMeta?.let { m ->
                    Text("Client: ${m.clientName}")
                    Text("Address: ${m.clientAddress1}${m.clientAddress2?.let { ", $it" } ?: ""}")
                    Text("Location: ${m.clientCity}, ${m.clientState} ${m.clientZip}")
                    Text("Payable To: ${m.payableTo}")
                    m.invoiceNumber?.let { Text("Invoice #: $it") }
                }

                Spacer(Modifier.height(16.dp))

                // Line-items
                lines.forEachIndexed { index, line ->
                    Text("${index + 1}. ${line.description} ×${line.quantity} = $${line.lineTotal.toInt()}")
                }

                Spacer(Modifier.height(12.dp))

                // Grand total
                val total = lines.sumOf { it.lineTotal.toDouble() }.toInt()
                Text("Total Due: $${total}", style = MaterialTheme.typography.titleLarge)

                Spacer(Modifier.weight(1f))

                // Back & Finish buttons
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = { step = 3 }) {
                        Text("Back")
                    }
                    Button(onClick = {
                        // 1) generate the file as before
                        val file = InvoicePdfGenerator.generate(
                            context = context,
                            seller  = sellerInfo!!,
                            meta    = invoiceMeta!!,
                            lines   = lines
                                    )
                        // 2) launch Print Preview (with "Save as PDF" built in)
                        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                        printManager.print(
                            "Invoice — ${file.name}",
                            PdfDocumentAdapter(context, file.absolutePath),
                            PrintAttributes.Builder().build()
                                    )
                        }) {
                        Text("Print / Save")
                        }
                }
            }
        }
    }
}
