// AccountTab.kt
package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.auth.AuthViewModel
import android.widget.Toast

@Composable
fun AccountTab(
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var currentPwd by remember { mutableStateOf("") }
    var newPwd     by remember { mutableStateOf("") }
    var confirmPwd by remember { mutableStateOf("") }
    var loading    by remember { mutableStateOf(false) }
    var errorMsg   by remember { mutableStateOf<String?>(null) }

    // Only enable when all fields filled and newPwd matches confirmPwd
    val canSubmit = currentPwd.isNotBlank()
            && newPwd.isNotBlank()
            && confirmPwd.isNotBlank()
            && newPwd == confirmPwd

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Account Settings", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = currentPwd,
            onValueChange = { currentPwd = it },
            label = { Text("Current Password") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = newPwd,
            onValueChange = { newPwd = it },
            label = { Text("New Password") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = confirmPwd,
            onValueChange = { confirmPwd = it },
            label = { Text("Confirm New Password") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        errorMsg?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Button(
            onClick = {
                loading = true
                errorMsg = null
                authViewModel.changePassword(currentPwd, newPwd) { success, err ->
                    loading = false
                    if (success) {
                        Toast.makeText(context, "Password changed", Toast.LENGTH_SHORT).show()
                        // Clear fields
                        currentPwd = ""
                        newPwd = ""
                        confirmPwd = ""
                    } else {
                        errorMsg = err ?: "Failed to change password"
                    }
                }
            },
            enabled = canSubmit && !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .height(20.dp)
                        .width(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text("Updating…")
            } else {
                Text("Change Password")
            }
        }
    }
}
