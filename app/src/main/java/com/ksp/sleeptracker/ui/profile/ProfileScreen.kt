package com.ksp.sleeptracker.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import android.app.Activity
import com.ksp.sleeptracker.billing.EntitlementGate
import com.ksp.sleeptracker.data.model.UserProfile
import com.ksp.sleeptracker.ui.theme.AccentIndigo
import com.ksp.sleeptracker.ui.theme.DMSerifDisplay
import com.ksp.sleeptracker.ui.theme.MidnightIndigo
import com.ksp.sleeptracker.ui.theme.NightNavy
import com.ksp.sleeptracker.ui.theme.SurfaceCard
import com.ksp.sleeptracker.ui.theme.TextMuted
import com.ksp.sleeptracker.ui.theme.TextPrimary

@Composable
fun ProfileScreen(
    vm: ProfileViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val activity = LocalContext.current as? Activity
    var pickingBedtime by remember { mutableStateOf(false) }
    var pickingWake by remember { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }
    var editingName by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(NightNavy)) {
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Header() }
            item {
                ProfileHeader(
                    profile = state.profile,
                    nightsTracked = state.nightsTracked,
                    avgScore = state.avgScore
                )
            }
            item {
                SettingsSection("Account") {
                    SettingsRow(
                        title = "Name",
                        value = state.profile?.name?.ifBlank { "Add your name" } ?: "Add your name",
                        onClick = { editingName = true }
                    )
                    RowDivider()
                    SettingsRow(
                        title = "Age range",
                        value = state.profile?.ageRange?.ifBlank { "Not set" } ?: "Not set"
                    )
                }
            }
            item {
                SettingsSection("Sleep goal") {
                    val goal = state.profile?.goalMinutes ?: 480
                    SettingsSlider(
                        title = "Nightly target",
                        value = goal,
                        range = 360f..600f,
                        steps = (600 - 360) / 30 - 1,
                        onChange = { v ->
                            vm.updateProfile { it.copy(goalMinutes = (v / 30) * 30) }
                        },
                        displayValue = "${goal / 60}h ${goal % 60}m"
                    )
                }
            }
            item {
                SettingsSection("Schedule") {
                    val p = state.profile
                    SettingsRow(
                        title = "Bedtime",
                        value = formatTime(p?.bedtimeHour ?: 22, p?.bedtimeMinute ?: 30),
                        onClick = { pickingBedtime = true }
                    )
                    RowDivider()
                    SettingsRow(
                        title = "Wake time",
                        value = formatTime(p?.wakeHour ?: 6, p?.wakeMinute ?: 0),
                        onClick = { pickingWake = true }
                    )
                    RowDivider()
                    SettingsSlider(
                        title = "Wind-down",
                        value = p?.windDownMinutes ?: 30,
                        range = 0f..60f,
                        steps = 11,
                        onChange = { v ->
                            vm.updateProfile { it.copy(windDownMinutes = (v / 5) * 5) }
                        },
                        displayValue = "${p?.windDownMinutes ?: 30} min"
                    )
                }
            }
            item {
                SettingsSection("Detection") {
                    val p = state.profile
                    SettingsToggleRow(
                        title = "Movement detection",
                        subtitle = "Use accelerometer to detect stages",
                        checked = p?.movementDetection ?: true,
                        onChange = { v -> vm.updateProfile { it.copy(movementDetection = v) } }
                    )
                    RowDivider()
                    SettingsToggleRow(
                        title = "Sound detection",
                        subtitle = "Listen for snoring during the night",
                        checked = p?.soundDetection ?: false,
                        onChange = { v -> vm.updateProfile { it.copy(soundDetection = v) } }
                    )
                    if (p?.soundDetection == true) {
                        RowDivider()
                        SettingsSlider(
                            title = "Snore sensitivity",
                            value = p.snoreSensitivity,
                            range = 0f..100f,
                            steps = 9,
                            onChange = { v -> vm.updateProfile { it.copy(snoreSensitivity = v) } },
                            displayValue = "${p.snoreSensitivity}"
                        )
                    }
                }
            }
            item {
                SettingsSection("Subscription") {
                    SubscriptionRows(
                        state = state.entitlement,
                        onOpenPaywall = { vm.openPaywall(activity) },
                        onRestore = { vm.restorePurchases() }
                    )
                }
            }
            item {
                SettingsSection("Danger zone") {
                    DangerRow(
                        title = "Reset all data",
                        subtitle = "Erase profile, nights, and onboarding",
                        onClick = { confirmReset = true }
                    )
                }
            }
        }
    }

    if (pickingBedtime) {
        val p = state.profile
        TimePickerDialog(
            initialHour = p?.bedtimeHour ?: 22,
            initialMinute = p?.bedtimeMinute ?: 30,
            onConfirm = { h, m ->
                vm.updateProfile { it.copy(bedtimeHour = h, bedtimeMinute = m) }
                pickingBedtime = false
            },
            onDismiss = { pickingBedtime = false }
        )
    }
    if (pickingWake) {
        val p = state.profile
        TimePickerDialog(
            initialHour = p?.wakeHour ?: 6,
            initialMinute = p?.wakeMinute ?: 0,
            onConfirm = { h, m ->
                vm.updateProfile { it.copy(wakeHour = h, wakeMinute = m) }
                pickingWake = false
            },
            onDismiss = { pickingWake = false }
        )
    }
    if (editingName) {
        NameEditDialog(
            initial = state.profile?.name.orEmpty(),
            onConfirm = { name ->
                vm.updateProfile { it.copy(name = name) }
                editingName = false
            },
            onDismiss = { editingName = false }
        )
    }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            containerColor = SurfaceCard,
            titleContentColor = TextPrimary,
            textContentColor = TextMuted,
            title = { Text("Reset all data?") },
            text = { Text("This wipes profile, nights, alarms-onboarding state. Cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.resetAllData()
                    confirmReset = false
                }) {
                    Text("Reset", color = com.ksp.sleeptracker.ui.theme.AccentRose)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun Header() {
    val title = buildAnnotatedString {
        append("Your ")
        withStyle(SpanStyle(fontStyle = FontStyle.Italic, fontFamily = DMSerifDisplay)) {
            append("profile")
        }
    }
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Personalize how Sleep Tracker tracks your rest.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCard,
        titleContentColor = TextPrimary,
        title = { Text("Pick a time") },
        text = {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                TimePicker(
                    state = state,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = MidnightIndigo,
                        clockDialSelectedContentColor = NightNavy,
                        clockDialUnselectedContentColor = TextPrimary,
                        selectorColor = AccentIndigo,
                        containerColor = SurfaceCard,
                        timeSelectorSelectedContainerColor = AccentIndigo,
                        timeSelectorUnselectedContainerColor = MidnightIndigo,
                        timeSelectorSelectedContentColor = NightNavy,
                        timeSelectorUnselectedContentColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text("Save", color = AccentIndigo)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

@Composable
private fun NameEditDialog(
    initial: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var value by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCard,
        titleContentColor = TextPrimary,
        title = { Text("Your name") },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                singleLine = true,
                textStyle = TextStyle(color = TextPrimary),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = AccentIndigo,
                    unfocusedBorderColor = MidnightIndigo,
                    cursorColor = AccentIndigo
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(value.trim()) }) {
                Text("Save", color = AccentIndigo)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

private fun formatTime(hour: Int, minute: Int): String = "%02d:%02d".format(hour, minute)

@Composable
fun SubscriptionRows(
    state: EntitlementGate,
    onOpenPaywall: () -> Unit,
    onRestore: () -> Unit
) {
    val (statusTitle, statusSubtitle, ctaLabel) = when (state) {
        EntitlementGate.Premium -> Triple("Premium", "Thanks for subscribing.", "Manage subscription")
        is EntitlementGate.Trial -> Triple(
            "Free trial active",
            "${state.daysLeft} day${if (state.daysLeft == 1) "" else "s"} left.",
            "Upgrade now"
        )
        EntitlementGate.Locked -> Triple("Trial ended", "Subscribe to continue.", "Unlock Premium")
        EntitlementGate.Loading -> Triple("Checking…", "Loading subscription status.", "Open paywall")
    }
    SettingsRow(title = statusTitle, subtitle = statusSubtitle)
    RowDivider()
    SettingsRow(title = ctaLabel, onClick = onOpenPaywall)
    if (state != EntitlementGate.Premium) {
        RowDivider()
        SettingsRow(title = "Restore purchases", onClick = onRestore)
    }
}
