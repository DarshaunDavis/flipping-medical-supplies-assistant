package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

/**
 * Displays a grid of historical prices with month labels,
 * tailored to the number of points per category.
 *
 * @param title         product description for dialog title and device‐type detection
 * @param category      product category (e.g. "Test Strips", "Devices", "Misc", etc.)
 * @param imageUrl      optional image URL
 * @param lastUpdated   e.g. "10/22/2024"
 * @param prices        list of prices from newest (index=0) to oldest
 * @param editable      if true, cells are clickable and a “Reset” button is shown
 * @param onPriceClick  invoked with the original index when a cell is clicked
 * @param onReset       invoked when the Reset button is tapped
 * @param onDismiss     close callback
 */
@Composable
fun PriceHistoryDialog(
    title: String,
    category: String,
    imageUrl: String? = null,
    lastUpdated: String,
    prices: List<Float>,
    editable: Boolean = false,
    onPriceClick: (index: Int) -> Unit = {},
    onReset: () -> Unit = {},
    onDismiss: () -> Unit
) {
    // 1) parse the lastUpdated date
    val inputFmt  = SimpleDateFormat("M/d/yyyy", Locale.US)
    val outputFmt = SimpleDateFormat("MM/yy", Locale.US)
    val baseDate: Date = try {
        inputFmt.parse(lastUpdated) ?: Date()
    } catch (_: Exception) {
        Date()
    }

    // 2) determine how many points to show
    val maxPoints = when (category) {
        "Test Strips" -> 10
        "Devices" -> when {
            title.contains("Dexcom", true) -> 7
            title.contains("Omnipod", true)
                    || title.contains("Dash", true)
                    || title.contains("Cequr", true) -> 6
            title.contains("Libre", true) -> 4
            title.contains("Medtronic", true)
                    || title.contains("Guardian", true)
                    || title.contains("Mio", true)
                    || title.contains("Sure-T", true)
                    || title.contains("Silhouette", true) -> 9
            title.contains("Tandem", true)
                    || title.contains("TSlim", true)
                    || title.contains("TrueSteel", true) -> 9
            else -> prices.size
        }
        "Misc"      -> 3
        "Insulin"   -> 1
        "Tabs"      -> 1
        "Inhalers"  -> 1
        else        -> prices.size
    }
    val displayCount   = prices.size.coerceAtMost(maxPoints)
    val displayPrices  = prices.take(displayCount)

    // 3) build the date labels for only displayCount points
    val dateLabels: List<String> = List(displayCount) { i ->
        Calendar.getInstance().apply {
            time = baseDate
            add(Calendar.MONTH, 11 - i)
        }.let { outputFmt.format(it.time) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text  = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // image
                imageUrl?.let { url ->
                    AsyncImage(
                        model             = url,
                        contentDescription = title,
                        modifier          = Modifier
                            .size(120.dp)
                            .padding(bottom = 12.dp)
                    )
                }

                // top row (first up to 5 points)
                if (displayCount > 0) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        dateLabels.take(displayCount.coerceAtMost(5)).forEach { month ->
                            Text(month, Modifier.padding(4.dp))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        displayPrices.take(displayCount.coerceAtMost(5)).forEachIndexed { idx, price ->
                            Text(
                                text     = "$${price.toInt()}",
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

                // bottom row (for points beyond the first 5)
                if (displayCount > 5) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        dateLabels.drop(5).forEach { month ->
                            Text(month, Modifier.padding(4.dp))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        displayPrices.drop(5).forEachIndexed { dropIdx, price ->
                            val idx = dropIdx + 5
                            Text(
                                text     = "$${price.toInt()}",
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
