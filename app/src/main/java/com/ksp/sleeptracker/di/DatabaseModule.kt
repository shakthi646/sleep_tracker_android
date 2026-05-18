package com.ksp.sleeptracker.di

import android.content.Context
import androidx.room.Room
import com.ksp.sleeptracker.data.db.AlarmDao
import com.ksp.sleeptracker.data.db.AppDatabase
import com.ksp.sleeptracker.data.db.ProfileDao
import com.ksp.sleeptracker.data.db.SleepDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "slumber.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideSleepDao(db: AppDatabase): SleepDao = db.sleepDao()
    @Provides fun provideAlarmDao(db: AppDatabase): AlarmDao = db.alarmDao()
    @Provides fun provideProfileDao(db: AppDatabase): ProfileDao = db.profileDao()
}
