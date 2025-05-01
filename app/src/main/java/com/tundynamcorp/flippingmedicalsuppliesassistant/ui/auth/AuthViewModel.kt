package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    // Expose current FirebaseUser (null if signed out)
    private val _user = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user

    /** Sign in existing user */
    fun signIn(
        email: String,
        password: String,
        onResult: (success: Boolean, errorMsg: String?) -> Unit
    ) {
        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    _user.value = auth.currentUser
                    onResult(true, null)
                }
                .addOnFailureListener { ex ->
                    onResult(false, ex.localizedMessage)
                }
        }
    }

    /** Register a new user */
    fun register(
        email: String,
        password: String,
        onResult: (success: Boolean, errorMsg: String?) -> Unit
    ) {
        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    _user.value = auth.currentUser
                    onResult(true, null)
                }
                .addOnFailureListener { ex ->
                    onResult(false, ex.localizedMessage)
                }
        }
    }

    /** Sign out the current user */
    fun signOut() {
        auth.signOut()
        _user.value = null
    }
}
