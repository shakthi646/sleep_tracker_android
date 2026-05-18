package com.ksp.sleeptracker.data.repository

import com.ksp.sleeptracker.data.db.SleepDao
import com.ksp.sleeptracker.data.model.SleepRecord
import com.ksp.sleeptracker.data.model.SleepStage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SleepRepository @Inject constructor(
    private val dao: SleepDao
) {
    fun observeAll(): Flow<List<SleepRecord>> = dao.observeAll()
    fun observeRange(from: Long, to: Long): Flow<List<SleepRecord>> = dao.observeRange(from, to)
    fun observeLatest(): Flow<SleepRecord?> = dao.observeLatest()

    suspend fun getRecord(id: Long): SleepRecord? = dao.getRecord(id)
    suspend fun stagesFor(recordId: Long): List<SleepStage> = dao.stagesFor(recordId)

    suspend fun saveRecord(record: SleepRecord, stages: List<SleepStage> = emptyList()): Long {
        val recordId = dao.upsertRecord(record)
        if (stages.isNotEmpty()) {
            dao.upsertStages(stages.map { it.copy(recordId = recordId) })
        }
        return recordId
    }

    suspend fun delete(record: SleepRecord) = dao.deleteRecord(record)
    suspend fun clear() = dao.clearAll()
}
