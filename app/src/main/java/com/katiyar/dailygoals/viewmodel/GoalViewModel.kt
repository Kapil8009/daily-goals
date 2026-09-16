package com.katiyar.dailygoals.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.katiyar.dailygoals.DailyGoalsApplication
import com.katiyar.dailygoals.data.local.GoalEntity
import com.katiyar.dailygoals.data.repository.GoalBackup
import com.katiyar.dailygoals.domain.model.AppSettings
import com.katiyar.dailygoals.domain.model.GoalStats
import com.katiyar.dailygoals.domain.model.ThemeMode
import com.katiyar.dailygoals.domain.usecase.GoalRules
import com.katiyar.dailygoals.notifications.ReminderScheduler
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class GoalViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as DailyGoalsApplication
    private val goals = app.container.goalRepository
    private val preferences = app.container.settingsRepository

    private val _today = MutableStateFlow(LocalDate.now())
    val today: StateFlow<LocalDate> = _today

    private val _historyDate = MutableStateFlow(LocalDate.now())
    val historyDate: StateFlow<LocalDate> = _historyDate

    val todayGoals: StateFlow<List<GoalEntity>> = _today
        .flatMapLatest(goals::observeGoals)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val historyGoals: StateFlow<List<GoalEntity>> = _historyDate
        .flatMapLatest(goals::observeGoals)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val incompleteGoals: StateFlow<List<GoalEntity>> = goals.observeIncomplete()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allGoals: StateFlow<List<GoalEntity>> = goals.observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val settings: StateFlow<AppSettings> = preferences.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    val statistics: StateFlow<GoalStats> = combine(allGoals, _today) { values, date ->
        GoalRules.statistics(values, date)
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GoalStats())

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages = _messages.asSharedFlow()

    fun refreshForCurrentDate() {
        val current = LocalDate.now()
        _today.value = current
        viewModelScope.launch {
            val latestSettings = preferences.settings.first()
            if (latestSettings.notificationsEnabled) {
                ReminderScheduler.schedule(app, latestSettings.reminderTime)
            }
            goals.syncCarryForward(current, latestSettings.carryForwardEnabled)
                .onFailure { _messages.emit(it.message ?: "Could not carry goals forward") }
        }
    }

    fun selectHistoryDate(date: LocalDate) {
        _historyDate.value = date
    }

    fun saveGoal(goal: GoalEntity, onSaved: () -> Unit) {
        viewModelScope.launch {
            goals.saveGoal(goal)
                .onSuccess { onSaved() }
                .onFailure { _messages.emit(it.message ?: "Could not save goal") }
        }
    }

    fun toggleCompleted(goal: GoalEntity) {
        viewModelScope.launch {
            goals.setCompleted(goal.id, !goal.isCompleted)
                .onFailure { _messages.emit(it.message ?: "Could not update goal") }
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            goals.deleteGoal(goal.id)
                .onFailure { _messages.emit(it.message ?: "Could not delete goal") }
        }
    }

    fun moveGoal(goal: GoalEntity, date: LocalDate) {
        viewModelScope.launch {
            goals.moveGoal(goal.id, date)
                .onSuccess { _messages.emit("Goal moved to $date") }
                .onFailure { _messages.emit(it.message ?: "Could not move goal") }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setNotificationsEnabled(enabled)
            if (enabled) ReminderScheduler.schedule(app, settings.value.reminderTime)
            else ReminderScheduler.cancel(app)
        }
    }

    fun setReminderTime(time: LocalTime) {
        viewModelScope.launch {
            preferences.setReminderTime(time)
            if (settings.value.notificationsEnabled) ReminderScheduler.schedule(app, time)
        }
    }

    fun setCarryForwardEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setCarryForwardEnabled(enabled)
            if (enabled) refreshForCurrentDate()
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferences.setThemeMode(mode) }
    }

    fun exportGoals(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val json = withContext(Dispatchers.IO) { GoalBackup.encode(goals.exportSnapshot()) }
                app.contentResolver.openOutputStream(uri, "wt")?.bufferedWriter()?.use { it.write(json) }
                    ?: error("Could not open the selected file")
            }.onSuccess {
                _messages.emit("Goals exported")
            }.onFailure {
                _messages.emit(it.message ?: "Could not export goals")
            }
        }
    }

    fun importGoals(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val json = withContext(Dispatchers.IO) {
                    app.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                        ?: error("Could not open the selected file")
                }
                goals.importGoals(GoalBackup.decode(json)).getOrThrow()
            }.onSuccess { count ->
                _messages.emit("Imported $count ${if (count == 1) "goal" else "goals"}")
                refreshForCurrentDate()
            }.onFailure {
                _messages.emit("Import failed: ${it.message ?: "invalid backup"}")
            }
        }
    }
}
