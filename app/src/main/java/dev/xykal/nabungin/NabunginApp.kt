package dev.xykal.nabungin

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import dev.xykal.nabungin.core.AppContainer
import dev.xykal.nabungin.notify.Notifications
import dev.xykal.nabungin.work.WorkScheduler
import kotlinx.coroutines.launch

class NabunginApp : Application(), Configuration.Provider {

    lateinit var container: AppContainer
        private set

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.WARN)
            .build()

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        Notifications.ensureChannel(this)
        container.appScope.launch {
            runCatching {
                if (!container.settings.current().seeded) {
                    container.repository.seedIfEmpty()
                    container.settings.setSeeded()
                }
                val settings = container.settings.current()
                WorkScheduler.syncAutoSave(this@NabunginApp)
                WorkScheduler.syncReminder(this@NabunginApp, settings)
            }.onFailure { Log.w("NabunginApp", "bootstrap gagal: ${it.message}") }
        }
    }
}
