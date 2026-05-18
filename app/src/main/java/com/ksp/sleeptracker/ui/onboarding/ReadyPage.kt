package com.ksp.sleeptracker.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.ksp.sleeptracker.ui.components.StarfieldBackground
import com.ksp.sleeptracker.ui.theme.AccentIndigo
import com.ksp.sleeptracker.ui.theme.MidnightIndigo
import com.ksp.sleeptracker.ui.theme.NightNavy
import com.ksp.sleeptracker.ui.theme.SurfaceCard
import com.ksp.sleeptracker.ui.theme.TextMuted
import com.ksp.sleeptracker.ui.theme.TextPrimary
import kotlinx.coroutines.delay

@Composable
fun ReadyPage(
    vm: OnboardingViewModel,
    onFinish: () -> Unit
) {
    val state by vm.state.collectAsState()
    var visible by remember { mutableStateOf(BooleanArray(3) { false }) }

    LaunchedEffect(Unit) {
        listOf(120L, 260L, 400L).forEachIndexed { i, d ->
            delay(d)
            visible = visible.copyOf().also { it[i] = true }
        }
    }

    val goalH = state.goalMinutes / 60
    val goalM = state.goalMinutes % 60
    val goalText = if (goalM == 0) "${goalH}h" else "${goalH}h ${goalM}m"
    val bedtime = formatHHMM(state.bedtimeHour, state.bedtimeMinute)
    val wake = formatHHMM(state.wakeHour, state.wakeMinute)
    val name = state.name.ifBlank { "friend" }

    Box(modifier = Modifier.fillMaxSize().background(NightNavy)) {
        StarfieldBackground(modifier = Modifier.fillMaxSize(), starCount = 90)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(72.dp))
            Text(
                text = "You're all set, $name 🌙",
                style = MaterialTheme.typography.headlineLarge.copy(fontStyle = FontStyle.Italic),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sleep Tracker is tuned to your rhythm.",
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(40.dp))

            AnimatedVisibility(
                visible = visible[0],
                enter = slideInVertically(animationSpec = tween(500)) { it / 2 } + fadeIn()
            ) { StatCard(Icons.Outlined.NightsStay, "Your goal", goalText) }
            Spacer(modifier = Modifier.height(12.dp))
            AnimatedVisibility(
                visible = visible[1],
                enter = slideInVertically(animationSpec = tween(500)) { it / 2 } + fadeIn()
            ) { StatCard(Icons.Outlined.Bedtime, "Bedtime", bedtime) }
            Spacer(modifier = Modifier.height(12.dp))
            AnimatedVisibility(
                visible = visible[2],
                enter = slideInVertically(animationSpec = tween(500)) { it / 2 } + fadeIn()
            ) { StatCard(Icons.Outlined.Alarm, "Alarm", wake) }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { vm.finishOnboarding(onFinish) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentIndigo,
                    contentColor = TextPrimary
                )
            ) {
                Text("Start tracking tonight", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StatCard(icon: ImageVector, label: String, value: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MidnightIndigo),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = AccentIndigo)
            }
            Column {
                Text(label, color = TextMuted, style = MaterialTheme.typography.labelMedium)
                Text(value, color = TextPrimary, style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}

private fun formatHHMM(h: Int, m: Int): String {
    val ampm = if (h >= 12) "PM" else "AM"
    val hr = ((h + 11) % 12) + 1
    return "%d:%02d %s".format(hr, m, ampm)
}
