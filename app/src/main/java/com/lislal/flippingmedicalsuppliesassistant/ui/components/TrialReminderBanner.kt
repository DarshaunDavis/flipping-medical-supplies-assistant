package com.lislal.flippingmedicalsuppliesassistant.ui.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Shows a “paginated” reminder banner during trial:
 *  • Once at 7d (represented by 4 minutes)
 *  • Once at 14d (3 minutes) and 21d (2 minutes)
 *  • Then daily for the final 1 minute (1 minute)
 *
 * For testing, this uses a 5-minute trial. Swap back to 30 minutes (30 days) for production.
 */
@Composable
fun TrialReminderBanner(
    trialStart: Long?,
    onUpgradeClick: () -> Unit
) {
    if (trialStart == null) return

    // For testing: 5 minutes total (represents 5 days). In production, use 30L * 24*60*60*1000
    val windowMs = 5L * 60_000L
    val cutoff = remember(trialStart) { trialStart + windowMs }

    // Track “now” each minute
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(cutoff) {
        while (true) {
            now = System.currentTimeMillis()
            delay(60_000L) // tick every minute
        }
    }

    // Minutes left in trial (≥ 0)
    val minutesLeft = ((cutoff - now).coerceAtLeast(0L) / 60_000L)

    // Only show banner at our key “minute” marks:
    val showBanner = when (minutesLeft) {
        4L,       // 1 day before (7d stand-in)
        3L, 2L,   // 2 and 3 days before (14d & 21d stand-in)
        1L        // final day (1 minute stand-in)
            -> true
        else   -> false
    }
    if (!showBanner) return

    // Traffic-light background colors
    val backgroundColor = when (minutesLeft) {
        4L          -> Color(0xFF2E7D32) // dark green
        in 3L..2L   -> Color(0xFFF9A825) // dark yellow
        1L          -> Color(0xFFC62828) // dark red
        else        -> MaterialTheme.colorScheme.primary
    }

    Surface(
        color          = backgroundColor.copy(alpha = 0.1f),
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
            val message = if (minutesLeft > 1L) {
                "⏳ $minutesLeft minutes left in your free trial!"
            } else {
                "⚠️ $minutesLeft minute left – trial ends soon!"
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
                    text  = message,
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
