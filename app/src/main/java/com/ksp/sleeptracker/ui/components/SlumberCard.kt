package com.ksp.sleeptracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ksp.sleeptracker.ui.theme.LocalSpacing
import com.ksp.sleeptracker.ui.theme.SurfaceCard

@Composable
fun SlumberCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val spacing = LocalSpacing.current
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .clip(RoundedCornerShape(spacing.cardRadius))
            .background(SurfaceCard)
            .padding(spacing.lg)
    ) { content() }
}
