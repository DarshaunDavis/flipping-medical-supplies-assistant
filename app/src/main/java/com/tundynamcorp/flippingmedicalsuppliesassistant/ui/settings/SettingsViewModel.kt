package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.settings

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.SettingsRepository
import com.tundynamcorp.flippingmedicalsuppliesassistant.ui.invoice.SellerInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
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

    private val _buyerList = MutableStateFlow<List<BuyerInfo>>(emptyList())
    val buyerList: StateFlow<List<BuyerInfo>> = _buyerList.asStateFlow()

    init {
        viewModelScope.launch {
            Log.d("SettingsViewModel", "🔍 Starting fetch from $baseUrl/buyers.json")
            try {
                val jsonText = withContext(Dispatchers.IO) {
                    (URL("$baseUrl/buyers.json").openConnection() as HttpURLConnection).run {
                        inputStream.bufferedReader().use { it.readText() }
                    }
                }
                val root = JSONObject(jsonText)
                val list = mutableListOf<BuyerInfo>()

                root.keys().forEach { id ->
                    val obj = root.getJSONObject(id)
                    val info = BuyerInfo(
                        id       = id,
                        name     = obj.optString("name", "<no-name>"),
                        address1 = obj.optString("address", ""),
                        address2 = obj.optString("suite", null.toString())
                            .takeIf { !it.isNullOrBlank() },
                        city     = obj.optString("city", ""),
                        state    = obj.optString("state", ""),
                        zip      = obj.optString("zip", obj.optString("zipCode", ""))
                    )
                    list += info
                    Log.d("SettingsViewModel", "  • Parsed buyer `$id`: ${info.name}")
                }

                Log.d("SettingsViewModel", "✅ Fetched ${list.size} buyers")
                _buyerList.value = list

            } catch (th: Throwable) {
                Log.e("SettingsViewModel", "❌ Error fetching buyers", th)
            }
        }
    }

    // ———————————
    // Seller profile from DataStore (unchanged)
    val profileInfo: StateFlow<SellerInfo> = repo.profileFlow
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.Eagerly,
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

    // Persisted selected-buyers map (unchanged)
    val selectedBuyersMap: StateFlow<Map<String, String?>> =
        repo.selectedBuyersMapFlow
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    fun updateProfile(new: SellerInfo) {
        viewModelScope.launch { repo.saveProfile(new) }
    }
}
