package com.ksp.sleeptracker.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "slumber_prefs")

@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val KEY_ONBOARDED = booleanPreferencesKey("onboarding_done")
    private val KEY_THEME = stringPreferencesKey("theme_pref")
    private val KEY_TRACKING_ACTIVE = booleanPreferencesKey("tracking_active")
    private val KEY_SESSION_START = longPreferencesKey("session_start_ms")
    private val KEY_FIRST_LAUNCH = longPreferencesKey("first_launch_ms")

    val onboardingComplete: Flow<Boolean> =
        context.dataStore.data.map { it[KEY_ONBOARDED] == true }

    val themePref: Flow<String> =
        context.dataStore.data.map { it[KEY_THEME] ?: "system" }

    val trackingActive: Flow<Boolean> =
        context.dataStore.data.map { it[KEY_TRACKING_ACTIVE] == true }

    val sessionStart: Flow<Long?> =
        context.dataStore.data.map { it[KEY_SESSION_START] }

    val firstLaunchMillis: Flow<Long?> =
        context.dataStore.data.map { it[KEY_FIRST_LAUNCH] }

    suspend fun setFirstLaunchIfAbsent(nowMillis: Long) {
        context.dataStore.edit { prefs ->
            if (prefs[KEY_FIRST_LAUNCH] == null) prefs[KEY_FIRST_LAUNCH] = nowMillis
        }
    }

    suspend fun setOnboardingComplete(value: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDED] = value }
    }

    suspend fun setThemePref(value: String) {
        context.dataStore.edit { it[KEY_THEME] = value }
    }

    suspend fun setTrackingActive(value: Boolean) {
        context.dataStore.edit { it[KEY_TRACKING_ACTIVE] = value }
    }

    suspend fun setSessionStart(value: Long?) {
        context.dataStore.edit {
            if (value == null) it.remove(KEY_SESSION_START) else it[KEY_SESSION_START] = value
        }
    }
}
