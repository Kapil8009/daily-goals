package com.katiyar.dailygoals.domain.usecase

import com.katiyar.dailygoals.data.local.GoalEntity
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class GoalRulesTest {
    private val today = LocalDate.of(2026, 9, 15)

    @Test
    fun `empty goal title is rejected`() {
        assertNotNull(GoalRules.validateTitle("   "))
    }

    @Test
    fun `non-empty goal title is accepted`() {
        assertNull(GoalRules.validateTitle("Read 20 pages"))
    }

    @Test
    fun `completion percentage is calculated`() {
        val goals = listOf(goal(today, true), goal(today, true), goal(today, false))
        assertEquals(66, GoalRules.completionPercentage(goals))
    }

    @Test
    fun `empty completion percentage is zero`() {
        assertEquals(0, GoalRules.completionPercentage(emptyList()))
    }

    @Test
    fun `multiple carry dates include every missed day`() {
        assertEquals(
            listOf(today.minusDays(2), today.minusDays(1), today),
            GoalRules.datesToCarry(today.minusDays(3), today),
        )
    }

    @Test
    fun `statistics calculate totals week and streaks`() {
        val goals = listOf(
            goal(today.minusDays(3), true),
            goal(today.minusDays(2), true),
            goal(today.minusDays(1), true),
            goal(today, true),
            goal(today, false),
        )
        val stats = GoalRules.statistics(goals, today)
        assertEquals(50, stats.todayPercentage)
        assertEquals(66, stats.weeklyPercentage)
        assertEquals(4, stats.totalCompleted)
        assertEquals(1, stats.totalIncomplete)
        assertEquals(3, stats.currentStreak)
        assertEquals(3, stats.bestStreak)
    }

    private fun goal(date: LocalDate, completed: Boolean) = GoalEntity(
        title = "Goal $date $completed",
        date = date,
        isCompleted = completed,
    )
}
