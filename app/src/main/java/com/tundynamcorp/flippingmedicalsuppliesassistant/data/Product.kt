package com.tundynamcorp.flippingmedicalsuppliesassistant.data

data class Product(
    val barcode: String,
    val description: String,
    val category: String,
    val imageUrl: String? = null
)
