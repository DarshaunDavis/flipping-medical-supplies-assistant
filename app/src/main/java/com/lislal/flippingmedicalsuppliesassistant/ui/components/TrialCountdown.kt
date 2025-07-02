// app/src/main/java/com/lislal/flippingmedicalsuppliesassistant/ui/components/TrialCountdown.kt
package com.lislal.flippingmedicalsuppliesassistant.ui.components

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
 *
 * @param trialStart      ms since epoch when trial began
 * @param onActiveChanged invoked with true while timeLeft > 0, and once when timeLeft hits 0
 * @param modifier        optional Modifier
 */
@Composable
fun TrialCountdown(
    trialStart: Long?,
    onActiveChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (trialStart == null) return

    // 1) 30-day window in ms:
    val windowMs = 30L * 24 * 60 * 60 * 1000
    val cutoff   = remember(trialStart) { trialStart + windowMs }

    // 2) Track timeLeft in ms
    var timeLeft by remember { mutableLongStateOf(cutoff - System.currentTimeMillis()) }

    // 3) Derive isActive flag
    val isActive = timeLeft > 0L

    // 4) Immediately emit “true” once upon first composition if still active.
    LaunchedEffect(Unit) {
        if (isActive) {
            onActiveChanged(true)
        }
    }

    // 5) Each time `isActive` changes, notify the host.
    LaunchedEffect(isActive) {
        onActiveChanged(isActive)
    }

    // 6) Tick every second until zero—but keep Composable visible even after expiry.
    LaunchedEffect(cutoff) {
        while (true) {
            val now     = System.currentTimeMillis()
            val newLeft = (cutoff - now).coerceAtLeast(0L)
            if (newLeft != timeLeft) {
                timeLeft = newLeft
            }
            if (timeLeft == 0L) {
                // final onActiveChanged(false) was already emitted above
                break
            }
            delay(1_000L)
        }
    }

    // 7) Convert timeLeft → days / hours / minutes / seconds
    val days    = timeLeft / (1000 * 60 * 60 * 24)
    val hours   = (timeLeft / (1000 * 60 * 60)) % 24
    val minutes = (timeLeft / (1000 * 60)) % 60
    val seconds = (timeLeft / 1000) % 60

    // 8) Render UI
    Row(modifier = modifier) {
        Text("🕒", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(4.dp))
        Text("${days}d", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(4.dp))
        Text("${hours}h", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(4.dp))
        Text("${minutes}m", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(4.dp))
        Text("${seconds}s", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(8.dp))
        Text(
            text  = if (isActive) "left in trial" else "Trial expired",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
