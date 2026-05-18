package com.ksp.sleeptracker.billing

import com.billingsdk.core.BillingSDK
import com.ksp.sleeptracker.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.ceil

@Singleton
class EntitlementRepository @Inject constructor(
    private val prefs: PreferencesRepository
) {
    suspend fun stampFirstLaunchIfNeeded() {
        prefs.setFirstLaunchIfAbsent(System.currentTimeMillis())
    }

    fun observeGate(now: () -> Long = System::currentTimeMillis): Flow<EntitlementGate> =
        combine(
            prefs.firstLaunchMillis,
            BillingSDK.observePremiumStatus()
        ) { firstLaunch, premium ->
            when {
                premium.isPremium -> EntitlementGate.Premium
                firstLaunch == null -> EntitlementGate.Loading
                else -> {
                    val elapsed = now() - firstLaunch
                    val remaining = TrialConfig.TRIAL_MILLIS - elapsed
                    if (remaining > 0) {
                        EntitlementGate.Trial(daysLeft = ceil(remaining / (24.0 * 60 * 60 * 1000)).toInt())
                    } else {
                        EntitlementGate.Locked
                    }
                }
            }
        }.distinctUntilChanged()

    suspend fun restorePurchases() {
        BillingSDK.restorePurchases()
    }

    fun refresh() {
        BillingSDK.refreshPremiumStatusIfOnline()
    }
}
