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

    // Tracks how many invoices the current user has generated this month
    private val _countThisMonth = MutableStateFlow(0)
    val countThisMonth: StateFlow<Int> = _countThisMonth.asStateFlow()

    init {
        // On startup, fetch this month’s count from RTDB
        viewModelScope.launch {
            auth.currentUser?.uid?.let { uid ->
                val monthKey = SimpleDateFormat("yyyyMM", Locale.US).format(Date())
                db.child(uid)
                    .child(monthKey)
                    .get()
                    .addOnSuccessListener { snap ->
                        _countThisMonth.value = snap.childrenCount.toInt()
                    }
                    .addOnFailureListener {
                        // log or ignore
                    }
            }
        }
    }

    /**
     * Pushes a timestamp into /invoices/{uid}/{yyyyMM}/… and immediately bumps
     * the in‐memory count so UI gating reacts without a restart.
     */
    fun recordInvoice(onComplete: (() -> Unit)? = null) {
        val uid = auth.currentUser?.uid ?: return
        val monthKey = SimpleDateFormat("yyyyMM", Locale.US).format(Date())

        db.child(uid)
            .child(monthKey)
            .push()
            .setValue(ServerValue.TIMESTAMP)
            .addOnSuccessListener {
                // instantly reflect in-app
                _countThisMonth.value += 1
                onComplete?.invoke()
            }
        // you may want to handle failure here too
    }
}
