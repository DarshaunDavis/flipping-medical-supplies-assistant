package com.tundynamcorp.flippingmedicalsuppliesassistant.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class HomeRepository {
    private val endpoint =
        "https://test-strip-marketplace-default-rtdb.firebaseio.com/barcodes.json"

    /**
     * Emits a flat list of all Products across categories, once, sorted by description
     */
    fun getAllProducts(): Flow<List<Product>> = callbackFlow {
        try {
            // Fetch on IO
            val text = withContext(Dispatchers.IO) {
                val conn = URL(endpoint).openConnection() as HttpURLConnection
                conn.inputStream.bufferedReader().use { it.readText() }
            }

            // Parse JSON
            val root = JSONObject(text)
            val list = mutableListOf<Product>()
            root.keys().forEach { category ->
                val catObj = root.getJSONObject(category)
                catObj.keys().forEach { barcode ->
                    val prodObj = catObj.getJSONObject(barcode)
                    val desc = prodObj.optString("description", "")
                    if (desc.isNotBlank()) {
                        list += Product(
                            barcode = barcode,
                            description = desc,
                            category = category
                        )
                    }
                }
            }

            // Sort and emit
            trySend(list.sortedBy { it.description })
        } catch (e: Exception) {
            close(e)
        }
        close()
        awaitClose { /* nothing to do */ }
    }

    /**
     * Fetches a product’s last‑updated date and the last 10 monthly prices.
     */
    suspend fun getPriceHistory(category: String, barcode: String): PriceHistory = withContext(Dispatchers.IO) {
        // Replace spaces with %20 so they match your RTDB keys
        val cat  = category.replace(" ", "%20")
        val code = barcode.replace(" ", "%20")

        // 1) last‑updated for category
        val luUrl = "https://test-strip-marketplace-default-rtdb.firebaseio.com/last%20updated/$cat.json"
        val lastUpdated = (URL(luUrl).openConnection() as HttpURLConnection).run {
            inputStream.bufferedReader().use { it.readText().trim('"') }
        }

        // 2) price map for barcode
        val pricesUrl = "https://test-strip-marketplace-default-rtdb.firebaseio.com/barcodes/$cat/$code/Strip%20Flip.json"
        val rawPrices = (URL(pricesUrl).openConnection() as HttpURLConnection).run {
            inputStream.bufferedReader().use { it.readText().trim() }
        }

        // 3) parse into floats
        val prices = if (rawPrices == "null" || rawPrices.isEmpty()) {
                        emptyList()
            } else {
            JSONObject(rawPrices).let { obj ->
                            (1..10).mapNotNull { i ->
                               // always returns a String, so toFloatOrNull() is enough
                                obj.optString("price$i", "").toFloatOrNull()
                            }
                        }
                   }

        // 4) return model (description filled in by ViewModel)
        PriceHistory(
            description = "",
            lastUpdated = lastUpdated,
            prices      = prices
        )
    }
}
