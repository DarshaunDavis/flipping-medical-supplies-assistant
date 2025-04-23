// app/src/main/java/com/tundynamcorp/flippingmedicalsuppliesassistant/ui/admin/AdminScreen.kt
package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tundynamcorp.flippingmedicalsuppliesassistant.data.AdminViewModel

@Composable
fun AdminScreen(viewModel: AdminViewModel = viewModel()) {
    // 1) Observe the stored margin
    val margin by viewModel.profitMargin.collectAsState()

    // 2) Local UI state for the input field
    var input by remember { mutableStateOf("") }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Current Profit Margin: ${margin.toInt()}%")

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Set Profit Margin (%)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                input.toDoubleOrNull()?.let { pct ->
                    viewModel.setProfitMargin(pct)
                    input = ""
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Submit")
        }
    }
}
