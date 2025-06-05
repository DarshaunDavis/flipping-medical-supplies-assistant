// app/src/main/java/com/lislal/flippingmedicalsuppliesassistant/ui/components/TrialReminderBanner.kt
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
 * Shows a “paginated” reminder banner during a 30-day trial:
 *  • Once at 7 days before expiry (i.e. day 23 of trial) — green
 *  • Once at 14 days before expiry (i.e. day 16 of trial) — yellow
 *  • Once at 21 days before expiry (i.e. day 9 of trial) — yellow
 *  • Daily for the final 7 days (days 7..1) — red
 *
 * @param trialStart ms since epoch when trial began
 * @param onUpgradeClick lambda invoked when user taps “Upgrade”
 */
@Composable
fun TrialReminderBanner(
    trialStart: Long?,
    onUpgradeClick: () -> Unit
) {
    if (trialStart == null) return

    // 1) 30-day window in ms:
    val windowMs = 30L * 24 * 60 * 60 * 1000
    val cutoff   = remember(trialStart) { trialStart + windowMs }

    // 2) Track “now” each minute (we only care about day‐level granularity)
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(cutoff) {
        while (true) {
            now = System.currentTimeMillis()
            delay(60_000L) // tick every minute
        }
    }

    // 3) Days left in trial (coerced ≥ 0)
    val daysLeft = ((cutoff - now).coerceAtLeast(0L) / (24 * 60 * 60 * 1000L))

    // 4) Only show banner at our key “daysLeft” marks:
    val showBanner = when (daysLeft) {
        23L,               // 7 days before expiry (i.e. day #23 → 30 - 7 = 23)
        16L, 9L,           // 14 & 21 days before expiry (i.e. day #16 and #9)
        in 7L downTo 1L    // final 7 days
            -> true
        else -> false
    }
    if (!showBanner) return

    // 5) Traffic‐light background colors:
    val backgroundColor = when (daysLeft) {
        23L                  -> Color(0xFF2E7D32) // dark green
        16L, 9L              -> Color(0xFFF9A825) // dark yellow
        in 7L downTo 1L      -> Color(0xFFC62828) // dark red
        else                 -> MaterialTheme.colorScheme.primary
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
            val message = if (daysLeft > 1L) {
                "⏳ $daysLeft days left in your free trial!"
            } else {
                "⚠️ $daysLeft day left – trial ends soon!"
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
