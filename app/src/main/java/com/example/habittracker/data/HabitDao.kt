package com.example.habittracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
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

    @Query("SELECT * FROM daily_progress")
    fun getAllProgress(): Flow<List<DailyProgress>>

    @Query("SELECT * FROM daily_progress WHERE habitId = :habitId AND epochDay = :epochDay LIMIT 1")
    suspend fun getProgress(habitId: Long, epochDay: Long): DailyProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: DailyProgress)

    @Query("SELECT * FROM daily_progress WHERE habitId = :habitId")
    suspend fun getProgressForHabit(habitId: Long): List<DailyProgress>

    @Query("SELECT * FROM daily_progress")
    suspend fun getAllProgressOnce(): List<DailyProgress>
}
