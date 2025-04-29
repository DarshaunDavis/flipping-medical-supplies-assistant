// app/src/main/java/com/tundynamcorp/flippingmedicalsuppliesassistant/ui/invoice/InvoiceStep1Screen.kt
package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.invoice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
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
import com.tundynamcorp.flippingmedicalsuppliesassistant.R

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
    initial: SellerInfo = SellerInfo(
        name = "",
        dba = null,
        address1 = "",
        address2 = null,
        city = "",
        state = "",
        zip = "",
        phone = "",
        email = null
    ),
    onNext: (SellerInfo) -> Unit
) {
    // Fields initialized from `initial`
    var name by rememberSaveable { mutableStateOf(initial.name) }
    var dba by rememberSaveable { mutableStateOf(initial.dba.orEmpty()) }
    var address1 by rememberSaveable { mutableStateOf(initial.address1) }
    var address2 by rememberSaveable { mutableStateOf(initial.address2.orEmpty()) }
    var city by rememberSaveable { mutableStateOf(initial.city) }
    var state by rememberSaveable { mutableStateOf(initial.state) }
    var zip by rememberSaveable { mutableStateOf(initial.zip) }
    var phone by rememberSaveable { mutableStateOf(initial.phone) }
    var email by rememberSaveable { mutableStateOf(initial.email.orEmpty()) }

    // Dropdown state
    val statesList = stringArrayResource(id = R.array.states).toList()
    var stateDropdownExpanded by remember { mutableStateOf(false) }

    // Scroll for overflow
    val scrollState = rememberScrollState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Step 1: Seller Information", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = dba,
                onValueChange = { dba = it },
                label = { Text("DBA (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
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
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City") },
                modifier = Modifier.fillMaxWidth()
            )

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

            OutlinedTextField(
                value = zip,
                onValueChange = { zip = it.filter(Char::isDigit) },
                label = { Text("Zip Code") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
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

            Spacer(modifier = Modifier.height(24.dp))

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
