package com.lislal.flippingmedicalsuppliesassistant.ui.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lislal.flippingmedicalsuppliesassistant.data.AdminViewModel
import com.lislal.flippingmedicalsuppliesassistant.data.HomeViewModel
import com.lislal.flippingmedicalsuppliesassistant.data.UserRole

@Composable
fun AdminScreen(
    homeViewModel: HomeViewModel,
    currentRole: UserRole,
    isTrialActive: Boolean,
    onUpgradeClick: () -> Unit,
    adminViewModel: AdminViewModel = viewModel()
) {
    val tabs = listOf("Profit Margin", "Prices", "Products")
    val minRequiredRole = listOf(
        UserRole.Guest,      // Profit Margin always enabled
        UserRole.Subscriber, // Prices only for subs/trialers/admins
        UserRole.User        // Products for users+trialers, subs, admins
    )

    // Treat a trialing User as a Subscriber
    val effectiveOrdinal = when {
        currentRole == UserRole.User && isTrialActive -> UserRole.Subscriber.ordinal
        else                                           -> currentRole.ordinal
    }

    var selectedTab by remember { mutableIntStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                val enabled = effectiveOrdinal >= minRequiredRole[index].ordinal
                Tab(
                    selected    = selectedTab == index,
                    onClick     = { if (enabled) selectedTab = index else onUpgradeClick() },
                    enabled     = enabled,
                    text        = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> ProfitMarginTab(
                margins = adminViewModel.margins.collectAsState().value,
                onSubmit = adminViewModel::updateMargin
            )
            1 -> PricesTab(homeViewModel)
            2 -> ProductsTab(homeViewModel)
        }
    }
}
