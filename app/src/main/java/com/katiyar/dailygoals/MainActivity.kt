package com.katiyar.dailygoals

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.katiyar.dailygoals.ui.navigation.AppNavigation
import com.katiyar.dailygoals.ui.theme.DailyGoalsTheme
import com.katiyar.dailygoals.viewmodel.GoalViewModel

class MainActivity : ComponentActivity() {
    private val goalViewModel: GoalViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by goalViewModel.settings.collectAsStateWithLifecycle()
            DailyGoalsTheme(themeMode = settings.themeMode) {
                AppNavigation(viewModel = goalViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        goalViewModel.refreshForCurrentDate()
    }
}
