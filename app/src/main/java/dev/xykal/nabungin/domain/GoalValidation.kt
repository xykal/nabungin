package dev.xykal.nabungin.domain

import java.time.LocalDate

/** Errors are tied to the field instead of appearing far below the keypad. */
data class GoalErrors(val name: String? = null, val target: String? = null,
    val deadline: String? = null, val ruleAmount: String? = null) {
    val isValid: Boolean get() = name == null && target == null && deadline == null && ruleAmount == null
    val first: String? get() = name ?: target ?: deadline ?: ruleAmount
}

object GoalValidation {
    fun validate(name: String, target: Long, deadline: LocalDate?,
        autoSaveEnabled: Boolean, autoSaveAmount: Long, today: LocalDate = LocalDate.now()): GoalErrors = GoalErrors(
        name = if (name.isBlank()) "Tulis nama tujuan dulu." else null,
        target = if (target <= 0L) "Target harus lebih dari Rp 0." else null,
        deadline = if (deadline != null && !deadline.isAfter(today)) "Pilih tanggal setelah hari ini." else null,
        ruleAmount = if (autoSaveEnabled && autoSaveAmount <= 0L) "Isi nominal auto-save atau matikan jadwal." else null,
    )
}
