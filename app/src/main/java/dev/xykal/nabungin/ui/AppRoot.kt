package dev.xykal.nabungin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.xykal.nabungin.NabunginApp
import dev.xykal.nabungin.data.prefs.AppSettings
import dev.xykal.nabungin.ui.lock.LockScreen
import dev.xykal.nabungin.ui.nav.AppNav
import dev.xykal.nabungin.ui.theme.LocalNabunginColors
import dev.xykal.nabungin.ui.theme.NabunginTheme

@Composable
fun AppRoot(activity: FragmentActivity?) {
    val context = LocalContext.current
    val container = remember(context) { (context.applicationContext as NabunginApp).container }
    val settings by container.settings.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
    val lifecycleOwner = LocalLifecycleOwner.current
    var unlocked by rememberSaveable { mutableStateOf(false) }

    // Kunci ulang otomatis begitu app keluar dari foreground.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) unlocked = false
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    NabunginTheme(themeMode = settings.themeMode) {
        val colors = LocalNabunginColors.current
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
        ) {
            if (settings.lockEnabled && !unlocked) {
                LockScreen(
                    activity = activity,
                    settings = settings,
                    onUnlocked = { unlocked = true },
                )
            } else {
                AppNav()
            }
        }
    }
}
