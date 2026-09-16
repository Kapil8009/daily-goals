package com.katiyar.dailygoals.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.katiyar.dailygoals.domain.usecase.GoalRules
import com.katiyar.dailygoals.viewmodel.GoalViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle

@Composable
fun StatisticsScreen(viewModel: GoalViewModel) {
    val stats by viewModel.statistics.collectAsStateWithLifecycle()
    val goals by viewModel.allGoals.collectAsStateWithLifecycle()
    val today by viewModel.today.collectAsStateWithLifecycle()
    val locale = LocalConfiguration.current.locales[0]
    val weekStart = today.minusDays((today.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("Your progress 📊", style = MaterialTheme.typography.headlineMedium)
            Text("Small steps add up.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Today", "${stats.todayPercentage}%", Modifier.weight(1f))
                StatCard("This week", "${stats.weeklyPercentage}%", Modifier.weight(1f))
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Completed goals", stats.totalCompleted.toString(), Modifier.weight(1f))
                StatCard("Incomplete goals", stats.totalIncomplete.toString(), Modifier.weight(1f))
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Current streak", "${stats.currentStreak} days 🔥", Modifier.weight(1f))
                StatCard("Best streak", "${stats.bestStreak} days 🏆", Modifier.weight(1f))
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("This week", style = MaterialTheme.typography.titleLarge)
                    repeat(7) { offset ->
                        val date = weekStart.plusDays(offset.toLong())
                        val dayGoals = goals.filter { it.date == date }
                        val percentage = GoalRules.completionPercentage(dayGoals)
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale))
                                Text(if (dayGoals.isEmpty()) "—" else "$percentage%")
                            }
                            LinearProgressIndicator(
                                progress = { percentage / 100f },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}
