package dev.xykal.nabungin.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val targetAmount: Long,
    val deadlineEpochDay: Long? = null,
    val accentIndex: Int = 0,
    val iconKey: String = "coins",
    val category: String = "Umum",
    val purpose: String = "",
    val dailyPlan: Long = 0L,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val archived: Boolean = false,
)

@Entity(
    tableName = "deposits",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("goalId"), Index("epochDay")],
)
data class DepositEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val goalId: Long,
    val amount: Long,
    val epochDay: Long,
    val note: String = "",
    val source: String = "manual",
    val createdAtMillis: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "auto_rules",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["goalId"], unique = true)],
)
data class AutoRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val goalId: Long,
    val amount: Long,
    val interval: String = "daily",
    val hour: Int = 20,
    val minute: Int = 0,
    val enabled: Boolean = false,
    val lastRunEpochDay: Long? = null,
)

data class GoalTotal(
    @ColumnInfo(name = "goalId") val goalId: Long,
    @ColumnInfo(name = "saved") val saved: Long,
    @ColumnInfo(name = "cnt") val cnt: Int,
    @ColumnInfo(name = "lastDay") val lastDay: Long?,
)

data class DayTotalRow(
    @ColumnInfo(name = "epochDay") val epochDay: Long,
    @ColumnInfo(name = "total") val total: Long,
)
