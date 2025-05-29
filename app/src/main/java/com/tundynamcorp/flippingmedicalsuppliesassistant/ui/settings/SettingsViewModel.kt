package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.settings

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.SettingsRepository
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.invoice.SellerInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val TAG = "SettingsVM"

/**
 * Mirror of each entry under the top-level `/buyers` node.
 */
data class BuyerInfo(
    val id: String = "",
    val name: String = "",
    val address1: String = "",
    val address2: String? = null,
    val city: String = "",
    val state: String = "",
    val zip: String = ""
)

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo    = SettingsRepository(app)
    private val baseUrl = "https://test-strip-marketplace-default-rtdb.firebaseio.com"

    // ─── LIVE BUYERS ─────────────────────────────
    private val _buyerList = MutableStateFlow<List<BuyerInfo>>(emptyList())
    val buyerList: StateFlow<List<BuyerInfo>> = _buyerList.asStateFlow()

    // ─── LIVE CATEGORIES ─────────────────────────────
    private val _categoryList = MutableStateFlow<List<String>>(emptyList())
    val categoryList: StateFlow<List<String>> = _categoryList.asStateFlow()

    init {
        // initial load of both buyers and categories
        viewModelScope.launch {
            loadBuyers()
            loadCategories()
        }
    }

    /** Public API to re-pull the buyers list on demand */
    fun refreshBuyers() {
        viewModelScope.launch { loadBuyers() }
    }

    private suspend fun loadBuyers() {
        Log.d(TAG, "🔍 Fetching buyers from $baseUrl/buyers.json")
        try {
            val jsonText = withContext(Dispatchers.IO) {
                (URL("$baseUrl/buyers.json").openConnection() as HttpURLConnection).run {
                    inputStream.bufferedReader().use { it.readText() }
                }
            }
            val root = JSONObject(jsonText)
            val list = root.keys().asSequence().map { id ->
                root.getJSONObject(id).let { obj ->
                    BuyerInfo(
                        id       = id,
                        name     = obj.optString("name", "<no-name>"),
                        address1 = obj.optString("address", ""),
                        address2 = obj.optString("suite", null.toString())
                            .takeIf { it.isNotBlank() },
                        city     = obj.optString("city", ""),
                        state    = obj.optString("state", ""),
                        zip      = obj.optString("zip", obj.optString("zipCode", ""))
                    )
                }
            }.toList()
            _buyerList.value = list
            Log.d(TAG, "✅ Loaded ${list.size} buyers")
        } catch (th: Throwable) {
            Log.e(TAG, "❌ Failed fetching buyers", th)
        }
    }

    /** Public API to re-pull the categories list on demand */
    fun refreshCategories() {
        viewModelScope.launch { loadCategories() }
    }

    private suspend fun loadCategories() {
        Log.d(TAG, "🔍 Fetching categories from $baseUrl/categories.json")
        try {
            val jsonText = withContext(Dispatchers.IO) {
                (URL("$baseUrl/categories.json").openConnection() as HttpURLConnection).run {
                    inputStream.bufferedReader().use { it.readText() }
                }
            }
            val root = JSONObject(jsonText)
            val list = root.keys().asSequence()
                .filter { key -> root.optBoolean(key, false) }
                .sorted()
                .toList()
            _categoryList.value = list
            Log.d(TAG, "✅ Loaded categories: $list")
        } catch (th: Throwable) {
            Log.e(TAG, "❌ Failed fetching categories", th)
        }
    }

    // ─── SELLER PROFILE (unchanged) ─────────────────────────────
    val profileInfo: StateFlow<SellerInfo> = repo.profileFlow
        .stateIn(
            scope   = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SellerInfo(
                name     = "",
                dba      = null,
                address1 = "",
                address2 = null,
                city     = "",
                state    = "",
                zip      = "",
                phone    = "",
                email    = null
            )
        )

    val selectedBuyersMap: StateFlow<Map<String,String?>> =
        repo.selectedBuyersMapFlow
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    fun updateProfile(new: SellerInfo) {
        viewModelScope.launch { repo.saveProfile(new) }
    }
}
