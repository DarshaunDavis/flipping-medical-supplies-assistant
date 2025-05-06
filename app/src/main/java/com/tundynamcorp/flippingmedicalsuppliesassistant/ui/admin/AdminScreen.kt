package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.AdminViewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.UserRole

@Composable
fun AdminScreen(
    currentRole: UserRole,
    onUpgradeClick: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    // tab titles and minimum role required
    val tabs = listOf("Profit Margin", "Prices", "Products")
    val minRequiredRole = listOf(
        UserRole.Guest,      // Profit Margin always enabled
        UserRole.Subscriber, // Prices only for subscribers & admins
        UserRole.User        // Products for users+trialers, subscribers, admins
    )

    var selectedTab by remember { mutableIntStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                val enabled = currentRole.ordinal >= minRequiredRole[index].ordinal
                Tab(
                    selected = (index == selectedTab),
                    onClick = {
                        if (enabled) {
                            selectedTab = index
                        } else {
                            onUpgradeClick()
                        }
                    },
                    enabled = enabled,
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> ProfitMarginTab(
                margins = viewModel.margins.collectAsState().value,
                onSubmit = viewModel::updateMargin
            )
            1 -> PricesTab()
            2 -> ProductsTab()
        }
    }
}
