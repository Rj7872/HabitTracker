package com.example.habittracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.Habit
import com.example.habittracker.data.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HabitUiState(
    val habit: Habit,
    val doneToday: Boolean,
    val streak: Int
)

class HabitViewModel(private val repository: HabitRepository) : ViewModel() {

    private val today = LocalDate.now().toEpochDay()

    // Recomputed streaks whenever completions change; kept simple by
    // re-deriving from the raw completion list rather than caching per habit.
    val uiState: StateFlow<List<HabitUiState>> =
        combine(repository.getAllHabits(), repository.getAllCompletions()) { habits, completions ->
            habits.map { habit ->
                val habitDays = completions.filter { it.habitId == habit.id }.map { it.epochDay }.toSet()
                val doneToday = today in habitDays
                var cursor = if (doneToday) today else today - 1
                var streak = 0
                while (cursor in habitDays) {
                    streak++
                    cursor--
                }
                HabitUiState(habit, doneToday, streak)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addHabit(name: String, colorHex: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repository.addHabit(name.trim(), colorHex) }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch { repository.deleteHabit(habit) }
    }

    fun toggleToday(habitId: Long) {
        viewModelScope.launch { repository.toggleToday(habitId) }
    }
}
