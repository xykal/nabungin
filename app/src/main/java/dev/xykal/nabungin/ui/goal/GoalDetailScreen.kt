package dev.xykal.nabungin.ui.goal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.xykal.nabungin.core.appViewModel
import dev.xykal.nabungin.domain.SavingsMath
import dev.xykal.nabungin.domain.format.Dates
import dev.xykal.nabungin.domain.format.Money
import dev.xykal.nabungin.domain.model.AutoRule
import dev.xykal.nabungin.domain.model.Deposit
import dev.xykal.nabungin.domain.model.Goal
import dev.xykal.nabungin.domain.model.SaveInterval
import dev.xykal.nabungin.ui.components.AmountDisplay
import dev.xykal.nabungin.ui.components.AmountText
import dev.xykal.nabungin.ui.components.BottomSheet
import dev.xykal.nabungin.ui.components.ButtonTone
import dev.xykal.nabungin.ui.components.Chip
import dev.xykal.nabungin.ui.components.ConfirmOverlay
import dev.xykal.nabungin.ui.components.DepositSheet
import dev.xykal.nabungin.ui.components.EmptyState
import dev.xykal.nabungin.ui.components.IconBubble
import dev.xykal.nabungin.ui.components.IconSquareButton
import dev.xykal.nabungin.ui.components.Keypad
import dev.xykal.nabungin.ui.components.LabelValueRow
import dev.xykal.nabungin.ui.components.NButton
import dev.xykal.nabungin.ui.components.NabunginCard
import dev.xykal.nabungin.ui.components.NabunginTopBar
import dev.xykal.nabungin.ui.components.ProgressRing
import dev.xykal.nabungin.ui.components.SectionHeader
import dev.xykal.nabungin.ui.components.SheetTitle
import dev.xykal.nabungin.ui.components.SourceBadge
import dev.xykal.nabungin.ui.icons.AppIcons
import dev.xykal.nabungin.ui.theme.GoalAccents
import dev.xykal.nabungin.ui.theme.LocalNabunginColors

private val timeOptions = listOf(6 to 0, 9 to 0, 12 to 0, 18 to 0, 20 to 0, 21 to 0)
private fun timeLabel(hour: Int, minute: Int): String = "%02d:%02d".format(hour, minute)

@Composable
fun GoalDetailScreen(
    goalId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
) {
    val colors = LocalNabunginColors.current
    val vm = appViewModel { GoalDetailViewModel(it, goalId) }
    val goal by vm.goal.collectAsStateWithLifecycle()
    val deposits by vm.deposits.collectAsStateWithLifecycle()
    val rule by vm.rule.collectAsStateWithLifecycle()

    var showDeposit by remember { mutableStateOf(false) }
    var showRuleEditor by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    val current = goal
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(14.dp))
        NabunginTopBar(
            title = current?.name ?: "Tujuan",
            onBack = onBack,
            actions = {
                if (current != null) {
                    IconSquareButton(
                        icon = AppIcons.Pencil,
                        onClick = { onEdit(current.id) },
                        contentDescription = "Edit",
                    )
                    IconSquareButton(
                        icon = AppIcons.Trash,
                        onClick = { confirmDelete = true },
                        contentDescription = "Hapus",
                        tint = colors.danger,
                    )
                }
            },
        )

        if (current == null) {
            Spacer(Modifier.height(40.dp))
            EmptyState(
                icon = AppIcons.Target,
                title = "Tujuan tidak ditemukan",
                body = "Data mungkin sudah dihapus.",
            )
            return@Column
        }

        val accent = GoalAccents[current.accentIndex.coerceIn(0, GoalAccents.lastIndex)]
        val pace = SavingsMath.pace(current)
        val currentRule = rule

        Spacer(Modifier.height(22.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ProgressRing(progress = current.progress, size = 168.dp, stroke = 14.dp, progressColor = accent) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(current.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.displayMedium,
                        color = colors.onSurface,
                    )
                    Text("terkumpul", style = MaterialTheme.typography.bodySmall, color = colors.muted)
                }
            }
            Spacer(Modifier.height(16.dp))
            AmountText(amount = current.saved, color = accent)
            Text(
                text = "dari target ${Money.format(current.targetAmount)}",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.muted,
            )
        }

        Spacer(Modifier.height(22.dp))
        NabunginCard(padding = PaddingValues(18.dp)) {
            LabelValueRow("Sisa ke target", Money.format(current.remaining))
            LabelValueRow(
                label = "Perlu nabung",
                value = if (pace.dailyNeeded > 0L) "${Money.format(pace.dailyNeeded)}/hari" else "-",
            )
            LabelValueRow(
                label = "Deadline",
                value = current.deadline?.let { "${Dates.full(it)} (${Dates.daysLeftLabel(it)})" } ?: "Tanpa deadline",
            )
            LabelValueRow(
                label = "Estimasi tercapai",
                value = pace.etaDays?.let { hari -> LocalEta.describe(hari) } ?: "Belum bisa diprediksi",
                valueColor = if (pace.onTrack) colors.accent else colors.warning,
            )
            LabelValueRow("Rata-rata harian", Money.format(SavingsMath.averagePerDay(current)))
            LabelValueRow("Jumlah setoran", "${current.depositCount}x")
        }

        Spacer(Modifier.height(18.dp))
        SectionHeader(
            title = "Auto-save",
            trailing = {
                NButton(
                    text = if (rule?.enabled == true) "Atur" else "Aktifkan",
                    onClick = { showRuleEditor = true },
                    tone = ButtonTone.Quiet,
                    fillWidth = false,
                )
            },
        )
        Spacer(Modifier.height(10.dp))
        NabunginCard(padding = PaddingValues(16.dp)) {
            val active = rule?.enabled == true
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBubble(
                    icon = AppIcons.Clock,
                    accent = if (active) colors.accent else colors.muted,
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (active) "Jalan otomatis" else "Belum aktif",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface,
                    )
                    Text(
                        text = if (active && currentRule != null) {
                            "${Money.format(currentRule.amount)} - ${currentRule.interval.label} - ${timeLabel(currentRule.hour, currentRule.minute)}"
                        } else {
                            "Setor berkala tanpa perlu ingat-inget"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.muted,
                    )
                }
                if (active) {
                    Text(
                        text = rule?.lastRun?.let { "Terakhir ${Dates.relative(it)}" } ?: "Belum jalan",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.muted,
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        SectionHeader(title = "Riwayat setoran")
        Spacer(Modifier.height(8.dp))
        if (deposits.isEmpty()) {
            EmptyState(
                icon = AppIcons.Ledger,
                title = "Belum ada setoran",
                body = "Tiap setoran bakal muncul di sini, lengkap dengan sumbernya (manual atau auto).",
            )
        } else {
            deposits.take(30).forEach { deposit ->
                DepositRow(
                    deposit = deposit,
                    onDelete = { vm.deleteDeposit(deposit.id) },
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(20.dp))
        NButton(text = "Setor sekarang", onClick = { showDeposit = true }, icon = AppIcons.Plus)
        Spacer(Modifier.height(10.dp))
        NButton(
            text = "Hapus tujuan ini",
            onClick = { confirmDelete = true },
            tone = ButtonTone.Ghost,
            icon = AppIcons.Trash,
        )
        Spacer(Modifier.height(40.dp))
    }

    if (showDeposit && current != null) {
        DepositSheet(
            goal = current,
            onDismiss = { showDeposit = false },
            onSave = { amount, note, day -> vm.addDeposit(amount, note, day) },
        )
    }

    if (showRuleEditor && current != null) {
        RuleEditorSheet(
            goal = current,
            existing = rule,
            onDismiss = { showRuleEditor = false },
            onSave = { vm.saveRule(it) },
            onDelete = { vm.deleteRule() },
        )
    }

    if (confirmDelete) {
        ConfirmOverlay(
            visible = true,
            title = "Hapus \"${current?.name ?: ""}\"?",
            body = "Semua setoran yang nyangkut di tujuan ini ikut terhapus permanen. Nggak bisa di-undo.",
            confirmLabel = "Hapus permanen",
            onConfirm = {
                confirmDelete = false
                vm.deleteGoal(onBack)
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun DepositRow(deposit: Deposit, onDelete: () -> Unit) {
    val colors = LocalNabunginColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = deposit.note.ifBlank { "Setoran" },
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurface,
                maxLines = 1,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = Dates.relative(deposit.day),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.muted,
                )
                Spacer(Modifier.width(8.dp))
                SourceBadge(isAuto = deposit.source != dev.xykal.nabungin.domain.model.DepositSource.MANUAL)
            }
        }
        AmountText(amount = deposit.amount, compact = true)
        Spacer(Modifier.width(6.dp))
        IconSquareButton(
            icon = AppIcons.Close,
            onClick = onDelete,
            contentDescription = "Hapus setoran",
            tint = colors.muted,
        )
    }
}

@Composable
private fun RuleEditorSheet(
    goal: Goal,
    existing: AutoRule?,
    onDismiss: () -> Unit,
    onSave: (AutoRule) -> Unit,
    onDelete: () -> Unit,
) {
    val colors = LocalNabunginColors.current
    var digits by remember { mutableStateOf(existing?.amount?.takeIf { it > 0 }?.toString() ?: "") }
    var interval by remember { mutableStateOf(existing?.interval ?: SaveInterval.DAILY) }
    var hour by remember { mutableStateOf(existing?.hour ?: 20) }
    var minute by remember { mutableStateOf(existing?.minute ?: 0) }
    var enabled by remember { mutableStateOf(existing?.enabled ?: true) }
    val amount = Money.parseDigits(digits)

    BottomSheet(visible = true, onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SheetTitle(
                title = "Auto-save",
                subtitle = "Setor otomatis ke ${goal.name}",
                onClose = onDismiss,
            )
            Spacer(Modifier.height(16.dp))
            Text("NOMINAL PER JADWAL", style = MaterialTheme.typography.labelSmall, color = colors.muted)
            AmountDisplay(amount = amount, fontSize = 34)
            Spacer(Modifier.height(12.dp))
            Text("INTERVAL", style = MaterialTheme.typography.labelSmall, color = colors.muted)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SaveInterval.entries.forEach { option ->
                    Chip(option.label, interval == option) { interval = option }
                }
            }
            Spacer(Modifier.height(14.dp))
            Text("JAM EKSEKUSI", style = MaterialTheme.typography.labelSmall, color = colors.muted)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                timeOptions.take(3).forEach { (h, m) ->
                    Chip(timeLabel(h, m), hour == h && minute == m) {
                        hour = h
                        minute = m
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                timeOptions.drop(3).forEach { (h, m) ->
                    Chip(timeLabel(h, m), hour == h && minute == m) {
                        hour = h
                        minute = m
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Aktifkan auto-save",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Chip(if (enabled) "Aktif" else "Mati", enabled) { enabled = !enabled }
            }
            Spacer(Modifier.height(12.dp))
            Keypad(
                onDigit = { digit -> if (digits.length < 10) digits += digit },
                onBackspace = { digits = digits.dropLast(1) },
            )
            Spacer(Modifier.height(14.dp))
            NButton(
                text = "Simpan aturan",
                onClick = {
                    onSave(
                        AutoRule(
                            id = existing?.id ?: 0L,
                            goalId = goal.id,
                            amount = amount,
                            interval = interval,
                            hour = hour,
                            minute = minute,
                            enabled = enabled && amount > 0L,
                            lastRun = existing?.lastRun,
                        ),
                    )
                    onDismiss()
                },
                enabled = amount > 0L,
            )
            if (existing != null) {
                Spacer(Modifier.height(8.dp))
                NButton(
                    text = "Matikan & hapus aturan",
                    onClick = {
                        onDelete()
                        onDismiss()
                    },
                    tone = ButtonTone.Ghost,
                )
            }
        }
    }
}

private object LocalEta {
    fun describe(days: Long): String {
        val date = java.time.LocalDate.now().plusDays(days)
        return "${Dates.full(date)} (sekitar $days hari)"
    }
}
