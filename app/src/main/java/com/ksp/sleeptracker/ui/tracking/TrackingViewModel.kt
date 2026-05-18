package com.ksp.sleeptracker.ui.tracking

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ksp.sleeptracker.data.repository.PreferencesRepository
import com.ksp.sleeptracker.service.SleepTrackingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

data class TrackingUiState(
    val sessionStart: Long? = null,
    val nowMs: Long = System.currentTimeMillis(),
    val currentStage: Stage = Stage.AWAKE
) {
    val elapsedMs: Long get() = sessionStart?.let { (nowMs - it).coerceAtLeast(0L) } ?: 0L
    val isActive: Boolean get() = sessionStart != null
}

enum class Stage { AWAKE, LIGHT, DEEP, REM }

@HiltViewModel
class TrackingViewModel @Inject constructor(
    application: Application,
    private val prefs: PreferencesRepository
) : AndroidViewModel(application) {

    private val tick = MutableStateFlow(System.currentTimeMillis())

    init {
        viewModelScope.launch {
            while (isActive) {
                tick.value = System.currentTimeMillis()
                delay(1000)
            }
        }
    }

    val state: StateFlow<TrackingUiState> = combine(
        prefs.sessionStart,
        tick
    ) { start, now ->
        TrackingUiState(
            sessionStart = start,
            nowMs = now,
            currentStage = stageForElapsed(start?.let { now - it } ?: 0L)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TrackingUiState())

    fun startSession() {
        SleepTrackingService.start(getApplication())
    }

    fun endSession() {
        SleepTrackingService.stop(getApplication())
    }

    private fun stageForElapsed(elapsedMs: Long): Stage {
        if (elapsedMs <= 0) return Stage.AWAKE
        val minutes = (elapsedMs / 60_000L).toInt()
        // Synthetic cycle (90 min): light → deep → light → REM → repeat.
        val pos = minutes % 90
        return when {
            pos < 5 -> Stage.AWAKE
            pos < 25 -> Stage.LIGHT
            pos < 55 -> Stage.DEEP
            pos < 75 -> Stage.LIGHT
            else -> Stage.REM
        }
    }
}
