package com.katiyar.dailygoals.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.katiyar.dailygoals.data.local.GoalEntity
import com.katiyar.dailygoals.ui.components.EmptyState
import com.katiyar.dailygoals.ui.components.GoalItem
import com.katiyar.dailygoals.ui.components.ProgressCard
import com.katiyar.dailygoals.viewmodel.GoalViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun TodayScreen(
    viewModel: GoalViewModel,
    onAdd: () -> Unit,
    onEdit: (GoalEntity) -> Unit,
) {
    val goals by viewModel.todayGoals.collectAsStateWithLifecycle()
    val today by viewModel.today.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<GoalEntity?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(greeting(), style = MaterialTheme.typography.headlineMedium)
            Text(
                today.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)),
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            ProgressCard(
                goalsCount = goals.size,
                completedCount = goals.count(GoalEntity::isCompleted),
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
        item {
            Text("Today's goals", style = MaterialTheme.typography.titleLarge)
        }
        if (goals.isEmpty()) {
            item {
                EmptyState(
                    title = "No goals for today 🎯",
                    message = "Add your first goal and make today productive!",
                    actionLabel = "+ Add Goal",
                    onAction = onAdd,
                )
            }
        } else {
            items(goals, key = GoalEntity::id) { goal ->
                GoalItem(
                    goal = goal,
                    onToggle = { viewModel.toggleCompleted(goal) },
                    onEdit = { onEdit(goal) },
                    onDelete = { pendingDelete = goal },
                )
            }
            if (goals.all(GoalEntity::isCompleted)) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text("All goals completed! 🎉", style = MaterialTheme.typography.titleLarge)
                        Text("Great job!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    pendingDelete?.let { goal ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete goal?") },
            text = { Text("“${goal.title}” will stop carrying forward. Its earlier history stays intact.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteGoal(goal)
                    pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } },
        )
    }
}

private fun greeting(now: LocalTime = LocalTime.now()): String = when (now.hour) {
    in 5..11 -> "Good morning 👋"
    in 12..16 -> "Good afternoon 👋"
    else -> "Good evening 👋"
}
