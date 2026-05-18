package com.ksp.sleeptracker.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ksp.sleeptracker.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {

    @Upsert
    suspend fun upsert(profile: UserProfile)

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun observe(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun get(): UserProfile?

    @Query("DELETE FROM user_profile")
    suspend fun clear()
}
