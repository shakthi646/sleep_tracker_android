package com.ksp.sleeptracker.ui.onboarding

import android.app.TimePickerDialog
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.ksp.sleeptracker.ui.theme.AccentIndigo
import com.ksp.sleeptracker.ui.theme.Divider
import com.ksp.sleeptracker.ui.theme.MidnightIndigo
import com.ksp.sleeptracker.ui.theme.NightNavy
import com.ksp.sleeptracker.ui.theme.SurfaceCard
import com.ksp.sleeptracker.ui.theme.TextMuted
import com.ksp.sleeptracker.ui.theme.TextPrimary
import kotlin.math.PI
import kotlin.math.atan2

@Composable
fun GoalsPage(
    vm: OnboardingViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().background(NightNavy)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "What's your sleep goal?",
                style = MaterialTheme.typography.headlineLarge.copy(fontStyle = FontStyle.Italic),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pick a nightly target — you can change it anytime.",
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(28.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                DurationDial(
                    minutes = state.goalMinutes,
                    onMinutes = vm::onGoalMinutesChange
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            RecommendedChip()

            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TimeCard(
                    label = "Bedtime",
                    hour = state.bedtimeHour,
                    minute = state.bedtimeMinute,
                    modifier = Modifier.weight(1f)
                ) {
                    TimePickerDialog(context, { _, h, m -> vm.onBedtimeChange(h, m) },
                        state.bedtimeHour, state.bedtimeMinute, false).show()
                }
                TimeCard(
                    label = "Wake",
                    hour = state.wakeHour,
                    minute = state.wakeMinute,
                    modifier = Modifier.weight(1f)
                ) {
                    TimePickerDialog(context, { _, h, m -> vm.onWakeChange(h, m) },
                        state.wakeHour, state.wakeMinute, false).show()
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentIndigo,
                    contentColor = TextPrimary
                )
            ) { Text("Next", style = MaterialTheme.typography.titleMedium) }
            TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Back", color = TextMuted)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DurationDial(minutes: Int, onMinutes: (Int) -> Unit) {
    val minM = 360
    val maxM = 600
    val progress = (minutes - minM).coerceAtLeast(0) / (maxM - minM).toFloat()
    var angleState by remember { mutableFloatStateOf(progress * 360f) }
    angleState = progress * 360f

    Box(
        modifier = Modifier.size(260.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(260.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val dx = change.position.x - centerX
                        val dy = change.position.y - centerY
                        var angle = (atan2(dy, dx) * 180 / PI).toFloat() + 90
                        if (angle < 0) angle += 360f
                        val pct = angle / 360f
                        val newMin = (minM + pct * (maxM - minM)).toInt()
                        val snapped = (newMin / 30) * 30
                        onMinutes(snapped)
                    }
                }
        ) {
            val stroke = 18.dp.toPx()
            val inset = stroke / 2
            drawArc(
                color = Divider,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - stroke, size.height - stroke),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = AccentIndigo,
                startAngle = -90f,
                sweepAngle = angleState,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - stroke, size.height - stroke),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val h = minutes / 60
            val m = minutes % 60
            Text(
                text = if (m == 0) "${h}h" else "${h}h ${m}m",
                style = MaterialTheme.typography.displayMedium,
                color = TextPrimary
            )
            Text(
                text = "nightly target",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StepButton("−") { onMinutes(minutes - 30) }
                StepButton("+") { onMinutes(minutes + 30) }
            }
        }
    }
}

@Composable
private fun StepButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MidnightIndigo)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun RecommendedChip() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MidnightIndigo)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = "Experts recommend 7–9 hours",
            color = TextMuted,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun TimeCard(
    label: String,
    hour: Int,
    minute: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Column {
            Text(label, color = TextMuted, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = formatTime(hour, minute),
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
        }
    }
}

private fun formatTime(h: Int, m: Int): String {
    val ampm = if (h >= 12) "PM" else "AM"
    val hr = ((h + 11) % 12) + 1
    return "%d:%02d %s".format(hr, m, ampm)
}
