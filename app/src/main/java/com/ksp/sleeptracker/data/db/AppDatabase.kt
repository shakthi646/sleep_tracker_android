package com.ksp.sleeptracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ksp.sleeptracker.data.model.Alarm
import com.ksp.sleeptracker.data.model.SleepRecord
import com.ksp.sleeptracker.data.model.SleepStage
import com.ksp.sleeptracker.data.model.UserProfile

@Database(
    entities = [SleepRecord::class, SleepStage::class, Alarm::class, UserProfile::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sleepDao(): SleepDao
    abstract fun alarmDao(): AlarmDao
    abstract fun profileDao(): ProfileDao
}
