package dev.xykal.nabungin.core

import android.content.Context
import dev.xykal.nabungin.data.backup.BackupManager
import dev.xykal.nabungin.data.local.NabunginDatabase
import dev.xykal.nabungin.data.prefs.SettingsStore
import dev.xykal.nabungin.data.repo.SavingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** DI manual: ringan, tanpa refleksi, cold-start tetap cepat. */
class AppContainer(context: Context) {
    val appScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val database: NabunginDatabase = NabunginDatabase.build(context)
    val settings: SettingsStore = SettingsStore(context)
    val repository: SavingsRepository = SavingsRepository(
        goalDao = database.goalDao(),
        depositDao = database.depositDao(),
        ruleDao = database.autoRuleDao(),
    )
    val backup: BackupManager = BackupManager(repository)
}
