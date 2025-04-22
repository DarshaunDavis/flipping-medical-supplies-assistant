package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PriceHistoryDialog(
    title: String,
    lastUpdated: String,    // e.g. "10/22/2024"
    prices: List<Float>,    // exactly 10 values: price1 (highest) … price10 (lowest)
    onDismiss: () -> Unit
) {
    // Parse and build the 10 month labels, furthest first
    val inputFmt  = SimpleDateFormat("M/d/yyyy", Locale.US)
    val outputFmt = SimpleDateFormat("MM/yy", Locale.US)
    val baseDate: Date = try {
        inputFmt.parse(lastUpdated) ?: Date()
    } catch (_: Exception) {
        Date()
    }

    //    Build labels so that:
    //    index 0 → baseDate + 11 months  (label above price1)
    //    index 9 → baseDate +  2 months  (label above price10)
    val dateLabels = List(prices.size) { i ->
        Calendar.getInstance().apply {
            time = baseDate
            add(Calendar.MONTH, 11 - i)
        }.let { outputFmt.format(it.time) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top row: labels for price1..price5 (i=0..4)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    dateLabels.take(5).forEach { Text(it, modifier = Modifier.padding(4.dp)) }
                }
                Spacer(Modifier.height(4.dp))
                // Top row of prices (original order)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    prices.take(5).forEach { price ->
                        Text("$${price.toInt()}", modifier = Modifier.padding(4.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                // Bottom row: labels for price6..price10 (i=5..9)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    dateLabels.drop(5).forEach { Text(it, modifier = Modifier.padding(4.dp)) }
                }
                Spacer(Modifier.height(4.dp))
                // Bottom row of prices (original order)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    prices.drop(5).forEach { price ->
                        Text("$${price.toInt()}", modifier = Modifier.padding(4.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}
