package com.katiyar.dailygoals.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy policy") },
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { Text("Daily Goals privacy", style = MaterialTheme.typography.headlineSmall) }
            item { PolicyParagraph("Local storage", "Goals, completion history, and settings are stored in the app's private storage on your device. The app does not upload this data to a developer-operated server.") }
            item { PolicyParagraph("No account", "Daily Goals does not require an account and does not ask for your name, email address, contacts, location, camera, microphone, phone, or SMS data.") }
            item { PolicyParagraph("Notifications", "If you choose to enable reminders, the app uses Android notifications to show the number of incomplete goals. Notification permission is optional and requested only after an explanation.") }
            item { PolicyParagraph("Backups", "Export and import happen only when you select a file through Android's system file picker. Android device backup may include app data when device backup is enabled in system settings.") }
            item { PolicyParagraph("Network access", "The application does not declare the Internet permission. Core features work completely offline.") }
            item { PolicyParagraph("Your control", "You can edit or delete goals at any time, clear app data in Android settings, or uninstall the app to remove its local data.") }
            item { Text("Last updated: September 15, 2026", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun PolicyParagraph(title: String, body: String) {
    androidx.compose.foundation.layout.Column {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(body, modifier = Modifier.padding(top = 4.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
