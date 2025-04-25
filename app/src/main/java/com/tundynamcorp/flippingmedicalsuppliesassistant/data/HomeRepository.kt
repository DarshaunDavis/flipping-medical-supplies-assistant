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
    private val baseUrl = "https://test-strip-marketplace-default-rtdb.firebaseio.com"

    /**
     * Emits a flat list of all Products across categories, once, sorted by description
     */
    fun getAllProducts(): Flow<List<Product>> = callbackFlow {
        try {
            val text = withContext(Dispatchers.IO) {
                URL("$baseUrl/barcodes.json")
                    .openConnection()
                    .let { it as HttpURLConnection }
                    .run { inputStream.bufferedReader().use { it.readText() } }
            }

            val root = JSONObject(text)
            val list = mutableListOf<Product>()
            root.keys().forEach { category ->
                val catObj = root.getJSONObject(category)
                catObj.keys().forEach { barcode ->
                    val prodObj = catObj.getJSONObject(barcode)
                    val desc = prodObj.optString("description", "")
                    if (desc.isNotBlank()) {
                        list += Product(barcode = barcode, description = desc, category = category)
                    }
                }
            }

            trySend(list.sortedBy { it.description })
        } catch (e: Exception) {
            close(e)
        }
        close()
        awaitClose { /* nothing to clean up */ }
    }

    /**
     * Emits the list of buyer names (once) from `/buyers.json`.
     */
    fun getBuyers(): Flow<List<String>> = callbackFlow {
        try {
            val text = withContext(Dispatchers.IO) {
                URL("$baseUrl/buyers.json")
                    .openConnection()
                    .let { it as HttpURLConnection }
                    .run { inputStream.bufferedReader().use { it.readText() } }
            }

            val root = JSONObject(text)
            val names = mutableListOf<String>()
            root.keys().forEach { key ->
                root.getJSONObject(key)
                    .optString("name", null)
                    ?.takeIf { it.isNotBlank() }
                    ?.let { names += it }
            }

            trySend(names)
        } catch (e: Exception) {
            close(e)
        }
        close()
        awaitClose { /* nothing to do */ }
    }

    /**
     * Fetches a product’s last-updated date and the last 10 monthly prices.
     */
    suspend fun getPriceHistory(category: String, barcode: String): PriceHistory =
        withContext(Dispatchers.IO) {
            val cat  = category.replace(" ", "%20")
            val code = barcode.replace(" ", "%20")

            // 1) last-updated for category
            val luUrl = "$baseUrl/last%20updated/$cat.json"
            val lastUpdated = (URL(luUrl).openConnection() as HttpURLConnection).run {
                inputStream.bufferedReader().use { it.readText().trim('"') }
            }

            // 2) price map for barcode ("Strip Flip" node)
            val pricesUrl = "$baseUrl/barcodes/$cat/$code/Strip%20Flip.json"
            val rawPrices = (URL(pricesUrl).openConnection() as HttpURLConnection).run {
                inputStream.bufferedReader().use { it.readText().trim() }
            }

            // 3) parse into floats
            val prices = if (rawPrices == "null" || rawPrices.isEmpty()) {
                emptyList()
            } else {
                JSONObject(rawPrices).let { obj ->
                    (1..10).mapNotNull { i ->
                        obj.optString("price$i", "").toFloatOrNull()
                    }
                }
            }

            // 4) return model (description is filled by ViewModel)
            PriceHistory(
                description = "",
                lastUpdated = lastUpdated,
                prices      = prices
            )
        }
}
