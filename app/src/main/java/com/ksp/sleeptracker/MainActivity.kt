package com.ksp.sleeptracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ksp.sleeptracker.billing.EntitlementGate
import com.ksp.sleeptracker.billing.EntitlementRepository
import com.ksp.sleeptracker.billing.LockedScreen
import com.ksp.sleeptracker.data.repository.PreferencesRepository
import com.ksp.sleeptracker.ui.navigation.Routes
import com.ksp.sleeptracker.ui.navigation.SlumberNavHost
import com.ksp.sleeptracker.ui.theme.NightNavy
import com.ksp.sleeptracker.ui.theme.SleepTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SleepTrackerTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize().background(NightNavy),
                    color = NightNavy
                ) {
                    AppRoot()
                }
            }
        }
    }
}

@Composable
private fun AppRoot(vm: AppRootViewModel = hiltViewModel()) {
    val onboarded by vm.onboardingComplete.collectAsState()
    val gate by vm.entitlement.collectAsState()
    if (onboarded == null) return

    when (gate) {
        EntitlementGate.Locked -> LockedScreen()
        else -> {
            val start = if (onboarded == true) Routes.HOME else Routes.ONBOARDING
            SlumberNavHost(startDestination = start)
        }
    }
}

@HiltViewModel
class AppRootViewModel @Inject constructor(
    prefs: PreferencesRepository,
    private val entitlementRepo: EntitlementRepository
) : ViewModel() {
    val onboardingComplete: StateFlow<Boolean?> = prefs.onboardingComplete
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val entitlement: StateFlow<EntitlementGate> = entitlementRepo.observeGate()
        .stateIn(viewModelScope, SharingStarted.Eagerly, EntitlementGate.Loading)

    init {
        viewModelScope.launch { entitlementRepo.stampFirstLaunchIfNeeded() }
    }
}
