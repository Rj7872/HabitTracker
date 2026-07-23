package com.example.habittracker.data

import java.time.LocalDate

class HabitRepository(private val dao: HabitDao) {

    fun getAllHabits() = dao.getAllHabits()

    fun getAllProgress() = dao.getAllProgress()

    /** Inserts the habit and returns it with its generated id (needed to schedule reminders). */
    suspend fun addHabit(
        name: String,
        colorHex: String,
        type: HabitType,
        targetCount: Int,
        targetDurationSeconds: Int,
        repeatDays: Set<Int>,
        reminderEnabled: Boolean,
        reminderMode: ReminderMode,
        reminderHour: Int,
        reminderMinute: Int,
        reminderIntervalMinutes: Int
    ): Habit {
        val habit = Habit(
            name = name,
            colorHex = colorHex,
            habitType = type,
            targetCount = targetCount,
            targetDurationSeconds = targetDurationSeconds,
            createdAtEpochDay = LocalDate.now().toEpochDay(),
            repeatDaysCsv = repeatDays.toCsv(),
            reminderEnabled = reminderEnabled,
            reminderMode = reminderMode,
            reminderHour = reminderHour,
            reminderMinute = reminderMinute,
            reminderIntervalMinutes = reminderIntervalMinutes
        )
        val id = dao.insertHabit(habit)
        return habit.copy(id = id)
    }

    suspend fun deleteHabit(habit: Habit) {
        dao.deleteHabit(habit)
    }

    /** SIMPLE habits: flips done for the given day. */
    suspend fun toggleSimple(habitId: Long, epochDay: Long) {
        val existing = dao.getProgress(habitId, epochDay)
        val nowDone = existing?.done?.not() ?: true
        dao.upsertProgress(DailyProgress(habitId, epochDay, value = if (nowDone) 1 else 0, done = nowDone))
    }

    /**
     * COUNT habits: tap to increment by one, wrapping back to 0 once a full
     * cycle past the target is completed (so a mis-tap is easy to undo).
     */
    suspend fun incrementCount(habitId: Long, epochDay: Long, target: Int) {
        val existing = dao.getProgress(habitId, epochDay)
        val current = existing?.value ?: 0
        val next = if (current >= target) 0 else current + 1
        dao.upsertProgress(DailyProgress(habitId, epochDay, value = next, done = next >= target))
    }

    /** TIMER habits: called once per second while a timer is running. */
    suspend fun addTimerSecond(habitId: Long, epochDay: Long, target: Int) {
        val existing = dao.getProgress(habitId, epochDay)
        val next = (existing?.value ?: 0) + 1
        dao.upsertProgress(DailyProgress(habitId, epochDay, value = next, done = next >= target))
    }

    suspend fun getProgressForHabit(habitId: Long): List<DailyProgress> = dao.getProgressForHabit(habitId)

    /** Spends a Streak Freeze: marks the day as protected without requiring actual completion. */
    suspend fun freezeDay(habitId: Long, epochDay: Long) {
        val existing = dao.getProgress(habitId, epochDay)
        if (existing?.done == true) return // already done, no need to freeze
        dao.upsertProgress(DailyProgress(habitId, epochDay, value = existing?.value ?: 0, done = false, frozen = true))
    }

    /**
     * Current streak = consecutive days ending today (or yesterday, if today
     * isn't done yet) that are either completed or protected by a freeze.
     */
    suspend fun currentStreak(habitId: Long): Int {
        val countedDays = dao.getProgressForHabit(habitId).filter { it.done || it.frozen }.map { it.epochDay }.toSet()
        if (countedDays.isEmpty()) return 0

        var cursor = LocalDate.now().toEpochDay()
        if (cursor !in countedDays) cursor -= 1

        var streak = 0
        while (cursor in countedDays) {
            streak++
            cursor -= 1
        }
        return streak
    }

    /** Longest ever run of consecutive completed-or-frozen days. */
    suspend fun bestStreak(habitId: Long): Int {
        val countedDays = dao.getProgressForHabit(habitId).filter { it.done || it.frozen }.map { it.epochDay }.sorted()
        if (countedDays.isEmpty()) return 0

        var best = 1
        var current = 1
        for (i in 1 until countedDays.size) {
            current = if (countedDays[i] == countedDays[i - 1] + 1) current + 1 else 1
            if (current > best) best = current
        }
        return best
    }

    /** Total number of habit-days actually completed across all habits, ever — used for achievement badges. */
    suspend fun totalCompletions(): Int = dao.getAllProgressOnce().count { it.done }
}
