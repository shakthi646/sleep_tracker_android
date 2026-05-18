package com.ksp.sleeptracker.ui.alarm

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ksp.sleeptracker.service.AlarmReceiver
import com.ksp.sleeptracker.service.AlarmRingerService
import com.ksp.sleeptracker.ui.theme.AccentIndigo
import com.ksp.sleeptracker.ui.theme.NightNavy
import com.ksp.sleeptracker.ui.theme.SleepTrackerTheme
import com.ksp.sleeptracker.ui.theme.TextMuted
import com.ksp.sleeptracker.ui.theme.TextPrimary
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class AlarmAlertActivity : ComponentActivity() {

    private var alarmId: Int = -1
    private var label: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            km?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
        readExtras(intent)
        setContent {
            SleepTrackerTheme(darkTheme = true) {
                AlarmAlertScreen(
                    label = label.ifBlank { "Wake up" },
                    onDismiss = { dismiss() }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readExtras(intent)
    }

    private fun readExtras(intent: Intent?) {
        alarmId = intent?.getIntExtra(EXTRA_ALARM_ID, -1) ?: -1
        label = intent?.getStringExtra(EXTRA_LABEL).orEmpty()
    }

    private fun dismiss() {
        val stop = Intent(this, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_DISMISS
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }
        sendBroadcast(stop)
        startService(AlarmRingerService.stopIntent(this))
        finishAndRemoveTask()
    }

    @Suppress("OVERRIDE_DEPRECATION", "MissingSuperCall")
    override fun onBackPressed() {
        // ignore back; require explicit dismiss
    }

    companion object {
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_LABEL = "label"

        fun intent(context: Context, alarmId: Int, label: String): Intent =
            Intent(context, AlarmAlertActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra(EXTRA_ALARM_ID, alarmId)
                putExtra(EXTRA_LABEL, label)
            }
    }
}

@Composable
private fun AlarmAlertScreen(label: String, onDismiss: () -> Unit) {
    var now by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalTime.now()
            delay(1000)
        }
    }
    val timeText = now.format(DateTimeFormatter.ofPattern("HH:mm"))
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NightNavy)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(Modifier.weight(0.6f))
            Icon(
                imageVector = Icons.Filled.Alarm,
                contentDescription = null,
                tint = AccentIndigo,
                modifier = Modifier.size(72.dp)
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = timeText,
                style = MaterialTheme.typography.displayLarge,
                color = TextPrimary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                color = TextMuted
            )
            Spacer(Modifier.weight(1f))
            StopButton(onClick = onDismiss)
            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun StopButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(160.dp)
            .clip(CircleShape)
            .background(AccentIndigo)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Stop",
            style = MaterialTheme.typography.headlineMedium,
            color = NightNavy
        )
    }
}
