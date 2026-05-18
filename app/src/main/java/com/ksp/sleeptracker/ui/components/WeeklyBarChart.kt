package com.ksp.sleeptracker.ui.components

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ksp.sleeptracker.ui.theme.AccentIndigo
import com.ksp.sleeptracker.ui.theme.Divider
import com.ksp.sleeptracker.ui.theme.TextMuted
import com.ksp.sleeptracker.ui.theme.TextPrimary
import kotlinx.coroutines.delay

data class DayBar(
    val label: String,
    val hours: Float,
    val isToday: Boolean = false,
    val isFuture: Boolean = false
)

@Composable
fun WeeklyBarChart(
    bars: List<DayBar>,
    modifier: Modifier = Modifier,
    onBarClick: (Int) -> Unit = {},
    goalHours: Float = 8f
) {
    val maxValue = (bars.maxOfOrNull { it.hours }?.coerceAtLeast(goalHours) ?: goalHours)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        bars.forEachIndexed { index, bar ->
            BarColumn(
                bar = bar,
                fraction = (bar.hours / maxValue).coerceIn(0f, 1f),
                animationDelay = index * 60L,
                onClick = { onBarClick(index) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BarColumn(
    bar: DayBar,
    fraction: Float,
    animationDelay: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var play by remember { mutableStateOf(false) }
    val animated by animateFloatAsState(
        targetValue = if (play) fraction else 0f,
        animationSpec = tween(durationMillis = 700, easing = EaseOutCubic),
        label = "bar"
    )
    LaunchedEffect(Unit) {
        delay(animationDelay)
        play = true
    }

    val barColor = when {
        bar.isToday -> AccentIndigo
        bar.isFuture -> Color.Transparent
        else -> AccentIndigo.copy(alpha = 0.45f)
    }

    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .clickable(enabled = !bar.isFuture) { onClick() }
                .width(20.dp)
                .height(140.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            if (bar.isFuture) {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Transparent)
                )
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(Divider)
                )
            } else {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .fillMaxHeight(animated)
                        .clip(RoundedCornerShape(10.dp))
                        .background(barColor)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = bar.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (bar.isToday) TextPrimary else TextMuted
        )
    }
}
