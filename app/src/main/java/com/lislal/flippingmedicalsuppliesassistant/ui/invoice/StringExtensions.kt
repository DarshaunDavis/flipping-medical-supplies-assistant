package com.lislal.flippingmedicalsuppliesassistant.ui.invoice

fun String.formatPhone(): String {
        val digits = filter(Char::isDigit)
        return when (digits.length) {
            10 -> "(${digits.substring(0,3)}) ${digits.substring(3,6)}-${digits.substring(6)}"
            7  -> "${digits.substring(0,3)}-${digits.substring(3)}"
            else -> this
        }
    }