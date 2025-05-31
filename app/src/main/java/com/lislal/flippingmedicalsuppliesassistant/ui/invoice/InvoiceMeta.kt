package com.lislal.flippingmedicalsuppliesassistant.ui.invoice

data class InvoiceMeta(
    val clientName: String,
    val clientAddress1: String,
    val clientAddress2: String?,
    val clientCity: String,
    val clientState: String,
    val clientZip: String,
    val payableTo: String,
    val invoiceNumber: String?
)

