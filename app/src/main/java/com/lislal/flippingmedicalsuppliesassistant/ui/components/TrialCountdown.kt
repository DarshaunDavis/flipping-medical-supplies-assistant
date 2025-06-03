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
 * Shows a live countdown of minutes/seconds remaining
 * in a 5-minute trial (for testing) that started at [trialStart].
 *
 * Even after it hits zero, this UI remains visible.
 *
 * @param trialStart      ms since epoch when trial began
 * @param onActiveChanged invoked with true while timeLeft>0, then false once expired
 * @param modifier        optional Modifier
 */
@Composable
fun TrialCountdown(
    trialStart: Long?,
    onActiveChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    if (trialStart == null) return

    // 5-minute window (for testing). Swap back to 30L * 24*60*60*1000 for production.
    val windowMs = 5L * 60_000L
    val cutoff   = remember(trialStart) { trialStart + windowMs }

    // Track timeLeft (ms)
    var timeLeft by remember { mutableLongStateOf(cutoff - System.currentTimeMillis()) }

    // Derive active flag
    val isActive = timeLeft > 0L

    // Notify host whenever `isActive` changes
    LaunchedEffect(isActive) {
        onActiveChanged(isActive)
    }

    // Tick every second until zero, but keep the Composable visible even if expired
    LaunchedEffect(cutoff) {
        while (true) {
            val now = System.currentTimeMillis()
            timeLeft = (cutoff - now).coerceAtLeast(0L)
            if (timeLeft == 0L) break
            delay(1_000L)
        }
    }

    // Convert timeLeft → minutes & seconds
    val minutes = timeLeft / 60_000L
    val seconds = (timeLeft / 1_000L) % 60

    Row(modifier = modifier) {
        Text("🕒", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(4.dp))
        Text("${minutes}m", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(4.dp))
        Text("${seconds}s", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (isActive) "left in trial" else "Trial expired",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
