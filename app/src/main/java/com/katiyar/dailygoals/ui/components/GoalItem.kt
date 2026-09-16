package com.katiyar.dailygoals.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.katiyar.dailygoals.data.local.GoalEntity
import com.katiyar.dailygoals.domain.model.Priority
import java.time.format.DateTimeFormatter

@Composable
fun GoalItem(
    goal: GoalEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    onMove: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (goal.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (goal.isCompleted) 0.dp else 2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Checkbox(checked = goal.isCompleted, onCheckedChange = { onToggle() })
            Column(modifier = Modifier.weight(1f).padding(top = 9.dp, bottom = 6.dp)) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (goal.isCompleted) TextDecoration.LineThrough else null,
                    color = if (goal.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
                goal.description?.let {
                    Text(
                        text = it,
                        modifier = Modifier.padding(top = 3.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PriorityLabel(goal.priority)
                    goal.reminderTime?.let {
                        Text(
                            it.format(DateTimeFormatter.ofPattern("h:mm a")),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (goal.isCarriedForward) {
                    Text(
                        "↻ Carried forward",
                        modifier = Modifier.padding(top = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Column {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit ${goal.title}")
                }
                if (onMove != null) {
                    IconButton(onClick = onMove) {
                        Icon(Icons.Default.DateRange, contentDescription = "Move ${goal.title}")
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete ${goal.title}")
                }
            }
        }
    }
}

@Composable
private fun PriorityLabel(priority: Priority) {
    val color = when (priority) {
        Priority.LOW -> Color(0xFF4F7F69)
        Priority.MEDIUM -> Color(0xFF9B6A16)
        Priority.HIGH -> MaterialTheme.colorScheme.error
    }
    Text(priority.label, style = MaterialTheme.typography.labelMedium, color = color)
}
