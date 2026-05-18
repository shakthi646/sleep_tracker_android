package com.ksp.sleeptracker.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.ksp.sleeptracker.data.model.SleepRecord
import com.ksp.sleeptracker.data.model.SleepStage
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepDao {

    @Upsert
    suspend fun upsertRecord(record: SleepRecord): Long

    @Upsert
    suspend fun upsertStages(stages: List<SleepStage>)

    @Delete
    suspend fun deleteRecord(record: SleepRecord)

    @Query("SELECT * FROM sleep_records ORDER BY startTime DESC")
    fun observeAll(): Flow<List<SleepRecord>>

    @Query("SELECT * FROM sleep_records WHERE startTime >= :from AND startTime <= :to ORDER BY startTime ASC")
    fun observeRange(from: Long, to: Long): Flow<List<SleepRecord>>

    @Query("SELECT * FROM sleep_records WHERE id = :id LIMIT 1")
    suspend fun getRecord(id: Long): SleepRecord?

    @Query("SELECT * FROM sleep_records ORDER BY startTime DESC LIMIT 1")
    fun observeLatest(): Flow<SleepRecord?>

    @Query("SELECT * FROM sleep_stages WHERE recordId = :recordId ORDER BY startTime ASC")
    suspend fun stagesFor(recordId: Long): List<SleepStage>

    @Query("DELETE FROM sleep_records")
    suspend fun clearAll()
}
