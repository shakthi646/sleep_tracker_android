package com.ksp.sleeptracker.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sleep_stages",
    foreignKeys = [
        ForeignKey(
            entity = SleepRecord::class,
            parentColumns = ["id"],
            childColumns = ["recordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recordId")]
)
data class SleepStage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recordId: Long,
    val stage: String,
    val startTime: Long,
    val endTime: Long
) {
    companion object {
        const val LIGHT = "light"
        const val DEEP = "deep"
        const val REM = "rem"
        const val AWAKE = "awake"
    }
}
