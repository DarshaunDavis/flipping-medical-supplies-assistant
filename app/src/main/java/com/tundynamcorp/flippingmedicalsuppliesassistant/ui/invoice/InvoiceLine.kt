package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.invoice

data class InvoiceLine(
    val description: String,
    val unitPrice: Float,
    var quantity: Int = 1
) {
    /** computed property */
    val lineTotal: Float
        get() = unitPrice * quantity
}
