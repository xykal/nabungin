package dev.xykal.nabungin.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.xykal.nabungin.BuildConfig
import dev.xykal.nabungin.core.appViewModel
import dev.xykal.nabungin.data.prefs.ThemeMode
import dev.xykal.nabungin.security.Biometrics
import dev.xykal.nabungin.ui.components.BottomSheet
import dev.xykal.nabungin.ui.components.ButtonTone
import dev.xykal.nabungin.ui.components.Chip
import dev.xykal.nabungin.ui.components.ConfirmOverlay
import dev.xykal.nabungin.ui.components.IconBubble
import dev.xykal.nabungin.ui.components.Keypad
import dev.xykal.nabungin.ui.components.LabelValueRow
import dev.xykal.nabungin.ui.components.NButton
import dev.xykal.nabungin.ui.components.NabunginCard
import dev.xykal.nabungin.ui.components.NabunginTopBar
import dev.xykal.nabungin.ui.components.ProgressRing
import dev.xykal.nabungin.ui.components.SectionHeader
import dev.xykal.nabungin.ui.components.SheetTitle
import dev.xykal.nabungin.ui.icons.AppIcons
import dev.xykal.nabungin.ui.theme.LocalNabunginColors

private val reminderTimes = listOf(6 to 0, 9 to 0, 12 to 0, 18 to 0, 20 to 0, 21 to 0)

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val colors = LocalNabunginColors.current
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val vm = appViewModel { SettingsViewModel(it) }
    val settings by vm.settings.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()

    var showPinSetup by remember { mutableStateOf(false) }
    var confirmWipe by remember { mutableStateOf(false) }
    var confirmDisableLock by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> uri?.let { vm.export(context, it) } }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let { vm.import(context, it) } }

    // Ask only when the user enables reminders, never during Activity.onCreate.
    val notificationPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) vm.setReminder(context, true)
    }

    LaunchedEffect(message) {
        if (message != null) {
            kotlinx.coroutines.delay(2_500)
            vm.consumeMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(14.dp))
        NabunginTopBar(title = "Setelan", onBack = onBack)
        Spacer(Modifier.height(18.dp))

        SectionHeader(title = "Tampilan")
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            ThemeMode.entries.forEach { mode ->
                Chip(mode.label, settings.themeMode == mode) { vm.setTheme(mode) }
            }
        }

        Spacer(Modifier.height(22.dp))
        SectionHeader(title = "Pengingat harian")
        Spacer(Modifier.height(10.dp))
        NabunginCard(padding = PaddingValues(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBubble(icon = AppIcons.Bell, accent = if (settings.reminderEnabled) colors.accent else colors.muted)
                Spacer(Modifier.padding(horizontal = 6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (settings.reminderEnabled) "Pengingat aktif" else "Pengingat mati",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface,
                    )
                    Text(
                        text = "Notifikasi tiap hari jam %02d:%02d".format(settings.reminderHour, settings.reminderMinute),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.muted,
                    )
                }
                Chip(if (settings.reminderEnabled) "Aktif" else "Mati", settings.reminderEnabled) {
                    if (settings.reminderEnabled) {
                        vm.setReminder(context, false)
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        vm.setReminder(context, true)
                    }
                }
            }
            if (settings.reminderEnabled) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    reminderTimes.take(3).forEach { (hour, minute) ->
                        Chip(
                            label = "%02d:%02d".format(hour, minute),
                            selected = settings.reminderHour == hour && settings.reminderMinute == minute,
                        ) { vm.setReminderTime(context, hour, minute) }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    reminderTimes.drop(3).forEach { (hour, minute) ->
                        Chip(
                            label = "%02d:%02d".format(hour, minute),
                            selected = settings.reminderHour == hour && settings.reminderMinute == minute,
                        ) { vm.setReminderTime(context, hour, minute) }
                    }
                }
            }
        }

        Spacer(Modifier.height(22.dp))
        SectionHeader(title = "Keamanan")
        Spacer(Modifier.height(10.dp))
        NabunginCard(padding = PaddingValues(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBubble(icon = AppIcons.Lock, accent = if (settings.lockEnabled) colors.accent else colors.muted)
                Spacer(Modifier.padding(horizontal = 6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (settings.lockEnabled) "Kunci aplikasi aktif" else "Belum dikunci",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface,
                    )
                    Text(
                        text = if (settings.lockEnabled) "PIN 6 digit + biometrik opsional" else "Aktifkan biar data tabungan nggak kebuka orang lain",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.muted,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            NButton(
                text = if (settings.lockEnabled) "Ganti PIN" else "Bikin PIN",
                onClick = { showPinSetup = true },
                tone = ButtonTone.Quiet,
                icon = AppIcons.Lock,
            )
            if (settings.lockEnabled) {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Buka pakai biometrik",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onSurface,
                        )
                        Text(
                            text = if (activity != null && Biometrics.canUse(activity)) {
                                "Sidik jari atau kunci layar HP"
                            } else {
                                "Belum ada biometrik terdaftar di HP ini"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.muted,
                        )
                    }
                    Chip(if (settings.biometricEnabled) "Aktif" else "Mati", settings.biometricEnabled) {
                        vm.setBiometric(!settings.biometricEnabled)
                    }
                }
                Spacer(Modifier.height(10.dp))
                NButton(
                    text = "Matikan kunci aplikasi",
                    onClick = { confirmDisableLock = true },
                    tone = ButtonTone.Ghost,
                )
            }
        }

        Spacer(Modifier.height(22.dp))
        SectionHeader(title = "Data")
        Spacer(Modifier.height(10.dp))
        NabunginCard(padding = PaddingValues(16.dp)) {
            LabelValueRow("Total data lokal", "Room + DataStore, offline")
            Spacer(Modifier.height(8.dp))
            NButton(
                text = "Export backup (JSON)",
                onClick = { exportLauncher.launch("nabungin-backup.json") },
                icon = AppIcons.Download,
                tone = ButtonTone.Quiet,
            )
            Spacer(Modifier.height(8.dp))
            NButton(
                text = "Import backup",
                onClick = { importLauncher.launch(arrayOf("application/json")) },
                icon = AppIcons.Upload,
                tone = ButtonTone.Quiet,
            )
            Spacer(Modifier.height(8.dp))
            NButton(
                text = "Hapus semua data",
                onClick = { confirmWipe = true },
                icon = AppIcons.Trash,
                tone = ButtonTone.Danger,
            )
        }

        Spacer(Modifier.height(22.dp))
        SectionHeader(title = "Tentang")
        Spacer(Modifier.height(10.dp))
        NabunginCard(padding = PaddingValues(16.dp)) {
            LabelValueRow("Versi", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            LabelValueRow("Package", BuildConfig.APPLICATION_ID)
            LabelValueRow("Build", if (BuildConfig.DEBUG) "Debug" else "Release")
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Nabungin nyimpen semua data di HP lu. Nggak ada server, nggak ada tracking.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
            )
        }

        if (message != null) {
            Spacer(Modifier.height(16.dp))
            NabunginCard(accent = colors.accent, padding = PaddingValues(14.dp)) {
                Text(message ?: "", style = MaterialTheme.typography.bodyMedium, color = colors.onSurface)
            }
        }
        Spacer(Modifier.height(90.dp))
    }

    if (showPinSetup) {
        PinSetupSheet(
            isChange = settings.lockEnabled,
            onDismiss = { showPinSetup = false },
            onSave = { pin ->
                vm.savePin(pin)
                showPinSetup = false
            },
        )
    }

    if (confirmWipe) {
        ConfirmOverlay(
            visible = true,
            title = "Hapus semua data?",
            body = "Semua tujuan, setoran, dan aturan auto-save bakal hilang dari HP ini. Export backup dulu kalau masih ragu.",
            confirmLabel = "Hapus semua",
            onConfirm = {
                confirmWipe = false
                vm.wipe()
            },
            onDismiss = { confirmWipe = false },
        )
    }

    if (confirmDisableLock) {
        ConfirmOverlay(
            visible = true,
            title = "Matikan kunci aplikasi?",
            body = "PIN tersimpan bakal dihapus dan app bisa dibuka siapa aja yang pegang HP ini.",
            confirmLabel = "Matikan kunci",
            onConfirm = {
                confirmDisableLock = false
                vm.disableLock()
            },
            onDismiss = { confirmDisableLock = false },
        )
    }
}

@Composable
private fun PinSetupSheet(
    isChange: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    val colors = LocalNabunginColors.current
    var firstPin by remember { mutableStateOf<String?>(null) }
    var digits by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    BottomSheet(visible = true, onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SheetTitle(
                title = if (firstPin == null) (if (isChange) "PIN baru" else "Bikin PIN") else "Ulangi PIN",
                subtitle = "6 digit angka, disimpan sebagai PBKDF2 hash",
                onClose = onDismiss,
            )
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProgressRing(progress = digits.length / 6f, size = 96.dp, stroke = 8.dp) {
                    Text(
                        text = "*".repeat(digits.length),
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.onSurface,
                    )
                }
            }
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error ?: "", style = MaterialTheme.typography.bodySmall, color = colors.danger)
            }
            Spacer(Modifier.height(16.dp))
            Keypad(
                onDigit = { digit ->
                    if (digits.length < 6) {
                        digits += digit
                        if (digits.length == 6) {
                            if (firstPin == null) {
                                firstPin = digits
                                digits = ""
                                error = null
                            } else if (firstPin == digits) {
                                onSave(digits)
                            } else {
                                error = "PIN nggak sama, coba lagi"
                                firstPin = null
                                digits = ""
                            }
                        }
                    }
                },
                onBackspace = { digits = digits.dropLast(1) },
            )
            Spacer(Modifier.height(12.dp))
            NButton(text = "Batal", onClick = onDismiss, tone = ButtonTone.Ghost)
        }
    }
}
