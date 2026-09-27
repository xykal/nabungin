package dev.xykal.nabungin.ui.history

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.xykal.nabungin.core.appViewModel
import dev.xykal.nabungin.domain.SavingsMath
import dev.xykal.nabungin.domain.format.Dates
import dev.xykal.nabungin.domain.format.Money
import dev.xykal.nabungin.domain.model.DepositSource
import dev.xykal.nabungin.ui.components.AmountText
import dev.xykal.nabungin.ui.components.Chip
import dev.xykal.nabungin.ui.components.EmptyState
import dev.xykal.nabungin.ui.components.IconSquareButton
import dev.xykal.nabungin.ui.components.NabunginCard
import dev.xykal.nabungin.ui.components.SourceBadge
import dev.xykal.nabungin.ui.icons.AppIcons
import dev.xykal.nabungin.ui.theme.LocalNabunginColors
import java.time.LocalDate

@Composable
fun HistoryScreen(onOpenGoal: (Long) -> Unit) {
    val colors = LocalNabunginColors.current
    val vm = appViewModel { HistoryViewModel(it) }
    val deposits by vm.deposits.collectAsStateWithLifecycle()
    val goals by vm.goals.collectAsStateWithLifecycle()
    var filterGoalId by remember { mutableStateOf<Long?>(null) }

    val goalNames = goals.associate { it.id to it.name }
    val filtered = deposits.filter { filterGoalId == null || it.goalId == filterGoalId }
    val grouped = filtered.groupBy { it.day }.toSortedMap(compareByDescending { it })
    val monthTotal = SavingsMath.monthTotal(deposits, LocalDate.now())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(14.dp))
        Text("RIWAYAT", style = MaterialTheme.typography.labelSmall, color = colors.muted)
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Bulan ini ${Money.format(monthTotal)}",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onSurface,
        )
        Text(
            text = "${deposits.size} kali nabung tercatat, total ${Money.format(deposits.sumOf { it.amount })}",
            style = MaterialTheme.typography.bodySmall,
            color = colors.muted,
        )
        Spacer(Modifier.height(16.dp))

        if (goals.size > 1) {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Chip("Semua", filterGoalId == null) { filterGoalId = null }
                goals.forEach { goal ->
                    Chip(goal.name, filterGoalId == goal.id) { filterGoalId = goal.id }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        if (grouped.isEmpty()) {
            EmptyState(
                icon = AppIcons.Ledger,
                title = "Belum ada catatan",
                body = "Tabungan yang lu masukin bakal muncul di sini, dikelompokkan per tanggal.",
            )
        } else {
            grouped.forEach { (day, items) ->
                NabunginCard(padding = PaddingValues(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = Dates.relative(day),
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.onSurface,
                            )
                            Text(
                                text = Dates.full(day),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.muted,
                            )
                        }
                        AmountText(amount = items.sumOf { it.amount }, compact = true)
                    }
                    Spacer(Modifier.height(10.dp))
                    items.forEach { deposit ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = goalNames[deposit.goalId] ?: "Tujuan terhapus",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colors.onSurface,
                                )
                                if (deposit.note.isNotBlank()) {
                                    Text(
                                        text = deposit.note,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.muted,
                                    )
                                }
                            }
                            if (deposit.source != DepositSource.MANUAL) {
                                SourceBadge(isAuto = true)
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(
                                text = Money.format(deposit.amount),
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.onSurface,
                            )
                            Spacer(Modifier.width(6.dp))
                            IconSquareButton(
                                icon = AppIcons.Close,
                                onClick = { vm.delete(deposit.id) },
                                contentDescription = "Hapus",
                                tint = colors.muted,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
        Spacer(Modifier.height(90.dp))
    }
}
