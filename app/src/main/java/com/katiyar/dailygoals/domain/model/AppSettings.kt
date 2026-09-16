package com.katiyar.dailygoals.domain.model

import java.time.LocalTime

enum class ThemeMode(val label: String) {
    SYSTEM("System default"),
    LIGHT("Light"),
    DARK("Dark"),
}

data class AppSettings(
    val notificationsEnabled: Boolean = false,
    val reminderTime: LocalTime = LocalTime.of(20, 0),
    val carryForwardEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)
