package dev.xykal.nabungin.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** A brief, non-looping celebration on positive savings changes. Overlay never intercepts taps. */
@Composable
fun SavingsSparkle(trigger: Long, color: Color, modifier: Modifier = Modifier) {
    val enabled = motionEnabled()
    val burst = remember { Animatable(1f) }
    val previous = remember { longArrayOf(trigger) }
    LaunchedEffect(trigger, enabled) {
        if (trigger > previous[0] && enabled) {
            burst.snapTo(0f)
            burst.animateTo(1f, tween(720, easing = FastOutSlowInEasing))
        } else burst.snapTo(1f)
        previous[0] = trigger
    }
    Canvas(modifier.fillMaxSize()) {
        val t = burst.value
        if (t >= 1f) return@Canvas
        val mid = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension * (.25f + .36f * t)
        repeat(9) { i ->
            val angle = i * 2 * PI / 9 - PI / 2
            val pos = Offset(mid.x + radius * cos(angle).toFloat(), mid.y + radius * sin(angle).toFloat())
            drawCircle(if (i % 3 == 0) Color(0xFFEBAF60) else color,
                radius = size.minDimension * .013f * (1f - t), center = pos, alpha = (1f - t).coerceIn(0f, 1f))
        }
    }
}
