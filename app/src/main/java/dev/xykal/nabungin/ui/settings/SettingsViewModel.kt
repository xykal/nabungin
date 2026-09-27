package dev.xykal.nabungin.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.xykal.nabungin.core.AppContainer
import dev.xykal.nabungin.data.prefs.AppSettings
import dev.xykal.nabungin.data.prefs.ThemeMode
import dev.xykal.nabungin.security.PinHasher
import dev.xykal.nabungin.work.WorkScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val container: AppContainer) : ViewModel() {

    val settings: StateFlow<AppSettings> = container.settings.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun consumeMessage() {
        _message.value = null
    }

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { container.settings.setTheme(mode) }
    }

    fun setReminder(context: Context, enabled: Boolean) {
        viewModelScope.launch {
            container.settings.setReminder(enabled)
            WorkScheduler.syncReminder(context, container.settings.current())
            _message.value = if (enabled) "Pengingat harian aktif" else "Pengingat dimatikan"
        }
    }

    fun setReminderTime(context: Context, hour: Int, minute: Int) {
        viewModelScope.launch {
            container.settings.setReminderTime(hour, minute)
            WorkScheduler.syncReminder(context, container.settings.current())
        }
    }

    fun savePin(pin: String) {
        viewModelScope.launch {
            val salt = PinHasher.newSalt()
            val hash = PinHasher.hash(pin, salt)
            container.settings.setPin(hash, salt)
            _message.value = "PIN tersimpan, aplikasi terkunci"
        }
    }

    fun disableLock() {
        viewModelScope.launch {
            container.settings.disableLock()
            _message.value = "Kunci aplikasi dimatikan"
        }
    }

    fun setBiometric(enabled: Boolean) {
        viewModelScope.launch { container.settings.setBiometric(enabled) }
    }

    fun export(context: Context, uri: Uri) {
        viewModelScope.launch {
            container.backup.export(context, uri)
                .onSuccess { count -> _message.value = "Backup tersimpan: $count tujuan" }
                .onFailure { _message.value = "Export gagal: ${it.message}" }
        }
    }

    fun import(context: Context, uri: Uri) {
        viewModelScope.launch {
            container.backup.import(context, uri)
                .onSuccess { (goals, deposits) ->
                    _message.value = "Restore sukses: $goals tujuan, $deposits setoran"
                }
                .onFailure { _message.value = "Import gagal: ${it.message}" }
        }
    }

    fun wipe() {
        viewModelScope.launch {
            container.repository.wipeEverything()
            _message.value = "Semua data dihapus"
        }
    }
}
