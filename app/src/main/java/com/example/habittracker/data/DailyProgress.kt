package com.example.habittracker.data

import androidx.room.Entity
import androidx.room.ForeignKey

/**
 * One habit's progress on one day.
 * - For SIMPLE habits: `value` is unused, `done` is toggled directly.
 * - For COUNT habits: `value` is the tally (e.g. glasses drunk), `done` is
 *   true once value >= habit.targetCount.
 * - For TIMER habits: `value` is elapsed seconds, `done` is true once
 *   value >= habit.targetDurationSeconds.
 */
@Entity(
    tableName = "daily_progress",
    primaryKeys = ["habitId", "epochDay"],
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DailyProgress(
    val habitId: Long,
    val epochDay: Long,
    val value: Int = 0,
    val done: Boolean = false
)
