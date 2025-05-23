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
     * Emits a map: category → ( "all" → list of all barcodes in that category )
     *
     * We no longer have per‐buyer data under each barcode, so we jam
     * everything into a single buyer key "all" to avoid crashes.
     */
    fun getBarcodesByCategoryAndBuyer(): Flow<Map<String, Map<String, List<String>>>> =
        callbackFlow {
            try {
                // 1️⃣ Fetch the flat products node
                val text = withContext(Dispatchers.IO) {
                    (URL("$baseUrl/products.json").openConnection() as HttpURLConnection).run {
                        inputStream.bufferedReader().use { it.readText() }
                    }
                }

                // 2️⃣ Parse into category → ( "all" → [ barcodes... ] )
                val root = JSONObject(text)
                val result = mutableMapOf<String, MutableMap<String, MutableList<String>>>()

                root.keys().forEach { barcode ->
                    val prodObj = root.getJSONObject(barcode)
                    val category = prodObj.optString("category", "")
                    if (category.isNotBlank()) {
                        val buyerMap = result.getOrPut(category) { mutableMapOf() }
                        buyerMap.getOrPut("all") { mutableListOf() }.add(barcode)
                    }
                }

                // 3️⃣ Freeze into immutable structures
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
     * Emits a map: category → list of buyer-keys for that category.
     * (Right now every category only has the single buyer key "all".)
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
            // 1️⃣ Fetch the same /products.json
            val text = withContext(Dispatchers.IO) {
                (URL("$baseUrl/products.json").openConnection() as HttpURLConnection).run {
                    inputStream.bufferedReader().use { it.readText() }
                }
            }

            // 2️⃣ Parse into a List<Product>
            val root = JSONObject(text)
            val list = mutableListOf<Product>()
            root.keys().forEach { barcode ->
                val prodObj  = root.getJSONObject(barcode)
                val desc     = prodObj.optString("description", "")
                val category = prodObj.optString("category", "")
                if (desc.isNotBlank() && category.isNotBlank()) {
                    list += Product(barcode, desc, category)
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
     * (Left as‐is; if you later move your price history node under /products,
     * you’ll need to update these URLs too.)
     */
    suspend fun getPriceHistory(category: String, barcode: String): PriceHistory =
        withContext(Dispatchers.IO) {
            // URL-encode spaces
            val catEnc  = category.replace(" ", "%20")
            val codeEnc = barcode.replace(" ", "%20")

            // 1️⃣ last‐updated
            val lastUpdated = try {
                val luUrl = "$baseUrl/last%20updated/$catEnc.json"
                (URL(luUrl).openConnection() as HttpURLConnection).run {
                    inputStream.bufferedReader().use { it.readText().trim('"') }
                }
            } catch (e: FileNotFoundException) {
                SimpleDateFormat("M/d/yyyy", Locale.US).format(Date())
            }

            // 2️⃣ price array
            val prices = try {
                val pricesUrl = "$baseUrl/barcodes/$catEnc/$codeEnc/Strip%20Flip.json"
                val raw = (URL(pricesUrl).openConnection() as HttpURLConnection).run {
                    inputStream.bufferedReader().use { it.readText().trim() }
                }
                if (raw == "null" || raw.isEmpty()) emptyList()
                else JSONObject(raw).let { obj ->
                    (1..10).mapNotNull { i -> obj.optString("price$i", "").toFloatOrNull() }
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
