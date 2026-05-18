package com.ksp.sleeptracker.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ksp.sleeptracker.data.model.SleepRecord
import com.ksp.sleeptracker.ui.components.SleepScoreRing
import com.ksp.sleeptracker.ui.components.StagePill
import com.ksp.sleeptracker.ui.components.WeeklyBarChart
import com.ksp.sleeptracker.ui.theme.AccentAmber
import com.ksp.sleeptracker.ui.theme.AccentGreen
import com.ksp.sleeptracker.ui.theme.AccentIndigo
import com.ksp.sleeptracker.ui.theme.AccentRose
import com.ksp.sleeptracker.ui.theme.DMSerifDisplay
import com.ksp.sleeptracker.ui.theme.MidnightIndigo
import com.ksp.sleeptracker.ui.theme.NightNavy
import com.ksp.sleeptracker.ui.theme.SurfaceCard
import com.ksp.sleeptracker.ui.theme.TextMuted
import com.ksp.sleeptracker.ui.theme.TextPrimary

@Composable
fun HomeScreen(
    onOpenProfile: () -> Unit,
    onStartSleep: () -> Unit,
    vm: HomeViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val name = state.profile?.name.orEmpty()
    val greeting = greetingForNow(name)
    val sub = subLabelForNow(state.profile, state.latest)
    val latest = state.latest

    Box(modifier = Modifier.fillMaxSize().background(NightNavy)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 120.dp)
        ) {
            HomeHeader(
                greeting = greeting,
                subLabel = sub,
                initials = name.toInitials(),
                onAvatarClick = onOpenProfile
            )

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = !state.isLoading,
                enter = fadeIn() + expandVertically()
            ) {
                Column {
                    ScoreSection(latest = latest, onStartSleep = onStartSleep)
                    Spacer(modifier = Modifier.height(20.dp))
                    StagePillsRow(latest = latest)
                    Spacer(modifier = Modifier.height(24.dp))
                    WeeklySection(state = state)
                    Spacer(modifier = Modifier.height(24.dp))
                    TonightCard(profile = state.profile)
                    Spacer(modifier = Modifier.height(20.dp))
                    InsightChipsRow(latest = latest)
                    Spacer(modifier = Modifier.height(20.dp))
                    SmartAlarmCard(
                        primaryAlarm = state.primaryAlarm,
                        onToggle = { alarm, on -> vm.toggleSmartAlarm(alarm, on) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    greeting: GreetingParts,
    subLabel: String,
    initials: String,
    onAvatarClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildAnnotatedString {
                    append("${greeting.head} ")
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, fontFamily = DMSerifDisplay)) {
                        append(greeting.tail)
                    }
                    append(",\n${greeting.name}")
                },
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
            if (subLabel.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subLabel,
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MidnightIndigo)
                .clickable { onAvatarClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun ScoreSection(latest: SleepRecord?, onStartSleep: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (latest == null) {
                SleepScoreRing(score = 0, deepRatio = 0f, sizeDp = 160)
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "No sleep tracked yet",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Tap to start your first session.",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                SleepScoreRing(
                    score = latest.score,
                    deepRatio = if (latest.totalMinutes > 0) latest.deepMinutes / latest.totalMinutes.toFloat() else 0f,
                    sizeDp = 180
                )
                Spacer(modifier = Modifier.height(16.dp))
                val h = latest.totalMinutes / 60
                val m = latest.totalMinutes % 60
                Text(
                    text = "${h}h ${m}m total",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            StartSleepButton(onClick = onStartSleep)
        }
    }
}

@Composable
private fun StartSleepButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(28.dp))
            .background(AccentIndigo)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = TextPrimary)
        Spacer(modifier = Modifier.size(6.dp))
        Text("Start sleep", color = TextPrimary, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun StagePillsRow(latest: SleepRecord?) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            StagePill(
                label = "Light",
                duration = formatMinutes(latest?.lightMinutes ?: 0),
                dotColor = AccentIndigo
            )
        }
        item {
            StagePill(
                label = "Deep",
                duration = formatMinutes(latest?.deepMinutes ?: 0),
                dotColor = AccentGreen
            )
        }
        item {
            StagePill(
                label = "REM",
                duration = formatMinutes(latest?.remMinutes ?: 0),
                dotColor = AccentAmber
            )
        }
        item {
            StagePill(
                label = "Awake",
                duration = formatMinutes(latest?.awakeMinutes ?: 0),
                dotColor = AccentRose
            )
        }
    }
}

@Composable
private fun WeeklySection(state: HomeUiState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard)
            .padding(20.dp)
    ) {
        Column {
            Text(
                "This week",
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (state.weeklyBars.isEmpty()) {
                Text("No data yet — your bars will appear here.",
                    color = TextMuted, style = MaterialTheme.typography.bodyMedium)
            } else {
                val goal = (state.profile?.goalMinutes ?: 480) / 60f
                WeeklyBarChart(bars = state.weeklyBars, goalHours = goal)
            }
        }
    }
}

@Composable
private fun TonightCard(profile: com.ksp.sleeptracker.data.model.UserProfile?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScheduleSide(
                icon = Icons.Outlined.Bedtime,
                label = "Bedtime",
                value = profile?.let { formatHHMM(it.bedtimeHour, it.bedtimeMinute) } ?: "—",
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .height(1.dp)
                    .size(20.dp, 1.dp)
                    .background(TextMuted)
            )
            ScheduleSide(
                icon = Icons.Outlined.WbSunny,
                label = "Wake",
                value = profile?.let { formatHHMM(it.wakeHour, it.wakeMinute) } ?: "—",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ScheduleSide(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MidnightIndigo),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AccentIndigo)
        }
        Spacer(modifier = Modifier.size(12.dp))
        Column {
            Text(label, color = TextMuted, style = MaterialTheme.typography.labelMedium)
            Text(value, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun InsightChipsRow(latest: SleepRecord?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        InsightChip(
            icon = Icons.Outlined.Favorite,
            value = latest?.avgHeartRate?.let { "$it" } ?: "—",
            label = "BPM avg",
            modifier = Modifier.weight(1f)
        )
        InsightChip(
            icon = Icons.Outlined.Thermostat,
            value = "—",
            label = "Room",
            modifier = Modifier.weight(1f)
        )
        InsightChip(
            icon = Icons.Outlined.Timer,
            value = latest?.let { onsetLabel(it) } ?: "—",
            label = "Onset",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun InsightChip(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = AccentIndigo)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            value,
            color = TextPrimary,
            style = MaterialTheme.typography.headlineSmall
        )
        Text(label, color = TextMuted, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun SmartAlarmCard(
    primaryAlarm: com.ksp.sleeptracker.data.model.Alarm?,
    onToggle: (com.ksp.sleeptracker.data.model.Alarm, Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = primaryAlarm?.let { formatHHMM(it.hour, it.minute) } ?: "No alarm",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Text(
                    text = if (primaryAlarm?.smartAlarmEnabled == true)
                        "Smart alarm · wakes in light sleep"
                    else "Tap Alarms to schedule a wake-up",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (primaryAlarm != null) {
                Switch(
                    checked = primaryAlarm.isEnabled,
                    onCheckedChange = { onToggle(primaryAlarm, it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = TextPrimary,
                        checkedTrackColor = AccentIndigo
                    )
                )
            }
        }
    }
}

private fun formatMinutes(minutes: Int): String {
    if (minutes <= 0) return "0m"
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h == 0 -> "${m}m"
        m == 0 -> "${h}h"
        else -> "${h}h ${m}m"
    }
}

private fun formatHHMM(h: Int, m: Int): String {
    val ampm = if (h >= 12) "PM" else "AM"
    val hr = ((h + 11) % 12) + 1
    return "%d:%02d %s".format(hr, m, ampm)
}

private fun onsetLabel(record: SleepRecord): String {
    val duration = record.totalMinutes + record.awakeMinutes
    val onset = (duration * 0.05f).toInt().coerceAtLeast(8)
    return "${onset}m"
}

private fun String.toInitials(): String {
    val parts = trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "—"
        parts.size == 1 -> parts[0].take(1).uppercase()
        else -> (parts[0].take(1) + parts.last().take(1)).uppercase()
    }
}
