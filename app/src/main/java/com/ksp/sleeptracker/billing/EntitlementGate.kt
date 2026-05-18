package com.ksp.sleeptracker.billing

sealed interface EntitlementGate {
    data object Loading : EntitlementGate
    data class Trial(val daysLeft: Int) : EntitlementGate
    data object Locked : EntitlementGate
    data object Premium : EntitlementGate
}

object TrialConfig {
    const val TRIAL_DAYS = 3
    const val TRIAL_MILLIS = TRIAL_DAYS * 24L * 60L * 60L * 1000L
}
