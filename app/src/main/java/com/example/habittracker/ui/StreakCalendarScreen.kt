package com.example.habittracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.habittracker.data.DailyProgress
import com.example.habittracker.data.Habit
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun StreakCalendarScreen(viewModel: HabitViewModel) {
    val habits by viewModel.uiState.collectAsState()

    if (habits.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Add a habit first to see its streak calendar.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    var selectedHabit by remember(habits) { mutableStateOf(habits.first().habit) }
    var yearMonth by remember { mutableStateOf(YearMonth.now()) }
    var doneDays by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var best by remember { mutableStateOf(0) }

    LaunchedEffect(selectedHabit.id, habits) {
        // Re-pull this habit's full history whenever it changes.
        val progress = viewModel.progressForHabitBlocking(selectedHabit.id)
        doneDays = progress.filter { it.done }.map { it.epochDay }.toSet()
        best = viewModel.bestStreakBlocking(selectedHabit.id)
    }

    val streak = habits.find { it.habit.id == selectedHabit.id }?.streak ?: 0
    val baseColor = runCatching { Color(android.graphics.Color.parseColor(selectedHabit.colorHex)) }.getOrDefault(Color.Gray)

    val today = LocalDate.now()
    val daysElapsedInMonth = if (YearMonth.from(today) == yearMonth) today.dayOfMonth else yearMonth.lengthOfMonth()
    val doneThisMonth = doneDays.count { epochDay ->
        val date = LocalDate.ofEpochDay(epochDay)
        YearMonth.from(date) == yearMonth
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Streak calendar", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        // Habit selector chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            habits.forEach { state ->
                val color = runCatching { Color(android.graphics.Color.parseColor(state.habit.colorHex)) }.getOrDefault(Color.Gray)
                val isSelected = state.habit.id == selectedHabit.id
                AssistChip(
                    onClick = { selectedHabit = state.habit },
                    label = { Text(state.habit.name) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (isSelected) color else Color.Transparent
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Records", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                RecordCard(emoji = "📅", value = "$daysElapsedInMonth", label = "Days in ${yearMonth.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())}", color = baseColor, modifier = Modifier.weight(1f))
                RecordCard(emoji = "✅", value = "$doneThisMonth", label = "Done this month", color = baseColor, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                RecordCard(emoji = "🔥", value = "$streak", label = "Current streak", color = baseColor, modifier = Modifier.weight(1f))
                RecordCard(emoji = "🏆", value = "$best", label = "Best streak", color = baseColor, modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { yearMonth = yearMonth.minusMonths(1) }) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month")
            }
            Text(
                "${yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${yearMonth.year}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = { yearMonth = yearMonth.plusMonths(1) }) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Next month")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        MonthGrid(yearMonth = yearMonth, doneDays = doneDays, color = baseColor)
    }
}

@Composable
private fun RecordCard(emoji: String, value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(emoji, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MonthGrid(yearMonth: YearMonth, doneDays: Set<Long>, color: Color) {
    val firstOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    // Monday-first offset (0 = Monday)
    val leadingBlanks = (firstOfMonth.dayOfWeek.value + 6) % 7

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { label ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(44.dp * ((daysInMonth + leadingBlanks) / 7 + 1))
        ) {
            items(leadingBlanks) { Box(modifier = Modifier.size(44.dp)) }
            items(daysInMonth) { index ->
                val day = index + 1
                val date = yearMonth.atDay(day)
                val isDone = date.toEpochDay() in doneDays
                val isFuture = date.isAfter(LocalDate.now())
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                when {
                                    isDone -> color
                                    else -> Color.Transparent
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            day.toString(),
                            color = when {
                                isDone -> Color.White
                                isFuture -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
