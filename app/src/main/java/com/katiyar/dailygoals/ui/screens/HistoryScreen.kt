package com.katiyar.dailygoals.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.katiyar.dailygoals.data.local.GoalEntity
import com.katiyar.dailygoals.domain.model.Priority
import com.katiyar.dailygoals.domain.usecase.GoalRules
import com.katiyar.dailygoals.ui.components.EmptyState
import com.katiyar.dailygoals.ui.components.GoalItem
import com.katiyar.dailygoals.viewmodel.GoalViewModel
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private enum class StatusFilter(val label: String) {
    ALL("All"),
    COMPLETED("Completed"),
    INCOMPLETE("Incomplete"),
}

@Composable
fun HistoryScreen(
    viewModel: GoalViewModel,
    onEdit: (GoalEntity) -> Unit,
) {
    val date by viewModel.historyDate.collectAsStateWithLifecycle()
    val goals by viewModel.historyGoals.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(StatusFilter.ALL) }
    var priority by remember { mutableStateOf<Priority?>(null) }
    var pendingDelete by remember { mutableStateOf<GoalEntity?>(null) }

    val filtered = remember(goals, query, status, priority) {
        goals.filter { goal ->
            (query.isBlank() || goal.title.contains(query, true) || goal.description.orEmpty().contains(query, true)) &&
                (priority == null || goal.priority == priority) &&
                when (status) {
                    StatusFilter.ALL -> true
                    StatusFilter.COMPLETED -> goal.isCompleted
                    StatusFilter.INCOMPLETE -> !goal.isCompleted
                }
        }
    }
    val completed = goals.count(GoalEntity::isCompleted)
    val percentage = GoalRules.completionPercentage(goals)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("History", style = MaterialTheme.typography.headlineMedium)
            Text("Look back at any day's progress.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                viewModel.selectHistoryDate(java.time.LocalDate.of(year, month + 1, day))
                            },
                            date.year,
                            date.monthValue - 1,
                            date.dayOfMonth,
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { viewModel.selectHistoryDate(date.minusDays(1)) }) { Text("← Previous") }
                    TextButton(onClick = { viewModel.selectHistoryDate(java.time.LocalDate.now()) }) { Text("Today") }
                    TextButton(onClick = { viewModel.selectHistoryDate(date.plusDays(1)) }) { Text("Next →") }
                }
            }
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("$percentage% complete", style = MaterialTheme.typography.titleLarge)
                    LinearProgressIndicator(
                        progress = { percentage / 100f },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text("Completed: $completed  •  Incomplete: ${goals.size - completed}  •  Total: ${goals.size}")
                }
            }
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search this date") },
                singleLine = true,
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatusFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = status == filter,
                        onClick = { status = filter },
                        label = { Text(filter.label) },
                    )
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(selected = priority == null, onClick = { priority = null }, label = { Text("Any priority") })
                Priority.entries.forEach { value ->
                    FilterChip(
                        selected = priority == value,
                        onClick = { priority = value },
                        label = { Text(value.label) },
                    )
                }
            }
        }
        if (filtered.isEmpty()) {
            item {
                EmptyState(
                    title = if (goals.isEmpty()) "No goals on this date" else "No matching goals",
                    message = if (goals.isEmpty()) "Choose another date to review your history." else "Try changing the search or filters.",
                )
            }
        } else {
            items(filtered, key = GoalEntity::id) { goal ->
                GoalItem(
                    goal = goal,
                    onToggle = { viewModel.toggleCompleted(goal) },
                    onEdit = { onEdit(goal) },
                    onDelete = { pendingDelete = goal },
                )
            }
        }
    }

    pendingDelete?.let { goal ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete goal?") },
            text = { Text("Delete “${goal.title}” from this date?") },
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
