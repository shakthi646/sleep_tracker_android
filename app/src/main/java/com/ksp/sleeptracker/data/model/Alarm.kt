package com.ksp.sleeptracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val repeatDays: String = "",
    val smartAlarmEnabled: Boolean = true,
    val smartWindowMinutes: Int = 20,
    val ringtoneUri: String = "",
    val vibrate: Boolean = true,
    val isEnabled: Boolean = true,
    val label: String = ""
) {
    fun activeDays(): List<Int> = repeatDays
        .split(",")
        .mapNotNull { it.trim().toIntOrNull() }

    fun isOneTime(): Boolean = activeDays().isEmpty()
}
