package com.ksp.sleeptracker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ksp.sleeptracker.domain.usecase.DayPoint
import com.ksp.sleeptracker.ui.theme.AccentGreen
import com.ksp.sleeptracker.ui.theme.AccentIndigo
import com.ksp.sleeptracker.ui.theme.Divider
import com.ksp.sleeptracker.ui.theme.MidnightIndigo
import com.ksp.sleeptracker.ui.theme.TextMuted
import com.ksp.sleeptracker.ui.theme.TextPrimary
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SleepLineChart(
    points: List<DayPoint>,
    modifier: Modifier = Modifier,
    goalHours: Float = 8f
) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current

    val progress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(900, easing = EaseOutCubic))
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val totalLabel = AccentIndigo
    val deepLabel = AccentGreen
    val axisColor = Divider
    val mutedColor = TextMuted
    val primaryColor = TextPrimary
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = mutedColor, fontSize = 9.sp)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            LegendDot(totalLabel, "Total")
            Spacer(Modifier.width(12.dp))
            LegendDot(deepLabel, "Deep")
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .pointerInput(points) {
                        detectTapGestures { offset ->
                            if (points.isEmpty()) return@detectTapGestures
                            val leftPad = with(density) { 28.dp.toPx() }
                            val rightPad = with(density) { 8.dp.toPx() }
                            val w = size.width - leftPad - rightPad
                            val step = w / (points.size - 1).coerceAtLeast(1)
                            val raw = ((offset.x - leftPad) / step).roundToInt()
                            val clamped = raw.coerceIn(0, points.size - 1)
                            selectedIndex = if (selectedIndex == clamped) null else clamped
                        }
                    }
            ) {
                if (points.isEmpty()) return@Canvas

                val leftPad = 28.dp.toPx()
                val rightPad = 8.dp.toPx()
                val topPad = 12.dp.toPx()
                val bottomPad = 24.dp.toPx()
                val plotW = size.width - leftPad - rightPad
                val plotH = size.height - topPad - bottomPad

                val maxHours = (points.maxOf { it.totalHours }.coerceAtLeast(goalHours) + 1f)
                    .coerceAtLeast(4f)

                // Horizontal gridlines (4)
                for (i in 0..4) {
                    val y = topPad + plotH * (i / 4f)
                    drawLine(
                        color = axisColor,
                        start = Offset(leftPad, y),
                        end = Offset(size.width - rightPad, y),
                        strokeWidth = 1f
                    )
                    val hours = (maxHours * (1f - i / 4f)).roundToInt()
                    val tl = measurer.measure(
                        text = "${hours}h",
                        style = labelStyle
                    )
                    drawText(
                        tl,
                        topLeft = Offset(0f, y - tl.size.height / 2f)
                    )
                }

                // Goal line (dashed)
                val goalY = topPad + plotH * (1f - (goalHours / maxHours).coerceIn(0f, 1f))
                drawLine(
                    color = AccentGreen.copy(alpha = 0.4f),
                    start = Offset(leftPad, goalY),
                    end = Offset(size.width - rightPad, goalY),
                    strokeWidth = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                )

                val step = plotW / (points.size - 1).coerceAtLeast(1)
                fun xFor(i: Int) = leftPad + step * i
                fun yFor(h: Float) = topPad + plotH * (1f - (h / maxHours).coerceIn(0f, 1f))

                val visibleCount = (points.size * progress.value).roundToInt().coerceAtLeast(1)

                // Total line
                val totalPath = Path()
                val deepPath = Path()
                for (i in 0 until visibleCount) {
                    val x = xFor(i)
                    val yt = yFor(points[i].totalHours)
                    val yd = yFor(points[i].deepHours)
                    if (i == 0) {
                        totalPath.moveTo(x, yt)
                        deepPath.moveTo(x, yd)
                    } else {
                        totalPath.lineTo(x, yt)
                        deepPath.lineTo(x, yd)
                    }
                }
                drawPath(totalPath, color = totalLabel, style = Stroke(width = 3f))
                drawPath(deepPath, color = deepLabel, style = Stroke(width = 2.5f))

                // X-axis labels (max 7)
                val labelStep = (points.size / 7).coerceAtLeast(1)
                for (i in points.indices step labelStep) {
                    val tl = measurer.measure(
                        text = points[i].xLabel,
                        style = labelStyle
                    )
                    drawText(
                        tl,
                        topLeft = Offset(xFor(i) - tl.size.width / 2f, size.height - bottomPad + 4f)
                    )
                }

                // Selection markers + tooltip
                selectedIndex?.let { idx ->
                    if (idx in points.indices) {
                        val x = xFor(idx)
                        val yt = yFor(points[idx].totalHours)
                        val yd = yFor(points[idx].deepHours)
                        drawLine(
                            color = mutedColor.copy(alpha = 0.4f),
                            start = Offset(x, topPad),
                            end = Offset(x, topPad + plotH),
                            strokeWidth = 1f
                        )
                        drawCircle(totalLabel, radius = 5f, center = Offset(x, yt))
                        drawCircle(deepLabel, radius = 4f, center = Offset(x, yd))
                    }
                }
            }

            selectedIndex?.let { idx ->
                if (idx in points.indices) {
                    val p = points[idx]
                    val dateLabel = p.date.format(DateTimeFormatter.ofPattern("MMM d"))
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp, end = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MidnightIndigo)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Column {
                            Text(dateLabel, color = primaryColor, style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "Total ${formatHours(p.totalHours)}",
                                color = totalLabel,
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                "Deep ${formatHours(p.deepHours)}",
                                color = deepLabel,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(label, color = TextMuted, style = MaterialTheme.typography.labelSmall)
    }
}

private fun formatHours(hours: Float): String {
    val total = (hours * 60f).roundToInt()
    val h = total / 60
    val m = total % 60
    return when {
        h == 0 -> "${m}m"
        m == 0 -> "${h}h"
        else -> "${h}h ${m}m"
    }
}

private fun Float.toFixed(decimals: Int): String =
    "%.${decimals}f".format(if (abs(this) < 0.005f) 0f else this)
