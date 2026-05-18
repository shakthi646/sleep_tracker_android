package com.ksp.sleeptracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val ageRange: String = "",
    val biologicalSex: String = "",
    val goalMinutes: Int = 450,
    val bedtimeHour: Int = 22,
    val bedtimeMinute: Int = 30,
    val wakeHour: Int = 6,
    val wakeMinute: Int = 0,
    val windDownMinutes: Int = 30,
    val movementDetection: Boolean = true,
    val soundDetection: Boolean = false,
    val snoreSensitivity: Int = 50,
    val themePref: String = "system",
    val memberSince: Long = System.currentTimeMillis()
)
