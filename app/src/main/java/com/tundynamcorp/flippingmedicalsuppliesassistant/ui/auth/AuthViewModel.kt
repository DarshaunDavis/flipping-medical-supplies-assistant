package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.UserRole
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.invoice.SellerInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val dbRef = Firebase.database.reference.child("users")

    // FirebaseUser flow
    private val _user = MutableStateFlow(auth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user.asStateFlow()

    // UserRole flow
    private val _role = MutableStateFlow(UserRole.Guest)
    val role: StateFlow<UserRole> = _role.asStateFlow()

    // SellerInfo flow from RTDB
    private val _profileInfo = MutableStateFlow<SellerInfo?>(null)
    val profileInfo: StateFlow<SellerInfo?> = _profileInfo.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val u = firebaseAuth.currentUser
            _user.value = u
            if (u == null) {
                _role.value = UserRole.Guest
                _profileInfo.value = null
            } else {
                // load role
                dbRef.child(u.uid).child("role")
                    .addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            _role.value = snapshot.getValue(String::class.java)
                                ?.let { UserRole.valueOf(it) }
                                ?: UserRole.User
                        }
                        override fun onCancelled(error: DatabaseError) {
                            _role.value = UserRole.User
                        }
                    })
                listenForProfile(u.uid)
            }
        }
    }

    fun signIn(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { onResult(true, null) }
                .addOnFailureListener { ex ->
                    val msg = when (ex) {
                        is FirebaseAuthInvalidUserException ->
                            "No account found for that email. Would you like to register?"
                        is FirebaseAuthInvalidCredentialsException ->
                            "Oops—that password didn’t match. Please try again."
                        else -> "Couldn’t sign in. Check your connection and try again."
                    }
                    onResult(false, msg)
                }
        }
    }

    fun register(
        name: String,
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val capName = name.trim()
            .split("\\s+".toRegex())
            .joinToString(" ") { it.replaceFirstChar { c -> c.titlecase() } }

        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val u = auth.currentUser!!
                    val profileUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName(capName)
                        .build()
                    u.updateProfile(profileUpdate)
                        .addOnSuccessListener {
                            dbRef.child(u.uid)
                                .setValue(
                                    mapOf(
                                        "displayName" to capName,
                                        "role" to UserRole.User.name,
                                        "trialStart" to ServerValue.TIMESTAMP
                                    )
                                )
                                .addOnSuccessListener { onResult(true, null) }
                                .addOnFailureListener { ex -> onResult(false, ex.localizedMessage) }
                        }
                        .addOnFailureListener { ex -> onResult(false, ex.localizedMessage) }
                }
                .addOnFailureListener { ex -> onResult(false, ex.localizedMessage) }
        }
    }

    fun updateProfile(
        info: SellerInfo,
        onResult: (Boolean, String?) -> Unit
    ) {
        val u = auth.currentUser ?: run {
            onResult(false, "No user signed in"); return
        }
        val capName = info.name.trim()
            .split("\\s+".toRegex())
            .joinToString(" ") { it.replaceFirstChar { c -> c.titlecase() } }

        val profileUpdate = UserProfileChangeRequest.Builder()
            .setDisplayName(capName)
            .build()

        u.updateProfile(profileUpdate)
            .addOnSuccessListener {
                val updates = mutableMapOf<String, Any>(
                    "displayName" to capName
                )
                info.dba?.takeIf { it.isNotBlank() }?.let { d -> updates["dba"] = d }
                info.address1.takeIf { it.isNotBlank() }?.let { a -> updates["address1"] = a }
                info.address2?.takeIf { it.isNotBlank() }?.let { a -> updates["address2"] = a }
                info.city.takeIf { it.isNotBlank() }?.let { c -> updates["city"] = c }
                info.state.takeIf { it.isNotBlank() }?.let { s -> updates["state"] = s }
                info.zip.takeIf { it.isNotBlank() }?.let { z -> updates["zip"] = z }
                info.phone.takeIf { it.isNotBlank() }?.let { p -> updates["phone"] = p }
                info.email?.takeIf { it.isNotBlank() }?.let { e -> updates["email"] = e }

                if (updates.isEmpty()) {
                    onResult(true, null); return@addOnSuccessListener
                }

                dbRef.child(u.uid).updateChildren(updates)
                    .addOnSuccessListener { onResult(true, null) }
                    .addOnFailureListener { ex -> onResult(false, ex.localizedMessage) }
            }
            .addOnFailureListener { ex -> onResult(false, ex.localizedMessage) }
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = auth.currentUser
        val email = user?.email
        if (user == null || email.isNullOrBlank()) {
            onResult(false, "No signed-in user"); return
        }
        val cred = EmailAuthProvider.getCredential(email, currentPassword)
        user.reauthenticate(cred)
            .addOnSuccessListener {
                user.updatePassword(newPassword)
                    .addOnSuccessListener { onResult(true, null) }
                    .addOnFailureListener { ex -> onResult(false, ex.localizedMessage) }
            }
            .addOnFailureListener { ex -> onResult(false, ex.localizedMessage) }
    }

    fun signOut() {
        auth.signOut()
        _user.value = null
        _profileInfo.value = null
        _role.value = UserRole.Guest
    }

    fun onSubscriptionPurchased() {
        auth.currentUser?.uid?.let { uid ->
            dbRef.child(uid).child("role").setValue(UserRole.Subscriber.name)
        }
    }

    fun grantAdmin(uid: String) {
        dbRef.child(uid).child("role").setValue(UserRole.Admin.name)
    }

    private fun listenForProfile(uid: String) {
        dbRef.child(uid)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) return
                    _profileInfo.value = SellerInfo(
                        name     = snapshot.child("displayName").getValue(String::class.java).orEmpty(),
                        dba      = snapshot.child("dba").getValue(String::class.java)?.takeIf { it.isNotBlank() },
                        address1 = snapshot.child("address1").getValue(String::class.java).orEmpty(),
                        address2 = snapshot.child("address2").getValue(String::class.java)?.takeIf { it.isNotBlank() },
                        city     = snapshot.child("city").getValue(String::class.java).orEmpty(),
                        state    = snapshot.child("state").getValue(String::class.java).orEmpty(),
                        zip      = snapshot.child("zip").getValue(String::class.java).orEmpty(),
                        phone    = snapshot.child("phone").getValue(String::class.java).orEmpty(),
                        email    = snapshot.child("email").getValue(String::class.java)?.takeIf { it.isNotBlank() }
                    )
                }
                override fun onCancelled(error: DatabaseError) { /* no-op */ }
            })
    }
}
