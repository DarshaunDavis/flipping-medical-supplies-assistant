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

    // tick every minute
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(trialStart) {
        while (true) {
            delay(60_000L)
            now = System.currentTimeMillis()
        }
    }

    // compute elapsed / left (using minutes as stand-in for days)
    val minutesElapsed = ((now - trialStart) / 60_000L).coerceAtLeast(0L)
    val minutesLeft    = (30L - minutesElapsed).coerceAtLeast(0L)

    // fire at days 24, 17, 10 → minutesLeft == 24,17,10
    val showWeekly = minutesLeft == 24L ||
            minutesLeft == 17L ||
            minutesLeft == 10L

    // then each of the final 7 days → minutesLeft in 1..7
    val showFinalWeek = minutesLeft in 1L..7L

    Log.d("TrialReminderBanner", "elapsed=$minutesElapsed left=$minutesLeft " +
            "weekly=$showWeekly finalWeek=$showFinalWeek")

    if (!(showWeekly || showFinalWeek)) return

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
        Column(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onUpgradeClick)
                .padding(12.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter            = rememberVectorPainter(Icons.Default.Info),
                    contentDescription = null,
                    modifier           = Modifier.size(20.dp),
                    tint               = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text  = msg,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text     = "Upgrade",
                style    = MaterialTheme.typography.bodyMedium,
                color    = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable(onClick = onUpgradeClick)
            )
        }
    }
}
