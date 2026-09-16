package com.katiyar.dailygoals.domain.model

data class GoalStats(
    val todayPercentage: Int = 0,
    val weeklyPercentage: Int = 0,
    val totalCompleted: Int = 0,
    val totalIncomplete: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
)
