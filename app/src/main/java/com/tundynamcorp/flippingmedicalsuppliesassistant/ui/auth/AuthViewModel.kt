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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val auth   = FirebaseAuth.getInstance()
    private val dbRef  = Firebase.database.reference.child("users")

    // FirebaseUser flow
    private val _user = MutableStateFlow(auth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user.asStateFlow()

    // UserRole flow
    private val _role = MutableStateFlow(UserRole.Guest)
    val role: StateFlow<UserRole> = _role.asStateFlow()

    // Trial start timestamp (ms since epoch)
    private val _trialStart = MutableStateFlow<Long?>(null)
    val trialStart: StateFlow<Long?> = _trialStart.asStateFlow()

    @Suppress("unused") // used in UI gating elsewhere
    val isTrialActive: StateFlow<Boolean> = trialStart
        .map { ts ->
            ts?.let {
                val cutoff = it + 30L * 24 * 60 * 60 * 1000
                System.currentTimeMillis() <= cutoff
            } ?: false
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // SellerInfo flow from RTDB
    private val _profileInfo = MutableStateFlow<SellerInfo?>(null)
    val profileInfo: StateFlow<SellerInfo?> = _profileInfo.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val u = firebaseAuth.currentUser
            _user.value = u
            if (u == null) {
                // signed out
                _role.value = UserRole.Guest
                _trialStart.value = null
                _profileInfo.value = null
            } else {
                // load role
                dbRef.child(u.uid).child("role")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            _role.value = snapshot.getValue(String::class.java)
                                ?.let { UserRole.valueOf(it) }
                                ?: UserRole.User
                        }
                        override fun onCancelled(error: DatabaseError) {
                            _role.value = UserRole.User
                        }
                    })
                // load trialStart timestamp
                dbRef.child(u.uid).child("trialStart")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            _trialStart.value = snapshot.getValue(Long::class.java)
                        }
                        override fun onCancelled(error: DatabaseError) {
                            // no-op
                        }
                    })
                // load profile details
                listenForProfile(u.uid)
            }
        }
    }

    /** Sign in existing user */
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
                        else ->
                            "Couldn’t sign in. Check your connection and try again."
                    }
                    onResult(false, msg)
                }
        }
    }

    /** Register new user with trial start and default 'User' role */
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
                    // update Auth displayName
                    val profileUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName(capName)
                        .build()
                    u.updateProfile(profileUpdate)
                        .addOnSuccessListener {
                            // write initial user data: displayName, role, trialStart
                            dbRef.child(u.uid)
                                .setValue(
                                    mapOf(
                                        "displayName" to capName,
                                        "role" to UserRole.User.name,
                                        "trialStart" to ServerValue.TIMESTAMP
                                    )
                                )
                                .addOnSuccessListener {
                                    // **Immediate local state update** so UI gating reacts instantly:
                                    _role.value = UserRole.User
                                    _trialStart.value = System.currentTimeMillis()

                                    onResult(true, null)
                                }
                                .addOnFailureListener { ex -> onResult(false, ex.localizedMessage) }
                        }
                        .addOnFailureListener { ex -> onResult(false, ex.localizedMessage) }
                }
                .addOnFailureListener { ex -> onResult(false, ex.localizedMessage) }
        }
    }

    /** Update profile fields in Auth and RTDB */
    fun updateProfile(
        info: SellerInfo,
        onResult: (Boolean, String?) -> Unit
    ) {
        val u = auth.currentUser ?: run {
            onResult(false, "No user signed in")
            return
        }
        val capName = info.name.trim()
            .split("\\s+".toRegex())
            .joinToString(" ") { it.replaceFirstChar { c -> c.titlecase() } }

        val profileUpdate = UserProfileChangeRequest.Builder()
            .setDisplayName(capName)
            .build()

        u.updateProfile(profileUpdate)
            .addOnSuccessListener {
                val updates = mutableMapOf<String, Any>("displayName" to capName)
                info.dba?.takeIf { it.isNotBlank() }?.let { updates["dba"] = it }
                info.address1.takeIf { it.isNotBlank() }?.let { updates["address1"] = it }
                info.address2?.takeIf { it.isNotBlank() }?.let { updates["address2"] = it }
                info.city.takeIf { it.isNotBlank() }?.let { updates["city"] = it }
                info.state.takeIf { it.isNotBlank() }?.let { updates["state"] = it }
                info.zip.takeIf { it.isNotBlank() }?.let { updates["zip"] = it }
                info.phone.takeIf { it.isNotBlank() }?.let { updates["phone"] = it }
                info.email?.takeIf { it.isNotBlank() }?.let { updates["email"] = it }

                if (updates.isEmpty()) {
                    onResult(true, null)
                } else {
                    dbRef.child(u.uid).updateChildren(updates)
                        .addOnSuccessListener { onResult(true, null) }
                        .addOnFailureListener { ex -> onResult(false, ex.localizedMessage) }
                }
            }
            .addOnFailureListener { ex -> onResult(false, ex.localizedMessage) }
    }

    /** Change current user's password */
    fun changePassword(
        currentPassword: String,
        newPassword: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = auth.currentUser
        val email = user?.email
        if (user == null || email.isNullOrBlank()) {
            onResult(false, "No signed-in user")
            return
        }
        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        user
            .reauthenticate(credential)
            .addOnSuccessListener {
                user.updatePassword(newPassword)
                    .addOnSuccessListener { onResult(true, null) }
                    .addOnFailureListener { ex -> onResult(false, ex.localizedMessage) }
            }
            .addOnFailureListener { ex -> onResult(false, ex.localizedMessage) }
    }

    /** Sign out and reset state */
    fun signOut() {
        auth.signOut()
        _user.value = null
        _role.value = UserRole.Guest
        _trialStart.value = null
        _profileInfo.value = null
    }

    @Suppress("unused") // called by billing integration
    fun onSubscriptionPurchased() {
        auth.currentUser?.uid?.let { uid ->
            dbRef.child(uid)
                .child("role")
                .setValue(UserRole.Subscriber.name)
                .addOnSuccessListener {
                    // immediately reflect in-app
                    _role.value = UserRole.Subscriber
                }
        }
    }

    @Suppress("unused") // admin tool
    fun grantAdmin(uid: String) {
        dbRef.child(uid).child("role")
            .setValue(UserRole.Admin.name)
    }

    /** Listen for RTDB profile changes */
    private fun listenForProfile(uid: String) {
        dbRef.child(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) return
                    _profileInfo.value = SellerInfo(
                        name     = snapshot.child("displayName")
                            .getValue(String::class.java)
                            .orEmpty(),
                        dba      = snapshot.child("dba")
                            .getValue(String::class.java)
                            ?.takeIf { it.isNotBlank() },
                        address1 = snapshot.child("address1")
                            .getValue(String::class.java)
                            .orEmpty(),
                        address2 = snapshot.child("address2")
                            .getValue(String::class.java)
                            ?.takeIf { it.isNotBlank() },
                        city     = snapshot.child("city")
                            .getValue(String::class.java)
                            .orEmpty(),
                        state    = snapshot.child("state")
                            .getValue(String::class.java)
                            .orEmpty(),
                        zip      = snapshot.child("zip")
                            .getValue(String::class.java)
                            .orEmpty(),
                        phone    = snapshot.child("phone")
                            .getValue(String::class.java)
                            .orEmpty(),
                        email    = snapshot.child("email")
                            .getValue(String::class.java)
                            ?.takeIf { it.isNotBlank() }
                    )
                }
                override fun onCancelled(error: DatabaseError) {
                    // no-op
                }
            })
    }
}
