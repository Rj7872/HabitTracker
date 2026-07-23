package com.example.habittracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class HabitType { SIMPLE, COUNT, TIMER }

enum class ReminderMode { FIXED_TIME, INTERVAL }

/**
 * A habit the user wants to track, e.g. "Drink water" or "Read a book".
 *
 * - SIMPLE: a plain done/not-done checkbox for the day.
 * - COUNT: tap to increment (e.g. "2/4 glasses"), done once targetCount is reached.
 * - TIMER: tap play/pause to accumulate time (e.g. "12:30/30:00"), done once
 *   targetDurationSeconds is reached.
 */
@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String,
    val habitType: HabitType = HabitType.SIMPLE,
    val targetCount: Int = 4,
    val targetDurationSeconds: Int = 1800,
    val createdAtEpochDay: Long,
    /** Days of week this habit repeats on, using java.time.DayOfWeek values (Mon=1..Sun=7), comma-separated. */
    val repeatDaysCsv: String = "1,2,3,4,5,6,7",
    val reminderEnabled: Boolean = false,
    val reminderMode: ReminderMode = ReminderMode.FIXED_TIME,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
    /** Only used when reminderMode == INTERVAL (e.g. every 2 hours for a Counter habit). */
    val reminderIntervalMinutes: Int = 120
) {
    val repeatDays: Set<Int>
        get() = repeatDaysCsv.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
}

fun Set<Int>.toCsv(): String = joinToString(",")
