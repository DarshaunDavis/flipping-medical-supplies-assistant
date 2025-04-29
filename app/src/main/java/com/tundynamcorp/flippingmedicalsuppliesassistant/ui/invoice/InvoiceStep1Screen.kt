package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.invoice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
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

/**
 * Step 1 form: editable text fields for SellerInfo, with separate City/State/Zip.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceStep1Screen(
    onNext: (SellerInfo) -> Unit
) {
    // Local form state
    var name by remember { mutableStateOf("") }
    var dba by remember { mutableStateOf("") }
    var address1 by remember { mutableStateOf("") }
    var address2 by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var zip by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // Load states array from resources
    val statesList = stringArrayResource(id = R.array.states).toList()
    var stateDropdownExpanded by remember { mutableStateOf(false) }

    // Scroll state for long forms
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Step 1: Enter Name", style = MaterialTheme.typography.titleLarge)

        // Name
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Enter Name") },
            modifier = Modifier.fillMaxWidth()
        )

        // Doing-Business-As
        OutlinedTextField(
            value = dba,
            onValueChange = { dba = it },
            label = { Text("Doing-Business-As (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        // Address Line 1
        OutlinedTextField(
            value = address1,
            onValueChange = { address1 = it },
            label = { Text("Address Line 1") },
            modifier = Modifier.fillMaxWidth()
        )

        // Address Line 2
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

        // State Spinner
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
                    DropdownMenuItem(
                        text = { Text(s) },
                        onClick = {
                            state = s
                            stateDropdownExpanded = false
                        }
                    )
                }
            }
        }

        // Zip Code
        OutlinedTextField(
            value = zip,
            onValueChange = { zip = it.filter(Char::isDigit) },
            label = { Text("Zip Code") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        // Phone
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(24.dp))

        // Next Button
        Button(
            onClick = {
                onNext(
                    SellerInfo(
                        name = name,
                        dba = dba.takeIf(String::isNotBlank),
                        address1 = address1,
                        address2 = address2.takeIf(String::isNotBlank),
                        city = city,
                        state = state,
                        zip = zip,
                        phone = phone,
                        email = email.takeIf(String::isNotBlank)
                    )
                )
            },
            modifier = Modifier.align(Alignment.End),
            enabled = listOf(name, address1, city, state, zip, phone).all { it.isNotBlank() }
        ) {
            Text("Next")
        }
    }
}
