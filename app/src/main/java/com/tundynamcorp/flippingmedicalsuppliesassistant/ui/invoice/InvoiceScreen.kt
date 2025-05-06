package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.invoice

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
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
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.UserRole
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.auth.AuthViewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.common.UpgradePromptDialog
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.settings.SettingsViewModel

@Composable
fun InvoiceScreen(
    settingsViewModel: SettingsViewModel = viewModel(),
    authVm: AuthViewModel               = viewModel(),
    invoiceVm: InvoiceViewModel         = viewModel()
) {
    val context = LocalContext.current

    // 1️⃣ Wizard step
    var step by rememberSaveable { mutableIntStateOf(1) }

    // 2️⃣ Shared data between steps
    var sellerInfo: SellerInfo?   by remember { mutableStateOf(null) }
    var invoiceMeta: InvoiceMeta? by remember { mutableStateOf(null) }
    var lines: List<InvoiceLine>  by remember { mutableStateOf(emptyList()) }

    // 3️⃣ Role & trial state
    val role             by authVm.role.collectAsState()
    val isTrialActive    by authVm.isTrialActive.collectAsState()
    val invoicesThisMonth by invoiceVm.countThisMonth.collectAsState()

    // 4️⃣ Upgrade dialog flag
    var showUpgrade by remember { mutableStateOf(false) }

    Surface(Modifier.fillMaxSize()) {
        when (step) {
            // ── Step 1: Seller Info ──
            1 -> InvoiceStep1Screen(onNext = { info ->
                settingsViewModel.updateProfile(info)  // ✏️ uses settingsViewModel
                sellerInfo = info
                step = 2
            })

            // ── Step 2: Client & Invoice Metadata ──
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

            // ── Step 3: Line-Item Entry ──
            3 -> {
                if (sellerInfo == null || invoiceMeta == null) return@Surface
                InvoiceStep3Screen(
                    existingLines = lines,
                    onBack        = { step = 2 },
                    onAddLine     = { line -> lines = lines + line },
                    onDone        = { step = 4 }
                )
            }

            // ── Step 4: Review & Print/Save ──
            4 -> {
                if (sellerInfo == null || invoiceMeta == null) return@Surface

                // determine if the user can print
                val canPrint = when (role) {
                    UserRole.User       -> isTrialActive || invoicesThisMonth == 0
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
                    Text("Review Invoice", style = MaterialTheme.typography.titleLarge)

                    // ... display sellerInfo, invoiceMeta, lines, total ...

                    Spacer(Modifier.weight(1f))
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
            }
        }
    }

    // 5️⃣ Show upgrade dialog if user tapped but was not allowed
    if (showUpgrade) {
        UpgradePromptDialog(
            onSubscribe = { /* TODO: navigate to subscription screen */ },
            onDismiss   = { showUpgrade = false }
        )
    }
}
