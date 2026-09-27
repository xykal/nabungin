package dev.xykal.nabungin.ui.stats

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.xykal.nabungin.core.appViewModel
import dev.xykal.nabungin.domain.SavingsMath
import dev.xykal.nabungin.domain.format.Dates
import dev.xykal.nabungin.domain.format.Money
import dev.xykal.nabungin.ui.components.BarChart
import dev.xykal.nabungin.ui.components.animatedAmount
import dev.xykal.nabungin.ui.components.EmptyState
import dev.xykal.nabungin.ui.components.IconBubble
import dev.xykal.nabungin.ui.components.LabelValueRow
import dev.xykal.nabungin.ui.components.NabunginCard
import dev.xykal.nabungin.ui.components.ProgressRing
import dev.xykal.nabungin.ui.components.SectionHeader
import dev.xykal.nabungin.ui.home.StatsViewModel
import dev.xykal.nabungin.ui.icons.AppIcons
import dev.xykal.nabungin.ui.theme.GoalAccents
import dev.xykal.nabungin.ui.theme.LocalNabunginColors
import java.time.LocalDate

@Composable
fun StatsScreen() {
    val colors = LocalNabunginColors.current
    val vm = appViewModel { StatsViewModel(it) }
    val snapshot by vm.snapshot.collectAsStateWithLifecycle()

    val last30Total = snapshot.last30.sumOf { it.total }
    val activeDays = snapshot.last30.count { it.total > 0L }
    val perDay = if (activeDays > 0) last30Total / activeDays else 0L

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(14.dp))
        Text("STATISTIK", style = MaterialTheme.typography.labelSmall, color = colors.muted)
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Progres lu kebaca",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onSurface,
        )
        Spacer(Modifier.height(16.dp))

        NabunginCard(padding = PaddingValues(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    LabelValueRow("Total terkumpul", Money.format(animatedAmount(snapshot.totalSaved)))
                    LabelValueRow("Total target", Money.format(snapshot.totalTarget))
                    LabelValueRow("Bulan ini", Money.format(snapshot.monthTotal))
                    LabelValueRow("30 hari terakhir", Money.format(last30Total))
                    LabelValueRow("Rata-rata per hari aktif", Money.format(perDay))
                    LabelValueRow("Streak sekarang", "${snapshot.streak} hari")
                    LabelValueRow("Rencana seluruh tabungan", "${Money.format(snapshot.goals.sumOf { it.dailyPlan })}/hari")
                }
                ProgressRing(
                    progress = if (snapshot.totalTarget > 0L) {
                        (snapshot.totalSaved.toDouble() / snapshot.totalTarget.toDouble()).toFloat()
                    } else {
                        0f
                    },
                    size = 92.dp,
                    stroke = 9.dp,
                ) {
                    Text(
                        text = if (snapshot.totalTarget > 0L) "${(snapshot.totalSaved.toDouble() / snapshot.totalTarget * 100).toInt()}%" else "0%",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface,
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        SectionHeader(title = "Distribusi 30 hari")
        Spacer(Modifier.height(10.dp))
        NabunginCard(padding = PaddingValues(16.dp)) {
            BarChart(data = snapshot.last30, height = 120.dp)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(Dates.full(LocalDate.now().minusDays(29)).takeLast(6), style = MaterialTheme.typography.bodySmall, color = colors.muted)
                Text(Dates.full(LocalDate.now()).takeLast(6), style = MaterialTheme.typography.bodySmall, color = colors.muted)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$activeDays hari ada tabungan masuk dari 30 hari terakhir",
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
            )
        }

        Spacer(Modifier.height(20.dp))
        SectionHeader(title = "Peringkat tujuan")
        Spacer(Modifier.height(10.dp))
        if (snapshot.goals.isEmpty()) {
            EmptyState(
                icon = AppIcons.Chart,
                title = "Belum ada data",
                body = "Bikin tujuan dan mulai nabung, statistik bakal keisi otomatis.",
            )
        } else {
            snapshot.goals.sortedByDescending { it.saved }.forEach { goal ->
                val pace = SavingsMath.pace(goal)
                NabunginCard(padding = PaddingValues(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBubble(
                            icon = AppIcons.of(goal.iconKey),
                            accent = GoalAccents[goal.accentIndex.coerceIn(0, GoalAccents.lastIndex)],
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = goal.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.onSurface,
                            )
                            Text(
                                text = "${(goal.progress * 100).toInt()}% dari ${Money.formatCompact(goal.targetAmount)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.muted,
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = Money.format(goal.saved),
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.onSurface,
                            )
                            Text(
                                text = pace.etaDays?.let { runCatching { "ETA ${Dates.full(LocalDate.now().plusDays(it))}" }.getOrDefault("ETA belum tersedia") } ?: "ETA belum bisa dihitung",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (pace.onTrack) colors.accent else colors.warning,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }
        Spacer(Modifier.height(90.dp))
    }
}
