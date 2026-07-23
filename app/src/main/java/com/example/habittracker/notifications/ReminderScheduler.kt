package com.example.habittracker.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.habittracker.data.Habit
import com.example.habittracker.data.ReminderMode
import java.time.LocalDateTime
import java.time.ZoneId

object ReminderScheduler {

    private fun pendingIntentFor(context: Context, habit: Habit): PendingIntent {
        val intent = Intent(context, HabitReminderReceiver::class.java).apply {
            putExtra(HabitReminderReceiver.EXTRA_HABIT_ID, habit.id)
            putExtra(HabitReminderReceiver.EXTRA_HABIT_NAME, habit.name)
            putExtra(HabitReminderReceiver.EXTRA_HOUR, habit.reminderHour)
            putExtra(HabitReminderReceiver.EXTRA_MINUTE, habit.reminderMinute)
            putExtra(HabitReminderReceiver.EXTRA_REPEAT_DAYS, habit.repeatDaysCsv)
            putExtra(HabitReminderReceiver.EXTRA_MODE, habit.reminderMode.name)
            putExtra(HabitReminderReceiver.EXTRA_INTERVAL_MINUTES, habit.reminderIntervalMinutes)
        }
        return PendingIntent.getBroadcast(
            context,
            habit.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** Finds the next date/time (today or later) matching one of the given weekdays, at hour:minute. */
    fun nextFixedTriggerMillis(hour: Int, minute: Int, repeatDays: Set<Int>): Long {
        val now = LocalDateTime.now()
        val days = if (repeatDays.isEmpty()) (1..7).toSet() else repeatDays

        for (offset in 0..7) {
            val candidateDate = now.toLocalDate().plusDays(offset.toLong())
            val dow = candidateDate.dayOfWeek.value // Mon=1..Sun=7
            if (dow !in days) continue
            val candidate = candidateDate.atTime(hour, minute)
            if (candidate.isAfter(now)) {
                return candidate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }
        }
        return now.plusDays(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    /** Next occurrence is simply "intervalMinutes from now" — day-of-week filtering happens at fire time. */
    fun nextIntervalTriggerMillis(intervalMinutes: Int): Long {
        val safeInterval = intervalMinutes.coerceAtLeast(15)
        return System.currentTimeMillis() + safeInterval * 60_000L
    }

    fun schedule(context: Context, habit: Habit) {
        if (!habit.reminderEnabled) {
            cancel(context, habit)
            return
        }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerAt = when (habit.reminderMode) {
            ReminderMode.FIXED_TIME -> nextFixedTriggerMillis(habit.reminderHour, habit.reminderMinute, habit.repeatDays)
            ReminderMode.INTERVAL -> nextIntervalTriggerMillis(habit.reminderIntervalMinutes)
        }
        runCatching {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntentFor(context, habit)
            )
        }
    }

    fun cancel(context: Context, habit: Habit) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        alarmManager.cancel(pendingIntentFor(context, habit))
    }
}
