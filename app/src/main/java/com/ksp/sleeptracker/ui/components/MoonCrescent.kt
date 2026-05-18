package com.ksp.sleeptracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ksp.sleeptracker.ui.theme.NightNavy

@Composable
fun MoonCrescent(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    color: Color = Color(0xFFE8EAF6),
    backgroundColor: Color = NightNavy
) {
    Canvas(
        modifier = modifier
            .size(size)
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    ) {
        drawMoon(color, backgroundColor)
    }
}

private fun DrawScope.drawMoon(color: Color, backgroundColor: Color) {
    val r = size.minDimension / 2f
    drawCircle(color = color, radius = r, center = center)
    drawCircle(
        color = backgroundColor,
        radius = r * 0.92f,
        center = center.copy(x = center.x + r * 0.32f),
        blendMode = BlendMode.DstOut
    )
}
