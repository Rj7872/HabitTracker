package com.example.habittracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.habittracker.data.HabitType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Fixed "done" green used for the status indicator, independent of each
// habit's own color — gives a single consistent completion affordance.
private val DoneGreen = Color(0xFF2FB673)
private val FabDark = Color(0xFF16302B)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HabitListScreen(viewModel: HabitViewModel) {
    val habits by viewModel.uiState.collectAsState()
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

    val doneCount = habits.count { it.doneOnSelectedDay }
    val totalCount = habits.size
    val progressFraction = if (totalCount == 0) 0f else doneCount.toFloat() / totalCount
    val progressPercent = (progressFraction * 100).toInt()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = FabDark,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add habit")
                }
            },
            bottomBar = { BannerAd() }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    HomeHeader(doneCount = doneCount, totalCount = totalCount, progressFraction = progressFraction, progressPercent = progressPercent)
                }
                if (habits.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No habits yet. Tap + to add your first one.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(habits, key = { it.habit.id }) { state ->
                        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                            HabitCard(
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
            shape = RoundedCornerShape(24.dp),
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

@Composable
private fun HomeHeader(doneCount: Int, totalCount: Int, progressFraction: Float, progressPercent: Int) {
    val today = remember { LocalDate.now() }
    val dateText = remember { today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")) }

    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
        Text("Hello,", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
        Text(dateText, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (totalCount == 0) "Add your first habit to get started!"
                        else "Good job! You've completed $doneCount of $totalCount habits today",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    if (totalCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "$progressPercent%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (totalCount > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HabitCard(
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = {}, onLongClick = onLongPress),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(baseColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                if (state.frozenOnSelectedDay) {
                    Icon(Icons.Filled.AcUnit, contentDescription = "Frozen", tint = baseColor, modifier = Modifier.size(22.dp))
                } else {
                    Text(
                        habit.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        color = baseColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(habit.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitleFor(state), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    if (state.streak > 0) "Streak: ${state.streak} days \uD83D\uDD25" else "No streak yet",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (habit.habitType == HabitType.COUNT) {
                    Spacer(modifier = Modifier.height(6.dp))
                    CountDots(value = state.valueForSelectedDay, target = habit.targetCount, color = baseColor)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (freezeAvailable && !state.doneOnSelectedDay && !state.frozenOnSelectedDay) {
                IconButton(onClick = onUseFreeze, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.AcUnit, contentDescription = "Use Streak Freeze", tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
            StatusControl(state = state, baseColor = baseColor, onSimpleToggle = onSimpleToggle, onIncrement = onIncrement, onTimerToggle = onTimerToggle)
        }
    }
}

@Composable
private fun CountDots(value: Int, target: Int, color: Color) {
    val dotsToShow = target.coerceAtMost(8)
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(dotsToShow) { index ->
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(if (index < value) color else color.copy(alpha = 0.2f))
            )
        }
    }
}

private fun subtitleFor(state: HabitUiState): String {
    if (state.frozenOnSelectedDay) return "Streak protected \u2744\uFE0F"
    val habit = state.habit
    return when (habit.habitType) {
        HabitType.SIMPLE -> if (state.doneOnSelectedDay) "Completed" else "Not completed"
        HabitType.COUNT -> "${state.valueForSelectedDay}/${habit.targetCount} completed"
        HabitType.TIMER -> {
            val elapsed = formatSeconds(state.valueForSelectedDay)
            val target = formatSeconds(habit.targetDurationSeconds)
            "$elapsed / $target"
        }
    }
}

private fun formatSeconds(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) "%02d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}

@Composable
private fun StatusControl(
    state: HabitUiState,
    baseColor: Color,
    onSimpleToggle: () -> Unit,
    onIncrement: () -> Unit,
    onTimerToggle: () -> Unit
) {
    when (state.habit.habitType) {
        HabitType.SIMPLE, HabitType.COUNT -> {
            val done = state.doneOnSelectedDay
            val onClick = if (state.habit.habitType == HabitType.SIMPLE) onSimpleToggle else onIncrement
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable(onClick = onClick)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (done) DoneGreen else Color.Transparent)
                        .border(2.dp, if (done) DoneGreen else MaterialTheme.colorScheme.outline, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (done) Icon(Icons.Filled.Check, contentDescription = "Done", tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    if (done) "Done!" else "Tap",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (done) DoneGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (done) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
        HabitType.TIMER -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable(onClick = onTimerToggle)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (state.isTimerRunning) baseColor else Color.Transparent)
                        .border(2.dp, baseColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (state.isTimerRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (state.isTimerRunning) "Pause" else "Start",
                        tint = if (state.isTimerRunning) Color.White else baseColor
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    if (state.isTimerRunning) "Running" else "Start",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
