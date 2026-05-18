package com.ksp.sleeptracker.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ksp.sleeptracker.ui.components.PageDots
import com.ksp.sleeptracker.ui.theme.NightNavy
import kotlinx.coroutines.launch

private const val PAGE_SPLASH = 0
private const val PAGE_WELCOME = 1
private const val PAGE_PROFILE = 2
private const val PAGE_GOALS = 3
private const val PAGE_PERMISSIONS = 4
private const val PAGE_READY = 5
private const val PAGE_COUNT = 6

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    vm: OnboardingViewModel = hiltViewModel()
) {
    val pager = rememberPagerState(initialPage = 0) { PAGE_COUNT }
    val scope = rememberCoroutineScope()

    fun goTo(page: Int) {
        scope.launch { pager.animateScrollToPage(page) }
    }

    BackHandler(enabled = pager.currentPage > PAGE_WELCOME) {
        goTo(pager.currentPage - 1)
    }

    Box(modifier = Modifier.fillMaxSize().background(NightNavy)) {
        HorizontalPager(
            state = pager,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = pager.currentPage in PAGE_WELCOME..PAGE_PERMISSIONS,
            pageSpacing = 0.dp
        ) { page ->
            when (page) {
                PAGE_SPLASH -> SplashPage(onAdvance = { goTo(PAGE_WELCOME) })
                PAGE_WELCOME -> WelcomePage(onNext = { goTo(PAGE_PROFILE) })
                PAGE_PROFILE -> ProfilePage(
                    vm = vm,
                    onNext = { goTo(PAGE_GOALS) },
                    onBack = { goTo(PAGE_WELCOME) }
                )
                PAGE_GOALS -> GoalsPage(
                    vm = vm,
                    onNext = { goTo(PAGE_PERMISSIONS) },
                    onBack = { goTo(PAGE_PROFILE) }
                )
                PAGE_PERMISSIONS -> PermissionsPage(
                    vm = vm,
                    onNext = { goTo(PAGE_READY) },
                    onBack = { goTo(PAGE_GOALS) }
                )
                PAGE_READY -> ReadyPage(vm = vm, onFinish = onFinished)
            }
        }

        if (pager.currentPage in PAGE_PROFILE..PAGE_PERMISSIONS) {
            PageDots(
                total = 3,
                current = pager.currentPage - PAGE_PROFILE,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
            )
        }
    }
}
