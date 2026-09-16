package com.katiyar.dailygoals.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.katiyar.dailygoals.DailyGoalsApplication
import java.time.LocalDate
import kotlinx.coroutines.flow.first

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return runCatching {
            val container = (applicationContext as DailyGoalsApplication).container
            val settings = container.settingsRepository.settings.first()
            if (!settings.notificationsEnabled) return Result.success()
            val today = LocalDate.now()
            container.goalRepository.syncCarryForward(today, settings.carryForwardEnabled).getOrThrow()
            NotificationHelper.showReminder(
                applicationContext,
                container.goalRepository.incompleteCount(today),
            )
            Result.success()
        }.getOrElse { Result.retry() }
    }
}
