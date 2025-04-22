package com.tundynamcorp.flippingmedicalsuppliesassistant.data

/**
 * Holds the data needed to render a product’s price‑history chart.
 *
 * @param description The product’s description (for the dialog title).
 * @param lastUpdated The last‑updated date string (e.g. "10/22/2024").
 * @param prices A list of prices (one per month) in chronological order.
 */
data class PriceHistory(
    val description: String,
    val lastUpdated: String,
    val prices: List<Float>
)
