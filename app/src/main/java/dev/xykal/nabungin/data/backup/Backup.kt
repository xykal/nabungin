package dev.xykal.nabungin.data.backup

import android.content.Context
import android.net.Uri
import dev.xykal.nabungin.data.local.AutoRuleEntity
import dev.xykal.nabungin.data.local.DepositEntity
import dev.xykal.nabungin.data.local.GoalEntity
import dev.xykal.nabungin.data.repo.SavingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class BackupGoal(
    val id: Long,
    val name: String,
    val targetAmount: Long,
    val deadlineEpochDay: Long? = null,
    val accentIndex: Int = 0,
    val iconKey: String = "coins",
    val category: String = "Umum",
    val purpose: String = "",
    val dailyPlan: Long = 0L,
    val createdAtMillis: Long = 0L,
    val archived: Boolean = false,
)

@Serializable
data class BackupDeposit(
    val id: Long,
    val goalId: Long,
    val amount: Long,
    val epochDay: Long,
    val note: String = "",
    val source: String = "manual",
    val createdAtMillis: Long = 0L,
)

@Serializable
data class BackupRule(
    val id: Long,
    val goalId: Long,
    val amount: Long,
    val interval: String = "daily",
    val hour: Int = 20,
    val minute: Int = 0,
    val enabled: Boolean = false,
    val lastRunEpochDay: Long? = null,
)

@Serializable
data class BackupFile(
    val schema: Int = 1,
    val app: String = "nabungin",
    val exportedAtMillis: Long = System.currentTimeMillis(),
    val goals: List<BackupGoal> = emptyList(),
    val deposits: List<BackupDeposit> = emptyList(),
    val rules: List<BackupRule> = emptyList(),
)

class BackupManager(private val repository: SavingsRepository) {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun export(context: Context, uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val file = BackupFile(
                goals = repository.rawGoals().map { it.toBackup() },
                deposits = repository.rawDeposits().map { it.toBackup() },
                rules = repository.rawRules().map { it.toBackup() },
            )
            val text = json.encodeToString(file)
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(text.toByteArray())
                out.flush()
            } ?: error("Tidak bisa menulis ke lokasi yang dipilih")
            file.goals.size
        }
    }

    suspend fun import(context: Context, uri: Uri): Result<Pair<Int, Int>> = withContext(Dispatchers.IO) {
        runCatching {
            val text = context.contentResolver.openInputStream(uri)?.use { input ->
                input.readBytes().toString(Charsets.UTF_8)
            } ?: error("Tidak bisa membaca berkas")
            val file = json.decodeFromString<BackupFile>(text)
            require(file.app == "nabungin" && file.schema == 1) { "Format backup tidak dikenal" }
            require(file.goals.size <= 2_000 && file.deposits.size <= 100_000) { "Backup terlalu besar" }
            val ids = file.goals.map { it.id }.toSet()
            require(ids.size == file.goals.size && ids.none { it <= 0L }) { "ID tujuan duplikat atau tidak valid" }
            require(file.goals.all { it.targetAmount > 0L && it.dailyPlan >= 0L && it.name.isNotBlank() }) { "Target tabungan tidak valid" }
            require(file.deposits.all { it.goalId in ids && it.amount > 0L }) { "Catatan tabungan tidak valid" }
            require(file.rules.all { it.goalId in ids && it.amount > 0L }) { "Aturan otomatis tidak valid" }
            repository.importAll(
                goals = file.goals.map { it.toEntity() },
                deposits = file.deposits.map { it.toEntity() },
                rules = file.rules.map { it.toEntity() },
            )
            file.goals.size to file.deposits.size
        }
    }
}

private fun GoalEntity.toBackup() = BackupGoal(
    id = id, name = name, targetAmount = targetAmount, deadlineEpochDay = deadlineEpochDay,
    accentIndex = accentIndex, iconKey = iconKey, category = category,
    purpose = purpose, dailyPlan = dailyPlan,
    createdAtMillis = createdAtMillis, archived = archived,
)

private fun BackupGoal.toEntity() = GoalEntity(
    id = id, name = name, targetAmount = targetAmount, deadlineEpochDay = deadlineEpochDay,
    accentIndex = accentIndex, iconKey = iconKey, category = category,
    purpose = purpose, dailyPlan = dailyPlan,
    createdAtMillis = createdAtMillis, archived = archived,
)

private fun DepositEntity.toBackup() = BackupDeposit(
    id = id, goalId = goalId, amount = amount, epochDay = epochDay, note = note,
    source = source, createdAtMillis = createdAtMillis,
)

private fun BackupDeposit.toEntity() = DepositEntity(
    id = id, goalId = goalId, amount = amount, epochDay = epochDay, note = note,
    source = source, createdAtMillis = createdAtMillis,
)

private fun AutoRuleEntity.toBackup() = BackupRule(
    id = id, goalId = goalId, amount = amount, interval = interval, hour = hour,
    minute = minute, enabled = enabled, lastRunEpochDay = lastRunEpochDay,
)

private fun BackupRule.toEntity() = AutoRuleEntity(
    id = id, goalId = goalId, amount = amount, interval = interval, hour = hour,
    minute = minute, enabled = enabled, lastRunEpochDay = lastRunEpochDay,
)
