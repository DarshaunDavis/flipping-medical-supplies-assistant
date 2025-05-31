package com.lislal.flippingmedicalsuppliesassistant.ui.auth

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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale

@Composable
fun RegisterDialog(
    onDismiss: () -> Unit,
    onSignInClick: () -> Unit,
    onRegisterSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val focusManager = LocalFocusManager.current

    // create one FocusRequester per field
    val nameRequester    = remember { FocusRequester() }
    val emailRequester   = remember { FocusRequester() }
    val passRequester    = remember { FocusRequester() }
    val confirmRequester = remember { FocusRequester() }

    // kick off initial focus
    LaunchedEffect(Unit) {
        nameRequester.requestFocus()
    }

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
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { input ->
                        val locale = Locale.getDefault()
                        name = input
                            .split(' ')
                            .joinToString(" ") { word ->
                                word.lowercase(locale)
                                    .replaceFirstChar { it.uppercase(locale) }
                            }
                    },
                    label = { Text("Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { emailRequester.requestFocus() }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(nameRequester)
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
                        onNext = { passRequester.requestFocus() }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(emailRequester)
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
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
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
                        onNext = { confirmRequester.requestFocus() }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(passRequester)
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
                            imageVector = if (confirmVisible) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(confirmRequester)
                )

                // Error text
                errorMsg?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

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
                    authViewModel.register(name, email.trim(), password) { success, err ->
                        loading = false
                        if (success) onRegisterSuccess()
                        else          errorMsg = err ?: "Registration failed"
                    }
                },
                enabled = name.isNotBlank()
                        && email.isNotBlank()
                        && password.isNotBlank()
                        && confirmPassword.isNotBlank()
                        && passwordsMatch
                        && !loading
            ) {
                if (loading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                else         Text("Register")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
