package dev.xykal.nabungin.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import kotlin.math.PI
import kotlin.math.sin

/** One-shot liquid fill: settles rather than looping forever. Text percentage stays separately accessible. */
@Composable
fun LiquidProgress(progress: Float, color: Color, size: Dp = 168.dp,
    modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val enabled = motionEnabled()
    val filled by animateFloatAsState(progress.coerceIn(0f, 1f),
        tween(if (enabled) 1050 else 0, easing = FastOutSlowInEasing), label = "liquid-fill")
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val circle = Path().apply { addOval(androidx.compose.ui.geometry.Rect(0f, 0f, w, h)) }
            drawCircle(color.copy(alpha = .17f))
            clipPath(circle) {
                val level = h * (1f - filled)
                val wave = if (enabled && filled > .01f && filled < .99f) h * .014f else 0f
                val path = Path().apply {
                    moveTo(0f, h)
                    lineTo(0f, level)
                    for (x in 0..48) {
                        val px = w * x / 48f
                        lineTo(px, level + wave * sin(px / w * PI * 2).toFloat())
                    }
                    lineTo(w, h)
                    close()
                }
                drawPath(path, color.copy(alpha = .45f))
            }
            drawCircle(color, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx()))
        }
        content()
    }
}
