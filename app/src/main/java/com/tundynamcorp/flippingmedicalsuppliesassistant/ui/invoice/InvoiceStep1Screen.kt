package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.invoice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.R
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.auth.AuthViewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.settings.SettingsViewModel
import java.util.Locale

/** Seller/company info for the invoice form */
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
    settingsViewModel: SettingsViewModel = viewModel(),
    authViewModel:    AuthViewModel      = viewModel()
) {
    // 1️⃣ Pull in stored profile info
    val localProfile  by settingsViewModel.profileInfo.collectAsState()
    val remoteProfile by authViewModel.profileInfo.collectAsState()

    var useProfile by rememberSaveable { mutableStateOf(false) }

    // 2️⃣ Form state
    var name     by rememberSaveable { mutableStateOf("") }
    var dba      by rememberSaveable { mutableStateOf("") }
    var address1 by rememberSaveable { mutableStateOf("") }
    var address2 by rememberSaveable { mutableStateOf("") }
    var city     by rememberSaveable { mutableStateOf("") }
    var state    by rememberSaveable { mutableStateOf("") }

    // ── UPDATED ── restrict zip to digits, max length 5
    var zip      by rememberSaveable { mutableStateOf("") }

    // ── UPDATED ── restrict phone to digits, max length 9
    var phone    by rememberSaveable { mutableStateOf("") }

    var email    by rememberSaveable { mutableStateOf("") }

    // 3️⃣ Populate or clear when toggling “Use profile”
    LaunchedEffect(useProfile) {
        if (useProfile) {
            val p = remoteProfile ?: localProfile
            name     = p.name
            dba      = p.dba.orEmpty()
            address1 = p.address1
            address2 = p.address2.orEmpty()
            city     = p.city
            state    = p.state
            zip      = p.zip
            phone    = p.phone
            email    = p.email.orEmpty()
        } else {
            name = ""; dba = ""; address1 = ""
            address2 = ""; city = ""; state = ""
            zip = ""; phone = ""; email = ""
        }
    }

    val focusManager = LocalFocusManager.current
    val locale = Locale.getDefault()
    fun String.normalize() = split(' ')
        .joinToString(" ") { word ->
            word.lowercase(locale)
                .replaceFirstChar { it.uppercase(locale) }
        }

    val statesList = stringArrayResource(id = R.array.states).toList()
    var stateDropdownExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = useProfile,
                    onCheckedChange = { useProfile = it }
                )
                Spacer(Modifier.width(8.dp))
                Text("Use profile information")
            }

            Text("Step 1: Seller Information", style = MaterialTheme.typography.titleLarge)

            // — Name —
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

            // — DBA —
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

            // — Address Line 1 —
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

            // — Address Line 2 —
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

            // — City (opens state spinner on Next) —
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

            // — State spinner —
            ExposedDropdownMenuBox(
                expanded = stateDropdownExpanded,
                onExpandedChange = { stateDropdownExpanded = it }
            ) {
                TextField(
                    value = state.ifBlank { "Select State" },
                    onValueChange = {},
                    readOnly = true,
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

            // ── ZIP CODE (digits only, max 5) ──
            OutlinedTextField(
                value = zip,
                onValueChange = { zip = it.filter(Char::isDigit).take(5) },
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

            // ── PHONE (digits only, max 9) ──
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it.filter(Char::isDigit).take(9) },
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

            // — Email —
            OutlinedTextField(
                value = email,
                onValueChange = { email = it.normalize() },
                label = { Text("Email (optional)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

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
                enabled = listOf(name, address1, city, state, zip, phone).all { it.isNotBlank() }
            ) {
                Text("Next")
            }
        }
    }
}
