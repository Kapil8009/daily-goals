package com.katiyar.dailygoals.data.repository

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.katiyar.dailygoals.domain.model.AppSettings
import com.katiyar.dailygoals.domain.model.ThemeMode
import java.io.IOException
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(
    name = "settings",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
    produceMigrations = { context -> listOf(SharedPreferencesMigration(context, "settings")) },
)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val notifications = booleanPreferencesKey("notifications_enabled")
        val reminderHour = intPreferencesKey("reminder_hour")
        val reminderMinute = intPreferencesKey("reminder_minute")
        val carryForward = booleanPreferencesKey("carry_forward_enabled")
        val theme = stringPreferencesKey("theme_mode")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences ->
            AppSettings(
                notificationsEnabled = preferences[Keys.notifications] ?: false,
                reminderTime = LocalTime.of(
                    (preferences[Keys.reminderHour] ?: 20).coerceIn(0, 23),
                    (preferences[Keys.reminderMinute] ?: 0).coerceIn(0, 59),
                ),
                carryForwardEnabled = preferences[Keys.carryForward] ?: true,
                themeMode = runCatching {
                    ThemeMode.valueOf(preferences[Keys.theme] ?: ThemeMode.SYSTEM.name)
                }.getOrDefault(ThemeMode.SYSTEM),
            )
        }

    suspend fun setNotificationsEnabled(enabled: Boolean) = update(Keys.notifications, enabled)

    suspend fun setReminderTime(time: LocalTime) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.reminderHour] = time.hour
            preferences[Keys.reminderMinute] = time.minute
        }
    }

    suspend fun setCarryForwardEnabled(enabled: Boolean) = update(Keys.carryForward, enabled)

    suspend fun setThemeMode(mode: ThemeMode) = update(Keys.theme, mode.name)

    private suspend fun <T> update(key: Preferences.Key<T>, value: T) {
        context.settingsDataStore.edit { it[key] = value }
    }
}
