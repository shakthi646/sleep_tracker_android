package com.ksp.sleeptracker.ui.onboarding

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryAlert
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.ksp.sleeptracker.ui.theme.AccentGreen
import com.ksp.sleeptracker.ui.theme.AccentIndigo
import com.ksp.sleeptracker.ui.theme.MidnightIndigo
import com.ksp.sleeptracker.ui.theme.NightNavy
import com.ksp.sleeptracker.ui.theme.SurfaceCard
import com.ksp.sleeptracker.ui.theme.TextMuted
import com.ksp.sleeptracker.ui.theme.TextPrimary

@Composable
fun PermissionsPage(
    vm: OnboardingViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        vm.onNotificationsGranted(hasNotificationPermission(context))
        vm.onActivityGranted(hasActivityPermission(context))
        vm.onBatteryExempt(isBatteryExempt(context))
    }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { vm.onNotificationsGranted(it) }

    val activityLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { vm.onActivityGranted(it) }

    val batteryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { vm.onBatteryExempt(isBatteryExempt(context)) }

    Box(modifier = Modifier.fillMaxSize().background(NightNavy)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "Almost there.",
                style = MaterialTheme.typography.headlineLarge.copy(fontStyle = FontStyle.Italic),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Grant these so Sleep Tracker can work its magic.",
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(28.dp))

            PermissionRow(
                icon = Icons.Outlined.NotificationsActive,
                title = "Notifications",
                description = "Bedtime reminders & smart alarms",
                granted = state.notificationsGranted,
                onGrant = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        vm.onNotificationsGranted(true)
                    }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            PermissionRow(
                icon = Icons.Outlined.DirectionsRun,
                title = "Physical Activity",
                description = "Detect sleep movement (optional)",
                granted = state.activityGranted,
                onGrant = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        activityLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                    } else {
                        vm.onActivityGranted(true)
                    }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            PermissionRow(
                icon = Icons.Outlined.BatteryAlert,
                title = "Battery Optimization",
                description = "Ensure alarms fire reliably",
                granted = state.batteryExempt,
                onGrant = {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    runCatching { batteryLauncher.launch(intent) }
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onNext,
                enabled = state.notificationsGranted,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentIndigo,
                    contentColor = TextPrimary,
                    disabledContainerColor = MidnightIndigo,
                    disabledContentColor = TextMuted
                )
            ) { Text("All set — let's go", style = MaterialTheme.typography.titleMedium) }
            TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Back", color = TextMuted)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PermissionRow(
    icon: ImageVector,
    title: String,
    description: String,
    granted: Boolean,
    onGrant: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .clickable(enabled = !granted) { onGrant() }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MidnightIndigo),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = AccentIndigo)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                Text(description, color = TextMuted, style = MaterialTheme.typography.bodySmall)
            }
            if (granted) {
                Icon(
                    Icons.Outlined.CheckCircle,
                    contentDescription = "Granted",
                    tint = AccentGreen
                )
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(AccentIndigo)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Grant", color = TextPrimary, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

private fun hasNotificationPermission(context: Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else true

private fun hasActivityPermission(context: Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED
    } else true

private fun isBatteryExempt(context: Context): Boolean {
    val pm = context.getSystemService<PowerManager>() ?: return false
    return pm.isIgnoringBatteryOptimizations(context.packageName)
}
