package com.ksp.sleeptracker.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.ksp.sleeptracker.R
import com.ksp.sleeptracker.SleepTrackerApp
import com.ksp.sleeptracker.data.model.Alarm
import com.ksp.sleeptracker.data.repository.AlarmRepository
import com.ksp.sleeptracker.ui.alarm.AlarmAlertActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var repo: AlarmRepository
    @Inject lateinit var scheduler: AlarmScheduler

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_FIRE -> handleFire(context, intent)
            ACTION_DISMISS -> handleDismiss(context, intent)
        }
    }

    private fun handleFire(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra(EXTRA_ALARM_ID, -1)
        if (alarmId < 0) return
        val pending = goAsync()
        scope.launch {
            try {
                val alarm = repo.getById(alarmId) ?: return@launch
                if (!alarm.isEnabled) return@launch
                startRinger(context, alarm)
                notify(context, alarm)
                launchAlertScreen(context, alarm)
                if (alarm.isOneTime()) {
                    repo.upsert(alarm.copy(isEnabled = false))
                } else {
                    scheduler.schedule(alarm)
                }
            } finally {
                pending.finish()
            }
        }
    }

    private fun handleDismiss(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra(EXTRA_ALARM_ID, -1)
        context.startService(AlarmRingerService.stopIntent(context))
        if (alarmId >= 0) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(NOTIFICATION_BASE + alarmId)
        }
    }

    private fun startRinger(context: Context, alarm: Alarm) {
        val starter = AlarmRingerService.startIntent(context, alarm.ringtoneUri, alarm.vibrate)
        context.startService(starter)
    }

    private fun launchAlertScreen(context: Context, alarm: Alarm) {
        val intent = AlarmAlertActivity.intent(context, alarm.id, alarm.label)
        try {
            context.startActivity(intent)
        } catch (_: Throwable) {
            // Foreground launch may be restricted; full-screen intent fallback handles it.
        }
    }

    private fun notify(context: Context, alarm: Alarm) {
        val id = alarm.id
        val label = alarm.label.ifBlank { "Wake up" }
        val alertIntent = AlarmAlertActivity.intent(context, alarm.id, alarm.label)
        val openIntent = PendingIntent.getActivity(
            context, id,
            alertIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val dismissPi = PendingIntent.getBroadcast(
            context, DISMISS_REQUEST_BASE + id,
            Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_DISMISS
                putExtra(EXTRA_ALARM_ID, id)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notif = NotificationCompat.Builder(context, SleepTrackerApp.CHANNEL_ALARM)
            .setContentTitle(label)
            .setContentText("Time to start the day.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(openIntent, true)
            .setContentIntent(openIntent)
            .setDeleteIntent(dismissPi)
            .setOngoing(true)
            .setAutoCancel(true)
            .addAction(0, "Dismiss", dismissPi)
            .build()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_BASE + id, notif)
    }

    companion object {
        const val EXTRA_ALARM_ID = "alarm_id"
        const val ACTION_FIRE = "com.ksp.sleeptracker.action.FIRE_ALARM"
        const val ACTION_DISMISS = "com.ksp.sleeptracker.action.DISMISS_ALARM"
        const val NOTIFICATION_BASE = 2000
        private const val DISMISS_REQUEST_BASE = 3000
    }
}
