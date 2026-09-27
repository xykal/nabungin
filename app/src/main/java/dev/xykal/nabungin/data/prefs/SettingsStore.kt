package dev.xykal.nabungin.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class ThemeMode(val key: String, val label: String) {
    SYSTEM("system", "Sistem"),
    LIGHT("light", "Terang"),
    DARK("dark", "Gelap");

    companion object {
        fun fromKey(key: String?): ThemeMode = entries.firstOrNull { it.key == key } ?: SYSTEM
    }
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val lockEnabled: Boolean = false,
    val pinHash: String? = null,
    val pinSalt: String? = null,
    val biometricEnabled: Boolean = false,
    val seeded: Boolean = false,
)

class SettingsStore(private val context: Context) {

    private object Keys {
        val theme = stringPreferencesKey("theme_mode")
        val reminderOn = booleanPreferencesKey("reminder_enabled")
        val reminderHour = intPreferencesKey("reminder_hour")
        val reminderMinute = intPreferencesKey("reminder_minute")
        val lockOn = booleanPreferencesKey("lock_enabled")
        val pinHash = stringPreferencesKey("pin_hash")
        val pinSalt = stringPreferencesKey("pin_salt")
        val biometric = booleanPreferencesKey("biometric_enabled")
        val seeded = booleanPreferencesKey("seeded")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { p ->
        AppSettings(
            themeMode = ThemeMode.fromKey(p[Keys.theme]),
            reminderEnabled = p[Keys.reminderOn] ?: false,
            reminderHour = p[Keys.reminderHour] ?: 20,
            reminderMinute = p[Keys.reminderMinute] ?: 0,
            lockEnabled = p[Keys.lockOn] ?: false,
            pinHash = p[Keys.pinHash],
            pinSalt = p[Keys.pinSalt],
            biometricEnabled = p[Keys.biometric] ?: false,
            seeded = p[Keys.seeded] ?: false,
        )
    }

    suspend fun current(): AppSettings = settings.first()

    suspend fun setTheme(mode: ThemeMode) = context.settingsDataStore.edit { it[Keys.theme] = mode.key }

    suspend fun setReminder(enabled: Boolean) = context.settingsDataStore.edit { it[Keys.reminderOn] = enabled }

    suspend fun setReminderTime(hour: Int, minute: Int) = context.settingsDataStore.edit {
        it[Keys.reminderHour] = hour
        it[Keys.reminderMinute] = minute
    }

    suspend fun setPin(hash: String, salt: String) = context.settingsDataStore.edit {
        it[Keys.pinHash] = hash
        it[Keys.pinSalt] = salt
        it[Keys.lockOn] = true
    }

    suspend fun disableLock() = context.settingsDataStore.edit {
        it[Keys.lockOn] = false
        it[Keys.pinHash] = null
        it[Keys.pinSalt] = null
        it[Keys.biometric] = false
    }

    suspend fun setBiometric(enabled: Boolean) = context.settingsDataStore.edit { it[Keys.biometric] = enabled }

    suspend fun setSeeded() = context.settingsDataStore.edit { it[Keys.seeded] = true }
}
