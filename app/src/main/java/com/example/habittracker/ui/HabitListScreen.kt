package com.example.habittracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.habittracker.data.Habit
import com.example.habittracker.data.HabitType
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HabitListScreen(viewModel: HabitViewModel) {
    val habits by viewModel.uiState.collectAsState()
    val selectedEpochDay by viewModel.selectedEpochDayFlow.collectAsState()
    val selectedDate = remember(selectedEpochDay) { LocalDate.ofEpochDay(selectedEpochDay) }

    var showAddDialog by remember { mutableStateOf(false) }
    var habitPendingDelete by remember { mutableStateOf<HabitUiState?>(null) }

    val habitColors = habits.map { it.habit.colorHex }

    Scaffold(
        topBar = {
            WeekStrip(
                selectedDate = selectedDate,
                habitColors = habitColors,
                doneOnDay = { date ->
                    if (date == selectedDate) {
                        habits.mapIndexedNotNull { index, state -> if (state.doneOnSelectedDay) index else null }.toSet()
                    } else emptySet()
                },
                onSelectDate = { viewModel.selectDate(it) },
                onJumpToday = { viewModel.selectToday() }
            )
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(habits, key = { it.habit.id }) { state ->
                    HabitRow(
                        state = state,
                        onSimpleToggle = { viewModel.toggleSimple(state.habit) },
                        onIncrement = { viewModel.incrementCount(state.habit) },
                        onTimerToggle = { viewModel.toggleTimer(state.habit) },
                        onLongPress = { habitPendingDelete = state }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, colorHex, type, targetCount, targetMinutes, repeatDays, reminderEnabled, reminderHour, reminderMinute ->
                viewModel.addHabit(name, colorHex, type, targetCount, targetMinutes, repeatDays, reminderEnabled, reminderHour, reminderMinute)
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
    onSimpleToggle: () -> Unit,
    onIncrement: () -> Unit,
    onTimerToggle: () -> Unit,
    onLongPress: () -> Unit
) {
    val habit = state.habit
    val baseColor = runCatching { Color(android.graphics.Color.parseColor(habit.colorHex)) }.getOrDefault(Color.Gray)
    val backgroundColor = baseColor.copy(alpha = 0.22f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .combinedClickable(onClick = {}, onLongClick = onLongPress)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(baseColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                habit.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(habit.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitleFor(state), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(8.dp))
        HabitControl(state = state, baseColor = baseColor, onSimpleToggle = onSimpleToggle, onIncrement = onIncrement, onTimerToggle = onTimerToggle)
    }
}

private fun subtitleFor(state: HabitUiState): String {
    val habit = state.habit
    return when (habit.habitType) {
        HabitType.SIMPLE -> if (state.doneOnSelectedDay) "Completed" else "Not completed"
        HabitType.COUNT -> if (state.doneOnSelectedDay) "Completed" else "${state.valueForSelectedDay}/${habit.targetCount}"
        HabitType.TIMER -> {
            val elapsed = formatSeconds(state.valueForSelectedDay)
            val target = formatSeconds(habit.targetDurationSeconds)
            "$elapsed/$target"
        }
    }
}

private fun formatSeconds(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) "%02d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HabitControl(
    state: HabitUiState,
    baseColor: Color,
    onSimpleToggle: () -> Unit,
    onIncrement: () -> Unit,
    onTimerToggle: () -> Unit
) {
    when (state.habit.habitType) {
        HabitType.SIMPLE -> {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(if (state.doneOnSelectedDay) baseColor else Color.Transparent)
                    .border(2.dp, baseColor, CircleShape)
                    .combinedClickable(onClick = onSimpleToggle, onLongClick = {}),
                contentAlignment = Alignment.Center
            ) {
                if (state.doneOnSelectedDay) {
                    Icon(Icons.Filled.Check, contentDescription = "Completed", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
        HabitType.COUNT -> {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (state.doneOnSelectedDay) baseColor else Color.Transparent)
                    .border(2.dp, baseColor, CircleShape)
                    .combinedClickable(onClick = onIncrement, onLongClick = {}),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (state.doneOnSelectedDay) Icons.Filled.Check else Icons.Filled.Add,
                    contentDescription = if (state.doneOnSelectedDay) "Completed" else "Increment",
                    tint = if (state.doneOnSelectedDay) Color.White else baseColor
                )
            }
        }
        HabitType.TIMER -> {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (state.isTimerRunning) baseColor else Color.Transparent)
                    .combinedClickable(onClick = onTimerToggle, onLongClick = {}),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (state.isTimerRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (state.isTimerRunning) "Pause" else "Start",
                    tint = if (state.isTimerRunning) Color.White else baseColor
                )
            }
        }
    }
}
