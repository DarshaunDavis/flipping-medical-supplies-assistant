// app/src/main/java/com/tundynamcorp/flippingmedicalsuppliesassistant/data/HomeRepository.kt
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
                (URL("$baseUrl/barcodes.json").openConnection() as HttpURLConnection).run {
                    inputStream.bufferedReader().use { it.readText() }
                }
            }
            val root = JSONObject(text)
            val list = mutableListOf<Product>()
            root.keys().forEach { category ->
                val catObj = root.getJSONObject(category)
                catObj.keys().forEach { barcode ->
                    val prodObj = catObj.getJSONObject(barcode)
                    val desc = prodObj.optString("description", "")
                    if (desc.isNotBlank()) {
                        list += Product(barcode, desc, category)
                    }
                }
            }
            trySend(list.sortedBy { it.description })
        } catch (e: Exception) {
            close(e)
        }
        close()
        awaitClose { }
    }

    /**
     * Fetches a product’s last-updated date and the last 10 monthly prices.
     */
    suspend fun getPriceHistory(category: String, barcode: String): PriceHistory = withContext(Dispatchers.IO) {
        val cat  = category.replace(" ", "%20")
        val code = barcode.replace(" ", "%20")

        val luUrl = "$baseUrl/last%20updated/$cat.json"
        val lastUpdated = (URL(luUrl).openConnection() as HttpURLConnection).run {
            inputStream.bufferedReader().use { it.readText().trim('"') }
        }

        val pricesUrl = "$baseUrl/barcodes/$cat/$code/Strip%20Flip.json"
        val rawPrices = (URL(pricesUrl).openConnection() as HttpURLConnection).run {
            inputStream.bufferedReader().use { it.readText().trim() }
        }
        val prices = if (rawPrices == "null" || rawPrices.isEmpty()) {
            emptyList()
        } else {
            JSONObject(rawPrices).let { obj ->
                (1..10).mapNotNull { i ->
                    obj.optString("price$i", "").toFloatOrNull()
                }
            }
        }

        PriceHistory(
            description = "",
            lastUpdated = lastUpdated,
            prices      = prices
        )
    }

    /**
     * Emits a map of Category → list of Buyer-node names under that category.
     */
    fun getBuyersByCategory(): Flow<Map<String, List<String>>> = callbackFlow {
        try {
            val text = withContext(Dispatchers.IO) {
                (URL("$baseUrl/barcodes.json").openConnection() as HttpURLConnection).run {
                    inputStream.bufferedReader().use { it.readText() }
                }
            }
            val root = JSONObject(text)
            val map  = mutableMapOf<String, MutableSet<String>>()
            root.keys().forEach { category ->
                val catObj   = root.getJSONObject(category)
                val buyerSet = mutableSetOf<String>()
                catObj.keys().forEach { barcode ->
                    val prodObj = catObj.getJSONObject(barcode)
                    prodObj.keys().forEach { key ->
                        if (key != "description") buyerSet += key
                    }
                }
                map[category] = buyerSet
            }
            val result = map.mapValues { it.value.sorted() }
            trySend(result)
        } catch (e: Exception) {
            close(e)
        }
        close()
        awaitClose { }
    }
}
