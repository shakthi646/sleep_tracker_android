package com.ksp.sleeptracker.ui.alarms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ksp.sleeptracker.data.model.Alarm
import com.ksp.sleeptracker.data.repository.AlarmRepository
import com.ksp.sleeptracker.service.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class AlarmsUiState(
    val alarms: List<Alarm> = emptyList(),
    val editor: AlarmEditorState? = null
)

data class AlarmEditorState(
    val original: Alarm?,
    val hour: Int,
    val minute: Int,
    val days: Set<Int>,
    val smartAlarmEnabled: Boolean,
    val smartWindowMinutes: Int,
    val vibrate: Boolean,
    val label: String
) {
    fun toAlarm(): Alarm = Alarm(
        id = original?.id ?: 0,
        hour = hour,
        minute = minute,
        repeatDays = days.sorted().joinToString(","),
        smartAlarmEnabled = smartAlarmEnabled,
        smartWindowMinutes = smartWindowMinutes,
        ringtoneUri = original?.ringtoneUri.orEmpty(),
        vibrate = vibrate,
        isEnabled = true,
        label = label
    )
}

@HiltViewModel
class AlarmsViewModel @Inject constructor(
    private val repo: AlarmRepository,
    private val scheduler: AlarmScheduler
) : ViewModel() {

    private val editorFlow = MutableStateFlow<AlarmEditorState?>(null)

    val state: StateFlow<AlarmsUiState> = kotlinx.coroutines.flow.combine(
        repo.observeAll(),
        editorFlow
    ) { alarms, editor ->
        AlarmsUiState(alarms = alarms, editor = editor)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AlarmsUiState())

    fun toggle(alarm: Alarm, enabled: Boolean) {
        viewModelScope.launch {
            val updated = alarm.copy(isEnabled = enabled)
            repo.upsert(updated)
            if (enabled) scheduler.schedule(updated) else scheduler.cancel(updated)
        }
    }

    fun delete(alarm: Alarm) {
        viewModelScope.launch {
            scheduler.cancel(alarm)
            repo.delete(alarm)
        }
    }

    fun openEditor(alarm: Alarm? = null) {
        val now = LocalTime.now()
        val base = alarm ?: Alarm(hour = 7, minute = 0)
        editorFlow.value = AlarmEditorState(
            original = alarm,
            hour = base.hour,
            minute = base.minute,
            days = base.activeDays().toSet(),
            smartAlarmEnabled = base.smartAlarmEnabled,
            smartWindowMinutes = base.smartWindowMinutes,
            vibrate = base.vibrate,
            label = base.label
        )
    }

    fun closeEditor() { editorFlow.value = null }

    fun updateEditor(transform: (AlarmEditorState) -> AlarmEditorState) {
        editorFlow.value = editorFlow.value?.let(transform)
    }

    fun saveEditor() {
        val draft = editorFlow.value ?: return
        viewModelScope.launch {
            val alarm = draft.toAlarm()
            val id = repo.upsert(alarm).toInt()
            val saved = if (alarm.id == 0) alarm.copy(id = id) else alarm
            scheduler.schedule(saved)
            editorFlow.value = null
        }
    }

    fun nextFireDateTime(alarm: Alarm, now: LocalDateTime = LocalDateTime.now()): LocalDateTime? {
        if (!alarm.isEnabled) return null
        return scheduler.intendedFireTime(alarm, now)
    }

    fun deleteEditing() {
        val draft = editorFlow.value ?: return
        val original = draft.original ?: run {
            editorFlow.value = null
            return
        }
        viewModelScope.launch {
            scheduler.cancel(original)
            repo.delete(original)
            editorFlow.value = null
        }
    }

    fun nextFireLabel(alarm: Alarm, now: LocalDateTime = LocalDateTime.now()): String? {
        if (!alarm.isEnabled) return null
        val next = scheduler.intendedFireTime(alarm, now) ?: return null
        val today = now.toLocalDate()
        val day = when {
            next.toLocalDate() == today -> "Today"
            next.toLocalDate() == today.plusDays(1) -> "Tomorrow"
            else -> next.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        }
        return "$day · ${next.toLocalTime().format(timeFmt)}"
    }

    companion object {
        private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
    }
}
