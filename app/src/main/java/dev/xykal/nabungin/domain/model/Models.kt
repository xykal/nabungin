package dev.xykal.nabungin.domain.model

import java.time.LocalDate

/** Cara setoran masuk ke ledger. */
enum class DepositSource(val key: String) {
    MANUAL("manual"),
    AUTO("auto"),
    IMPORT("import");

    companion object {
        fun fromKey(key: String): DepositSource = entries.firstOrNull { it.key == key } ?: MANUAL
    }
}

/** Interval untuk aturan auto-save. */
enum class SaveInterval(val key: String, val label: String, val days: Int) {
    DAILY("daily", "Harian", 1),
    WEEKLY("weekly", "Mingguan", 7),
    MONTHLY("monthly", "Bulanan", 30);

    companion object {
        fun fromKey(key: String): SaveInterval = entries.firstOrNull { it.key == key } ?: DAILY
    }
}

data class Goal(
    val id: Long = 0L,
    val name: String,
    val targetAmount: Long,
    val deadline: LocalDate? = null,
    val accentIndex: Int = 0,
    val iconKey: String = "coins",
    val category: String = "Umum",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val archived: Boolean = false,
    val saved: Long = 0L,
    val depositCount: Int = 0,
    val lastDepositDay: LocalDate? = null,
) {
    val remaining: Long get() = (targetAmount - saved).coerceAtLeast(0L)
    val progress: Float
        get() = if (targetAmount <= 0L) 0f else (saved.toDouble() / targetAmount.toDouble())
            .coerceIn(0.0, 1.0).toFloat()
    val isReached: Boolean get() = targetAmount > 0L && saved >= targetAmount
}

data class Deposit(
    val id: Long = 0L,
    val goalId: Long,
    val amount: Long,
    val day: LocalDate,
    val note: String = "",
    val source: DepositSource = DepositSource.MANUAL,
    val createdAtMillis: Long = System.currentTimeMillis(),
)

data class AutoRule(
    val id: Long = 0L,
    val goalId: Long,
    val amount: Long,
    val interval: SaveInterval = SaveInterval.DAILY,
    val hour: Int = 20,
    val minute: Int = 0,
    val enabled: Boolean = false,
    val lastRun: LocalDate? = null,
)

data class DayTotal(val day: LocalDate, val total: Long)

data class GoalPace(
    val dailyNeeded: Long,
    val daysLeft: Long?,
    val etaDays: Long?,
    val onTrack: Boolean,
)

data class Snapshot(
    val goals: List<Goal> = emptyList(),
    val totalSaved: Long = 0L,
    val totalTarget: Long = 0L,
    val todayTotal: Long = 0L,
    val monthTotal: Long = 0L,
    val last30: List<DayTotal> = emptyList(),
    val streak: Int = 0,
    val depositCount: Int = 0,
)
