package com.katiyar.dailygoals

import android.app.Application
import androidx.room.Room
import com.katiyar.dailygoals.data.local.AppDatabase
import com.katiyar.dailygoals.data.repository.GoalRepository
import com.katiyar.dailygoals.data.repository.SettingsRepository
import com.katiyar.dailygoals.notifications.NotificationHelper

class DailyGoalsApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "daily_goals.db",
        ).build()
        container = AppContainer(
            goalRepository = GoalRepository(database),
            settingsRepository = SettingsRepository(applicationContext),
        )
        NotificationHelper.createChannel(this)
    }
}

data class AppContainer(
    val goalRepository: GoalRepository,
    val settingsRepository: SettingsRepository,
)
