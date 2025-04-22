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
                    val desc = prodObj.optString("description", null)
                    if (!desc.isNullOrBlank()) {
                        list += Product(barcode = barcode, description = desc, category = category)
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
}
