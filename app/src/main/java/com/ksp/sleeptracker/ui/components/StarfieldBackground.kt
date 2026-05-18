package com.ksp.sleeptracker.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.sin
import kotlin.random.Random

private data class Star(val x: Float, val y: Float, val radius: Float, val phase: Float)

@Composable
fun StarfieldBackground(
    modifier: Modifier = Modifier,
    starCount: Int = 80,
    color: Color = Color.White
) {
    val stars = remember {
        val rnd = Random(42)
        List(starCount) {
            Star(
                x = rnd.nextFloat(),
                y = rnd.nextFloat(),
                radius = rnd.nextFloat() * 1.5f + 0.5f,
                phase = rnd.nextFloat() * 6.283f
            )
        }
    }
    val transition = rememberInfiniteTransition(label = "stars")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "twinkle"
    )

    Canvas(modifier = modifier) {
        stars.forEach { star ->
            val alpha = ((sin(phase + star.phase) + 1f) / 2f) * 0.7f + 0.2f
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = star.radius * 1.2f,
                center = Offset(star.x * size.width, star.y * size.height)
            )
        }
    }
}
