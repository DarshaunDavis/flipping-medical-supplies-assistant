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
 * Profile tab: user profile and company info fields.
 */
@Composable
fun ProfileTab() {
    val scrollState = rememberScrollState()
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(16.dp)) {
        Text("Profile Information", modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 8.dp)
        )
        // TODO: add more profile fields (Phone, Email, etc.)
        Button(onClick = { /* Save profile changes */ }) {
            Text("Save")
        }
    }
}