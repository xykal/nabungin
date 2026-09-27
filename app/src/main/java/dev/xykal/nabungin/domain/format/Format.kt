package dev.xykal.nabungin.domain.format

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Format angka rupiah tanpa dependensi eksternal. Semua nominal disimpan sebagai Long (rupiah utuh). */
object Money {
    private val locale = Locale("in", "ID")
    private val grouped: NumberFormat = NumberFormat.getInstance(locale).apply { isGroupingUsed = true }

    fun format(amount: Long): String = "Rp " + grouped.format(amount)

    fun formatCompact(amount: Long): String = when {
        amount >= 1_000_000_000L -> trim(amount, 1_000_000_000L) + " M"
        amount >= 1_000_000L -> trim(amount, 1_000_000L) + " jt"
        amount >= 1_000L -> trim(amount, 1_000L) + " rb"
        else -> amount.toString()
    }

    private fun trim(amount: Long, unit: Long): String {
        val value = amount.toDouble() / unit.toDouble()
        val rounded = kotlin.math.round(value * 10.0) / 10.0
        return if (rounded % 1.0 == 0.0) rounded.toLong().toString() else rounded.toString().replace('.', ',')
    }

    /** Ambil digit dari input mentah lalu jadikan Long, aman untuk keypad maupun paste. */
    fun parseDigits(raw: String): Long {
        val digits = raw.filter { it.isDigit() }.take(15)
        return digits.toLongOrNull() ?: 0L
    }
}

object Dates {
    private val locale = Locale("in", "ID")
    private val longFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", locale)
    private val monthFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", locale)

    fun full(day: LocalDate): String = longFmt.format(day)
    fun month(day: LocalDate): String = monthFmt.format(day)

    fun relative(day: LocalDate): String {
        val today = LocalDate.now()
        return when (day) {
            today -> "Hari ini"
            today.minusDays(1) -> "Kemarin"
            today.plusDays(1) -> "Besok"
            else -> full(day)
        }
    }

    fun daysLeftLabel(day: LocalDate): String {
        val days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), day)
        return when {
            days < 0L -> "Lewat ${-days} hari"
            days == 0L -> "Hari terakhir"
            days < 31L -> "$days hari lagi"
            days < 365L -> "${days / 30} bulan lagi"
            else -> "${days / 365} tahun lagi"
        }
    }
}
