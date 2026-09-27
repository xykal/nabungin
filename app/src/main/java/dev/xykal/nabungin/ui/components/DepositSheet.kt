package dev.xykal.nabungin.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.xykal.nabungin.domain.format.Dates
import dev.xykal.nabungin.domain.format.Money
import dev.xykal.nabungin.domain.model.Goal
import dev.xykal.nabungin.ui.theme.LocalNabunginColors
import java.time.LocalDate

/** Sheet setoran: keypad numerik sendiri, chip tanggal cepat, catatan opsional. */
@Composable
fun DepositSheet(
    goal: Goal,
    onDismiss: () -> Unit,
    onSave: (amount: Long, note: String, day: LocalDate) -> Unit,
) {
    val colors = LocalNabunginColors.current
    var digits by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var day by remember { mutableStateOf(LocalDate.now()) }
    val amount = Money.parseDigits(digits)
    val overTarget = goal.targetAmount > 0L && amount > goal.remaining

    BottomSheet(visible = true, onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SheetTitle(
                title = "Tabung sekarang",
                subtitle = goal.name,
                onClose = onDismiss,
            )
            Spacer(Modifier.height(18.dp))
            Text("NOMINAL", style = MaterialTheme.typography.labelSmall, color = colors.muted)
            AmountDisplay(amount = amount, fontSize = 38)
            Text(
                text = if (goal.remaining > 0L && !overTarget) "Sisa target ${Money.format(goal.remaining)}" else "Target tercapai dengan tabungan ini",
                style = MaterialTheme.typography.bodySmall,
                color = if (overTarget) colors.warning else colors.muted,
            )
            if (goal.dailyPlan > 0L) {
                Spacer(Modifier.height(8.dp))
                Chip(label = "Pakai rencana harian ${Money.format(goal.dailyPlan)}", selected = false) {
                    digits = goal.dailyPlan.toString()
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(10_000L, 25_000L, 50_000L, 100_000L, 250_000L, 500_000L).forEach { quick ->
                    Chip(
                        label = Money.formatCompact(quick),
                        selected = false,
                        onClick = { digits = (Money.parseDigits(digits) + quick).toString() },
                    )
                }
                if (goal.remaining in 1..9_999_999L) {
                    Chip(
                        label = "Isi sisa",
                        selected = false,
                        onClick = { digits = goal.remaining.toString() },
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                Chip("Hari ini", day == LocalDate.now()) { day = LocalDate.now() }
                Chip("Kemarin", day == LocalDate.now().minusDays(1)) { day = LocalDate.now().minusDays(1) }
            }
            Spacer(Modifier.height(14.dp))
            NabunginTextField(
                value = note,
                onValueChange = { note = it },
                placeholder = "Catatan (opsional)",
                maxChars = 80,
            )
            Spacer(Modifier.height(16.dp))
            Keypad(
                onDigit = { digit -> if (digits.length < 12) digits += digit },
                onBackspace = { digits = digits.dropLast(1) },
            )
            Spacer(Modifier.height(14.dp))
            NButton(
                text = if (amount > 0L) "Tabung ${Money.format(amount)}" else "Masukin nominal dulu",
                onClick = {
                    onSave(amount, note, day)
                    onDismiss()
                },
                enabled = amount > 0L,
            )
        }
    }
}

/** Keypad angka generik untuk form nominal di tempat lain. */
@Composable
fun AmountKeypadField(
    digits: String,
    onDigitsChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    hint: String? = null,
) {
    val colors = LocalNabunginColors.current
    Column(modifier = modifier.fillMaxWidth()) {
        AmountDisplay(amount = Money.parseDigits(digits), fontSize = 34)
        if (hint != null) {
            Text(hint, style = MaterialTheme.typography.bodySmall, color = colors.muted)
        }
        Spacer(Modifier.height(12.dp))
        Keypad(
            onDigit = { digit -> if (digits.length < 12) onDigitsChange(digits + digit) },
            onBackspace = { onDigitsChange(digits.dropLast(1)) },
        )
    }
}
