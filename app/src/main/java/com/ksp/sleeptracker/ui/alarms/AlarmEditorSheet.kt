package com.ksp.sleeptracker.ui.alarms

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.ksp.sleeptracker.ui.theme.AccentIndigo
import com.ksp.sleeptracker.ui.theme.MidnightIndigo
import com.ksp.sleeptracker.ui.theme.NightNavy
import com.ksp.sleeptracker.ui.theme.SurfaceCard
import com.ksp.sleeptracker.ui.theme.TextMuted
import com.ksp.sleeptracker.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditorSheet(
    state: AlarmEditorState,
    onChange: ((AlarmEditorState) -> AlarmEditorState) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val timeState = rememberTimePickerState(
        initialHour = state.hour,
        initialMinute = state.minute,
        is24Hour = true
    )
    LaunchedEffect(timeState.hour, timeState.minute) {
        onChange { it.copy(hour = timeState.hour, minute = timeState.minute) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceCard,
        contentColor = TextPrimary,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(TextMuted)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (state.original == null) "New alarm" else "Edit alarm",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )
                if (state.original != null && onDelete != null) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MidnightIndigo)
                            .clickable { onDelete() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "Delete alarm",
                            tint = Color(0xFFE76A8C)
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(
                    state = timeState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = MidnightIndigo,
                        clockDialSelectedContentColor = NightNavy,
                        clockDialUnselectedContentColor = TextPrimary,
                        selectorColor = AccentIndigo,
                        containerColor = SurfaceCard,
                        periodSelectorBorderColor = MidnightIndigo,
                        periodSelectorSelectedContainerColor = AccentIndigo,
                        periodSelectorUnselectedContainerColor = MidnightIndigo,
                        periodSelectorSelectedContentColor = NightNavy,
                        periodSelectorUnselectedContentColor = TextMuted,
                        timeSelectorSelectedContainerColor = AccentIndigo,
                        timeSelectorUnselectedContainerColor = MidnightIndigo,
                        timeSelectorSelectedContentColor = NightNavy,
                        timeSelectorUnselectedContentColor = TextPrimary
                    )
                )
            }

            DayPickerRow(
                selected = state.days,
                onToggle = { day ->
                    onChange { st ->
                        val next = st.days.toMutableSet().apply {
                            if (contains(day)) remove(day) else add(day)
                        }
                        st.copy(days = next)
                    }
                }
            )

            OutlinedTextField(
                value = state.label,
                onValueChange = { v -> onChange { it.copy(label = v) } },
                label = { Text("Label", color = TextMuted) },
                singleLine = true,
                textStyle = TextStyle(color = TextPrimary),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = AccentIndigo,
                    unfocusedBorderColor = MidnightIndigo,
                    cursorColor = AccentIndigo,
                    focusedLabelColor = AccentIndigo,
                    unfocusedLabelColor = TextMuted
                )
            )

            ToggleRow(
                title = "Smart alarm",
                subtitle = "Wakes up to ${state.smartWindowMinutes} min before set time",
                checked = state.smartAlarmEnabled,
                onChange = { v -> onChange { it.copy(smartAlarmEnabled = v) } }
            )
            ToggleRow(
                title = "Vibrate",
                subtitle = null,
                checked = state.vibrate,
                onChange = { v -> onChange { it.copy(vibrate = v) } }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(MidnightIndigo)
                        .clickable { onDismiss() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cancel", color = TextPrimary, style = MaterialTheme.typography.labelLarge)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(AccentIndigo)
                        .clickable { onSave() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Save", color = NightNavy, style = MaterialTheme.typography.labelLarge)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DayPickerRow(selected: Set<Int>, onToggle: (Int) -> Unit) {
    val labels = listOf("M" to 1, "T" to 2, "W" to 3, "T" to 4, "F" to 5, "S" to 6, "S" to 7)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for ((label, day) in labels) {
            val active = selected.contains(day)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (active) AccentIndigo else MidnightIndigo)
                    .clickable { onToggle(day) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (active) NightNavy else TextMuted,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String?,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(subtitle, color = TextMuted, style = MaterialTheme.typography.labelSmall)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextPrimary,
                checkedTrackColor = AccentIndigo,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = MidnightIndigo,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}
