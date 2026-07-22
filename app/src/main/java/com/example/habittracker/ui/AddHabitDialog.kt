package com.example.habittracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.habittracker.data.HabitType

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitDialog(onDismiss: () -> Unit, onConfirm: (name: String, colorHex: String, type: HabitType, targetCount: Int, targetMinutes: Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(HabitColorPalette.first()) }
    var selectedType by remember { mutableStateOf(HabitType.SIMPLE) }
    var targetCountText by remember { mutableStateOf("4") }
    var targetMinutesText by remember { mutableStateOf("30") }

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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HabitColorPalette.forEach { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        val isSelected = hex == selectedColor
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color.Black.copy(alpha = 0.15f) else Color.Transparent)
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColor = hex }
                        )
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
                        targetMinutesText.toIntOrNull() ?: 30
                    )
                },
                enabled = name.isNotBlank()
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
