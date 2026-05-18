package com.ksp.sleeptracker.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.ksp.sleeptracker.MainActivity
import com.ksp.sleeptracker.R
import com.ksp.sleeptracker.SleepTrackerApp
import com.ksp.sleeptracker.data.model.SleepRecord
import com.ksp.sleeptracker.data.repository.PreferencesRepository
import com.ksp.sleeptracker.data.repository.SleepRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class SleepTrackingService : Service() {

    @Inject lateinit var prefs: PreferencesRepository
    @Inject lateinit var sleepRepo: SleepRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> handleStart()
            ACTION_STOP -> {
                handleStop()
                return START_NOT_STICKY
            }
            else -> handleStart()
        }
        return START_STICKY
    }

    private fun handleStart() {
        startForeground(NOTIFICATION_ID, buildNotification())
        scope.launch {
            val now = System.currentTimeMillis()
            val existing = prefs.sessionStart.first()
            if (existing == null) {
                prefs.setSessionStart(now)
            }
            prefs.setTrackingActive(true)
            // TODO: register accelerometer listeners + classify stages
        }
    }

    private fun handleStop() {
        scope.launch {
            val start = prefs.sessionStart.first() ?: System.currentTimeMillis()
            val end = System.currentTimeMillis()
            val totalMinutes = ((end - start) / 60_000L).toInt().coerceAtLeast(0)
            if (totalMinutes > 0) {
                val record = synthRecord(start, end, totalMinutes)
                sleepRepo.saveRecord(record)
            }
            prefs.setSessionStart(null)
            prefs.setTrackingActive(false)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun synthRecord(start: Long, end: Long, totalMinutes: Int): SleepRecord {
        // Heuristic split until real classification lands.
        val deep = (totalMinutes * 0.22f).roundToInt()
        val rem = (totalMinutes * 0.23f).roundToInt()
        val awake = (totalMinutes * 0.05f).roundToInt()
        val light = (totalMinutes - deep - rem - awake).coerceAtLeast(0)
        val score = ((totalMinutes / 480f) * 100f).coerceIn(0f, 100f).roundToInt()
        return SleepRecord(
            startTime = start,
            endTime = end,
            score = score,
            totalMinutes = totalMinutes,
            deepMinutes = deep,
            remMinutes = rem,
            lightMinutes = light,
            awakeMinutes = awake
        )
    }

    private fun buildNotification(): Notification {
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, SleepTrackerApp.CHANNEL_TRACKING)
            .setContentTitle("Sleep Tracker is tracking your sleep")
            .setContentText("Tap to view session")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.ksp.sleeptracker.action.START_TRACKING"
        const val ACTION_STOP = "com.ksp.sleeptracker.action.STOP_TRACKING"

        fun start(context: Context) {
            val intent = Intent(context, SleepTrackingService::class.java).apply {
                action = ACTION_START
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, SleepTrackingService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
