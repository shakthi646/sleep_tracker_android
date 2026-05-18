package com.ksp.sleeptracker.ui.profile

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.billingsdk.core.BillingSDK
import com.ksp.sleeptracker.billing.EntitlementGate
import com.ksp.sleeptracker.billing.EntitlementRepository
import com.ksp.sleeptracker.data.model.SleepRecord
import com.ksp.sleeptracker.data.model.UserProfile
import com.ksp.sleeptracker.data.repository.PreferencesRepository
import com.ksp.sleeptracker.data.repository.ProfileRepository
import com.ksp.sleeptracker.data.repository.SleepRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val profile: UserProfile? = null,
    val nightsTracked: Int = 0,
    val avgScore: Int = 0,
    val entitlement: EntitlementGate = EntitlementGate.Loading,
    val confirmReset: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepo: ProfileRepository,
    private val sleepRepo: SleepRepository,
    private val prefs: PreferencesRepository,
    private val entitlementRepo: EntitlementRepository
) : ViewModel() {

    val state: StateFlow<ProfileUiState> = combine(
        profileRepo.observe(),
        sleepRepo.observeAll(),
        entitlementRepo.observeGate()
    ) { profile, records, gate ->
        ProfileUiState(
            profile = profile,
            nightsTracked = records.size,
            avgScore = if (records.isEmpty()) 0 else records.map(SleepRecord::score).average().toInt(),
            entitlement = gate
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())

    fun updateProfile(transform: (UserProfile) -> UserProfile) {
        viewModelScope.launch {
            val current = state.value.profile ?: profileRepo.get() ?: UserProfile()
            profileRepo.save(transform(current))
        }
    }

    fun openPaywall(activity: Activity?) {
        activity?.let { BillingSDK.showPaywall(it) }
    }

    fun restorePurchases() {
        viewModelScope.launch { entitlementRepo.restorePurchases() }
    }

    fun resetAllData() {
        viewModelScope.launch {
            sleepRepo.clear()
            profileRepo.clear()
            prefs.setOnboardingComplete(false)
            prefs.setSessionStart(null)
            prefs.setTrackingActive(false)
        }
    }
}
