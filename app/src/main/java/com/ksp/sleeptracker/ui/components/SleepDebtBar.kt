package com.ksp.sleeptracker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ksp.sleeptracker.domain.usecase.formatDuration
import com.ksp.sleeptracker.ui.theme.AccentGreen
import com.ksp.sleeptracker.ui.theme.AccentRose
import com.ksp.sleeptracker.ui.theme.LocalSpacing
import com.ksp.sleeptracker.ui.theme.MidnightIndigo
import com.ksp.sleeptracker.ui.theme.SurfaceCard
import com.ksp.sleeptracker.ui.theme.TextMuted
import com.ksp.sleeptracker.ui.theme.TextPrimary
import kotlin.math.abs

@Composable
fun SleepDebtBar(
    debtMinutes: Int,
    goalTotalMinutes: Int,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val isAhead = debtMinutes >= 0
    val accent = if (isAhead) AccentGreen else AccentRose
    val absDebt = abs(debtMinutes).coerceAtMost(goalTotalMinutes.coerceAtLeast(1))
    val ratio = absDebt.toFloat() / goalTotalMinutes.coerceAtLeast(1).toFloat()

    val animated = remember { Animatable(0f) }
    LaunchedEffect(debtMinutes) {
        animated.snapTo(0f)
        animated.animateTo(ratio.coerceIn(0f, 1f), tween(800, easing = EaseOutCubic))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(spacing.cardRadius))
            .background(SurfaceCard)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Sleep ${if (isAhead) "surplus" else "debt"}",
                    color = TextMuted,
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(Modifier.fillMaxWidth(0f))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    Text(
                        "${if (isAhead) "+" else "-"}${abs(debtMinutes).formatDuration()}",
                        color = accent,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MidnightIndigo)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animated.value)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(accent)
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                if (isAhead) "You're ahead of your weekly goal." else "Try to add this back across upcoming nights.",
                color = TextPrimary,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
