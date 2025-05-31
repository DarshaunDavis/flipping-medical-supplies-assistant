package com.lislal.flippingmedicalsuppliesassistant.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Shows a live countdown of minutes/seconds remaining in a short
 * test trial (5 minutes for testing). As soon as the timer reaches 0,
 * we call `onActiveChanged(false)`, and this entire composable is meant
 * to be removed from the tree by its parent.
 *
 * @param trialStart      Timestamp (ms since epoch) from when the trial started.
 * @param onActiveChanged Callback invoked with “true” while timeLeft > 0, then “false” once expired.
 * @param modifier        Pass in any Modifier (e.g. .clickable { … }).
 */
@Composable
fun TrialCountdown(
    trialStart: Long?,
    onActiveChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    if (trialStart == null) return

    // ── FOR TESTING: 5‐minute window (5 * 60,000ms). In production, swap back to 30 days:
    // val windowMs = 30L * 24 * 60 * 60 * 1000L
    val windowMs = 5L * 60_000L
    val cutoff   = remember(trialStart) { trialStart + windowMs }

    // Compute “timeLeft” each tick (we’ll do this in a LaunchedEffect below).
    var timeLeft by remember { mutableLongStateOf(cutoff - System.currentTimeMillis()) }

    // Derive boolean “isActive” by checking if timeLeft > 0.
    val isActive = timeLeft > 0L

    // Notify parent of this active state on every change:
    // As soon as timeLeft hits zero, isActive becomes false, and this callback fires.
    LaunchedEffect(isActive) {
        onActiveChanged(isActive)
    }

    // Every second, update `timeLeft` until zero:
    LaunchedEffect(cutoff) {
        while (true) {
            val now = System.currentTimeMillis()
            val newLeft = (cutoff - now).coerceAtLeast(0L)
            if (newLeft == 0L) {
                timeLeft = 0L
                break
            }
            timeLeft = newLeft
            delay(1_000L)
        }
    }

    // Break down timeLeft into mm:ss
    val minutes = timeLeft / 60_000L
    val seconds = (timeLeft / 1_000L) % 60

    Row(modifier = modifier) {
        Text("🕒", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(4.dp))
        Text("$minutes m $seconds s", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (isActive) "left in trial" else "Trial expired",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}