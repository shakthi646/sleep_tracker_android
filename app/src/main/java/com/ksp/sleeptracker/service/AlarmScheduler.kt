package com.ksp.sleeptracker.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.ksp.sleeptracker.data.model.Alarm
import com.ksp.sleeptracker.data.repository.AlarmRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repo: AlarmRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alarm: Alarm) {
        cancel(alarm)
        if (!alarm.isEnabled) return
        val next = nextFireTime(alarm, LocalDateTime.now()) ?: return
        val triggerAt = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val pi = pendingIntent(alarm.id)
        val show = PendingIntent.getActivity(
            context, alarm.id,
            Intent(context, com.ksp.sleeptracker.MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        try {
            if (canUseExactAlarms()) {
                manager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAt, show), pi)
            } else {
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        } catch (se: SecurityException) {
            Log.w(TAG, "Exact alarm denied, falling back to inexact", se)
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    fun cancel(alarm: Alarm) {
        manager.cancel(pendingIntent(alarm.id))
    }

    fun rescheduleAll() {
        scope.launch {
            val enabled = repo.getEnabled()
            for (a in enabled) schedule(a)
        }
    }

    private fun canUseExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            manager.canScheduleExactAlarms()
        } else true
    }

    private fun pendingIntent(id: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_FIRE
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, id)
        }
        return PendingIntent.getBroadcast(
            context, id, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun nextFireTime(alarm: Alarm, now: LocalDateTime): LocalDateTime? {
        val intended = intendedFireTime(alarm, now) ?: return null
        val shift = if (alarm.smartAlarmEnabled) alarm.smartWindowMinutes.toLong() else 0L
        return intended.minusMinutes(shift)
    }

    fun intendedFireTime(alarm: Alarm, now: LocalDateTime): LocalDateTime? {
        val time = LocalTime.of(alarm.hour, alarm.minute)
        val today = now.toLocalDate()
        val activeDays = alarm.activeDays()

        if (activeDays.isEmpty()) {
            val candidate = LocalDateTime.of(today, time)
            return if (candidate.isAfter(now)) candidate
            else LocalDateTime.of(today.plusDays(1), time)
        }
        for (offset in 0..7) {
            val date: LocalDate = today.plusDays(offset.toLong())
            val dayOfWeek = date.dayOfWeek.value
            if (dayOfWeek in activeDays) {
                val candidate = LocalDateTime.of(date, time)
                if (candidate.isAfter(now)) return candidate
            }
        }
        return null
    }

    companion object {
        private const val TAG = "AlarmScheduler"
    }
}

fun DayOfWeek.toMondayBased(): Int = this.value
