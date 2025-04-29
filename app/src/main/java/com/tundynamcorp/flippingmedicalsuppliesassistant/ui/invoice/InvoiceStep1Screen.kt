package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.invoice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.R
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.settings.SettingsViewModel

/**
 * Data class representing seller (company) information.
 */
data class SellerInfo(
    val name: String,
    val dba: String?,
    val address1: String,
    val address2: String?,
    val city: String,
    val state: String,
    val zip: String,
    val phone: String,
    val email: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceStep1Screen(
    onNext: (SellerInfo) -> Unit,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    // 1️⃣ Get the saved profile from the ViewModel
    val profile by settingsViewModel.profileInfo.collectAsState()

    // 2️⃣ Checkbox state
    var useProfile by rememberSaveable { mutableStateOf(false) }

    // 3️⃣ Local form state
    var name     by rememberSaveable { mutableStateOf("") }
    var dba      by rememberSaveable { mutableStateOf("") }
    var address1 by rememberSaveable { mutableStateOf("") }
    var address2 by rememberSaveable { mutableStateOf("") }
    var city     by rememberSaveable { mutableStateOf("") }
    var state    by rememberSaveable { mutableStateOf("") }
    var zip      by rememberSaveable { mutableStateOf("") }
    var phone    by rememberSaveable { mutableStateOf("") }
    var email    by rememberSaveable { mutableStateOf("") }

    // 4️⃣ When checkbox toggles on, copy profile into fields
    LaunchedEffect(useProfile) {
        if (useProfile) {
            name     = profile.name
            dba      = profile.dba.orEmpty()
            address1 = profile.address1
            address2 = profile.address2.orEmpty()
            city     = profile.city
            state    = profile.state
            zip      = profile.zip
            phone    = profile.phone
            email    = profile.email.orEmpty()
        } else {
            // clear all fields
            name = ""
            dba = ""
            address1 = ""
            address2 = ""
            city = ""
            state = ""
            zip = ""
            phone = ""
            email = ""
            }
    }

    // 5️⃣ Dropdown state and scroll
    val statesList = stringArrayResource(id = R.array.states).toList()
    var stateDropdownExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Checkbox
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = useProfile,
                    onCheckedChange = { useProfile = it },
                    colors = CheckboxDefaults.colors()
                )
                Spacer(Modifier.width(8.dp))
                Text("Use profile information")
            }

            // Form title
            Text("Step 1: Seller Information", style = MaterialTheme.typography.titleLarge)

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            // DBA
            OutlinedTextField(
                value = dba,
                onValueChange = { dba = it },
                label = { Text("DBA (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Address 1 & 2
            OutlinedTextField(
                value = address1,
                onValueChange = { address1 = it },
                label = { Text("Address Line 1") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = address2,
                onValueChange = { address2 = it },
                label = { Text("Address Line 2 (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            // City
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City") },
                modifier = Modifier.fillMaxWidth()
            )

            // State spinner
            ExposedDropdownMenuBox(
                expanded = stateDropdownExpanded,
                onExpandedChange = { stateDropdownExpanded = it }
            ) {
                TextField(
                    value = state.ifBlank { "Select State" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("State") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(stateDropdownExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(
                            type = MenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        )
                )
                ExposedDropdownMenu(
                    expanded = stateDropdownExpanded,
                    onDismissRequest = { stateDropdownExpanded = false }
                ) {
                    statesList.forEach { s ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(s) },
                            onClick = {
                                state = s
                                stateDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Zip
            OutlinedTextField(
                value = zip,
                onValueChange = { zip = it.filter(Char::isDigit) },
                label = { Text("Zip Code") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Phone & Email
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(Modifier.height(24.dp))

            // Next
            Button(
                onClick = {
                    onNext(
                        SellerInfo(
                            name     = name,
                            dba      = dba.takeIf(String::isNotBlank),
                            address1 = address1,
                            address2 = address2.takeIf(String::isNotBlank),
                            city     = city,
                            state    = state,
                            zip      = zip,
                            phone    = phone,
                            email    = email.takeIf(String::isNotBlank)
                        )
                    )
                },
                modifier = Modifier.align(Alignment.End),
                enabled = listOf(name, address1, city, state, zip, phone)
                    .all { it.isNotBlank() }
            ) {
                Text("Next")
            }
        }
    }
}
