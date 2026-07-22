package com.example.habittracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits ORDER BY createdAtEpochDay ASC")
    fun getAllHabits(): Flow<List<Habit>>

    @Insert
    suspend fun insertHabit(habit: Habit): Long

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("SELECT * FROM completions")
    fun getAllCompletions(): Flow<List<CompletionRecord>>

    @Query("SELECT EXISTS(SELECT 1 FROM completions WHERE habitId = :habitId AND epochDay = :epochDay)")
    suspend fun isCompleted(habitId: Long, epochDay: Long): Boolean

    @Insert
    suspend fun insertCompletion(record: CompletionRecord)

    @Query("DELETE FROM completions WHERE habitId = :habitId AND epochDay = :epochDay")
    suspend fun deleteCompletion(habitId: Long, epochDay: Long)

    @Query("SELECT epochDay FROM completions WHERE habitId = :habitId ORDER BY epochDay DESC")
    suspend fun getCompletedDaysForHabit(habitId: Long): List<Long>
}
