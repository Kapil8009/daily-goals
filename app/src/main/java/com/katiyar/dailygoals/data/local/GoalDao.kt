package com.katiyar.dailygoals.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(goal: GoalEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(goals: List<GoalEntity>): List<Long>

    @Update
    suspend fun update(goal: GoalEntity): Int

    @Delete
    suspend fun hardDelete(goal: GoalEntity): Int

    @Query("SELECT * FROM goals WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): GoalEntity?

    @Query("SELECT * FROM goals WHERE seriesId = :seriesId AND date = :date LIMIT 1")
    suspend fun getBySeriesAndDate(seriesId: String, date: LocalDate): GoalEntity?

    @Query("SELECT * FROM goals WHERE date = :date AND isCancelled = 0 ORDER BY isCompleted, priority DESC, reminderTime, createdAt")
    fun observeByDate(date: LocalDate): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE isCompleted = 0 AND isCancelled = 0 ORDER BY date DESC, priority DESC, createdAt")
    fun observeIncomplete(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE isCompleted = 1 AND isCancelled = 0 ORDER BY date DESC, completedAt DESC")
    fun observeCompleted(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE date BETWEEN :start AND :end AND isCancelled = 0 ORDER BY date, createdAt")
    fun observeBetween(start: LocalDate, end: LocalDate): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE isCancelled = 0 ORDER BY date, createdAt")
    fun observeAll(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE isCancelled = 0 ORDER BY date, createdAt")
    suspend fun getAllSnapshot(): List<GoalEntity>

    @Query(
        """
        SELECT g.* FROM goals g
        INNER JOIN (
            SELECT seriesId, MAX(date) AS latestDate
            FROM goals
            WHERE date < :targetDate
            GROUP BY seriesId
        ) latest ON latest.seriesId = g.seriesId AND latest.latestDate = g.date
        WHERE g.isCompleted = 0 AND g.isCancelled = 0
        ORDER BY g.date, g.createdAt
        """,
    )
    suspend fun getLatestActiveGoalsBefore(targetDate: LocalDate): List<GoalEntity>

    @Query("UPDATE goals SET isCompleted = :completed, completedAt = :completedAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setCompleted(id: Long, completed: Boolean, completedAt: Long?, updatedAt: Long): Int

    @Query("UPDATE goals SET isCancelled = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun cancel(id: Long, updatedAt: Long): Int

    @Query("SELECT COUNT(*) FROM goals WHERE date = :date AND isCompleted = 0 AND isCancelled = 0")
    suspend fun countIncomplete(date: LocalDate): Int
}
