package com.example.habittracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * A habit the user wants to track, e.g. "Drink water" or "Read 10 pages".
 */
@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String = "#6750A4",
    val createdAtEpochDay: Long
)

/**
 * A single completion of a habit on a specific day.
 * One row per (habitId, epochDay) — existence of a row means "done that day".
 */
@Entity(
    tableName = "completions",
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
data class CompletionRecord(
    val habitId: Long,
    val epochDay: Long
)
