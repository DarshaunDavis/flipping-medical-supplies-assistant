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

@Composable
fun AdminScreen(viewModel: AdminViewModel = viewModel()) {
    val tabs = listOf("Profit Margin", "Prices", "Products")
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = (index == selectedTab),
                    onClick  = { selectedTab = index },
                    text     = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> ProfitMarginTab(viewModel.margins.collectAsState().value, viewModel::updateMargin)
            1 -> PricesTab()
            2 -> ProductsTab()
        }
    }
}
