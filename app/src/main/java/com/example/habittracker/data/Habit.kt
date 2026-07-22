package com.example.habittracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class HabitType { SIMPLE, COUNT, TIMER }

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
    val createdAtEpochDay: Long
)
