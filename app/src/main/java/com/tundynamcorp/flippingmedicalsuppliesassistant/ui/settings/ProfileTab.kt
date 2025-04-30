package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.tundynamcorp.flippingmedicalsuppliesassistant.R
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.invoice.SellerInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTab(
    profileInfo: SellerInfo,
    onSave: (SellerInfo) -> Unit
) {
    val focusManager = LocalFocusManager.current

    var editing by rememberSaveable { mutableStateOf(false) }

    // Local editable state, init from profileInfo
    var name       by rememberSaveable { mutableStateOf(profileInfo.name) }
    var dba        by rememberSaveable { mutableStateOf(profileInfo.dba.orEmpty()) }
    var address1   by rememberSaveable { mutableStateOf(profileInfo.address1) }
    var address2   by rememberSaveable { mutableStateOf(profileInfo.address2.orEmpty()) }
    var city       by rememberSaveable { mutableStateOf(profileInfo.city) }
    var state      by rememberSaveable { mutableStateOf(profileInfo.state) }
    var zip        by rememberSaveable { mutableStateOf(profileInfo.zip) }
    var phone      by rememberSaveable { mutableStateOf(profileInfo.phone) }
    var email      by rememberSaveable { mutableStateOf(profileInfo.email.orEmpty()) }

    val scrollState = rememberScrollState()
    val statesList = stringArrayResource(id = R.array.states).toList()
    var stateDropdownExpanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!editing) {
            Text(
                text = "Edit",
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { editing = true },
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Gray)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Name: ${profileInfo.name}")
                profileInfo.dba?.takeIf(String::isNotBlank)?.let { Text("DBA: $it") }
                Text("Address Line 1: ${profileInfo.address1}")
                profileInfo.address2?.takeIf(String::isNotBlank)?.let { Text("Address Line 2: $it") }
                Text("City: ${profileInfo.city}")
                Text("State: ${profileInfo.state}")
                Text("Zip Code: ${profileInfo.zip}")
                Text("Phone: ${profileInfo.phone}")
                profileInfo.email?.takeIf(String::isNotBlank)?.let { Text("Email: $it") }
            }
        } else {
            Text("Edit Profile", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = dba,
                onValueChange = { dba = it },
                label = { Text("DBA (optional)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = address1,
                onValueChange = { address1 = it },
                label = { Text("Address Line 1") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = address2,
                onValueChange = { address2 = it },
                label = { Text("Address Line 2 (optional)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }),
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
                    singleLine = true,
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
                                focusManager.moveFocus(FocusDirection.Down)
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = zip,
                onValueChange = { zip = it.filter(Char::isDigit) },
                label = { Text("Zip Code") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                }),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

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
