package com.example.habittracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.Habit
import com.example.habittracker.data.HabitRepository
import com.example.habittracker.data.HabitType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HabitUiState(
    val habit: Habit,
    val valueForSelectedDay: Int,
    val doneOnSelectedDay: Boolean,
    val streak: Int,
    val isTimerRunning: Boolean
)

class HabitViewModel(private val repository: HabitRepository) : ViewModel() {

    private val selectedEpochDay = MutableStateFlow(LocalDate.now().toEpochDay())
    val selectedEpochDayFlow: StateFlow<Long> = selectedEpochDay

    private val runningHabitId = MutableStateFlow<Long?>(null)
    private var timerJob: Job? = null

    // Streaks depend on all historical progress, not just the selected day,
    // so they're recomputed whenever the habit list or progress changes —
    // kept as a simple in-memory map refreshed alongside uiState.
    private val streaks = MutableStateFlow<Map<Long, Int>>(emptyMap())

    val uiState: StateFlow<List<HabitUiState>> =
        combine(
            repository.getAllHabits(),
            repository.getAllProgress(),
            selectedEpochDay,
            runningHabitId,
            streaks
        ) { habits, progress, day, running, streakMap ->
            habits.map { habit ->
                val todayProgress = progress.find { it.habitId == habit.id && it.epochDay == day }
                HabitUiState(
                    habit = habit,
                    valueForSelectedDay = todayProgress?.value ?: 0,
                    doneOnSelectedDay = todayProgress?.done ?: false,
                    streak = streakMap[habit.id] ?: 0,
                    isTimerRunning = running == habit.id
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Recompute streaks whenever the habit list changes size (added/removed).
        viewModelScope.launch {
            repository.getAllHabits().collect { habits ->
                val map = habits.associate { it.id to repository.currentStreak(it.id) }
                streaks.value = map
            }
        }
    }

    fun selectDate(date: LocalDate) {
        selectedEpochDay.value = date.toEpochDay()
    }

    fun selectToday() {
        selectedEpochDay.value = LocalDate.now().toEpochDay()
    }

    fun addHabit(name: String, colorHex: String, type: HabitType, targetCount: Int, targetDurationMinutes: Int) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addHabit(name.trim(), colorHex, type, targetCount, targetDurationMinutes * 60)
            refreshStreaks()
        }
    }

    fun deleteHabit(habit: Habit) {
        if (runningHabitId.value == habit.id) stopTimer()
        viewModelScope.launch {
            repository.deleteHabit(habit)
            refreshStreaks()
        }
    }

    fun toggleSimple(habit: Habit) {
        viewModelScope.launch {
            repository.toggleSimple(habit.id, selectedEpochDay.value)
            refreshStreaks()
        }
    }

    fun incrementCount(habit: Habit) {
        viewModelScope.launch {
            repository.incrementCount(habit.id, selectedEpochDay.value, habit.targetCount)
            refreshStreaks()
        }
    }

    /** Starts the timer if not running for this habit, otherwise pauses it. */
    fun toggleTimer(habit: Habit) {
        if (runningHabitId.value == habit.id) {
            stopTimer()
            return
        }
        timerJob?.cancel()
        runningHabitId.value = habit.id
        val day = selectedEpochDay.value
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                repository.addTimerSecond(habit.id, day, habit.targetDurationSeconds)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        runningHabitId.value = null
        viewModelScope.launch { refreshStreaks() }
    }

    suspend fun progressForHabitBlocking(habitId: Long) = repository.getProgressForHabit(habitId)

    private suspend fun refreshStreaks() {
        val current = uiState.value
        if (current.isEmpty()) return
        streaks.value = current.associate { it.habit.id to repository.currentStreak(it.habit.id) }
    }
}
