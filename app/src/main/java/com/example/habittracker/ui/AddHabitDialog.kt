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
    "#F783AC"  // pink
)

// Monday=1 .. Sunday=7, matching java.time.DayOfWeek.value
private val AllDays = (1..7).toList()

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
        reminderHour: Int,
        reminderMinute: Int
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(HabitColorPalette.first()) }
    var selectedType by remember { mutableStateOf(HabitType.SIMPLE) }
    var targetCountText by remember { mutableStateOf("4") }
    var targetMinutesText by remember { mutableStateOf("30") }
    var repeatDays by remember { mutableStateOf(AllDays.toSet()) }
    var reminderEnabled by remember { mutableStateOf(false) }
    var reminderHour by remember { mutableStateOf(9) }
    var reminderMinute by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New habit") },
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
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HabitColorPalette.forEach { hex ->
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
                    Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                }

                if (reminderEnabled) {
                    TimeStepper(
                        hour = reminderHour,
                        minute = reminderMinute,
                        onHourChange = { reminderHour = it },
                        onMinuteChange = { reminderMinute = it }
                    )
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
                        reminderHour,
                        reminderMinute
                    )
                },
                enabled = name.isNotBlank() && repeatDays.isNotEmpty()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
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
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onToggleDay(day) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TimeStepper(hour: Int, minute: Int, onHourChange: (Int) -> Unit, onMinuteChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NumberStepper(value = hour, range = 0..23, onChange = onHourChange, label = { "%02d".format(it) })
        Text(":", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(horizontal = 8.dp))
        NumberStepper(value = minute, range = 0..59, step = 5, onChange = onMinuteChange, label = { "%02d".format(it) })
    }
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
