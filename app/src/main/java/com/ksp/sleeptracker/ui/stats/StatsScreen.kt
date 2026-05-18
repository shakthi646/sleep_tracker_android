package com.ksp.sleeptracker.ui.stats

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.clickable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.ksp.sleeptracker.domain.usecase.StatsData
import com.ksp.sleeptracker.domain.usecase.StatsPeriod
import com.ksp.sleeptracker.domain.usecase.formatDuration
import com.ksp.sleeptracker.ui.components.SleepDebtBar
import com.ksp.sleeptracker.ui.components.SleepLineChart
import com.ksp.sleeptracker.ui.components.SlumberCard
import com.ksp.sleeptracker.ui.components.StageDonutChart
import com.ksp.sleeptracker.ui.components.TrendCard
import com.ksp.sleeptracker.ui.theme.AccentAmber
import com.ksp.sleeptracker.ui.theme.AccentGreen
import com.ksp.sleeptracker.ui.theme.AccentIndigo
import com.ksp.sleeptracker.ui.theme.AccentRose
import com.ksp.sleeptracker.ui.theme.DMSerifDisplay
import com.ksp.sleeptracker.ui.theme.MidnightIndigo
import com.ksp.sleeptracker.ui.theme.NightNavy
import com.ksp.sleeptracker.ui.theme.TextMuted
import com.ksp.sleeptracker.ui.theme.TextPrimary

@Composable
fun StatsScreen(
    vm: StatsViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NightNavy)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 16.dp,
                bottom = 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Header() }
            item {
                PeriodTabRow(
                    selected = state.period,
                    onSelect = { vm.selectPeriod(it) }
                )
            }
            item {
                Crossfade(
                    targetState = state,
                    animationSpec = tween(300),
                    label = "statsCrossfade"
                ) { current ->
                    StatsBody(current)
                }
            }
        }
    }
}

@Composable
private fun Header() {
    val title = buildAnnotatedString {
        append("Your ")
        withStyle(
            SpanStyle(fontStyle = FontStyle.Italic, fontFamily = DMSerifDisplay)
        ) { append("rhythm") }
    }
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Trends and patterns across recent nights.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )
    }
}

@Composable
private fun PeriodTabRow(
    selected: StatsPeriod,
    onSelect: (StatsPeriod) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(MidnightIndigo)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (period in StatsPeriod.entries) {
            val active = period == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(if (active) AccentIndigo else androidx.compose.ui.graphics.Color.Transparent)
                    .clickable {
                        if (period != selected) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSelect(period)
                        }
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    period.label,
                    color = if (active) NightNavy else TextMuted,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun StatsBody(state: StatsUiState) {
    val data = state.data
    if (data == null || data.points.all { it.record == null }) {
        EmptyState()
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SlumberCard {
            Column {
                Text(
                    "Sleep duration",
                    color = TextMuted,
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(Modifier.height(8.dp))
                SleepLineChart(points = data.points)
            }
        }

        SlumberCard {
            Column {
                Text(
                    "Stage distribution",
                    color = TextMuted,
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(Modifier.height(12.dp))
                StageDonutChart(distribution = data.distribution)
            }
        }

        TrendsGrid(data)

        SleepDebtBar(
            debtMinutes = data.debtMinutes,
            goalTotalMinutes = data.period.days * data.goalMinutes
        )

        data.bestNight?.let { best ->
            SlumberCard {
                Column {
                    Text(
                        "Best night",
                        color = TextMuted,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "${best.totalMinutes.formatDuration()} • score ${best.score}",
                        color = TextPrimary,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Aim for nights like this one.",
                        color = TextMuted,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendsGrid(data: StatsData) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TrendCard(
                label = "Avg duration",
                value = data.avgTotalMinutes.formatDuration(),
                sub = "per night",
                accent = AccentIndigo,
                modifier = Modifier.weight(1f)
            )
            TrendCard(
                label = "Consistency",
                value = "${data.consistencyScore}",
                sub = "out of 100",
                accent = AccentGreen,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TrendCard(
                label = "Sleep onset",
                value = "${data.avgOnsetMin}m",
                sub = "avg to fall asleep",
                accent = AccentAmber,
                modifier = Modifier.weight(1f)
            )
            TrendCard(
                label = "Longest streak",
                value = "${data.longestStreakDays}",
                sub = if (data.longestStreakDays == 1) "day on goal" else "days on goal",
                accent = AccentRose,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EmptyState() {
    SlumberCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "No nights yet",
                color = TextPrimary,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Track a night to start seeing trends here.",
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
