package com.lislal.flippingmedicalsuppliesassistant.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
     * Emits a map: category → ( wholesaler → list of barcodes )
     *
     * NEW SCHEMA: all products now under /products.json, each with a
     * `prices` child whose keys are wholesaler names.
     */
    fun getBarcodesByCategoryAndWholesaler(): Flow<Map<String, Map<String, List<String>>>> =
        callbackFlow {
            try {
                // ▶ fetch /products.json instead of /barcodes.json
                val text = withContext(Dispatchers.IO) {
                    (URL("$baseUrl/products.json").openConnection() as HttpURLConnection).run {
                        inputStream.bufferedReader().use { it.readText() }
                    }
                }

                val root = JSONObject(text)
                val result = mutableMapOf<String, MutableMap<String, MutableList<String>>>()

                root.keys().forEach { barcode ->
                    val prodObj = root.getJSONObject(barcode)
                    val category = prodObj.optString("category", "")
                    if (category.isNotBlank()) {
                        val wholesalerMap = result.getOrPut(category) { mutableMapOf() }
                        // iterate over each wholesaler under this product’s `prices` child
                        prodObj.optJSONObject("prices")
                            ?.keys()
                            ?.forEach { wholesaler ->
                                wholesalerMap
                                    .getOrPut(wholesaler) { mutableListOf() }
                                    .add(barcode)
                            }
                    }
                }

                // freeze to immutable
                val immutable = result.mapValues { (_, wholesalers) ->
                    wholesalers.mapValues { it.value.toList() }
                }

                trySend(immutable)
            } catch (e: Exception) {
                close(e)
            }
            awaitClose { }
        }

    /**
     * Emits a flat list of all Products across categories, once, sorted by description.
     * Now pulls from /products.json.
     */
    fun getAllProducts(): Flow<List<Product>> = callbackFlow {
        try {
            val text = withContext(Dispatchers.IO) {
                (URL("$baseUrl/products.json").openConnection() as HttpURLConnection).run {
                    inputStream.bufferedReader().use { it.readText() }
                }
            }

            val root = JSONObject(text)
            val list = mutableListOf<Product>()
            root.keys().forEach { barcode ->
                val prodObj = root.getJSONObject(barcode)
                val desc = prodObj.optString("description", "")
                val category = prodObj.optString("category", "")
                if (desc.isNotBlank() && category.isNotBlank()) {
                    val imageUrl = prodObj.optString("imageUrl", null.toString())
                    list += Product(barcode, desc, category, imageUrl)
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
     * Fetches a product’s last‐updated date and the last 10 monthly prices for a *specific* wholesaler.
     * Now reads from /products/{barcode}/prices/{wholesaler}.json.
     */
    suspend fun getPriceHistory(
        category: String,
        barcode: String,
        wholesaler: String
    ): PriceHistory = withContext(Dispatchers.IO) {
        val catEnc   = category.replace(" ", "%20")
        val codeEnc  = barcode.replace(" ", "%20")
        val wholesalerEnc = wholesaler.replace(" ", "%20")

        // 1️⃣ last‐updated unchanged
        val lastUpdated = try {
            val luUrl = "$baseUrl/last%20updated/$catEnc.json"
            (URL(luUrl).openConnection() as HttpURLConnection).run {
                inputStream.bufferedReader().use { it.readText().trim('"') }
            }
        } catch (_: FileNotFoundException) {
            SimpleDateFormat("M/d/yyyy", Locale.US).format(Date())
        }

        // 2️⃣ fetch that wholesaler’s 10‐month price map
        val prices = try {
            val pu = "$baseUrl/products/$codeEnc/prices/$wholesalerEnc.json"
            val raw = (URL(pu).openConnection() as HttpURLConnection).run {
                inputStream.bufferedReader().use { it.readText().trim() }
            }
            if (raw == "null" || raw.isEmpty()) emptyList()
            else JSONObject(raw).let { obj ->
                (1..10).mapNotNull { i ->
                    obj.optString("price$i", "").toFloatOrNull()
                }
            }
        } catch (_: FileNotFoundException) {
            emptyList()
        }

        PriceHistory(
            description = "",
            lastUpdated = lastUpdated,
            prices      = prices
        )
    }
}
