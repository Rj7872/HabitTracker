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
import androidx.compose.material.icons.filled.AcUnit
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
    val freezeCount by viewModel.freezeCountFlow.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var habitPendingDelete by remember { mutableStateOf<HabitUiState?>(null) }
    var showCelebration by remember { mutableStateOf(false) }
    var previousDone by remember { mutableStateOf<Map<Long, Boolean>>(emptyMap()) }

    LaunchedEffect(habits) {
        val justCompleted = habits.any { state ->
            previousDone[state.habit.id] == false && state.doneOnSelectedDay
        }
        if (justCompleted) showCelebration = true
        previousDone = habits.associate { it.habit.id to it.doneOnSelectedDay }
    }

    val habitColors = habits.map { it.habit.colorHex }

    Box(modifier = Modifier.fillMaxSize()) {
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
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("New habit", fontWeight = FontWeight.SemiBold) }
                )
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
                            freezeAvailable = freezeCount > 0,
                            onSimpleToggle = { viewModel.toggleSimple(state.habit) },
                            onIncrement = { viewModel.incrementCount(state.habit) },
                            onTimerToggle = { viewModel.toggleTimer(state.habit) },
                            onUseFreeze = { viewModel.useFreeze(state.habit) },
                            onLongPress = { habitPendingDelete = state }
                        )
                    }
                }
            }
        }

        if (showCelebration) {
            CelebrationOverlay(onFinished = { showCelebration = false })
        }
    }

    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, colorHex, type, targetCount, targetMinutes, repeatDays, reminderEnabled, reminderMode, reminderHour, reminderMinute, reminderIntervalMinutes ->
                viewModel.addHabit(
                    name, colorHex, type, targetCount, targetMinutes, repeatDays,
                    reminderEnabled, reminderMode, reminderHour, reminderMinute, reminderIntervalMinutes
                )
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
    freezeAvailable: Boolean,
    onSimpleToggle: () -> Unit,
    onIncrement: () -> Unit,
    onTimerToggle: () -> Unit,
    onUseFreeze: () -> Unit,
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
            if (state.frozenOnSelectedDay) {
                Icon(Icons.Filled.AcUnit, contentDescription = "Frozen", tint = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Text(
                    habit.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(habit.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitleFor(state), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (freezeAvailable && !state.doneOnSelectedDay && !state.frozenOnSelectedDay) {
            IconButton(onClick = onUseFreeze, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Filled.AcUnit,
                    contentDescription = "Use Streak Freeze",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        HabitControl(state = state, baseColor = baseColor, onSimpleToggle = onSimpleToggle, onIncrement = onIncrement, onTimerToggle = onTimerToggle)
    }
}

private fun subtitleFor(state: HabitUiState): String {
    if (state.frozenOnSelectedDay) return "Streak protected \u2744\uFE0F"
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
