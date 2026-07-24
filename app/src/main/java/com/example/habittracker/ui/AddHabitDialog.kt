package com.example.habittracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.habittracker.data.HabitType
import com.example.habittracker.data.ReminderMode
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

val HabitColorPalette = listOf(
    "#6FCF97", // green
    "#F2C94C", // yellow
    "#9B51E0", // purple
    "#EB5757", // red/orange
    "#F2994A", // orange
    "#BB6BD9", // pink-purple
    "#56CCF2", // blue
    "#F783AC", // pink
    "#26A69A", // teal
    "#5C6BC0", // indigo
    "#8D6E63", // brown
    "#78909C", // blue-gray
    "#66BB6A", // dark green
    "#4DD0E1", // cyan
    "#EC407A", // magenta
    "#D4E157"  // lime
)

// Monday=1 .. Sunday=7, matching java.time.DayOfWeek.value
private val AllDays = (1..7).toList()
private val IntervalOptionsMinutes = listOf(30, 60, 120, 180, 240, 360)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        colorHex: String,
        type: HabitType,
        targetCount: Int,
        targetMinutes: Int,
        repeatDays: Set<Int>,
        reminderEnabled: Boolean,
        reminderMode: ReminderMode,
        reminderHour: Int,
        reminderMinute: Int,
        reminderIntervalMinutes: Int
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(HabitColorPalette.first()) }
    var selectedType by remember { mutableStateOf(HabitType.SIMPLE) }
    var targetCountText by remember { mutableStateOf("4") }
    var targetMinutesText by remember { mutableStateOf("30") }
    var repeatDays by remember { mutableStateOf(setOf(java.time.LocalDate.now().dayOfWeek.value)) }

    var reminderEnabled by remember { mutableStateOf(false) }
    var reminderMode by remember { mutableStateOf(ReminderMode.FIXED_TIME) }
    var reminderHour by remember { mutableStateOf(9) }
    var reminderMinute by remember { mutableStateOf(0) }
    var reminderIntervalMinutes by remember { mutableStateOf(120) }
    var showReminderPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
        title = { Text("New habit", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Habit name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Color", style = MaterialTheme.typography.labelLarge)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HabitColorPalette.chunked(6).forEach { rowColors ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            rowColors.forEach { hex ->
                                val color = Color(android.graphics.Color.parseColor(hex))
                                val isSelected = hex == selectedColor
                                Box(
                                    modifier = Modifier
                                        .size(if (isSelected) 40.dp else 32.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .then(
                                            if (isSelected)
                                                Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                            else Modifier
                                        )
                                        .clickable { selectedColor = hex },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Filled.Check, contentDescription = "Selected", tint = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                Text("Type", style = MaterialTheme.typography.labelLarge)
                Column {
                    TypeOption("Simple check-off", selectedType == HabitType.SIMPLE) { selectedType = HabitType.SIMPLE }
                    TypeOption("Counter (e.g. glasses of water)", selectedType == HabitType.COUNT) { selectedType = HabitType.COUNT }
                    TypeOption("Timer (e.g. reading, yoga)", selectedType == HabitType.TIMER) { selectedType = HabitType.TIMER }
                }

                if (selectedType == HabitType.COUNT) {
                    OutlinedTextField(
                        value = targetCountText,
                        onValueChange = { targetCountText = it.filter { c -> c.isDigit() } },
                        label = { Text("Target count per day") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (selectedType == HabitType.TIMER) {
                    OutlinedTextField(
                        value = targetMinutesText,
                        onValueChange = { targetMinutesText = it.filter { c -> c.isDigit() } },
                        label = { Text("Target minutes per day") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text("Repeat on", style = MaterialTheme.typography.labelLarge)
                DayOfWeekSelector(
                    selectedDays = repeatDays,
                    onToggleDay = { day ->
                        repeatDays = if (day in repeatDays) repeatDays - day else repeatDays + day
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Remind me", style = MaterialTheme.typography.labelLarge)
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = {
                            reminderEnabled = it
                            if (it) showReminderPicker = true
                        }
                    )
                }

                if (reminderEnabled) {
                    OutlinedButton(
                        onClick = { showReminderPicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (reminderMode == ReminderMode.FIXED_TIME)
                                formatFixedTime(reminderHour, reminderMinute)
                            else
                                "Every ${formatInterval(reminderIntervalMinutes)}"
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        name,
                        selectedColor,
                        selectedType,
                        targetCountText.toIntOrNull() ?: 4,
                        targetMinutesText.toIntOrNull() ?: 30,
                        repeatDays,
                        reminderEnabled,
                        reminderMode,
                        reminderHour,
                        reminderMinute,
                        reminderIntervalMinutes
                    )
                },
                enabled = name.isNotBlank() && repeatDays.isNotEmpty()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showReminderPicker) {
        ReminderPickerDialog(
            habitType = selectedType,
            initialMode = reminderMode,
            initialHour = reminderHour,
            initialMinute = reminderMinute,
            initialIntervalMinutes = reminderIntervalMinutes,
            onDismiss = {
                showReminderPicker = false
                // If they backed out without ever confirming a time, leave the
                // toggle on with defaults rather than silently turning it off.
            },
            onConfirm = { mode, hour, minute, intervalMinutes ->
                reminderMode = mode
                reminderHour = hour
                reminderMinute = minute
                reminderIntervalMinutes = intervalMinutes
                showReminderPicker = false
            }
        )
    }
}

@Composable
private fun TypeOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun DayOfWeekSelector(selectedDays: Set<Int>, onToggleDay: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        AllDays.forEach { day ->
            val label = DayOfWeek.of(day).getDisplayName(TextStyle.NARROW, Locale.getDefault())
            val isSelected = day in selectedDays
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) com.example.habittracker.ui.theme.HabitLightGreen else Color.Transparent)
                    .border(
                        width = 1.5.dp,
                        color = if (isSelected) com.example.habittracker.ui.theme.HabitLightGreen else MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    )
                    .clickable { onToggleDay(day) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (isSelected) Color(0xFF0B3D0B) else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

private fun to12Hour(hour24: Int): Pair<Int, Boolean> {
    val isAm = hour24 < 12
    var h12 = hour24 % 12
    if (h12 == 0) h12 = 12
    return h12 to isAm
}

private fun to24Hour(hour12: Int, isAm: Boolean): Int {
    val h = hour12 % 12
    return if (isAm) h else h + 12
}

private fun formatFixedTime(hour24: Int, minute: Int): String {
    val (h12, isAm) = to12Hour(hour24)
    return "Reminder at %d:%02d %s".format(h12, minute, if (isAm) "AM" else "PM")
}

@Composable
private fun AmPmToggle(isAm: Boolean, onChange: (Boolean) -> Unit) {
    Column {
        listOf(true to "AM", false to "PM").forEach { (value, label) ->
            val selected = isAm == value
            Box(
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onChange(value) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    label,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun formatInterval(minutes: Int): String = when {
    minutes % 60 == 0 -> "${minutes / 60}h"
    else -> "${minutes}m"
}

/** Popup shown when "Remind me" is turned on — lets the user pick a fixed time,
 * or (for Counter habits) a repeating interval instead. */
@Composable
private fun ReminderPickerDialog(
    habitType: HabitType,
    initialMode: ReminderMode,
    initialHour: Int,
    initialMinute: Int,
    initialIntervalMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (ReminderMode, Int, Int, Int) -> Unit
) {
    var mode by remember { mutableStateOf(initialMode) }
    var hour12 by remember { mutableStateOf(to12Hour(initialHour).first) }
    var isAm by remember { mutableStateOf(to12Hour(initialHour).second) }
    var minute by remember { mutableStateOf(initialMinute) }
    var intervalMinutes by remember { mutableStateOf(initialIntervalMinutes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
        title = { Text("Set reminder", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (habitType == HabitType.COUNT) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = mode == ReminderMode.FIXED_TIME,
                            onClick = { mode = ReminderMode.FIXED_TIME },
                            label = { Text("Once a day") }
                        )
                        FilterChip(
                            selected = mode == ReminderMode.INTERVAL,
                            onClick = { mode = ReminderMode.INTERVAL },
                            label = { Text("Every few hours") }
                        )
                    }
                }

                if (mode == ReminderMode.FIXED_TIME) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NumberStepper(value = hour12, range = 1..12, onChange = { hour12 = it }, label = { "%d".format(it) })
                        Text(":", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(horizontal = 8.dp))
                        NumberStepper(value = minute, range = 0..59, step = 5, onChange = { minute = it }, label = { "%02d".format(it) })
                        Spacer(modifier = Modifier.width(12.dp))
                        AmPmToggle(isAm = isAm, onChange = { isAm = it })
                    }
                } else {
                    Text("Remind every:", style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IntervalOptionsMinutes.forEach { option ->
                            val isSelected = intervalMinutes == option
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { intervalMinutes = option }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    formatInterval(option),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                    Text(
                        "You'll get a nudge every ${formatInterval(intervalMinutes)} on the days this habit repeats.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(mode, to24Hour(hour12, isAm), minute, intervalMinutes) }) { Text("Done") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun NumberStepper(
    value: Int,
    range: IntRange,
    step: Int = 1,
    onChange: (Int) -> Unit,
    label: (Int) -> String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = {
            val next = value + step
            onChange(if (next > range.last) range.first else next)
        }) {
            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Increase")
        }
        Text(label(value), style = MaterialTheme.typography.headlineSmall)
        IconButton(onClick = {
            val next = value - step
            onChange(if (next < range.first) range.last else next)
        }) {
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Decrease")
        }
    }
}
