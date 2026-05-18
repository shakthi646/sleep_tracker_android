package com.ksp.sleeptracker.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ksp.sleeptracker.ui.alarms.AlarmsScreen
import com.ksp.sleeptracker.ui.components.SlumberBottomNav
import com.ksp.sleeptracker.ui.home.HomeScreen
import com.ksp.sleeptracker.ui.onboarding.OnboardingScreen
import com.ksp.sleeptracker.ui.profile.ProfileScreen
import com.ksp.sleeptracker.ui.stats.StatsScreen
import com.ksp.sleeptracker.ui.tracking.TrackingScreen
import com.ksp.sleeptracker.ui.theme.NightNavy

private val tabRoutes = setOf(Routes.HOME, Routes.STATS, Routes.ALARMS, Routes.PROFILE)

@Composable
fun SlumberNavHost(
    startDestination: String,
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Box(modifier = Modifier.fillMaxSize().background(NightNavy)) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { slideInHorizontally(initialOffsetX = { it / 4 }) + fadeIn() },
            exitTransition = { fadeOut() },
            popEnterTransition = { fadeIn() },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it / 4 }) + fadeOut() }
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onFinished = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenProfile = { navController.navigate(Routes.PROFILE) },
                    onStartSleep = { navController.navigate(Routes.TRACKING) }
                )
            }
            composable(Routes.TRACKING) {
                TrackingScreen(onSessionEnded = { navController.popBackStack() })
            }
            composable(Routes.STATS) {
                StatsScreen()
            }
            composable(Routes.ALARMS) {
                AlarmsScreen()
            }
            composable(Routes.PROFILE) {
                ProfileScreen()
            }
        }

        if (currentRoute in tabRoutes) {
            SlumberBottomNav(
                currentRoute = currentRoute ?: Routes.HOME,
                onSelect = { route ->
                    if (route != currentRoute) {
                        navController.navigate(route) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

