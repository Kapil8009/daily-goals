package com.katiyar.dailygoals.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.ColumnScope
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.katiyar.dailygoals.BuildConfig
import com.katiyar.dailygoals.domain.model.ThemeMode
import com.katiyar.dailygoals.viewmodel.GoalViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen(
    viewModel: GoalViewModel,
    onPrivacy: () -> Unit,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showNotificationRationale by remember { mutableStateOf(false) }
    var permissionDenied by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) viewModel.setNotificationsEnabled(true) else permissionDenied = true
    }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> uri?.let(viewModel::exportGoals) }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let(viewModel::importGoals) }

    fun requestNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.setNotificationsEnabled(true)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("Settings", style = MaterialTheme.typography.headlineMedium)
            Text("Make Daily Goals work your way.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item { SectionLabel("Planning") }
        item {
            SettingsCard {
                SettingToggleRow(
                    title = "Notifications",
                    subtitle = "A daily prompt to review unfinished goals",
                    checked = settings.notificationsEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) showNotificationRationale = true else viewModel.setNotificationsEnabled(false)
                    },
                )
                SettingRow(
                    title = "Daily reminder",
                    value = settings.reminderTime.format(DateTimeFormatter.ofPattern("h:mm a")),
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, hour, minute -> viewModel.setReminderTime(java.time.LocalTime.of(hour, minute)) },
                            settings.reminderTime.hour,
                            settings.reminderTime.minute,
                            false,
                        ).show()
                    },
                )
                SettingToggleRow(
                    title = "Carry forward",
                    subtitle = "Copy unfinished goals into each new day",
                    checked = settings.carryForwardEnabled,
                    onCheckedChange = viewModel::setCarryForwardEnabled,
                )
            }
        }
        item { SectionLabel("Appearance") }
        item {
            SettingsCard {
                Text("Theme", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = settings.themeMode == mode,
                            onClick = { viewModel.setThemeMode(mode) },
                            label = { Text(mode.label) },
                        )
                    }
                }
            }
        }
        item { SectionLabel("Backup") }
        item {
            SettingsCard {
                SettingRow("Export goals", "JSON file") {
                    exportLauncher.launch("daily-goals-${LocalDate.now()}.json")
                }
                SettingRow("Import goals", "Choose a Daily Goals JSON backup") {
                    importLauncher.launch(arrayOf("application/json", "text/json", "text/plain"))
                }
            }
        }
        item { SectionLabel("About") }
        item {
            SettingsCard {
                SettingRow("Privacy policy", "How your on-device data is handled", onPrivacy)
                SettingRow("Rate app", "Open the Play Store") {
                    val market = Intent(Intent.ACTION_VIEW, "market://details?id=${context.packageName}".toUri())
                    try {
                        context.startActivity(market)
                    } catch (_: ActivityNotFoundException) {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=${context.packageName}".toUri()),
                        )
                    }
                }
                SettingRow("Share app", "Tell someone about Daily Goals") {
                    val share = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Try Daily Goals: https://play.google.com/store/apps/details?id=${context.packageName}",
                        )
                    }
                    context.startActivity(Intent.createChooser(share, "Share Daily Goals"))
                }
                SettingRow("Version", BuildConfig.VERSION_NAME, null)
            }
        }
    }

    if (showNotificationRationale) {
        AlertDialog(
            onDismissRequest = { showNotificationRationale = false },
            title = { Text("Stay on top of unfinished goals") },
            text = {
                Text("Daily Goals can send one reminder around your chosen time. Notifications contain only the number of unfinished goals and can be turned off anytime.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showNotificationRationale = false
                    requestNotifications()
                }) { Text("Continue") }
            },
            dismissButton = { TextButton(onClick = { showNotificationRationale = false }) { Text("Not now") } },
        )
    }
    if (permissionDenied) {
        AlertDialog(
            onDismissRequest = { permissionDenied = false },
            title = { Text("Notifications are off") },
            text = { Text("Permission was not granted. You can enable notifications later from Android system settings.") },
            confirmButton = { TextButton(onClick = { permissionDenied = false }) { Text("OK") } },
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content,
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingRow(title: String, value: String, onClick: (() -> Unit)?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 12.dp))
    }
}
