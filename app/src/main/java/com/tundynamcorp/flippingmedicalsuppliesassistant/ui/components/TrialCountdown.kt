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
 * Shows a live countdown of days/hours/minutes/seconds remaining
 * in a 30-day trial that started at [trialStart] (ms since epoch).
 */
@Composable
fun TrialCountdown(
    trialStart: Long?,
    modifier: Modifier = Modifier
) {
    if (trialStart == null) return

    // Compute the trial cutoff timestamp
    val cutoff = remember(trialStart) {
        trialStart + 30L * 24 * 60 * 60 * 1000
    }

    // timeLeft will tick down every second
    var timeLeft by remember { mutableLongStateOf(cutoff - System.currentTimeMillis()) }

    LaunchedEffect(cutoff) {
        while (true) {
            val now = System.currentTimeMillis()
            timeLeft = (cutoff - now).coerceAtLeast(0L)
            if (timeLeft == 0L) break
            delay(1_000L)
        }
    }

    // Break into days / hours / minutes / seconds
    val days    = timeLeft / (1000 * 60 * 60 * 24)
    val hours   = (timeLeft / (1000 * 60 * 60)      ) % 24
    val minutes = (timeLeft / (1000 * 60)            ) % 60
    val seconds = (timeLeft / 1000                   ) % 60

    Row(modifier = modifier) {
        Text(
            text = "🕒 ",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "${days}d",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "${hours}h",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "${minutes}m",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "${seconds}s",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "left in trial",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
