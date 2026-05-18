package com.ksp.sleeptracker.ui.alarms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AlarmOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ksp.sleeptracker.data.model.Alarm
import com.ksp.sleeptracker.ui.components.AlarmCard
import com.ksp.sleeptracker.ui.components.SlumberCard
import com.ksp.sleeptracker.ui.util.rememberHapticTap
import com.ksp.sleeptracker.ui.theme.AccentIndigo
import com.ksp.sleeptracker.ui.theme.DMSerifDisplay
import com.ksp.sleeptracker.ui.theme.NightNavy
import com.ksp.sleeptracker.ui.theme.TextMuted
import com.ksp.sleeptracker.ui.theme.TextPrimary

@Composable
fun AlarmsScreen(
    vm: AlarmsViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val openFab = rememberHapticTap { vm.openEditor() }
    val openEditAlarm: (com.ksp.sleeptracker.data.model.Alarm) -> Unit = { alarm -> vm.openEditor(alarm) }

    Box(modifier = Modifier.fillMaxSize().background(NightNavy)) {
        if (state.alarms.isEmpty()) {
            EmptyState(onCreate = openFab)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 16.dp,
                    bottom = 140.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Header() }
                item { NextAlarmHint(state.alarms, vm) }
                items(state.alarms, key = { it.id }) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        onToggle = { enabled -> vm.toggle(alarm, enabled) },
                        onClick = { openEditAlarm(alarm) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }

        AddAlarmFab(
            onClick = openFab,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 104.dp)
        )

        state.editor?.let { editor ->
            AlarmEditorSheet(
                state = editor,
                onChange = { transform -> vm.updateEditor(transform) },
                onSave = { vm.saveEditor() },
                onDismiss = { vm.closeEditor() },
                onDelete = { vm.deleteEditing() }
            )
        }
    }
}

@Composable
private fun Header() {
    val title = buildAnnotatedString {
        append("Your ")
        withStyle(
            SpanStyle(fontStyle = FontStyle.Italic, fontFamily = DMSerifDisplay)
        ) { append("alarms") }
    }
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Smart wake within a light-sleep window.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )
    }
}

@Composable
private fun NextAlarmHint(alarms: List<Alarm>, vm: AlarmsViewModel) {
    val nextEntry = remember(alarms) {
        alarms.filter { it.isEnabled }
            .mapNotNull { alarm ->
                val fireAt = vm.nextFireDateTime(alarm) ?: return@mapNotNull null
                val label = vm.nextFireLabel(alarm) ?: return@mapNotNull null
                Triple(alarm, label, fireAt)
            }
            .minByOrNull { it.third }
    } ?: return
    val (alarm, label, _) = nextEntry
    SlumberCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text("Next alarm", color = TextMuted, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                "%02d:%02d".format(alarm.hour, alarm.minute),
                color = TextPrimary,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(label, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun AddAlarmFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(AccentIndigo)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add alarm",
            tint = NightNavy
        )
    }
}

@Composable
private fun EmptyState(onCreate: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.AlarmOff,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "No alarms yet",
            color = TextPrimary,
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Set a smart wake-up — Sleep Tracker will rouse you during light sleep.",
            color = TextMuted,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(Modifier.height(18.dp))
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(AccentIndigo)
                .clickable { onCreate() }
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                "Add alarm",
                color = NightNavy,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
