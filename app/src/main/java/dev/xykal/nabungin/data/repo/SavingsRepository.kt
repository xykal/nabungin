package dev.xykal.nabungin.data.repo

import dev.xykal.nabungin.data.local.AutoRuleDao
import dev.xykal.nabungin.data.local.AutoRuleEntity
import dev.xykal.nabungin.data.local.DepositDao
import dev.xykal.nabungin.data.local.DepositEntity
import dev.xykal.nabungin.data.local.GoalDao
import dev.xykal.nabungin.data.local.GoalEntity
import dev.xykal.nabungin.domain.SavingsMath
import dev.xykal.nabungin.domain.model.AutoRule
import dev.xykal.nabungin.domain.model.DayTotal
import dev.xykal.nabungin.domain.model.Deposit
import dev.xykal.nabungin.domain.model.DepositSource
import dev.xykal.nabungin.domain.model.Goal
import dev.xykal.nabungin.domain.model.SaveInterval
import dev.xykal.nabungin.domain.model.Snapshot
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class SavingsRepository(
    private val goalDao: GoalDao,
    private val depositDao: DepositDao,
    private val ruleDao: AutoRuleDao,
) {

    val goals: Flow<List<Goal>> = combine(
        goalDao.observeActive(),
        depositDao.observeTotals(),
    ) { goals, totals ->
        val byId = totals.associateBy { it.goalId }
        goals.map { g ->
            val t = byId[g.id]
            g.toDomain(
                saved = t?.saved ?: 0L,
                count = t?.cnt ?: 0,
                lastDay = t?.lastDay?.let(LocalDate::ofEpochDay),
            )
        }
    }

    val allDeposits: Flow<List<Deposit>> = depositDao.observeAll().map { list -> list.map { it.toDomain() } }

    val snapshot: Flow<Snapshot> = combine(goals, allDeposits) { goalList, deposits ->
        val today = LocalDate.now()
        val last30 = SavingsMath.totalsByDay(deposits, today.minusDays(29), today)
            .map { (day, total) -> DayTotal(day, total) }
        Snapshot(
            goals = goalList,
            totalSaved = goalList.sumOf { it.saved },
            totalTarget = goalList.sumOf { it.targetAmount },
            todayTotal = SavingsMath.dayTotal(deposits, today),
            monthTotal = SavingsMath.monthTotal(deposits, today),
            last30 = last30,
            streak = SavingsMath.streak(deposits),
            depositCount = deposits.size,
        )
    }

    fun goal(id: Long): Flow<Goal?> = combine(
        goalDao.observeById(id),
        depositDao.observeTotals(),
    ) { entity, totals ->
        entity?.toDomain(
            saved = totals.firstOrNull { it.goalId == id }?.saved ?: 0L,
            count = totals.firstOrNull { it.goalId == id }?.cnt ?: 0,
            lastDay = totals.firstOrNull { it.goalId == id }?.lastDay?.let(LocalDate::ofEpochDay),
        )
    }

    fun depositsOf(goalId: Long): Flow<List<Deposit>> =
        depositDao.observeForGoal(goalId).map { list -> list.map { it.toDomain() } }

    fun ruleOf(goalId: Long): Flow<AutoRule?> =
        ruleDao.observeAll().map { rules -> rules.firstOrNull { it.goalId == goalId }?.toDomain() }

    val rules: Flow<List<AutoRule>> = ruleDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun upsertGoal(goal: Goal): Long =
        if (goal.id == 0L) {
            goalDao.insert(goal.toEntity())
        } else {
            val existing = goalDao.getById(goal.id)
            goalDao.update(
                goal.toEntity().copy(
                    createdAtMillis = existing?.createdAtMillis ?: goal.createdAtMillis,
                    archived = existing?.archived ?: goal.archived,
                ),
            )
            goal.id
        }

    suspend fun deleteGoal(id: Long) = goalDao.delete(id)

    suspend fun setArchived(id: Long, archived: Boolean) = goalDao.setArchived(id, archived)

    suspend fun addDeposit(
        goalId: Long,
        amount: Long,
        day: LocalDate = LocalDate.now(),
        note: String = "",
        source: DepositSource = DepositSource.MANUAL,
    ): Long {
        if (amount <= 0L) return 0L
        return depositDao.insert(
            DepositEntity(
                goalId = goalId,
                amount = amount,
                epochDay = day.toEpochDay(),
                note = note.trim(),
                source = source.key,
            ),
        )
    }

    suspend fun deleteDeposit(id: Long) = depositDao.delete(id)

    suspend fun upsertRule(rule: AutoRule) {
        val existing = ruleDao.getForGoal(rule.goalId)
        if (existing == null) {
            ruleDao.insert(rule.toEntity(id = 0L, lastRun = null))
        } else {
            ruleDao.update(rule.toEntity(id = existing.id, lastRun = existing.lastRunEpochDay))
        }
    }

    suspend fun deleteRule(goalId: Long) = ruleDao.deleteForGoal(goalId)

    suspend fun wipeEverything() {
        depositDao.wipe()
        ruleDao.wipe()
        goalDao.wipe()
    }

    suspend fun totalSavedNow(): Long = depositDao.totalSaved()

    suspend fun enabledRules(): List<AutoRule> = ruleDao.getEnabled().map { it.toDomain() }

    suspend fun markRuleRun(goalId: Long, day: LocalDate) {
        val existing = ruleDao.getForGoal(goalId) ?: return
        ruleDao.update(existing.copy(lastRunEpochDay = day.toEpochDay()))
    }

    suspend fun allDepositsOnce(): List<Deposit> = depositDao.getAll().map { it.toDomain() }

    suspend fun rawGoals(): List<GoalEntity> = goalDao.getAll()

    suspend fun rawDeposits(): List<DepositEntity> = depositDao.getAll()

    suspend fun rawRules(): List<AutoRuleEntity> = ruleDao.getAll()

    suspend fun importAll(
        goals: List<GoalEntity>,
        deposits: List<DepositEntity>,
        rules: List<AutoRuleEntity>,
    ) {
        depositDao.wipe()
        ruleDao.wipe()
        goalDao.wipe()
        goals.forEach { goalDao.insert(it.copy(id = it.id)) }
        deposits.forEach { depositDao.insert(it) }
        rules.forEach { ruleDao.insert(it) }
    }

    suspend fun seedIfEmpty() {
        if (goalDao.getAll().isNotEmpty()) return
        val id = goalDao.insert(
            GoalEntity(
                name = "Dana Darurat",
                targetAmount = 10_000_000L,
                deadlineEpochDay = LocalDate.now().plusMonths(6).toEpochDay(),
                accentIndex = 0,
                iconKey = "shield",
                category = "Darurat",
            ),
        )
        depositDao.insert(
            DepositEntity(goalId = id, amount = 250_000L, epochDay = LocalDate.now().toEpochDay(), note = "Setoran pertama"),
        )
    }
}

private fun GoalEntity.toDomain(saved: Long, count: Int, lastDay: LocalDate?) = Goal(
    id = id,
    name = name,
    targetAmount = targetAmount,
    deadline = deadlineEpochDay?.let(LocalDate::ofEpochDay),
    accentIndex = accentIndex,
    iconKey = iconKey,
    category = category,
    createdAtMillis = createdAtMillis,
    archived = archived,
    saved = saved,
    depositCount = count,
    lastDepositDay = lastDay,
)

private fun Goal.toEntity() = GoalEntity(
    id = id,
    name = name.trim(),
    targetAmount = targetAmount,
    deadlineEpochDay = deadline?.toEpochDay(),
    accentIndex = accentIndex,
    iconKey = iconKey,
    category = category,
    createdAtMillis = createdAtMillis,
    archived = archived,
)

private fun DepositEntity.toDomain() = Deposit(
    id = id,
    goalId = goalId,
    amount = amount,
    day = LocalDate.ofEpochDay(epochDay),
    note = note,
    source = DepositSource.fromKey(source),
    createdAtMillis = createdAtMillis,
)

private fun Deposit.toEntity() = DepositEntity(
    id = id,
    goalId = goalId,
    amount = amount,
    epochDay = day.toEpochDay(),
    note = note,
    source = source.key,
    createdAtMillis = createdAtMillis,
)

private fun AutoRuleEntity.toDomain() = AutoRule(
    id = id,
    goalId = goalId,
    amount = amount,
    interval = SaveInterval.fromKey(interval),
    hour = hour,
    minute = minute,
    enabled = enabled,
    lastRun = lastRunEpochDay?.let(LocalDate::ofEpochDay),
)

private fun AutoRule.toEntity(id: Long, lastRun: Long?) = AutoRuleEntity(
    id = id,
    goalId = goalId,
    amount = amount,
    interval = interval.key,
    hour = hour,
    minute = minute,
    enabled = enabled,
    lastRunEpochDay = lastRun,
)
