package dev.xykal.nabungin.ui.components

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/** Animations respect Android's animator duration scale (0 = reduced motion). */
@Composable
fun motionEnabled(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    }
}

@Composable
fun animatedAmount(target: Long): Long {
    val enabled = motionEnabled()
    val progress = remember { Animatable(0f) }
    var from by remember { mutableStateOf(0L) }
    var to by remember { mutableStateOf(0L) }
    LaunchedEffect(target, enabled) {
        // Start from current displayed value, including when a new transaction arrives mid-animation.
        from = (from.toDouble() + (to.toDouble() - from.toDouble()) * progress.value).toLong()
        to = target
        progress.snapTo(0f)
        if (enabled) progress.animateTo(1f, tween(760, easing = FastOutSlowInEasing))
        else progress.snapTo(1f)
    }
    return (from.toDouble() + (to.toDouble() - from.toDouble()) * progress.value).toLong()
}
