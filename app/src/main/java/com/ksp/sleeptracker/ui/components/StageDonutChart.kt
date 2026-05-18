package com.ksp.sleeptracker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.ksp.sleeptracker.domain.usecase.StageDistribution
import com.ksp.sleeptracker.ui.theme.StageAwake
import com.ksp.sleeptracker.ui.theme.StageDeep
import com.ksp.sleeptracker.ui.theme.StageLight
import com.ksp.sleeptracker.ui.theme.StageRem
import com.ksp.sleeptracker.ui.theme.TextMuted
import com.ksp.sleeptracker.ui.theme.TextPrimary
import kotlin.math.roundToInt

@Composable
fun StageDonutChart(
    distribution: StageDistribution,
    modifier: Modifier = Modifier
) {
    val total = distribution.totalMin.coerceAtLeast(1)
    val segments = listOf(
        Segment("Deep", distribution.deepMin, StageDeep),
        Segment("REM", distribution.remMin, StageRem),
        Segment("Light", distribution.lightMin, StageLight),
        Segment("Awake", distribution.awakeMin, StageAwake)
    )

    val sweep = remember { Animatable(0f) }
    LaunchedEffect(distribution) {
        sweep.snapTo(0f)
        sweep.animateTo(1f, tween(900, easing = EaseOutCubic))
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(140.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(140.dp)) {
                val stroke = 18.dp.toPx()
                val inset = stroke / 2f
                val arcSize = Size(size.width - stroke, size.height - stroke)
                val topLeft = Offset(inset, inset)
                var startAngle = -90f
                val anim = sweep.value
                for (seg in segments) {
                    val portion = seg.minutes.toFloat() / total
                    val angle = portion * 360f * anim
                    if (angle > 0f) {
                        drawArc(
                            color = seg.color,
                            startAngle = startAngle,
                            sweepAngle = angle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = stroke)
                        )
                    }
                    startAngle += portion * 360f
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val totalH = total / 60
                val totalM = total % 60
                Text(
                    if (totalH > 0) "${totalH}h ${totalM}m" else "${totalM}m",
                    color = TextPrimary,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text("Total", color = TextMuted, style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(Modifier.width(20.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (seg in segments) {
                LegendRow(seg, total)
            }
        }
    }
}

@Composable
private fun LegendRow(seg: Segment, total: Int) {
    val pct = (seg.minutes.toFloat() / total * 100f).roundToInt()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(seg.color)
        )
        Spacer(Modifier.width(8.dp))
        Text(seg.name, color = TextPrimary, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.width(8.dp))
        Text(
            "${pct}%",
            color = TextMuted,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(start = 0.dp)
        )
    }
}

private data class Segment(val name: String, val minutes: Int, val color: Color)
