package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.invoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class InvoiceViewModel : ViewModel() {
    private val db   = Firebase.database.reference.child("invoices")
    private val auth = FirebaseAuth.getInstance()

    private val _countThisMonth = MutableStateFlow(0)
    val countThisMonth: StateFlow<Int> = _countThisMonth.asStateFlow()

    init {
        // Launch in coroutine, then only act if the user is signed in
        viewModelScope.launch {
            auth.currentUser?.uid?.let { uid ->
                val monthKey = SimpleDateFormat("yyyyMM", Locale.US).format(Date())
                db.child(uid)
                    .child(monthKey)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        _countThisMonth.value = snapshot.childrenCount.toInt()
                    }
                    .addOnFailureListener {
                        // optionally log the error
                    }
            }
        }
    }

    fun recordInvoice(onComplete: (() -> Unit)? = null) {
        val uid = auth.currentUser?.uid ?: return
        val monthKey = SimpleDateFormat("yyyyMM", Locale.US).format(Date())
        // push a simple timestamp under /invoices/{uid}/{yyyyMM}
        db.child(uid).child(monthKey).push()
            .setValue(ServerValue.TIMESTAMP)
            .addOnSuccessListener {
                // bump local flow immediately
                _countThisMonth.value += 1
                onComplete?.invoke()
            }
        }
}
