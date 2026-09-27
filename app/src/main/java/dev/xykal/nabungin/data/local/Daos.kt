package dev.xykal.nabungin.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE archived = 0 ORDER BY createdAtMillis DESC")
    fun observeActive(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id")
    fun observeById(id: Long): Flow<GoalEntity?>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getById(id: Long): GoalEntity?

    @Query("SELECT * FROM goals")
    suspend fun getAll(): List<GoalEntity>

    @Insert
    suspend fun insert(entity: GoalEntity): Long

    @Update
    suspend fun update(entity: GoalEntity)

    @Query("UPDATE goals SET archived = :archived WHERE id = :id")
    suspend fun setArchived(id: Long, archived: Boolean)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM goals")
    suspend fun wipe()
}

@Dao
interface DepositDao {
    @Query("SELECT * FROM deposits ORDER BY epochDay DESC, createdAtMillis DESC")
    fun observeAll(): Flow<List<DepositEntity>>

    @Query("SELECT * FROM deposits WHERE goalId = :goalId ORDER BY epochDay DESC, createdAtMillis DESC")
    fun observeForGoal(goalId: Long): Flow<List<DepositEntity>>

    @Query("SELECT goalId AS goalId, COALESCE(SUM(amount), 0) AS saved, COUNT(id) AS cnt, MAX(epochDay) AS lastDay FROM deposits GROUP BY goalId")
    fun observeTotals(): Flow<List<GoalTotal>>

    @Query("SELECT epochDay AS epochDay, COALESCE(SUM(amount), 0) AS total FROM deposits WHERE epochDay >= :fromDay GROUP BY epochDay ORDER BY epochDay ASC")
    fun observeDailyTotals(fromDay: Long): Flow<List<DayTotalRow>>

    @Query("SELECT * FROM deposits")
    suspend fun getAll(): List<DepositEntity>

    @Query("SELECT * FROM deposits WHERE epochDay >= :fromDay ORDER BY epochDay ASC, createdAtMillis ASC")
    suspend fun getSince(fromDay: Long): List<DepositEntity>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM deposits")
    suspend fun totalSaved(): Long

    @Insert
    suspend fun insert(entity: DepositEntity): Long

    @Query("DELETE FROM deposits WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM deposits WHERE goalId = :goalId")
    suspend fun deleteForGoal(goalId: Long)

    @Query("DELETE FROM deposits")
    suspend fun wipe()
}

@Dao
interface AutoRuleDao {
    @Query("SELECT * FROM auto_rules")
    fun observeAll(): Flow<List<AutoRuleEntity>>

    @Query("SELECT * FROM auto_rules WHERE enabled = 1")
    suspend fun getEnabled(): List<AutoRuleEntity>

    @Query("SELECT * FROM auto_rules WHERE goalId = :goalId")
    suspend fun getForGoal(goalId: Long): AutoRuleEntity?

    @Query("SELECT * FROM auto_rules")
    suspend fun getAll(): List<AutoRuleEntity>

    @Insert
    suspend fun insert(entity: AutoRuleEntity): Long

    @Update
    suspend fun update(entity: AutoRuleEntity)

    @Query("DELETE FROM auto_rules WHERE goalId = :goalId")
    suspend fun deleteForGoal(goalId: Long)

    @Query("DELETE FROM auto_rules")
    suspend fun wipe()
}
