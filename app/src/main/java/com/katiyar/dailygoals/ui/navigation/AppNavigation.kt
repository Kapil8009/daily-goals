package com.katiyar.dailygoals.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.katiyar.dailygoals.ui.screens.AddEditGoalScreen
import com.katiyar.dailygoals.ui.screens.HistoryScreen
import com.katiyar.dailygoals.ui.screens.IncompleteScreen
import com.katiyar.dailygoals.ui.screens.PrivacyPolicyScreen
import com.katiyar.dailygoals.ui.screens.SettingsScreen
import com.katiyar.dailygoals.ui.screens.StatisticsScreen
import com.katiyar.dailygoals.ui.screens.TodayScreen
import com.katiyar.dailygoals.viewmodel.GoalViewModel

private data class NavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

private val bottomItems = listOf(
    NavItem("today", "Today", Icons.Default.Home),
    NavItem("incomplete", "Incomplete", Icons.AutoMirrored.Filled.List),
    NavItem("history", "History", Icons.Default.DateRange),
    NavItem("stats", "Stats", Icons.Default.Star),
    NavItem("settings", "Settings", Icons.Default.Settings),
)

@Composable
fun AppNavigation(viewModel: GoalViewModel) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val isTopLevel = bottomItems.any { it.route == route }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (isTopLevel) {
                NavigationBar {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            selected = route == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            alwaysShowLabel = true,
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (route == "today") {
                FloatingActionButton(onClick = { navController.navigate("goal/-1") }) {
                    Icon(Icons.Default.Add, contentDescription = "Add goal")
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "today",
            modifier = Modifier.padding(padding),
        ) {
            composable("today") {
                TodayScreen(
                    viewModel = viewModel,
                    onAdd = { navController.navigate("goal/-1") },
                    onEdit = { navController.navigate("goal/${it.id}") },
                )
            }
            composable("incomplete") {
                IncompleteScreen(
                    viewModel = viewModel,
                    onEdit = { navController.navigate("goal/${it.id}") },
                )
            }
            composable("history") {
                HistoryScreen(
                    viewModel = viewModel,
                    onEdit = { navController.navigate("goal/${it.id}") },
                )
            }
            composable("stats") { StatisticsScreen(viewModel) }
            composable("settings") {
                SettingsScreen(
                    viewModel = viewModel,
                    onPrivacy = { navController.navigate("privacy") },
                )
            }
            composable("goal/{goalId}") { entry ->
                AddEditGoalScreen(
                    goalId = entry.arguments?.getString("goalId")?.toLongOrNull()?.takeIf { it > 0 },
                    viewModel = viewModel,
                    onBack = navController::popBackStack,
                )
            }
            composable("privacy") { PrivacyPolicyScreen(onBack = navController::popBackStack) }
        }
    }
}
