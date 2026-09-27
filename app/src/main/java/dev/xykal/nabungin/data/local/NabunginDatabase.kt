package dev.xykal.nabungin.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [GoalEntity::class, DepositEntity::class, AutoRuleEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class NabunginDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun depositDao(): DepositDao
    abstract fun autoRuleDao(): AutoRuleDao

    companion object {
        /** Migrasi v1 -> v2 tanpa menghapus tujuan atau riwayat tabungan lama. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE goals ADD COLUMN purpose TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE goals ADD COLUMN dailyPlan INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun build(context: Context): NabunginDatabase =
            Room.databaseBuilder(context.applicationContext, NabunginDatabase::class.java, "nabungin.db")
                .addCallback(object : Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        // WAL + foreign key cascade aktif (Room bikin FK tapi pragma harus dinyalakan).
                        db.execSQL("PRAGMA foreign_keys = ON")
                    }
                })
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}
