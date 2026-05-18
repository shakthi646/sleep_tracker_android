package com.ksp.sleeptracker.data.repository

import com.ksp.sleeptracker.data.db.ProfileDao
import com.ksp.sleeptracker.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val dao: ProfileDao
) {
    fun observe(): Flow<UserProfile?> = dao.observe()
    suspend fun get(): UserProfile? = dao.get()
    suspend fun save(profile: UserProfile) = dao.upsert(profile)
    suspend fun clear() = dao.clear()
}
