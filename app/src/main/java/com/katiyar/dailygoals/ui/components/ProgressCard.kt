package com.katiyar.dailygoals.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProgressCard(goalsCount: Int, completedCount: Int, modifier: Modifier = Modifier) {
    val percentage = if (goalsCount == 0) 0 else (completedCount * 100 / goalsCount)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Today's progress", style = MaterialTheme.typography.titleMedium)
                Text("$percentage%", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            }
            LinearProgressIndicator(
                progress = { if (goalsCount == 0) 0f else completedCount.toFloat() / goalsCount },
                modifier = Modifier.fillMaxWidth(),
                trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
            )
            Text(
                if (goalsCount == 0) "Ready when you are" else "$completedCount of $goalsCount goals completed",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
