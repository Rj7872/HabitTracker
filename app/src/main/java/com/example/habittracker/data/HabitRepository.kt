package com.example.habittracker.data

import java.time.LocalDate

class HabitRepository(private val dao: HabitDao) {

    fun getAllHabits() = dao.getAllHabits()

    fun getAllCompletions() = dao.getAllCompletions()

    suspend fun addHabit(name: String, colorHex: String) {
        dao.insertHabit(
            Habit(
                name = name,
                colorHex = colorHex,
                createdAtEpochDay = LocalDate.now().toEpochDay()
            )
        )
    }

    suspend fun deleteHabit(habit: Habit) {
        dao.deleteHabit(habit)
    }

    /** Toggles today's completion state for a habit. */
    suspend fun toggleToday(habitId: Long) {
        val today = LocalDate.now().toEpochDay()
        if (dao.isCompleted(habitId, today)) {
            dao.deleteCompletion(habitId, today)
        } else {
            dao.insertCompletion(CompletionRecord(habitId, today))
        }
    }

    /**
     * Current streak = consecutive days ending today (or yesterday, if today
     * isn't marked done yet) with a completion record.
     */
    suspend fun currentStreak(habitId: Long): Int {
        val doneDays = dao.getCompletedDaysForHabit(habitId).toSet()
        if (doneDays.isEmpty()) return 0

        var cursor = LocalDate.now().toEpochDay()
        // If today isn't done yet, start counting from yesterday instead,
        // so the streak doesn't reset to 0 the moment the clock rolls over.
        if (cursor !in doneDays) cursor -= 1

        var streak = 0
        while (cursor in doneDays) {
            streak++
            cursor -= 1
        }
        return streak
    }
}
