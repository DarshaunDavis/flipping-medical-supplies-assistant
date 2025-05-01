package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db   = Firebase.database.reference

    // 1️⃣ Raw user flow (FirebaseUser? → emits null when signed out)
    private val _user = MutableStateFlow(auth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user.asStateFlow()

    // 2️⃣ Derived displayName flow (String? from the user’s Firebase profile)
    val displayName: StateFlow<String?> = user
        .map { it?.displayName }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = auth.currentUser?.displayName
        )

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

    /** Register a new user with display name */
    fun register(
        name: String,
        email: String,
        password: String,
        onResult: (success: Boolean, errorMsg: String?) -> Unit
    ) {
        // Capitalize each word
        val displayName = name
            .trim()
            .split("\\s+".toRegex())
            .joinToString(" ") { it.replaceFirstChar { c -> c.titlecase() } }

        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val user = auth.currentUser!!
                    // 1) Update FirebaseAuth profile
                    val profileUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build()
                    user.updateProfile(profileUpdate)
                        .addOnCompleteListener {
                            // 2) Write to Realtime Database at /users/{uid}/displayName
                            db.child("users")
                                .child(user.uid)
                                .child("displayName")
                                .setValue(displayName)
                                .addOnCompleteListener { dbTask ->
                                    if (dbTask.isSuccessful) {
                                        _user.value = user
                                        onResult(true, null)
                                    } else {
                                        onResult(false, dbTask.exception?.localizedMessage)
                                    }
                                }
                        }
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
