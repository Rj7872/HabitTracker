package com.example.habittracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

private enum class Screen { HOME, STATS, AWARDS, SETTINGS }

@Composable
fun RootScreen(viewModel: HabitViewModel, onDynamicColorChanged: (Boolean) -> Unit) {
    var screen by remember { mutableStateOf(Screen.HOME) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = screen == Screen.HOME,
                    onClick = { screen = Screen.HOME },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = screen == Screen.STATS,
                    onClick = { screen = Screen.STATS },
                    icon = { Icon(Icons.Filled.BarChart, contentDescription = "Stats") },
                    label = { Text("Stats") }
                )
                NavigationBarItem(
                    selected = screen == Screen.AWARDS,
                    onClick = { screen = Screen.AWARDS },
                    icon = { Icon(Icons.Filled.EmojiEvents, contentDescription = "Awards") },
                    label = { Text("Awards") }
                )
                NavigationBarItem(
                    selected = screen == Screen.SETTINGS,
                    onClick = { screen = Screen.SETTINGS },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(bottom = padding.calculateBottomPadding())) {
            when (screen) {
                Screen.HOME -> HabitListScreen(viewModel)
                Screen.STATS -> StreakCalendarScreen(viewModel)
                Screen.AWARDS -> AchievementsScreen(viewModel)
                Screen.SETTINGS -> SettingsScreen(viewModel, onDynamicColorChanged)
            }
        }
    }
}
