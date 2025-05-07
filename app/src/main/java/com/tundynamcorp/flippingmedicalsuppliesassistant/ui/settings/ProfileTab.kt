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

    // fields
    var name     by rememberSaveable { mutableStateOf("") }
    var dba      by rememberSaveable { mutableStateOf("") }
    var address1 by rememberSaveable { mutableStateOf("") }
    var address2 by rememberSaveable { mutableStateOf("") }
    var city     by rememberSaveable { mutableStateOf("") }
    var state    by rememberSaveable { mutableStateOf("") }
    // ZIP: digits only, max 5
    var zip      by rememberSaveable { mutableStateOf("") }
    // PHONE: digits only, max 9
    var phone    by rememberSaveable { mutableStateOf("") }
    var email    by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(editing) {
        if (editing) {
            name = ""; dba = ""
            address1 = ""; address2 = ""
            city = ""; state = ""
            zip = ""; phone = ""; email = ""
        }
    }

    val scrollState = rememberScrollState()
    val statesList = stringArrayResource(id = R.array.states).toList()
    var stateDropdownExpanded by rememberSaveable { mutableStateOf(false) }

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
            // … view mode …
            Text(
                text = "Edit",
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { editing = true },
                color = MaterialTheme.colorScheme.primary
            )
            // … display profile …
        } else {
            Text("Edit Profile", style = MaterialTheme.typography.titleLarge)
            // … other fields …

            // ZIP
            OutlinedTextField(
                value = zip,
                onValueChange = { zip = it.filter(Char::isDigit).take(5) },
                label = { Text("Zip Code") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // PHONE
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it.filter(Char::isDigit).take(9) },
                label = { Text("Phone") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // … email + save/cancel buttons …
        }
    }
}
