package dev.xykal.nabungin.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.xykal.nabungin.core.appViewModel
import dev.xykal.nabungin.domain.SavingsMath
import dev.xykal.nabungin.ui.components.animatedAmount
import androidx.compose.ui.text.style.TextOverflow
import dev.xykal.nabungin.domain.format.Dates
import dev.xykal.nabungin.domain.format.Money
import dev.xykal.nabungin.domain.model.Goal
import dev.xykal.nabungin.ui.components.AmountText
import dev.xykal.nabungin.ui.components.BarChart
import dev.xykal.nabungin.ui.components.DepositSheet
import dev.xykal.nabungin.ui.components.EmptyState
import dev.xykal.nabungin.ui.components.IconBubble
import dev.xykal.nabungin.ui.components.IconSquareButton
import dev.xykal.nabungin.ui.components.NButton
import dev.xykal.nabungin.ui.components.NabunginCard
import dev.xykal.nabungin.ui.components.ProgressRing
import dev.xykal.nabungin.ui.components.ProgressTrack
import dev.xykal.nabungin.ui.components.SectionHeader
import dev.xykal.nabungin.ui.components.StatTile
import dev.xykal.nabungin.ui.icons.AppIcons
import dev.xykal.nabungin.ui.theme.GoalAccents
import dev.xykal.nabungin.ui.theme.LocalNabunginColors

@Composable
fun HomeScreen(
    onOpenGoal: (Long) -> Unit,
    onNewGoal: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenStats: () -> Unit,
) {
    val colors = LocalNabunginColors.current
    val vm = appViewModel { HomeViewModel(it) }
    val snapshot by vm.snapshot.collectAsStateWithLifecycle()
    var depositTarget by remember { mutableStateOf<Goal?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("NABUNGIN", style = MaterialTheme.typography.labelSmall, color = colors.muted)
                Text(
                    text = "Tabungan lu hari ini",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.muted,
                )
            }
            IconSquareButton(icon = AppIcons.Sliders, onClick = onOpenSettings, contentDescription = "Setelan")
        }
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Column(modifier = Modifier.weight(1f)) {
                Text("TOTAL TERKUMPUL", style = MaterialTheme.typography.labelSmall, color = colors.muted)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = Money.format(animatedAmount(snapshot.totalSaved)),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 34.sp,
                    lineHeight = 40.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = colors.onSurface,
                )
                Text(
                    text = "dari target ${Money.format(snapshot.totalTarget)} di ${snapshot.goals.size} tujuan",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.muted,
                )
            }
            ProgressRing(
                progress = if (snapshot.totalTarget > 0L) {
                    (snapshot.totalSaved.toDouble() / snapshot.totalTarget.toDouble()).toFloat()
                } else {
                    0f
                },
                size = 66.dp,
                stroke = 7.dp,
            ) {
                Text(
                    text = if (snapshot.totalTarget > 0L) {
                        "${(snapshot.totalSaved.toDouble() / snapshot.totalTarget * 100).toInt()}%"
                    } else {
                        "0%"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface,
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile("Hari ini", Money.formatCompact(snapshot.todayTotal), Modifier.weight(1f))
            StatTile("Bulan ini", Money.formatCompact(snapshot.monthTotal), Modifier.weight(1f))
            StatTile(
                label = "Streak",
                value = "${snapshot.streak} hari",
                modifier = Modifier.weight(1f),
                valueColor = if (snapshot.streak > 0) colors.accent else colors.onSurface,
            )
        }
        Spacer(Modifier.height(16.dp))
        NabunginCard(
            onClick = onOpenStats,
            padding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "30 HARI TERAKHIR",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.muted,
                    modifier = Modifier.weight(1f),
                )
                Icon(AppIcons.Chart, contentDescription = null, tint = colors.muted, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(10.dp))
            BarChart(data = snapshot.last30)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Total ${Money.format(snapshot.last30.sumOf { it.total })} masuk dalam 30 hari",
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
            )
        }
        if (snapshot.goals.any { it.dailyPlan > 0L }) {
            Spacer(Modifier.height(18.dp))
            NabunginCard(padding = androidx.compose.foundation.layout.PaddingValues(18.dp)) {
                Text("RENCANA HARI INI", style = MaterialTheme.typography.labelSmall, color = colors.muted)
                Spacer(Modifier.height(6.dp))
                Text(Money.format(snapshot.goals.filter { !it.isReached }.sumOf { it.dailyPlan }),
                    style = MaterialTheme.typography.headlineMedium, color = colors.accent)
                Text("Gabungan target harian dari tabungan yang belum selesai. Ini rencana, bukan uang yang sudah ditabung.",
                    style = MaterialTheme.typography.bodySmall, color = colors.muted)
            }
        }
        Spacer(Modifier.height(24.dp))
        SectionHeader(
            title = "Tabungan lu",
            trailing = {
                NButton(
                    text = "+ Tabungan",
                    onClick = onNewGoal,
                    tone = dev.xykal.nabungin.ui.components.ButtonTone.Quiet,
                    fillWidth = false,
                )
            },
        )
        Spacer(Modifier.height(12.dp))
        if (snapshot.goals.isEmpty()) {
            EmptyState(
                icon = AppIcons.Target,
                title = "Belum ada tabungan",
                body = "Bikin tujuan dulu: dana darurat, DP rumah, atau gadget baru. Nabungin bakal hitung pace harian lu.",
                action = { NButton(text = "Buat tabungan", onClick = onNewGoal, fillWidth = false) },
            )
        } else {
            snapshot.goals.forEach { goal ->
                GoalCard(
                    goal = goal,
                    onOpen = { onOpenGoal(goal.id) },
                    onDeposit = { depositTarget = goal },
                )
                Spacer(Modifier.height(12.dp))
            }
        }
        Spacer(Modifier.height(90.dp))
    }

    depositTarget?.let { goal ->
        DepositSheet(
            goal = goal,
            onDismiss = { depositTarget = null },
            onSave = { amount, note, day -> vm.addDeposit(goal.id, amount, note, day) },
        )
    }
}

@Composable
private fun GoalCard(
    goal: Goal,
    onOpen: () -> Unit,
    onDeposit: () -> Unit,
) {
    val colors = LocalNabunginColors.current
    val accent = GoalAccents[goal.accentIndex.coerceIn(0, GoalAccents.lastIndex)]
    NabunginCard(onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBubble(icon = AppIcons.of(goal.iconKey), accent = accent)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goal.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onSurface,
                    maxLines = 1,
                )
                Text(
                    text = buildString {
                        append(goal.category)
                        append(" - ")
                        append("${(goal.progress * 100).toInt()}%")
                        goal.deadline?.let { append(" - ${Dates.daysLeftLabel(it)}") }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.muted,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                AmountText(amount = goal.saved, compact = true, color = accent)
                Text(
                    text = "dari ${Money.formatCompact(goal.targetAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.muted,
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        ProgressTrack(goal.progress, accent)
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(
                progress = goal.progress,
                size = 52.dp,
                stroke = 6.dp,
                progressColor = accent,
            ) {
                Text(
                    text = "${(goal.progress * 100).toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurface,
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (goal.isReached) "Target tercapai" else "Kurang ${Money.format(goal.remaining)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (goal.isReached) colors.accent else colors.onSurface,
                )
                Text(
                    text = if (goal.dailyPlan > 0L) "Rencana ${Money.format(goal.dailyPlan)}/hari" else if (goal.depositCount == 0) "Belum ada tabungan masuk" else "${goal.depositCount} kali nabung",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.muted,
                )
            }
            NButton(text = "Tabung", onClick = onDeposit, tone = dev.xykal.nabungin.ui.components.ButtonTone.Ghost, fillWidth = false)
        }
    }
}
