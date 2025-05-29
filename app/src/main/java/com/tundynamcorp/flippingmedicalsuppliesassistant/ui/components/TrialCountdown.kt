package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Shows a live countdown of minutes/seconds remaining
 * in a 30-minute trial that started at [trialStart] (ms since epoch).
 */
@Composable
fun TrialCountdown(
    trialStart: Long?,
    modifier: Modifier = Modifier
) {
    if (trialStart == null) return

    // now a 30-minute trial instead of 30 days
    val cutoff = remember(trialStart) {
        trialStart + 30L * 60 * 1000
    }

    var timeLeft by remember { mutableLongStateOf(cutoff - System.currentTimeMillis()) }

    LaunchedEffect(cutoff) {
        while (true) {
            val now = System.currentTimeMillis()
            timeLeft = (cutoff - now).coerceAtLeast(0L)
            if (timeLeft == 0L) break
            delay(1_000L)
        }
    }

    // Compute minutes / seconds (days & hours will always be zero here)
    val days    = timeLeft / (1000 * 60 * 60 * 24)    // always 0
    val hours   = (timeLeft / (1000 * 60 * 60)) % 24  // always 0
    val minutes = (timeLeft / (1000 * 60)) % 60
    val seconds = (timeLeft / 1000) % 60

    Row(modifier = modifier) {
        Text("🕒 ", style = MaterialTheme.typography.bodyMedium)
        // You can omit days/hours if you like; here they’ll simply read “0d 0h”
        Text("${days}d",     style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(4.dp))
        Text("${hours}h",    style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(4.dp))
        Text("${minutes}m",  style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(4.dp))
        Text("${seconds}s",  style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(4.dp))
        Text("left in trial", style = MaterialTheme.typography.bodyMedium)
    }
}
