package com.ksp.sleeptracker.ui.components

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.ksp.sleeptracker.ui.theme.AccentGreen
import com.ksp.sleeptracker.ui.theme.AccentIndigo
import com.ksp.sleeptracker.ui.theme.Divider
import com.ksp.sleeptracker.ui.theme.ScoreGood
import com.ksp.sleeptracker.ui.theme.ScoreLow
import com.ksp.sleeptracker.ui.theme.ScoreMid
import com.ksp.sleeptracker.ui.theme.TextMuted

@Composable
fun SleepScoreRing(
    score: Int,
    deepRatio: Float,
    modifier: Modifier = Modifier,
    sizeDp: Int = 180
) {
    val scoreColor = when {
        score < 60 -> ScoreLow
        score < 76 -> ScoreMid
        else -> ScoreGood
    }
    val target = (score.coerceIn(0, 100)) / 100f
    val animatedProgress by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 1200, easing = EaseOutCubic),
        label = "ringProgress"
    )
    val animatedDeep by animateFloatAsState(
        targetValue = deepRatio.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1400, easing = EaseOutCubic),
        label = "deepRing"
    )

    Box(
        modifier = modifier.size(sizeDp.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp.dp)) {
            val strokeOuter = 14.dp.toPx()
            val strokeInner = 8.dp.toPx()
            val inset = strokeOuter / 2
            val outerSize = Size(size.width - strokeOuter, size.height - strokeOuter)
            val outerOffset = Offset(inset, inset)

            drawArc(
                color = Divider,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = outerOffset,
                size = outerSize,
                style = Stroke(width = strokeOuter, cap = StrokeCap.Round)
            )
            drawArc(
                color = scoreColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = outerOffset,
                size = outerSize,
                style = Stroke(width = strokeOuter, cap = StrokeCap.Round)
            )

            val innerInset = strokeOuter + 6.dp.toPx()
            val innerSize = Size(size.width - innerInset * 2, size.height - innerInset * 2)
            val innerOffset = Offset(innerInset, innerInset)
            drawArc(
                color = AccentGreen.copy(alpha = 0.5f),
                startAngle = -90f,
                sweepAngle = 360f * animatedDeep,
                useCenter = false,
                topLeft = innerOffset,
                size = innerSize,
                style = Stroke(width = strokeInner, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.displayMedium,
                color = scoreColor
            )
            Text(
                text = "SCORE",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}
