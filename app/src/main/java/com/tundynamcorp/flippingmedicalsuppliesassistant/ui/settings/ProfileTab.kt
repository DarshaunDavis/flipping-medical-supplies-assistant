package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tundynamcorp.flippingmedicalsuppliesassistant.R
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.invoice.SellerInfo

/**
 * Profile tab: displays profile info in read-only mode with an edit toggle.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTab(
    profileInfo: SellerInfo,
    onSave: (SellerInfo) -> Unit
) {
    var editing by remember { mutableStateOf(false) }
    // local editable fields initialized from profileInfo
    var name by remember { mutableStateOf(profileInfo.name) }
    var dba by remember { mutableStateOf(profileInfo.dba.orEmpty()) }
    var address1 by remember { mutableStateOf(profileInfo.address1) }
    var address2 by remember { mutableStateOf(profileInfo.address2.orEmpty()) }
    var city by remember { mutableStateOf(profileInfo.city) }
    var state by remember { mutableStateOf(profileInfo.state) }
    var zip by remember { mutableStateOf(profileInfo.zip) }
    var phone by remember { mutableStateOf(profileInfo.phone) }
    var email by remember { mutableStateOf(profileInfo.email.orEmpty()) }

    val scrollState = rememberScrollState()
    val statesList = stringArrayResource(id = R.array.states).toList()
    var stateDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!editing) {
            // Edit link above info
            Text(
                text = "Edit",
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { editing = true },
                color = MaterialTheme.colorScheme.primary
            )

            // Read-only display wrapped in border
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Gray)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Name: ${profileInfo.name}")
                Text("DBA: ${profileInfo.dba.orEmpty()}")
                Text("Address Line 1: ${profileInfo.address1}")
                Text("Address Line 2: ${profileInfo.address2.orEmpty()}")
                Text("City: ${profileInfo.city}")
                Text("State: ${profileInfo.state}")
                Text("Zip Code: ${profileInfo.zip}")
                Text("Phone: ${profileInfo.phone}")
                Text("Email: ${profileInfo.email.orEmpty()}")
            }
        } else {
            // Editable form
            Text("Edit Profile", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = dba,
                onValueChange = { dba = it },
                label = { Text("Doing-Business-As (optional)") },
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
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(stateDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(onClick = { editing = false }) {
                    Text("Cancel")
                }
                Button(onClick = {
                    onSave(
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
                    editing = false
                }) {
                    Text("Save")
                }
            }
        }
    }
}