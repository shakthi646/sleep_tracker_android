package com.ksp.sleeptracker.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ksp.sleeptracker.data.model.UserProfile
import com.ksp.sleeptracker.data.repository.PreferencesRepository
import com.ksp.sleeptracker.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    val name: String = "",
    val ageRange: String = "",
    val biologicalSex: String = "",
    val goalMinutes: Int = 450,
    val bedtimeHour: Int = 22,
    val bedtimeMinute: Int = 30,
    val wakeHour: Int = 6,
    val wakeMinute: Int = 0,
    val notificationsGranted: Boolean = false,
    val activityGranted: Boolean = false,
    val batteryExempt: Boolean = false,
    val nameError: String? = null,
    val ageError: String? = null
) {
    val canExitProfile: Boolean get() = name.isNotBlank() && ageRange.isNotBlank()
    val canFinish: Boolean get() = notificationsGranted
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val profileRepo: ProfileRepository,
    private val prefs: PreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun onNameChange(value: String) {
        _state.update { it.copy(name = value, nameError = null) }
    }

    fun onAgeRangeChange(value: String) {
        _state.update { it.copy(ageRange = value, ageError = null) }
    }

    fun onBiologicalSexChange(value: String) {
        _state.update { it.copy(biologicalSex = value) }
    }

    fun onGoalMinutesChange(value: Int) {
        _state.update { it.copy(goalMinutes = value.coerceIn(360, 600)) }
    }

    fun onBedtimeChange(hour: Int, minute: Int) {
        _state.update { it.copy(bedtimeHour = hour, bedtimeMinute = minute) }
    }

    fun onWakeChange(hour: Int, minute: Int) {
        _state.update { it.copy(wakeHour = hour, wakeMinute = minute) }
    }

    fun onNotificationsGranted(granted: Boolean) {
        _state.update { it.copy(notificationsGranted = granted) }
    }

    fun onActivityGranted(granted: Boolean) {
        _state.update { it.copy(activityGranted = granted) }
    }

    fun onBatteryExempt(granted: Boolean) {
        _state.update { it.copy(batteryExempt = granted) }
    }

    fun validateProfileForAdvance(): Boolean {
        val s = _state.value
        var ok = true
        if (s.name.isBlank()) {
            _state.update { it.copy(nameError = "Please enter your name") }
            ok = false
        }
        if (s.ageRange.isBlank()) {
            _state.update { it.copy(ageError = "Pick an age range") }
            ok = false
        }
        return ok
    }

    fun finishOnboarding(onDone: () -> Unit) {
        val s = _state.value
        viewModelScope.launch {
            profileRepo.save(
                UserProfile(
                    id = 1,
                    name = s.name.trim(),
                    ageRange = s.ageRange,
                    biologicalSex = s.biologicalSex,
                    goalMinutes = s.goalMinutes,
                    bedtimeHour = s.bedtimeHour,
                    bedtimeMinute = s.bedtimeMinute,
                    wakeHour = s.wakeHour,
                    wakeMinute = s.wakeMinute
                )
            )
            prefs.setOnboardingComplete(true)
            onDone()
        }
    }
}
