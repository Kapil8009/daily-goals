package com.katiyar.dailygoals.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.katiyar.dailygoals.data.local.GoalEntity
import com.katiyar.dailygoals.domain.model.Priority
import com.katiyar.dailygoals.domain.usecase.GoalRules
import com.katiyar.dailygoals.viewmodel.GoalViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditGoalScreen(
    goalId: Long?,
    viewModel: GoalViewModel,
    onBack: () -> Unit,
) {
    val allGoals by viewModel.allGoals.collectAsStateWithLifecycle()
    val existing = goalId?.let { id -> allGoals.firstOrNull { it.id == id } }
    val context = LocalContext.current
    var initialized by remember(goalId) { mutableStateOf(goalId == null) }
    var title by remember(goalId) { mutableStateOf("") }
    var description by remember(goalId) { mutableStateOf("") }
    var date by remember(goalId) { mutableStateOf(LocalDate.now()) }
    var priority by remember(goalId) { mutableStateOf(Priority.MEDIUM) }
    var time by remember(goalId) { mutableStateOf<LocalTime?>(null) }
    var titleError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(existing?.id) {
        if (!initialized && existing != null) {
            title = existing.title
            description = existing.description.orEmpty()
            date = existing.date
            priority = existing.priority
            time = existing.reminderTime
            initialized = true
        }
    }

    fun save() {
        val validation = GoalRules.validateTitle(title)
        titleError = validation
        if (validation != null) return
        val goal = (existing ?: GoalEntity(title = title, date = date)).copy(
            title = title,
            description = description,
            date = date,
            priority = priority,
            reminderTime = time,
        )
        viewModel.saveGoal(goal, onBack)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (goalId == null) "Add goal" else "Edit goal") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it.take(120)
                        titleError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Goal title") },
                    placeholder = { Text("Study Java for 2 hours") },
                    supportingText = { Text(titleError ?: "${title.length}/120") },
                    isError = titleError != null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )
            }
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it.take(500) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description (optional)") },
                    minLines = 3,
                    maxLines = 5,
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Date", style = MaterialTheme.typography.titleMedium)
                    OutlinedButton(
                        onClick = {
                            DatePickerDialog(
                                context,
                                { _, year, month, day -> date = LocalDate.of(year, month + 1, day) },
                                date.year,
                                date.monthValue - 1,
                                date.dayOfMonth,
                            ).show()
                        },
                    ) { Text(date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))) }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Priority", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Priority.entries.forEach { value ->
                            FilterChip(
                                selected = priority == value,
                                onClick = { priority = value },
                                label = { Text(value.label) },
                            )
                        }
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Time (optional)", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                val initial = time ?: LocalTime.of(9, 0)
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute -> time = LocalTime.of(hour, minute) },
                                    initial.hour,
                                    initial.minute,
                                    false,
                                ).show()
                            },
                        ) {
                            Text(time?.format(DateTimeFormatter.ofPattern("h:mm a")) ?: "Choose time")
                        }
                        if (time != null) TextButton(onClick = { time = null }) { Text("Clear") }
                    }
                }
            }
            item {
                Button(onClick = ::save, modifier = Modifier.fillMaxWidth()) {
                    Text(if (goalId == null) "Save goal" else "Save changes")
                }
            }
        }
    }
}
