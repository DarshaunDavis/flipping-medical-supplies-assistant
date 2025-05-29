package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Shows an upgrade‐now banner at 7, 14, 21 minutes left,
 * then every minute from 1–7.
 */
@Composable
fun TrialReminderBanner(
    trialStart: Long?,
    onUpgradeClick: () -> Unit
) {
    if (trialStart == null) return

    // 1) keep a clock that advances every minute
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(trialStart) {
        while (true) {
            delay(60_000L)                      // tick every 60s
            now = System.currentTimeMillis()
        }
    }

    // 2) compute elapsed/left
    val minutesElapsed = ((now - trialStart) / 60_000L).coerceAtLeast(0L)
    val minutesLeft    = (30L - minutesElapsed).coerceAtLeast(0L)

    // 3) decide when to show
    val showWeekly    = minutesLeft > 7 && minutesLeft % 7 == 0L
    val showFinalWeek = minutesLeft in 1L..7L

    Log.d("TrialReminderBanner", "elapsed=$minutesElapsed left=$minutesLeft " +
            "showWeekly=$showWeekly showFinalWeek=$showFinalWeek")

    if (!showWeekly && !showFinalWeek) return

    val msg = if (showWeekly) {
        "⏳ $minutesLeft minutes left in your free trial!"
    } else {
        "⚠️ $minutesLeft minutes left – trial ends soon!"
    }

    Surface(
        color          = MaterialTheme.colorScheme.primary.copy(alpha = .1f),
        modifier       = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        tonalElevation = 2.dp,
        shape          = MaterialTheme.shapes.small
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onUpgradeClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter            = rememberVectorPainter(Icons.Default.Info),
                contentDescription = null,
                modifier           = Modifier.size(20.dp),
                tint               = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
            Text(msg, color = MaterialTheme.colorScheme.onPrimary)
            Spacer(Modifier.weight(1f))
            Text(
                "Upgrade",
                color    = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onUpgradeClick)
            )
        }
    }
}
