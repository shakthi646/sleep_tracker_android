package com.ksp.sleeptracker.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@Composable
fun rememberHapticTap(action: () -> Unit): () -> Unit {
    val haptic = LocalHapticFeedback.current
    return remember(action) {
        {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            action()
        }
    }
}

@Composable
fun rememberHapticConfirm(action: () -> Unit): () -> Unit {
    val haptic = LocalHapticFeedback.current
    return remember(action) {
        {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            action()
        }
    }
}

fun HapticFeedback.tap() = performHapticFeedback(HapticFeedbackType.TextHandleMove)
fun HapticFeedback.confirm() = performHapticFeedback(HapticFeedbackType.LongPress)
