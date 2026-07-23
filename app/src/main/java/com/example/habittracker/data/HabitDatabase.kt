package com.example.habittracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun fromHabitType(type: HabitType): String = type.name

    @TypeConverter
    fun toHabitType(value: String): HabitType = HabitType.valueOf(value)

    @TypeConverter
    fun fromReminderMode(mode: ReminderMode): String = mode.name

    @TypeConverter
    fun toReminderMode(value: String): ReminderMode = ReminderMode.valueOf(value)
}

@Database(
    entities = [Habit::class, DailyProgress::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HabitDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: HabitDatabase? = null

        fun getInstance(context: Context): HabitDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HabitDatabase::class.java,
                    "habit_tracker.db"
                )
                    // Schema changed (habit types added) — safe to wipe old
                    // local data rather than write a migration, since this
                    // app has no server backup to restore from anyway.
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
