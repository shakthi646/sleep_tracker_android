package com.ksp.sleeptracker.data.repository

import com.ksp.sleeptracker.data.db.AlarmDao
import com.ksp.sleeptracker.data.model.Alarm
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepository @Inject constructor(
    private val dao: AlarmDao
) {
    fun observeAll(): Flow<List<Alarm>> = dao.observeAll()
    suspend fun getEnabled(): List<Alarm> = dao.getEnabled()
    suspend fun getById(id: Int): Alarm? = dao.getById(id)
    suspend fun upsert(alarm: Alarm): Long = dao.upsert(alarm)
    suspend fun delete(alarm: Alarm) = dao.delete(alarm)
}
