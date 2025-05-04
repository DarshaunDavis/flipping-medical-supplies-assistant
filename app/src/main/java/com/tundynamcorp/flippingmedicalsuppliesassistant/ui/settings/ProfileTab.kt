package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.tundynamcorp.flippingmedicalsuppliesassistant.R
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.invoice.SellerInfo
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTab(
    profileInfo: SellerInfo,
    onSave: (SellerInfo) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var editing by rememberSaveable { mutableStateOf(false) }

    // Editable state starts blank when entering edit mode
    var name     by rememberSaveable { mutableStateOf("") }
    var dba      by rememberSaveable { mutableStateOf("") }
    var address1 by rememberSaveable { mutableStateOf("") }
    var address2 by rememberSaveable { mutableStateOf("") }
    var city     by rememberSaveable { mutableStateOf("") }
    var state    by rememberSaveable { mutableStateOf("") }
    var zip      by rememberSaveable { mutableStateOf("") }
    var phone    by rememberSaveable { mutableStateOf("") }
    var email    by rememberSaveable { mutableStateOf("") }

    // Clear fields when entering edit mode
    LaunchedEffect(editing) {
        if (editing) {
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

    val scrollState = rememberScrollState()
    val statesList = stringArrayResource(id = R.array.states).toList()
    var stateDropdownExpanded by rememberSaveable { mutableStateOf(false) }

    // Helper for word-capitalization
    val locale = Locale.getDefault()
    fun String.normalize() = split(' ')
        .joinToString(" ") { word ->
            word.lowercase(locale).replaceFirstChar { it.uppercase(locale) }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!editing) {
            // VIEW MODE
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
            // EDIT MODE
            Text("Edit Profile", style = MaterialTheme.typography.titleLarge)

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.normalize() },
                label = { Text("Name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // DBA
            OutlinedTextField(
                value = dba,
                onValueChange = { dba = it.normalize() },
                label = { Text("DBA (optional)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Address Line 1
            OutlinedTextField(
                value = address1,
                onValueChange = { address1 = it.normalize() },
                label = { Text("Address Line 1") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Address Line 2
            OutlinedTextField(
                value = address2,
                onValueChange = { address2 = it.normalize() },
                label = { Text("Address Line 2 (optional)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // City → opens state spinner
            OutlinedTextField(
                value = city,
                onValueChange = { city = it.normalize() },
                label = { Text("City") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { stateDropdownExpanded = true }
                ),
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
                    singleLine = true,
                    label = { Text("State") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(stateDropdownExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
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

            // Zip Code
            OutlinedTextField(
                value = zip,
                onValueChange = { zip = it.filter(Char::isDigit) },
                label = { Text("Zip Code") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Phone
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Email (case-sensitive, no normalization)
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email (optional)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
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
                    editing = false
                }) {
                    Text("Save")
                }
            }
        }
    }
}
