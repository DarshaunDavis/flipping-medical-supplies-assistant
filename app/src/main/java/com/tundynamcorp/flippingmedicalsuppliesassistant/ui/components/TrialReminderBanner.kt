package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Shows a “paginated” reminder banner during trial:
 *  • Once at  7d (→ 70 seconds into the 5-min test window)
 *  • Once at 14d (→ 140s) & 21d (→ 210s)
 *  • Then daily for final 7d (→ 70..10s)
 *
 * For testing, this uses seconds; swap back to minutes when ready.
 */
@Composable
fun TrialReminderBanner(
    trialStart: Long?,
    onUpgradeClick: () -> Unit
) {
    if (trialStart == null) return

    // 5-minute cutoff for testing (30 days → 5 minutes)
    // and 10 seconds = 1 day
    val cutoff = remember(trialStart) { trialStart + 5 * 60_000L }

    // Track “now” with long‐based state
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(cutoff) {
        while (true) {
            now = System.currentTimeMillis()
            delay(10_000L)  // tick every 10 seconds (≈ 1 day)
        }
    }

    // Seconds left in trial, coerced ≥ 0
    val secLeft = (cutoff - now).coerceAtLeast(0L) / 1_000L

    // Compute “daysLeft” in test‐sense: 1 day = 10 seconds
    val daysLeft = (secLeft / 10L).coerceAtLeast(0L)

    // Only show at our key “day” marks
    val showBanner = when (daysLeft) {
        7L,         // 7 days stand-in (70s)
        14L, 21L,   // 14d & 21d (140s,210s)
        in 1L..7L   // final 7 days (70s down to 10s)
            -> true
        else -> false
    }
    if (!showBanner) return

    // Traffic-light colors
    val background = when (daysLeft) {
        7L          -> Color(0xFF2E7D32) // dark green
        14L,21L     -> Color(0xFFF9A825) // dark yellow
        in 1L..7L   -> Color(0xFFC62828) // dark red
        else        -> MaterialTheme.colorScheme.primary
    }

    Surface(
        color          = background.copy(alpha = 0.1f),
        contentColor   = Color.White,
        tonalElevation = 4.dp,
        shape          = MaterialTheme.shapes.small,
        modifier       = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .clickable(onClick = onUpgradeClick)
                .padding(16.dp)
        ) {
            val msg = if (daysLeft > 7L) {
                "⏳ $daysLeft days left in your free trial!"
            } else {
                "⚠️ $daysLeft days left – trial ends soon!"
            }

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector       = Icons.Default.Info,
                    contentDescription= null,
                    tint              = Color.White,
                    modifier          = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text  = msg,
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text     = "Upgrade",
                style    = MaterialTheme.typography.bodyMedium.copy(
                    color      = Color.White,
                    fontWeight = MaterialTheme.typography.bodyMedium.fontWeight
                ),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
