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
 *  • Once at 7d (represented by 24 minutes)
 *  • Once at 14d (17 minutes) and 21d (10 minutes)
 *  • Then daily for the final 7d (7..1 minutes)
 *
 * For testing, this uses minutes; swap back to days when ready.
 */
@Composable
fun TrialReminderBanner(
    trialStart: Long?,
    onUpgradeClick: () -> Unit
) {
    if (trialStart == null) return

    // 30-minute trial end for testing (30 days → 30 minutes)
    val cutoff = remember(trialStart) { trialStart + 30 * 60_000L }

    // Track “now” with optimized long-based state
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(cutoff) {
        while (true) {
            now = System.currentTimeMillis()
            delay(60_000L)  // tick every minute
        }
    }

    // Minutes left in trial, coerced ≥ 0
    val minutesLeft = ((cutoff - now).coerceAtLeast(0L) / 60_000L)

    // Only show at our key “minute” marks
    val showBanner = when (minutesLeft) {
        24L,      // 7d stand-in
        17L, 10L, // 14d & 21d stand-ins
        in 7L downTo 1L // final 7 days stand-in
            -> true
        else -> false
    }
    if (!showBanner) return

    // Traffic-light background colors
    val backgroundColor = when (minutesLeft) {
        24L              -> Color(0xFF2E7D32) // dark green
        17L, 10L         -> Color(0xFFF9A825) // dark yellow
        in 7L downTo 1L  -> Color(0xFFC62828) // dark red
        else             -> MaterialTheme.colorScheme.primary
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
            val message = if (minutesLeft > 7L) {
                "⏳ $minutesLeft minutes left in your free trial!"
            } else {
                "⚠️ $minutesLeft minutes left – trial ends soon!"
            }

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector      = Icons.Default.Info,
                    contentDescription= null,
                    tint             = Color.White,
                    modifier         = Modifier.size(20.dp)
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
