package com.katiyar.dailygoals.data.repository

import androidx.room.withTransaction
import com.katiyar.dailygoals.data.local.AppDatabase
import com.katiyar.dailygoals.data.local.GoalDao
import com.katiyar.dailygoals.data.local.GoalEntity
import com.katiyar.dailygoals.domain.usecase.GoalRules
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

class GoalRepository(
    private val database: AppDatabase,
    private val dao: GoalDao = database.goalDao(),
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    fun observeGoals(date: LocalDate): Flow<List<GoalEntity>> = dao.observeByDate(date)

    fun observeIncomplete(): Flow<List<GoalEntity>> = dao.observeIncomplete()

    fun observeAll(): Flow<List<GoalEntity>> = dao.observeAll()

    fun observeBetween(start: LocalDate, end: LocalDate): Flow<List<GoalEntity>> =
        dao.observeBetween(start, end)

    suspend fun getGoal(id: Long): GoalEntity? = dao.getById(id)

    suspend fun saveGoal(goal: GoalEntity): Result<Long> = runCatching {
        GoalRules.validateTitle(goal.title)?.let { error -> throw IllegalArgumentException(error) }
        require(!goal.date.isBefore(LocalDate.of(1900, 1, 1))) { "Please choose a valid date" }
        val normalized = goal.copy(
            title = goal.title.trim(),
            description = goal.description?.trim()?.takeIf(String::isNotEmpty),
            updatedAt = clock.millis(),
        )
        if (normalized.id == 0L) {
            dao.insert(normalized).also { id ->
                check(id > 0) { "A goal from the same series already exists on that date" }
            }
        } else {
            check(dao.update(normalized) > 0) { "Goal no longer exists" }
            normalized.id
        }
    }

    suspend fun setCompleted(id: Long, completed: Boolean): Result<Unit> = runCatching {
        val now = clock.millis()
        check(dao.setCompleted(id, completed, now.takeIf { completed }, now) > 0) {
            "Goal no longer exists"
        }
    }

    suspend fun deleteGoal(id: Long): Result<Unit> = runCatching {
        check(dao.cancel(id, clock.millis()) > 0) { "Goal no longer exists" }
    }

    suspend fun moveGoal(id: Long, date: LocalDate): Result<Unit> = runCatching {
        val goal = dao.getById(id) ?: error("Goal no longer exists")
        saveGoal(goal.copy(date = date)).getOrThrow()
    }

    suspend fun syncCarryForward(targetDate: LocalDate, enabled: Boolean): Result<Int> = runCatching {
        if (!enabled) return@runCatching 0
        database.withTransaction {
            val activeGoals = dao.getLatestActiveGoalsBefore(targetDate)
            var insertedCount = 0
            activeGoals.forEach { source ->
                var parent = source
                GoalRules.datesToCarry(source.date, targetDate).forEach { date ->
                    val now = clock.millis()
                    val copy = source.copy(
                        id = 0,
                        date = date,
                        isCompleted = false,
                        completedAt = null,
                        isCarriedForward = true,
                        originalGoalId = source.originalGoalId ?: source.id,
                        carriedFromGoalId = parent.id,
                        isCancelled = false,
                        createdAt = now,
                        updatedAt = now,
                    )
                    val newId = dao.insert(copy)
                    if (newId > 0) {
                        insertedCount++
                        parent = copy.copy(id = newId)
                    } else {
                        val existing = dao.getBySeriesAndDate(source.seriesId, date)
                        if (existing != null) parent = existing
                    }
                }
            }
            insertedCount
        }
    }

    suspend fun incompleteCount(date: LocalDate): Int = dao.countIncomplete(date)

    suspend fun exportSnapshot(): List<GoalEntity> = dao.getAllSnapshot()

    suspend fun importGoals(goals: List<GoalEntity>): Result<Int> = runCatching {
        database.withTransaction {
            dao.insertAll(goals.map { it.copy(id = 0) }).count { it > 0 }
        }
    }
}
