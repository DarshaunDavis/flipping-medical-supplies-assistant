package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db   = Firebase.database.reference

    // 1️⃣ Raw user flow
    private val _user = MutableStateFlow(auth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user.asStateFlow()

    // 2️⃣ displayName flow from FirebaseUser.profile
    val displayName: StateFlow<String?> = user
        .map { it?.displayName }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            auth.currentUser?.displayName
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

    /** Register a new user with displayName in both Auth profile & RTDB */
    fun register(
        name: String,
        email: String,
        password: String,
        onResult: (success: Boolean, errorMsg: String?) -> Unit
    ) {
        // capitalize each word
        val displayName = name
            .trim()
            .split("\\s+".toRegex())
            .joinToString(" ") { it.replaceFirstChar { c -> c.titlecase() } }

        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val user = auth.currentUser!!
                    // 1) update FirebaseAuth profile
                    val profileUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build()
                    user.updateProfile(profileUpdate)
                        .addOnSuccessListener {
                            // 2) mirror into RTDB under /users/{uid}/displayName
                            db.child("users")
                                .child(user.uid)
                                .child("displayName")
                                .setValue(displayName)
                                .addOnSuccessListener {
                                    _user.value = user
                                    onResult(true, null)
                                }
                                .addOnFailureListener { ex ->
                                    onResult(false, ex.localizedMessage)
                                }
                        }
                        .addOnFailureListener { ex ->
                            onResult(false, ex.localizedMessage)
                        }
                }
                .addOnFailureListener { ex ->
                    onResult(false, ex.localizedMessage)
                }
        }
    }

    /** **NEW** – update an existing user’s displayName in both Auth profile & RTDB */
    fun updateDisplayName(
        name: String,
        onResult: (success: Boolean, errorMsg: String?) -> Unit
    ) {
        // capitalize each word
        val displayName = name
            .trim()
            .split("\\s+".toRegex())
            .joinToString(" ") { it.replaceFirstChar { c -> c.titlecase() } }

        val user = auth.currentUser
        if (user == null) {
            onResult(false, "No user signed in")
            return
        }

        // 1) update Auth profile
        val profileUpdate = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()
        user.updateProfile(profileUpdate)
            .addOnSuccessListener {
                // 2) mirror into RTDB
                db.child("users")
                    .child(user.uid)
                    .child("displayName")
                    .setValue(displayName)
                    .addOnSuccessListener {
                        _user.value = user
                        onResult(true, null)
                    }
                    .addOnFailureListener { ex ->
                        onResult(false, ex.localizedMessage)
                    }
            }
            .addOnFailureListener { ex ->
                onResult(false, ex.localizedMessage)
            }
    }

    /** Sign out */
    fun signOut() {
        auth.signOut()
        _user.value = null
    }
}
