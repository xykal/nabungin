package dev.xykal.nabungin.domain

import dev.xykal.nabungin.domain.model.Deposit
import dev.xykal.nabungin.domain.model.DepositSource
import dev.xykal.nabungin.domain.model.Goal
import dev.xykal.nabungin.domain.model.GoalPace
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Semua kalkulasi murni tanpa dependensi Android supaya bisa di-unit-test dari JVM. */
object SavingsMath {

    fun pace(goal: Goal): GoalPace {
        val today = LocalDate.now()
        val daysLeft = goal.deadline?.let { ChronoUnit.DAYS.between(today, it) }
        val dailyNeeded = when {
            goal.remaining <= 0L -> 0L
            daysLeft == null -> 0L
            daysLeft <= 0L -> goal.remaining
            else -> goal.remaining / daysLeft + if (goal.remaining % daysLeft != 0L) 1L else 0L
        }
        val etaDays = etaFromPace(goal.saved, goal.targetAmount,
            if (goal.dailyPlan > 0L) goal.dailyPlan else averagePerDay(goal))
        return GoalPace(
            dailyNeeded = dailyNeeded,
            daysLeft = daysLeft,
            etaDays = etaDays,
            onTrack = goal.isReached || daysLeft == null || (etaDays != null && etaDays <= daysLeft),
        )
    }


    /** Daily target required for a deadline. The optional plan is shown separately, not mistaken for actual savings. */
    fun requiredPerDay(remaining: Long, deadline: LocalDate?, today: LocalDate = LocalDate.now()): Long {
        if (remaining <= 0L || deadline == null) return 0L
        val days = ChronoUnit.DAYS.between(today, deadline)
        if (days <= 0L) return remaining
        return remaining / days + if (remaining % days != 0L) 1L else 0L
    }

    /** Pace forecast uses the user's planned amount, NEVER creates deposits. */
    fun projectedDate(remaining: Long, dailyPlan: Long, today: LocalDate = LocalDate.now()): LocalDate? {
        if (dailyPlan <= 0L || remaining <= 0L) return null
        val days = remaining / dailyPlan + if (remaining % dailyPlan != 0L) 1L else 0L
        return runCatching { today.plusDays(days) }.getOrNull()
    }

    fun monthTarget(dailyPlan: Long, daysInMonth: Int): Long =
        if (dailyPlan <= 0L) 0L else dailyPlan * daysInMonth

    fun averagePerDay(goal: Goal): Long {
        if (goal.saved <= 0L) return 0L
        val start = goal.createdAtDay() ?: return 0L
        val days = (ChronoUnit.DAYS.between(start, LocalDate.now()) + 1).coerceAtLeast(1L)
        return goal.saved / days
    }

    private fun Goal.createdAtDay(): LocalDate? = runCatching {
        java.time.Instant.ofEpochMilli(createdAtMillis)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
    }.getOrNull()

    /** ETA dalam hari berdasarkan laju rata-rata harian; null kalau lajunya nol. */
    fun etaFromPace(saved: Long, target: Long, perDay: Long): Long? {
        if (target <= 0L || saved >= target) return 0L
        if (perDay <= 0L) return null
        val remaining = target - saved
        return remaining / perDay + if (remaining % perDay != 0L) 1L else 0L
    }

    /** Streak = jumlah hari beruntun dengan minimal satu setoran, mundur dari hari ini (atau kemarin). */
    fun streak(deposits: List<Deposit>): Int {
        val days = deposits.map { it.day }.toSortedSet(reverseOrder())
        if (days.isEmpty()) return 0
        val today = LocalDate.now()
        var cursor = when {
            days.contains(today) -> today
            days.contains(today.minusDays(1)) -> today.minusDays(1)
            else -> return 0
        }
        var count = 0
        while (days.contains(cursor)) {
            count++
            cursor = cursor.minusDays(1)
        }
        return count
    }

    fun monthTotal(deposits: List<Deposit>, today: LocalDate = LocalDate.now()): Long =
        deposits.filter { it.day.year == today.year && it.day.monthValue == today.monthValue }
            .sumOf { it.amount }

    fun dayTotal(deposits: List<Deposit>, day: LocalDate): Long =
        deposits.filter { it.day == day }.sumOf { it.amount }

    fun totalsByDay(deposits: List<Deposit>, from: LocalDate, to: LocalDate): List<Pair<LocalDate, Long>> {
        val buckets = deposits.filter { !it.day.isBefore(from) && !it.day.isAfter(to) }
            .groupBy { it.day }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
        var cursor = from
        val out = ArrayList<Pair<LocalDate, Long>>()
        while (!cursor.isAfter(to)) {
            out += cursor to (buckets[cursor] ?: 0L)
            cursor = cursor.plusDays(1)
        }
        return out
    }

    fun autoSourceLabel(source: DepositSource): String = when (source) {
        DepositSource.MANUAL -> "Manual"
        DepositSource.AUTO -> "Auto"
        DepositSource.IMPORT -> "Impor"
    }
}
