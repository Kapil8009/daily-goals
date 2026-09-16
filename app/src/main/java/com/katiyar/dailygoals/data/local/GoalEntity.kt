package com.katiyar.dailygoals.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.katiyar.dailygoals.domain.model.Priority
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@Entity(
    tableName = "goals",
    indices = [
        Index(value = ["date"]),
        Index(value = ["seriesId", "date"], unique = true),
        Index(value = ["isCompleted", "isCancelled"]),
    ],
)
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String? = null,
    val date: LocalDate,
    val priority: Priority = Priority.MEDIUM,
    val reminderTime: LocalTime? = null,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val isCarriedForward: Boolean = false,
    val originalGoalId: Long? = null,
    val carriedFromGoalId: Long? = null,
    val seriesId: String = UUID.randomUUID().toString(),
    val isCancelled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
