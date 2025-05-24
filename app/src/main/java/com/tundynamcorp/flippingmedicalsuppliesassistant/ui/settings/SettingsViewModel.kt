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

    init {
        // initial load
        viewModelScope.launch { loadBuyers() }
    }

    /** Public API to re-pull the buyers list on demand */
    fun refreshBuyers() {
        viewModelScope.launch { loadBuyers() }
    }

    /** actual fetch logic */
    private suspend fun loadBuyers() {
        Log.d("SettingsViewModel", "🔍 Fetching buyers from $baseUrl/buyers.json")
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
            Log.d("SettingsViewModel", "✅ Loaded ${list.size} buyers")
        } catch (th: Throwable) {
            Log.e("SettingsViewModel", "❌ Failed to fetch buyers", th)
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