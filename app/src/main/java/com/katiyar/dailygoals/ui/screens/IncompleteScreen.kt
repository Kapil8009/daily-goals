package com.katiyar.dailygoals.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import com.katiyar.dailygoals.ui.components.EmptyState
import com.katiyar.dailygoals.ui.components.GoalItem
import com.katiyar.dailygoals.viewmodel.GoalViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private enum class DateFilter(val label: String) {
    ALL("All dates"),
    TODAY("Today"),
    OVERDUE("Past"),
    FUTURE("Future"),
}

@Composable
fun IncompleteScreen(
    viewModel: GoalViewModel,
    onEdit: (GoalEntity) -> Unit,
) {
    val allGoals by viewModel.incompleteGoals.collectAsStateWithLifecycle()
    val today by viewModel.today.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf<Priority?>(null) }
    var dateFilter by remember { mutableStateOf(DateFilter.ALL) }
    var pendingDelete by remember { mutableStateOf<GoalEntity?>(null) }

    val filtered = remember(allGoals, query, priority, dateFilter, today) {
        allGoals.filter { goal ->
            (query.isBlank() || goal.title.contains(query, true) || goal.description.orEmpty().contains(query, true)) &&
                (priority == null || goal.priority == priority) &&
                when (dateFilter) {
                    DateFilter.ALL -> true
                    DateFilter.TODAY -> goal.date == today
                    DateFilter.OVERDUE -> goal.date.isBefore(today)
                    DateFilter.FUTURE -> goal.date.isAfter(today)
                }
        }
    }
    val grouped = filtered.groupBy(GoalEntity::date).toSortedMap(compareByDescending { it })

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Incomplete goals", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Review, reschedule, or finish anything still open.",
                modifier = Modifier.padding(top = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search goals") },
                singleLine = true,
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DateFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = dateFilter == filter,
                        onClick = { dateFilter = filter },
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
                FilterChip(selected = priority == null, onClick = { priority = null }, label = { Text("All priorities") })
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
                    title = if (allGoals.isEmpty()) "Nothing left behind 🎉" else "No matching goals",
                    message = if (allGoals.isEmpty()) "Every goal is complete." else "Try changing the search or filters.",
                )
            }
        } else {
            grouped.forEach { (date, goals) ->
                item(key = "header-$date") {
                    Text(
                        friendlyDate(date, today),
                        modifier = Modifier.padding(top = 12.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                items(goals, key = GoalEntity::id) { goal ->
                    GoalItem(
                        goal = goal,
                        onToggle = { viewModel.toggleCompleted(goal) },
                        onEdit = { onEdit(goal) },
                        onDelete = { pendingDelete = goal },
                        onMove = {
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    viewModel.moveGoal(goal, LocalDate.of(year, month + 1, day))
                                },
                                goal.date.year,
                                goal.date.monthValue - 1,
                                goal.date.dayOfMonth,
                            ).show()
                        },
                    )
                }
            }
        }
    }

    pendingDelete?.let { goal ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete goal?") },
            text = { Text("This removes “${goal.title}” from its current date and stops this copy carrying forward.") },
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

private fun friendlyDate(date: LocalDate, today: LocalDate): String = when (date) {
    today -> "Today"
    today.minusDays(1) -> "Yesterday"
    else -> date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
}
