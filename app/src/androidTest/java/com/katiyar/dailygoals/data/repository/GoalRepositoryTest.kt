package com.katiyar.dailygoals.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.katiyar.dailygoals.data.local.AppDatabase
import com.katiyar.dailygoals.data.local.GoalEntity
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GoalRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: GoalRepository
    private val today = LocalDate.of(2026, 9, 15)

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = GoalRepository(
            database,
            clock = Clock.fixed(Instant.parse("2026-09-15T10:00:00Z"), ZoneOffset.UTC),
        )
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun addAndCompleteGoal() = runTest {
        val id = repository.saveGoal(GoalEntity(title = "Study", date = today)).getOrThrow()
        repository.setCompleted(id, true).getOrThrow()

        val saved = repository.exportSnapshot().single()
        assertTrue(saved.isCompleted)
        assertTrue(saved.completedAt != null)
    }

    @Test
    fun editGoal() = runTest {
        val id = repository.saveGoal(GoalEntity(title = "Study", date = today)).getOrThrow()
        val original = repository.getGoal(id)!!
        repository.saveGoal(original.copy(title = "Study Kotlin")).getOrThrow()

        assertEquals("Study Kotlin", repository.getGoal(id)?.title)
    }

    @Test
    fun deleteGoalHidesItAndStopsIt() = runTest {
        val id = repository.saveGoal(GoalEntity(title = "Study", date = today.minusDays(1))).getOrThrow()
        repository.deleteGoal(id).getOrThrow()
        repository.syncCarryForward(today, enabled = true).getOrThrow()

        assertTrue(repository.exportSnapshot().isEmpty())
    }

    @Test
    fun incompleteGoalCarriesToNextDay() = runTest {
        repository.saveGoal(GoalEntity(title = "Exercise", date = today.minusDays(1))).getOrThrow()
        assertEquals(1, repository.syncCarryForward(today, enabled = true).getOrThrow())

        val goals = repository.exportSnapshot()
        assertEquals(2, goals.size)
        assertTrue(goals.last().isCarriedForward)
        assertEquals(today, goals.last().date)
    }

    @Test
    fun carryForwardDoesNotDuplicate() = runTest {
        repository.saveGoal(GoalEntity(title = "Exercise", date = today.minusDays(1))).getOrThrow()
        repository.syncCarryForward(today, enabled = true).getOrThrow()
        assertEquals(0, repository.syncCarryForward(today, enabled = true).getOrThrow())
        assertEquals(2, repository.exportSnapshot().size)
    }

    @Test
    fun multipleDayCarryForwardPreservesDailyHistory() = runTest {
        repository.saveGoal(GoalEntity(title = "Read", date = today.minusDays(3))).getOrThrow()
        assertEquals(3, repository.syncCarryForward(today, enabled = true).getOrThrow())

        val goals = repository.exportSnapshot()
        assertEquals(4, goals.size)
        assertEquals(
            listOf(today.minusDays(3), today.minusDays(2), today.minusDays(1), today),
            goals.map { it.date },
        )
        assertTrue(goals.drop(1).all(GoalEntity::isCarriedForward))
    }

    @Test
    fun disabledCarryForwardLeavesGoalOnOriginalDate() = runTest {
        repository.saveGoal(GoalEntity(title = "Read", date = today.minusDays(1))).getOrThrow()
        assertEquals(0, repository.syncCarryForward(today, enabled = false).getOrThrow())
        assertFalse(repository.exportSnapshot().single().isCarriedForward)
    }
}
