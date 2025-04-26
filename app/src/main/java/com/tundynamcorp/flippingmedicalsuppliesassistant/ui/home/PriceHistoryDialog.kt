package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

/**
 * @param editable      whether to enable per-price clicks and show Reset button
 * @param onPriceClick  invoked with the index 0..9 when a price cell is clicked (only if editable)
 * @param onReset       invoked when the Reset button is tapped (only shown if editable)
 */
@Composable
fun PriceHistoryDialog(
    title: String,
    lastUpdated: String,    // e.g. "10/22/2024"
    prices: List<Float>,    // exactly 10 values: price1 (highest) … price10 (lowest)
    editable: Boolean = false,
    onPriceClick: (index: Int) -> Unit = {},
    onReset: () -> Unit = {},
    onDismiss: () -> Unit
) {
    // Parse dates
    val inputFmt  = SimpleDateFormat("M/d/yyyy", Locale.US)
    val outputFmt = SimpleDateFormat("MM/yy", Locale.US)
    val baseDate: Date = try {
        inputFmt.parse(lastUpdated) ?: Date()
    } catch (_: Exception) {
        Date()
    }

    // Build labels: index 0 → +11 months … index 9 → +2 months
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
                // Top row: dates 0..4
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    dateLabels.take(5).forEach { Text(it, modifier = Modifier.padding(4.dp)) }
                }
                Spacer(Modifier.height(4.dp))
                // Top row: prices 0..4
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    prices.take(5).forEachIndexed { idx, price ->
                        Text(
                            text = "$${price.toInt()}",
                            modifier = Modifier
                                .padding(4.dp)
                                .then(
                                    if (editable) Modifier.clickable { onPriceClick(idx) }
                                    else Modifier
                                )
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                // Bottom row: dates 5..9
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    dateLabels.drop(5).forEach { Text(it, modifier = Modifier.padding(4.dp)) }
                }
                Spacer(Modifier.height(4.dp))
                // Bottom row: prices 5..9
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    prices.drop(5).forEachIndexed { dropIdx, price ->
                        val idx = dropIdx + 5
                        Text(
                            text = "$${price.toInt()}",
                            modifier = Modifier
                                .padding(4.dp)
                                .then(
                                    if (editable) Modifier.clickable { onPriceClick(idx) }
                                    else Modifier
                                )
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (editable) {
                TextButton(onClick = onReset) {
                    Text("Reset")
                }
            }
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}
