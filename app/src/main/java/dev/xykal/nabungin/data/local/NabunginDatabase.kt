package dev.xykal.nabungin.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [GoalEntity::class, DepositEntity::class, AutoRuleEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class NabunginDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun depositDao(): DepositDao
    abstract fun autoRuleDao(): AutoRuleDao

    companion object {
        fun build(context: Context): NabunginDatabase =
            Room.databaseBuilder(context.applicationContext, NabunginDatabase::class.java, "nabungin.db")
                .addCallback(object : Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        // WAL + foreign key cascade aktif (Room bikin FK tapi pragma harus dinyalakan).
                        db.execSQL("PRAGMA foreign_keys = ON")
                    }
                })
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
