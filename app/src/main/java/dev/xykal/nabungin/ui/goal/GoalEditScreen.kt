package dev.xykal.nabungin.ui.goal

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import dev.xykal.nabungin.core.appViewModel
import dev.xykal.nabungin.domain.format.Dates
import dev.xykal.nabungin.domain.format.Money
import dev.xykal.nabungin.domain.model.AutoRule
import dev.xykal.nabungin.domain.model.Goal
import dev.xykal.nabungin.domain.model.SaveInterval
import dev.xykal.nabungin.ui.components.AmountKeypadField
import dev.xykal.nabungin.ui.components.BottomSheet
import dev.xykal.nabungin.ui.components.ButtonTone
import dev.xykal.nabungin.ui.components.Chip
import dev.xykal.nabungin.ui.components.IconBubble
import dev.xykal.nabungin.ui.components.NButton
import dev.xykal.nabungin.ui.components.NabunginCard
import dev.xykal.nabungin.ui.components.NabunginTextField
import dev.xykal.nabungin.ui.components.NabunginTopBar
import dev.xykal.nabungin.ui.components.SectionHeader
import dev.xykal.nabungin.ui.components.SheetTitle
import dev.xykal.nabungin.ui.components.SwatchRow
import dev.xykal.nabungin.ui.icons.AppIcons
import dev.xykal.nabungin.ui.theme.GoalAccents
import dev.xykal.nabungin.ui.theme.LocalNabunginColors
import java.time.LocalDate

private val categories = listOf("Umum", "Darurat", "Gadget", "Liburan", "Pendidikan", "Kendaraan")

@Composable
fun GoalEditScreen(
    goalId: Long,
    onDone: () -> Unit,
    onBack: () -> Unit,
) {
    val colors = LocalNabunginColors.current
    val context = LocalContext.current
    val vm = appViewModel { GoalEditViewModel(it) }

    var loaded by remember { mutableStateOf(goalId <= 0L) }
    var name by remember { mutableStateOf("") }
    var targetDigits by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf<LocalDate?>(null) }
    var accentIndex by remember { mutableStateOf(0) }
    var iconKey by remember { mutableStateOf("coins") }
    var category by remember { mutableStateOf("Umum") }
    var ruleEnabled by remember { mutableStateOf(false) }
    var ruleAmountDigits by remember { mutableStateOf("") }
    var ruleInterval by remember { mutableStateOf(SaveInterval.DAILY) }
    var ruleHour by remember { mutableStateOf(20) }
    var showRuleSheet by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(goalId) {
        if (goalId > 0L) {
            val goal = vm.loadGoal(goalId)
            if (goal != null) {
                name = goal.name
                targetDigits = goal.targetAmount.takeIf { it > 0 }?.toString() ?: ""
                deadline = goal.deadline
                accentIndex = goal.accentIndex
                iconKey = goal.iconKey
                category = goal.category
            }
            val rule = vm.loadRule(goalId)
            if (rule != null) {
                ruleEnabled = rule.enabled
                ruleAmountDigits = rule.amount.takeIf { it > 0 }?.toString() ?: ""
                ruleInterval = rule.interval
                ruleHour = rule.hour
            }
            loaded = true
        }
    }

    val target = Money.parseDigits(targetDigits)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(14.dp))
        NabunginTopBar(
            title = if (goalId > 0L) "Edit tujuan" else "Tujuan baru",
            onBack = onBack,
            actions = {
                IconBubble(icon = AppIcons.of(iconKey), accent = GoalAccents[accentIndex.coerceIn(0, GoalAccents.lastIndex)], size = 40.dp)
            },
        )
        Spacer(Modifier.height(18.dp))

        SectionHeader(title = "Nama tujuan")
        Spacer(Modifier.height(8.dp))
        NabunginTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = "Misal: Dana darurat 6 bulan",
        )
        Spacer(Modifier.height(18.dp))

        SectionHeader(title = "Target nominal")
        Spacer(Modifier.height(4.dp))
        AmountKeypadField(
            digits = targetDigits,
            onDigitsChange = { targetDigits = it },
            hint = "Tulis nominal penuh, tanpa titik",
        )
        Spacer(Modifier.height(18.dp))

        SectionHeader(title = "Deadline")
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Chip("Tanpa deadline", deadline == null) { deadline = null }
            listOf(1L, 3L, 6L, 12L).forEach { months ->
                val candidate = LocalDate.now().plusMonths(months)
                Chip("${months} bln", deadline == candidate) { deadline = candidate }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = deadline?.let { Dates.full(it) } ?: "Belum dipilih",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.muted,
                modifier = Modifier.weight(1f),
            )
            NButton(
                text = "Pilih tanggal",
                onClick = {
                    val base = deadline ?: LocalDate.now().plusMonths(1)
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            deadline = LocalDate.of(year, month + 1, day)
                        },
                        base.year,
                        base.monthValue - 1,
                        base.dayOfMonth,
                    ).show()
                },
                tone = ButtonTone.Quiet,
                fillWidth = false,
            )
        }
        Spacer(Modifier.height(18.dp))

        SectionHeader(title = "Warna")
        Spacer(Modifier.height(10.dp))
        SwatchRow(colors = GoalAccents, selectedIndex = accentIndex, onSelect = { accentIndex = it })
        Spacer(Modifier.height(18.dp))

        SectionHeader(title = "Ikon")
        Spacer(Modifier.height(10.dp))
        AppIcons.pickerKeys.chunked(5).forEach { rowKeys ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 10.dp)) {
                rowKeys.forEach { key ->
                    Box(
                        modifier = Modifier
                            .size(44.dp),
                    ) {
                        IconBubble(
                            icon = AppIcons.of(key),
                            accent = if (key == iconKey) GoalAccents[accentIndex.coerceIn(0, GoalAccents.lastIndex)] else colors.muted,
                            size = 44.dp,
                            iconSize = 22.dp,
                            modifier = Modifier
                                .size(44.dp)
                                .clickablePick { iconKey = key },
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        SectionHeader(title = "Kategori")
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            categories.take(3).forEach { option ->
                Chip(option, category == option) { category = option }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            categories.drop(3).forEach { option ->
                Chip(option, category == option) { category = option }
            }
        }
        Spacer(Modifier.height(24.dp))

        SectionHeader(title = "Auto-save (opsional)")
        Spacer(Modifier.height(10.dp))
        NabunginCard(padding = PaddingValues(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (ruleEnabled) "Auto-save aktif" else "Auto-save mati",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface,
                    )
                    Text(
                        text = if (ruleEnabled) {
                            "${Money.format(Money.parseDigits(ruleAmountDigits))} - ${ruleInterval.label} - %02d:00".format(ruleHour)
                        } else {
                            "Setoran berkala otomatis lewat WorkManager"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.muted,
                    )
                }
                Chip(if (ruleEnabled) "Aktif" else "Mati", ruleEnabled) { ruleEnabled = !ruleEnabled }
            }
            if (ruleEnabled) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SaveInterval.entries.forEach { option ->
                        Chip(option.label, ruleInterval == option) { ruleInterval = option }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(6, 9, 12, 18, 20).forEach { hour ->
                        Chip("%02d:00".format(hour), ruleHour == hour) { ruleHour = hour }
                    }
                }
                Spacer(Modifier.height(10.dp))
                NButton(
                    text = if (Money.parseDigits(ruleAmountDigits) > 0L) "Ubah nominal auto-save" else "Atur nominal auto-save",
                    onClick = { showRuleSheet = true },
                    tone = ButtonTone.Quiet,
                )
            }
        }

        if (error != null) {
            Spacer(Modifier.height(12.dp))
            Text(error ?: "", style = MaterialTheme.typography.bodyMedium, color = colors.danger)
        }

        Spacer(Modifier.height(24.dp))
        NButton(
            text = if (goalId > 0L) "Simpan perubahan" else "Bikin tujuan",
            onClick = {
                when {
                    name.isBlank() -> error = "Nama tujuan wajib diisi"
                    target <= 0L -> error = "Target nominal harus lebih dari 0"
                    else -> {
                        error = null
                        val goal = Goal(
                            id = if (goalId > 0L) goalId else 0L,
                            name = name,
                            targetAmount = target,
                            deadline = deadline,
                            accentIndex = accentIndex,
                            iconKey = iconKey,
                            category = category,
                        )
                        val rule = if (ruleEnabled && Money.parseDigits(ruleAmountDigits) > 0L) {
                            AutoRule(
                                goalId = goal.id,
                                amount = Money.parseDigits(ruleAmountDigits),
                                interval = ruleInterval,
                                hour = ruleHour,
                                minute = 0,
                                enabled = true,
                            )
                        } else {
                            null
                        }
                        vm.save(goal, rule) { onDone() }
                    }
                }
            },
            enabled = loaded,
            icon = AppIcons.Check,
        )
        Spacer(Modifier.height(10.dp))
        NButton(text = "Batal", onClick = onBack, tone = ButtonTone.Ghost)
        Spacer(Modifier.height(40.dp))
    }

    if (showRuleSheet) {
        val digitsState = ruleAmountDigits
        BottomSheet(visible = true, onDismiss = { showRuleSheet = false }) {
            Column(modifier = Modifier.fillMaxWidth()) {
                SheetTitle(
                    title = "Nominal auto-save",
                    subtitle = "Per ${ruleInterval.label.lowercase()}, jam %02d:00".format(ruleHour),
                    onClose = { showRuleSheet = false },
                )
                Spacer(Modifier.height(16.dp))
                AmountKeypadField(
                    digits = digitsState,
                    onDigitsChange = { ruleAmountDigits = it },
                    hint = "Nominal sekali eksekusi",
                )
                Spacer(Modifier.height(12.dp))
                NButton(text = "Simpan nominal", onClick = { showRuleSheet = false }, icon = AppIcons.Check)
            }
        }
    }
}

private fun Modifier.clickablePick(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)
