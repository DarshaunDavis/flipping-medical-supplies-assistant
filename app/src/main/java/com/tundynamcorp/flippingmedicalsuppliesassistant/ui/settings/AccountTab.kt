package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Account tab: password change and other account settings.
 */
@Composable
fun AccountTab() {
    val scrollState = rememberScrollState()
    var currentPwd by remember { mutableStateOf("") }
    var newPwd by remember { mutableStateOf("") }
    var confirmPwd by remember { mutableStateOf("") }

    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(16.dp)) {
        Text("Account Settings", modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = currentPwd,
            onValueChange = { currentPwd = it },
            label = { Text("Current Password") },
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = newPwd,
            onValueChange = { newPwd = it },
            label = { Text("New Password") },
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = confirmPwd,
            onValueChange = { confirmPwd = it },
            label = { Text("Confirm Password") },
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
        )
        Button(onClick = { /* Save password */ }) {
            Text("Change Password")
        }
    }
}
