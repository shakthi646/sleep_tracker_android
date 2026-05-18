package com.ksp.sleeptracker.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Spacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val xxxl: Dp = 32.dp,
    val screenPadding: Dp = 20.dp,
    val sectionGap: Dp = 24.dp,
    val cardRadius: Dp = 16.dp,
    val chipRadius: Dp = 20.dp
)

val LocalSpacing = compositionLocalOf { Spacing() }
