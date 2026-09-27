package dev.xykal.nabungin.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/** A transient splash follows each balance change and settles; no perpetual animation/battery drain. */
@Composable
fun LiquidProgress(progress: Float, color: Color, size: Dp = 168.dp,
    modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val enabled = motionEnabled()
    val fill = remember { Animatable(0f) }
    val splash = remember { Animatable(0f) }
    val target = progress.coerceIn(0f, 1f)
    LaunchedEffect(target, enabled) {
        if (!enabled) { fill.snapTo(target); splash.snapTo(0f) }
        else {
            splash.snapTo(1f)
            fill.animateTo(target, tween(1100, easing = FastOutSlowInEasing))
            splash.animateTo(0f, tween(750, easing = FastOutSlowInEasing))
        }
    }
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val circle = Path().apply { addOval(Rect(0f, 0f, w, h)) }
            drawCircle(color.copy(alpha = .12f))
            clipPath(circle) {
                val level = h * (1f - fill.value)
                val wave = if (fill.value in .02f.. .98f) h * .075f * splash.value else 0f
                val path = Path().apply {
                    moveTo(0f, h)
                    lineTo(0f, level)
                    for (x in 0..64) {
                        val px = w * x / 64f
                        lineTo(px, level + wave * sin(px / w * PI * 2.0 + 1.2).toFloat())
                    }
                    lineTo(w, h)
                    close()
                }
                drawPath(path, color.copy(alpha = .60f))
                if (fill.value > .08f) {
                    val dot = 2.5.dp.toPx()
                    drawCircle(color.copy(alpha = .75f * splash.value), dot,
                        center = androidx.compose.ui.geometry.Offset(w * .28f, level + h * .12f))
                    drawCircle(color.copy(alpha = .60f * splash.value), dot * .7f,
                        center = androidx.compose.ui.geometry.Offset(w * .72f, level + h * .08f))
                }
            }
            drawCircle(color.copy(alpha = .9f), style = Stroke(width = 3.dp.toPx()))
        }
        content()
    }
}
