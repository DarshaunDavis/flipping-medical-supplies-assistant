package com.tundynamcorp.flippingmedicalsuppliesassistant.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.FileNotFoundException
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class HomeRepository {
    private val baseUrl = "https://test-strip-marketplace-default-rtdb.firebaseio.com"

    /**
     * Emits a map: category → ( buyer → list of barcodes supplied by that buyer )
     */
    fun getBarcodesByCategoryAndBuyer(): Flow<Map<String, Map<String, List<String>>>> = callbackFlow {
        try {
            val text = withContext(Dispatchers.IO) {
                (URL("$baseUrl/barcodes.json").openConnection() as HttpURLConnection).run {
                    inputStream.bufferedReader().use { it.readText() }
                }
            }
            val root = JSONObject(text)
            val result = mutableMapOf<String, MutableMap<String, MutableList<String>>>()

            root.keys().forEach { category ->
                val catObj = root.getJSONObject(category)
                catObj.keys().forEach { barcode ->
                    val prodObj = catObj.getJSONObject(barcode)
                    prodObj.keys().forEach { key ->
                        if (key != "description") {
                            val buyerMap = result.getOrPut(category) { mutableMapOf() }
                            buyerMap.getOrPut(key) { mutableListOf() }.add(barcode)
                        }
                    }
                }
            }

            // convert inner maps to immutable lists
            val immutable = result.mapValues { (_, buyerMap) ->
                buyerMap.mapValues { it.value.toList() }
            }

            trySend(immutable)
        } catch (e: Exception) {
            close(e)
        }
        awaitClose { }
    }

    /**
     * Emits a map: category → list of buyers for that category.
     */
    fun getBuyersByCategory(): Flow<Map<String, List<String>>> =
        getBarcodesByCategoryAndBuyer()
            .map { bcMap ->
                bcMap.mapValues { it.value.keys.sorted() }
            }

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
     * Fetches a product’s last‐updated date and the last 10 monthly prices.
     * If either node is missing, we fall back to sensible defaults instead of crashing.
     */
    suspend fun getPriceHistory(category: String, barcode: String): PriceHistory =
        withContext(Dispatchers.IO) {
            // URL-encode spaces
            val catEnc  = category.replace(" ", "%20")
            val codeEnc = barcode.replace(" ", "%20")

            // 1️⃣ Try to fetch last‐updated; if missing, use today’s date
            val lastUpdated = try {
                val luUrl = "$baseUrl/last%20updated/$catEnc.json"
                (URL(luUrl).openConnection() as HttpURLConnection).run {
                    inputStream.bufferedReader().use { it.readText().trim('"') }
                }
            } catch (e: FileNotFoundException) {
                // no “last updated” entry yet → default to today
                SimpleDateFormat("M/d/yyyy", Locale.US).format(Date())
            }

            // 2️⃣ Try to fetch the price map; if missing or empty, use an empty list
            val prices = try {
                val pricesUrl = "$baseUrl/barcodes/$catEnc/$codeEnc/Strip%20Flip.json"
                val raw = (URL(pricesUrl).openConnection() as HttpURLConnection).run {
                    inputStream.bufferedReader().use { it.readText().trim() }
                }
                if (raw == "null" || raw.isEmpty()) {
                    emptyList()
                } else {
                    JSONObject(raw).let { obj ->
                        (1..10).mapNotNull { i ->
                            obj.optString("price$i", "").toFloatOrNull()
                        }
                    }
                }
            } catch (e: FileNotFoundException) {
                emptyList()
            }

            PriceHistory(
                description = "",
                lastUpdated = lastUpdated,
                prices      = prices
            )
        }
}
