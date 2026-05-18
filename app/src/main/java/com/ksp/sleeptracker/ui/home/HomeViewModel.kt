package com.ksp.sleeptracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ksp.sleeptracker.data.model.Alarm
import com.ksp.sleeptracker.data.model.SleepRecord
import com.ksp.sleeptracker.data.model.UserProfile
import com.ksp.sleeptracker.data.repository.AlarmRepository
import com.ksp.sleeptracker.service.AlarmScheduler
import com.ksp.sleeptracker.data.repository.ProfileRepository
import com.ksp.sleeptracker.data.repository.SleepRepository
import com.ksp.sleeptracker.domain.usecase.GetWeeklySleepUseCase
import com.ksp.sleeptracker.ui.components.DayBar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject
import kotlin.math.abs

data class HomeUiState(
    val profile: UserProfile? = null,
    val latest: SleepRecord? = null,
    val weeklyBars: List<DayBar> = emptyList(),
    val alarms: List<Alarm> = emptyList(),
    val isLoading: Boolean = true
) {
    val primaryAlarm: Alarm? get() = alarms.minByOrNull { it.hour * 60 + it.minute }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    profileRepo: ProfileRepository,
    sleepRepo: SleepRepository,
    private val alarmRepo: AlarmRepository,
    private val scheduler: AlarmScheduler,
    weeklyUseCase: GetWeeklySleepUseCase
) : ViewModel() {

    val state: StateFlow<HomeUiState> = combine(
        profileRepo.observe(),
        sleepRepo.observeLatest(),
        weeklyUseCase(),
        alarmRepo.observeAll()
    ) { profile, latest, bars, alarms ->
        HomeUiState(
            profile = profile,
            latest = latest,
            weeklyBars = bars,
            alarms = alarms,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun toggleSmartAlarm(alarm: Alarm, enabled: Boolean) {
        viewModelScope.launch {
            val updated = alarm.copy(isEnabled = enabled)
            alarmRepo.upsert(updated)
            if (enabled) scheduler.schedule(updated) else scheduler.cancel(updated)
        }
    }
}

fun greetingForNow(name: String, now: LocalDateTime = LocalDateTime.now()): GreetingParts {
    val hour = now.hour
    val (head, tail) = when (hour) {
        in 5..11 -> "Good" to "morning"
        in 12..16 -> "Good" to "afternoon"
        in 17..21 -> "Good" to "evening"
        else -> "Good" to "night"
    }
    val display = name.ifBlank { "there" }
    return GreetingParts(head = head, tail = tail, name = display)
}

data class GreetingParts(val head: String, val tail: String, val name: String)

fun subLabelForNow(
    profile: UserProfile?,
    latest: SleepRecord?,
    now: LocalDateTime = LocalDateTime.now()
): String {
    if (profile == null) return ""
    val bedtime = LocalTime.of(profile.bedtimeHour, profile.bedtimeMinute)
    val wake = LocalTime.of(profile.wakeHour, profile.wakeMinute)
    val nowT = now.toLocalTime()

    val woke = latest?.let {
        val end = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(it.endTime),
            java.time.ZoneId.systemDefault()
        )
        end.toLocalDate() == now.toLocalDate() && nowT.isBefore(LocalTime.of(14, 0))
    } == true

    if (woke && latest != null) {
        val h = latest.totalMinutes / 60
        val m = latest.totalMinutes % 60
        return "You slept ${h}h ${m}m"
    }

    val minutesToBed = minutesBetween(nowT, bedtime)
    if (minutesToBed in 1..90) return "Sleep in $minutesToBed minutes"
    if (minutesToBed <= 0 && abs(minutesToBed) <= 480) return "Ready to wind down?"
    return "Rest well today."
}

private fun minutesBetween(now: LocalTime, target: LocalTime): Int {
    val n = now.hour * 60 + now.minute
    val t = target.hour * 60 + target.minute
    return t - n
}
