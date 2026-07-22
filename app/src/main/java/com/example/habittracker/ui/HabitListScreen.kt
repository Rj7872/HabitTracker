package com.example.habittracker.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HabitListScreen(viewModel: HabitViewModel) {
    val habits by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var habitPendingDelete by remember { mutableStateOf<HabitUiState?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Habit Tracker", fontWeight = FontWeight.Bold) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add habit")
            }
        },
        bottomBar = { BannerAd() }
    ) { padding ->
        if (habits.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No habits yet. Tap + to add your first one.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(habits, key = { it.habit.id }) { state ->
                    HabitRow(
                        state = state,
                        onToggle = { viewModel.toggleToday(state.habit.id) },
                        onLongPress = { habitPendingDelete = state }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                viewModel.addHabit(name, "#6750A4")
                showAddDialog = false
            }
        )
    }

    habitPendingDelete?.let { state ->
        AlertDialog(
            onDismissRequest = { habitPendingDelete = null },
            title = { Text("Delete \"${state.habit.name}\"?") },
            text = { Text("This removes the habit and all its history.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteHabit(state.habit)
                    habitPendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { habitPendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HabitRow(
    state: HabitUiState,
    onToggle: () -> Unit,
    onLongPress: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onToggle, onLongClick = onLongPress)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (state.doneToday) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                contentDescription = if (state.doneToday) "Completed today" else "Not completed today",
                tint = if (state.doneToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(state.habit.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    if (state.streak > 0) "🔥 ${state.streak} day streak" else "No streak yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AddHabitDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New habit") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Habit name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }, enabled = text.isNotBlank()) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
