package com.katiyar.dailygoals.domain.usecase

import com.katiyar.dailygoals.data.local.GoalEntity
import com.katiyar.dailygoals.domain.model.GoalStats
import java.time.DayOfWeek
import java.time.LocalDate

object GoalRules {
    fun validateTitle(title: String): String? = when {
        title.isBlank() -> "Goal title is required"
        title.trim().length > 120 -> "Goal title must be 120 characters or fewer"
        else -> null
    }

    fun completionPercentage(goals: List<GoalEntity>): Int {
        if (goals.isEmpty()) return 0
        return ((goals.count { it.isCompleted } * 100f) / goals.size).toInt()
    }

    fun datesToCarry(sourceDate: LocalDate, targetDate: LocalDate): List<LocalDate> {
        if (!sourceDate.isBefore(targetDate)) return emptyList()
        return generateSequence(sourceDate.plusDays(1)) { it.plusDays(1) }
            .takeWhile { !it.isAfter(targetDate) }
            .toList()
    }

    fun statistics(goals: List<GoalEntity>, today: LocalDate): GoalStats {
        val visible = goals.filterNot { it.isCancelled }
        val todayGoals = visible.filter { it.date == today }
        val startOfWeek = today.minusDays((today.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong())
        val weekGoals = visible.filter { !it.date.isBefore(startOfWeek) && !it.date.isAfter(today) }
        val completedDays = visible
            .groupBy { it.date }
            .filterValues { day -> day.isNotEmpty() && day.all { it.isCompleted } }
            .keys

        return GoalStats(
            todayPercentage = completionPercentage(todayGoals),
            weeklyPercentage = completionPercentage(weekGoals),
            totalCompleted = visible.count { it.isCompleted },
            totalIncomplete = visible.count { !it.isCompleted },
            currentStreak = currentStreak(completedDays, today),
            bestStreak = bestStreak(completedDays),
        )
    }

    private fun currentStreak(completedDays: Set<LocalDate>, today: LocalDate): Int {
        var cursor = if (today in completedDays) today else today.minusDays(1)
        var count = 0
        while (cursor in completedDays) {
            count++
            cursor = cursor.minusDays(1)
        }
        return count
    }

    private fun bestStreak(completedDays: Set<LocalDate>): Int {
        if (completedDays.isEmpty()) return 0
        val sorted = completedDays.sorted()
        var best = 1
        var current = 1
        for (index in 1 until sorted.size) {
            current = if (sorted[index - 1].plusDays(1) == sorted[index]) current + 1 else 1
            best = maxOf(best, current)
        }
        return best
    }
}
