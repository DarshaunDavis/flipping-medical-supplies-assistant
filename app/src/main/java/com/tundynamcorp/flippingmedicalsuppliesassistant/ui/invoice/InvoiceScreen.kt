// app/src/main/java/com/tundynamcorp/flippingmedicalsuppliesassistant/ui/invoice/InvoiceScreen.kt
package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.invoice

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    settingsViewModel: SettingsViewModel = viewModel()
) {
    // pull the saved profile from your SettingsViewModel
    val profile by settingsViewModel.profileInfo.collectAsState()

    var step by rememberSaveable { mutableStateOf(1) }
    var invoiceData by remember { mutableStateOf<SellerInfo?>(null) }

    Surface(modifier = Modifier.fillMaxSize()) {
        when (step) {
            1 -> {
                InvoiceStep1Screen(
                    initial = profile,
                    onNext = { info ->
                        // persist any edits back to Settings
                        settingsViewModel.updateProfile(info)
                        invoiceData = info
                        step = 2
                    }
                )
            }
            2 -> {
                // placeholder until Step 2 implementation
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Collected Seller Info:", style = MaterialTheme.typography.titleLarge)
                    invoiceData?.let { s ->
                        Text("Name: ${s.name}")
                        Text("City: ${s.city}, ${s.state} ${s.zip}")
                    }
                    Spacer(Modifier.weight(1f))
                    Button(onClick = { step = 1 }) {
                        Text("Back")
                    }
                }
            }
        }
    }
}
