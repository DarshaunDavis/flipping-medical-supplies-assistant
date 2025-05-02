package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.invoice.SellerInfo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val auth   = FirebaseAuth.getInstance()
    private val dbRef  = Firebase.database.reference

    // 1️⃣ FirebaseUser flow
    private val _user = MutableStateFlow(auth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user.asStateFlow()

    // 3️⃣ SellerInfo flow from RTDB
    private val _profileInfo = MutableStateFlow<SellerInfo?>(null)
    val profileInfo: StateFlow<SellerInfo?> = _profileInfo.asStateFlow()

    init {
        // If already logged in, start listening
        auth.currentUser?.uid?.let { listenForProfile(it) }
    }

    /** Sign in existing user */
    fun signIn(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val u = auth.currentUser!!
                    _user.value = u
                    listenForProfile(u.uid)
                    onResult(true, null)
                }
                .addOnFailureListener { ex ->
                    onResult(false, ex.localizedMessage)
                }
        }
    }

    /** Register new user and mirror displayName to RTDB */
    fun register(
        name: String,
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val capName = name
            .trim()
            .split("\\s+".toRegex())
            .joinToString(" ") { it.replaceFirstChar { c -> c.titlecase() } }

        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val u = auth.currentUser!!
                    // 1) update Auth profile
                    val profileUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName(capName)
                        .build()
                    u.updateProfile(profileUpdate)
                        .addOnSuccessListener {
                            // 2) write into RTDB
                            dbRef.child("users")
                                .child(u.uid)
                                .child("displayName")
                                .setValue(capName)
                                .addOnSuccessListener {
                                    _user.value = u
                                    listenForProfile(u.uid)
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

    /** Update only non-blank fields in both Auth profile & RTDB */
    fun updateProfile(
        info: SellerInfo,
        onResult: (Boolean, String?) -> Unit
    ) {
        val u = auth.currentUser
        if (u == null) {
            onResult(false, "No user signed in")
            return
        }

        // Capitalize name
        val capName = info.name
            .trim()
            .split("\\s+".toRegex())
            .joinToString(" ") { it.replaceFirstChar { c -> c.titlecase() } }

        // 1) update Auth displayName
        val profileUpdate = UserProfileChangeRequest.Builder()
            .setDisplayName(capName)
            .build()

        u.updateProfile(profileUpdate)
            .addOnSuccessListener {
                // 2) build map of only non-blank fields
                val updates = mutableMapOf<String, Any>()
                if (capName.isNotBlank())                             updates["displayName"] = capName
                info.dba?.takeIf(String::isNotBlank)?.let { updates["dba"]       = it }
                info.address1.takeIf(String::isNotBlank)?.let     { updates["address1"] = it }
                info.address2?.takeIf(String::isNotBlank)?.let    { updates["address2"] = it }
                info.city.takeIf(String::isNotBlank)?.let         { updates["city"]     = it }
                info.state.takeIf(String::isNotBlank)?.let        { updates["state"]    = it }
                info.zip.takeIf(String::isNotBlank)?.let          { updates["zip"]      = it }
                info.phone.takeIf(String::isNotBlank)?.let        { updates["phone"]    = it }
                info.email?.takeIf(String::isNotBlank)?.let       { updates["email"]    = it }

                // If nothing to update, we're done
                if (updates.isEmpty()) {
                    onResult(true, null)
                    return@addOnSuccessListener
                }

                // 3) push into RTDB
                dbRef.child("users")
                    .child(u.uid)
                    .updateChildren(updates)
                    .addOnSuccessListener {
                        _user.value = auth.currentUser
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
        _profileInfo.value = null
    }

    /** Begin listening to RTDB for profile changes */
    private fun listenForProfile(uid: String) {
        dbRef.child("users")
            .child(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) return
                    _profileInfo.value = SellerInfo(
                        name       = snapshot.child("displayName").getValue(String::class.java).orEmpty(),
                        dba        = snapshot.child("dba").getValue(String::class.java).takeIf { it?.isNotBlank() == true },
                        address1   = snapshot.child("address1").getValue(String::class.java).orEmpty(),
                        address2   = snapshot.child("address2").getValue(String::class.java).takeIf { it?.isNotBlank() == true },
                        city       = snapshot.child("city").getValue(String::class.java).orEmpty(),
                        state      = snapshot.child("state").getValue(String::class.java).orEmpty(),
                        zip        = snapshot.child("zip").getValue(String::class.java).orEmpty(),
                        phone      = snapshot.child("phone").getValue(String::class.java).orEmpty(),
                        email      = snapshot.child("email").getValue(String::class.java).takeIf { it?.isNotBlank() == true }
                    )
                }
                override fun onCancelled(error: DatabaseError) {
                    // optionally log or handle the error
                }
            })
    }
}
