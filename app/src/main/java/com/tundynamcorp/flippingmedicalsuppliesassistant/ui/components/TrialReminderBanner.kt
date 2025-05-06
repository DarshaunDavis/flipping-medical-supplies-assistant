package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp

@Composable
fun TrialReminderBanner(
    trialStart: Long?,
    now: Long = System.currentTimeMillis(),
    onUpgradeClick: () -> Unit
) {
    if (trialStart == null) return

    val daysElapsed = ((now - trialStart) / 86_400_000L).coerceAtLeast(0L)
    val daysLeft    = (30L - daysElapsed).coerceAtLeast(0L)

    val showWeekly = daysLeft > 7 && daysLeft % 7 == 0L
    val showFinalWeek = daysLeft in 1L..7L

    if (!showWeekly && !showFinalWeek) return

    val msg = if (showWeekly) {
        "⏳ $daysLeft days left in your free trial!"
    } else {
        "⚠️ $daysLeft days left – trial ends soon!"
    }

        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = .1f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 8.dp),
            tonalElevation = 2.dp,
            shape = MaterialTheme.shapes.small
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onUpgradeClick)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = rememberVectorPainter(Icons.Default.Info),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    msg,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "Upgrade",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable(onClick = onUpgradeClick)
                )
            }
        }
    }
