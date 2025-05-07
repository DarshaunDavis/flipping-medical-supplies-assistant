package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.invoice

import android.app.Activity
import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.UserRole
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.auth.AuthViewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.common.UpgradePromptDialog
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.settings.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InvoiceScreen(
    settingsViewModel: SettingsViewModel = viewModel(),
    authVm: AuthViewModel               = viewModel(),
    invoiceVm: InvoiceViewModel         = viewModel()
) {
    val context = LocalContext.current
    // to pop back to HomeScreen on “Not now”
    val activity = context as? Activity

    // — wizard state
    var step by rememberSaveable { mutableIntStateOf(1) }
    var sellerInfo: SellerInfo?   by remember { mutableStateOf(null) }
    var invoiceMeta: InvoiceMeta? by remember { mutableStateOf(null) }
    var lines: List<InvoiceLine>  by remember { mutableStateOf(emptyList()) }

    // — gating & trial
    val role              by authVm.role.collectAsState()
    val isTrialActive     by authVm.isTrialActive.collectAsState()
    val invoicesThisMonth by invoiceVm.countThisMonth.collectAsState()

    // — upgrade dialog
    var showUpgrade by remember { mutableStateOf(false) }

    Surface(Modifier.fillMaxSize()) {
        when (step) {
            1 -> InvoiceStep1Screen(onNext = { info ->
                settingsViewModel.updateProfile(info)
                sellerInfo = info
                step = 2
            })

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

            3 -> {
                if (sellerInfo == null || invoiceMeta == null) return@Surface
                InvoiceStep3Screen(
                    existingLines = lines,
                    onBack        = { step = 2 },
                    onAddLine     = { line -> lines = lines + line },
                    onDone        = { step = 4 }
                )
            }

            4 -> {
                // guard
                if (sellerInfo == null || invoiceMeta == null) return@Surface

                // printing allowed?
                val canPrint = when (role) {
                    UserRole.User       -> isTrialActive || (invoicesThisMonth == 0)
                    UserRole.Subscriber -> true
                    UserRole.Admin      -> true
                    else                -> false
                }

                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ── Header ──
                    Text(
                        "Invoice",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "Submitted on ${SimpleDateFormat("M/d/yyyy", Locale.US).format(Date())}",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(Modifier.height(8.dp))

                    // ── Seller Info ──
                    sellerInfo!!.let { s ->
                        Text(s.name, fontWeight = FontWeight.SemiBold)
                        s.dba?.let { Text("d.b.a $it") }
                        Text(s.address1)
                        s.address2?.let { Text(it) }
                        Text("${s.city}, ${s.state} ${s.zip}")
                        Text(s.phone)
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Client / Invoice Details Row ──
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Invoice for", fontWeight = FontWeight.SemiBold)
                            Text(invoiceMeta!!.clientName)
                            Text(invoiceMeta!!.clientAddress1 + invoiceMeta!!.clientAddress2?.let { ", $it" }.orEmpty())
                            Text("${invoiceMeta!!.clientCity}, ${invoiceMeta!!.clientState} ${invoiceMeta!!.clientZip}")
                        }
                        Column {
                            Text("Payable to", fontWeight = FontWeight.SemiBold)
                            Text(invoiceMeta!!.payableTo)
                        }
                        Column {
                            Text("Invoice #", fontWeight = FontWeight.SemiBold)
                            Text(invoiceMeta!!.invoiceNumber.orEmpty())
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

                    // ── Line Items Table Header ──
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F0F0))
                            .padding(8.dp)
                    ) {
                        Text("Description", Modifier.weight(0.5f), fontWeight = FontWeight.SemiBold)
                        Text("Qty", Modifier.weight(0.1f), fontWeight = FontWeight.SemiBold)
                        Text("Unit price", Modifier.weight(0.2f), fontWeight = FontWeight.SemiBold)
                        Text("Total price", Modifier.weight(0.2f), fontWeight = FontWeight.SemiBold)
                    }

                    // ── Line Items ──
                    lines.forEach { line ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text(line.description, Modifier.weight(0.5f))
                            Text(line.quantity.toString(), Modifier.weight(0.1f))
                            Text("$${"%.2f".format(line.unitPrice)}", Modifier.weight(0.2f))
                            Text("$${"%.2f".format(line.lineTotal)}", Modifier.weight(0.2f))
                        }
                    }

                    HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

                    // ── Totals ──
                    val subtotal = lines.sumOf { it.lineTotal.toDouble()  }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Column(Modifier.width(200.dp)) {
                            Text("Subtotal", fontWeight = FontWeight.SemiBold)
                            Text("Adjustments", fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            Text("Total Due", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("$${"%.2f".format(subtotal)}")
                            Text("$0.00")
                            Spacer(Modifier.height(8.dp))
                            Text("$${"%.2f".format(subtotal)}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Back & Print ──
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(onClick = { step = 3 }) {
                            Text("Back")
                        }
                        Button(
                            onClick = {
                                if (canPrint) {
                                    // — generate & launch system print
                                    val file = InvoicePdfGenerator.generate(
                                        context = context,
                                        seller  = sellerInfo!!,
                                        meta    = invoiceMeta!!,
                                        lines   = lines
                                    )
                                    (context.getSystemService(Context.PRINT_SERVICE) as PrintManager)
                                        .print(
                                            "Invoice — ${file.name}",
                                            PdfDocumentAdapter(file.absolutePath),
                                            PrintAttributes.Builder().build()
                                        )
                                    // **record** this invoice immediately
                                    invoiceVm.recordInvoice()
                                } else {
                                    showUpgrade = true
                                }
                            },
                            enabled = canPrint
                        ) {
                            Text("Print / Save")
                        }
                    }
                }

                // ── Upgrade prompt ──
                if (showUpgrade) {
                    UpgradePromptDialog(
                        onSubscribe = { /* navigate to subscription */ },
                        onDismiss   = {
                            showUpgrade = false
                            // pop back to HomeScreen so they’re never “stuck”
                            activity?.onBackPressed()
                        }
                    )
                }
            }
        }
    }
}
