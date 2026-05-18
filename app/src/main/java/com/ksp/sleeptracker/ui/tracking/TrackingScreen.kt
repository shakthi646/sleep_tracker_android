package com.ksp.sleeptracker.ui.tracking

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ksp.sleeptracker.ui.components.MoonCrescent
import com.ksp.sleeptracker.ui.components.PulsingRing
import com.ksp.sleeptracker.ui.components.StarfieldBackground
import com.ksp.sleeptracker.ui.theme.AccentAmber
import com.ksp.sleeptracker.ui.theme.AccentGreen
import com.ksp.sleeptracker.ui.theme.AccentIndigo
import com.ksp.sleeptracker.ui.theme.AccentRose
import com.ksp.sleeptracker.ui.theme.MidnightIndigo
import com.ksp.sleeptracker.ui.theme.NightNavy
import com.ksp.sleeptracker.ui.theme.TextMuted
import com.ksp.sleeptracker.ui.theme.TextPrimary
import kotlinx.coroutines.delay

@Composable
fun TrackingScreen(
    onSessionEnded: () -> Unit,
    vm: TrackingViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        if (state.sessionStart == null) vm.startSession()
    }

    KeepScreenOnAndDim()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NightNavy),
        contentAlignment = Alignment.Center
    ) {
        StarfieldBackground(modifier = Modifier.fillMaxSize(), starCount = 110)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            StageBadge(stage = state.currentStage)

            Box(contentAlignment = Alignment.Center) {
                PulsingRing(diameter = 280.dp)
                MoonCrescent(size = 132.dp)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Sleeping for",
                    color = TextMuted,
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatElapsed(state.elapsedMs),
                    style = MaterialTheme.typography.displayLarge.copy(fontStyle = FontStyle.Italic),
                    color = TextPrimary
                )
            }

            EndSessionButton(
                onConfirmEnd = {
                    vm.endSession()
                    onSessionEnded()
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StageBadge(stage: Stage) {
    val (label, color) = when (stage) {
        Stage.LIGHT -> "Light sleep" to AccentIndigo
        Stage.DEEP -> "Deep sleep" to AccentGreen
        Stage.REM -> "REM" to AccentAmber
        Stage.AWAKE -> "Awake" to AccentRose
    }
    val animatedColor by animateColorAsState(targetValue = color, label = "stageColor")
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MidnightIndigo)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(animatedColor)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(label, color = TextPrimary, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun EndSessionButton(onConfirmEnd: () -> Unit) {
    var holding by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(MidnightIndigo.copy(alpha = 0.5f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        holding = true
                        val released = tryAwaitRelease()
                        holding = false
                        if (!released) onConfirmEnd()
                    },
                    onLongPress = { onConfirmEnd() }
                )
            }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (holding) "Hold to confirm…" else "Hold to end session",
            color = TextPrimary,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun KeepScreenOnAndDim() {
    val view = LocalView.current
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val window = activity?.window
        view.keepScreenOn = true
        val originalBrightness = window?.attributes?.screenBrightness ?: -1f
        onDispose {
            view.keepScreenOn = false
            window?.attributes = window?.attributes?.apply { screenBrightness = originalBrightness }
        }
    }
    LaunchedEffect(Unit) {
        delay(30_000)
        val activity = context as? Activity
        val window = activity?.window ?: return@LaunchedEffect
        window.attributes = window.attributes.apply { screenBrightness = 0.05f }
    }
}

private fun formatElapsed(ms: Long): String {
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

