package dev.xykal.nabungin.ui.lock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import dev.xykal.nabungin.data.prefs.AppSettings
import dev.xykal.nabungin.security.Biometrics
import dev.xykal.nabungin.security.PinHasher
import dev.xykal.nabungin.ui.components.IconBubble
import dev.xykal.nabungin.ui.components.Keypad
import dev.xykal.nabungin.ui.components.NButton
import dev.xykal.nabungin.ui.components.ProgressRing
import dev.xykal.nabungin.ui.icons.AppIcons
import dev.xykal.nabungin.ui.theme.LocalNabunginColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LockScreen(
    activity: FragmentActivity?,
    settings: AppSettings,
    onUnlocked: () -> Unit,
) {
    val colors = LocalNabunginColors.current
    val scope = rememberCoroutineScope()
    var digits by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    fun verify(pin: String) {
        val salt = settings.pinSalt
        val hash = settings.pinHash
        if (salt == null || hash == null) {
            onUnlocked()
            return
        }
        busy = true
        scope.launch {
            val ok = withContext(Dispatchers.Default) { PinHasher.verify(pin, salt, hash) }
            busy = false
            if (ok) {
                onUnlocked()
            } else {
                error = "PIN salah"
                digits = ""
            }
        }
    }

    val biometricAvailable = activity != null && settings.biometricEnabled && Biometrics.canUse(activity)

    fun biometric() {
        val host = activity ?: return
        Biometrics.prompt(
            activity = host,
            onSuccess = { onUnlocked() },
            onFailure = { message -> error = message },
        )
    }

    LaunchedEffect(biometricAvailable) {
        if (biometricAvailable) biometric()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        IconBubble(icon = AppIcons.Lock, size = 56.dp, iconSize = 26.dp)
        Spacer(Modifier.height(18.dp))
        Text(
            text = "Nabungin terkunci",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onSurface,
        )
        Text(
            text = "Masukin 6 digit PIN lu",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.muted,
        )
        Spacer(Modifier.height(22.dp))
        ProgressRing(progress = digits.length / 6f, size = 110.dp, stroke = 9.dp) {
            Text(
                text = "*".repeat(digits.length),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                color = colors.onSurface,
            )
        }
        if (error != null) {
            Spacer(Modifier.height(10.dp))
            Text(error ?: "", style = MaterialTheme.typography.bodyMedium, color = colors.danger)
        }
        Spacer(Modifier.height(20.dp))
        Keypad(
            onDigit = { digit ->
                if (!busy && digits.length < 6) {
                    digits += digit
                    error = null
                    if (digits.length == 6) verify(digits)
                }
            },
            onBackspace = { digits = digits.dropLast(1) },
        )
        Spacer(Modifier.height(14.dp))
        if (biometricAvailable) {
            Row(modifier = Modifier.fillMaxWidth()) {
                NButton(
                    text = "Pakai biometrik",
                    onClick = { biometric() },
                    tone = dev.xykal.nabungin.ui.components.ButtonTone.Ghost,
                    icon = AppIcons.Fingerprint,
                )
            }
        }
    }
}
