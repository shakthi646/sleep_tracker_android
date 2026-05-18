package com.ksp.sleeptracker.ui.onboarding

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.ksp.sleeptracker.ui.components.MoonCrescent
import com.ksp.sleeptracker.ui.components.StarfieldBackground
import com.ksp.sleeptracker.ui.theme.NightNavy
import com.ksp.sleeptracker.ui.theme.TextMuted
import com.ksp.sleeptracker.ui.theme.TextPrimary
import kotlinx.coroutines.delay

@Composable
fun SplashPage(onAdvance: () -> Unit) {
    var iconReady by remember { mutableStateOf(false) }
    var textReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(150)
        iconReady = true
        delay(800)
        textReady = true
        delay(1200)
        onAdvance()
    }

    val iconScale by animateFloatAsState(
        targetValue = if (iconReady) 1f else 0.6f,
        animationSpec = tween(900, easing = EaseOutCubic),
        label = "iconScale"
    )
    val iconAlpha by animateFloatAsState(
        targetValue = if (iconReady) 1f else 0f,
        animationSpec = tween(700),
        label = "iconAlpha"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (textReady) 1f else 0f,
        animationSpec = tween(900),
        label = "textAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NightNavy),
        contentAlignment = Alignment.Center
    ) {
        StarfieldBackground(modifier = Modifier.fillMaxSize())
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            MoonCrescent(
                modifier = Modifier
                    .scale(iconScale)
                    .alpha(iconAlpha),
                size = 96.dp
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Sleep Tracker",
                style = MaterialTheme.typography.displayMedium,
                color = TextPrimary,
                modifier = Modifier.alpha(textAlpha)
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Sleep smarter. Wake better.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                modifier = Modifier.alpha(textAlpha)
            )
        }
    }
}
