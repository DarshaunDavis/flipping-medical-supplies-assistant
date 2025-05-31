package com.lislal.flippingmedicalsuppliesassistant.ui.invoice

data class InvoiceLine(
    val description: String,
    val expiration: String,
    val unitPrice: Float,
    var quantity: Int = 1
) {
    /** computed property */
    val lineTotal: Float
        get() = unitPrice * quantity
}
