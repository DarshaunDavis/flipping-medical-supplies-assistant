package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.input.KeyboardCapitalization
import java.util.Locale

@Composable
fun RegisterDialog(
    onDismiss: () -> Unit,
    onSignInClick: () -> Unit,
    onRegisterSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val focusManager = LocalFocusManager.current

    var name            by rememberSaveable { mutableStateOf("") }
    var email           by rememberSaveable { mutableStateOf("") }
    var password        by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmVisible  by rememberSaveable { mutableStateOf(false) }
    var loading         by rememberSaveable { mutableStateOf(false) }
    var errorMsg        by rememberSaveable { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // ✏️ Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { input ->
                        // Normalize each word: first letter uppercase, rest lowercase
                        val locale = Locale.getDefault()
                        name = input
                            .split(' ')
                            .joinToString(" ") { word ->
                                word
                                    .lowercase(locale)
                                    .replaceFirstChar { it.uppercase(locale) }
                            }
                    },
                    label = { Text("Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Words,  // 2) request word-cap keyboard
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Default.VisibilityOff
                            else
                                Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.clickable {
                                passwordVisible = !passwordVisible
                            }
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Confirm Password
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    singleLine = true,
                    visualTransformation = if (confirmVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {
                        Icon(
                            imageVector = if (confirmVisible)
                                Icons.Default.VisibilityOff
                            else
                                Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.clickable {
                                confirmVisible = !confirmVisible
                            }
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Error
                errorMsg?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                Spacer(Modifier.height(4.dp))

                TextButton(onClick = {
                    onDismiss()
                    onSignInClick()
                }) {
                    Text("Already have an account? Sign In")
                }
            }
        },
        confirmButton = {
            val passwordsMatch = password.isNotBlank() && password == confirmPassword
            Button(
                onClick = {
                    loading = true
                    errorMsg = null
                    // 🚀 Pass name into the new register API
                    authViewModel.register(name, email.trim(), password) { success, err ->
                        loading = false
                        if (success) onRegisterSuccess()
                        else          errorMsg = err ?: "Registration failed"
                    }
                },
                enabled = name.isNotBlank() &&
                        email.isNotBlank() &&
                        password.isNotBlank() &&
                        confirmPassword.isNotBlank() &&
                        passwordsMatch &&
                        !loading
            ) {
                if (loading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                else             Text("Register")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
