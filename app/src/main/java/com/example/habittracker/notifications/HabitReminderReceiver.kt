package com.example.habittracker.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.habittracker.data.ReminderMode
import java.time.LocalDate

class HabitReminderReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_HABIT_ID = "habit_id"
        const val EXTRA_HABIT_NAME = "habit_name"
        const val EXTRA_HOUR = "hour"
        const val EXTRA_MINUTE = "minute"
        const val EXTRA_REPEAT_DAYS = "repeat_days"
        const val EXTRA_MODE = "mode"
        const val EXTRA_INTERVAL_MINUTES = "interval_minutes"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1L)
        val habitName = intent.getStringExtra(EXTRA_HABIT_NAME) ?: "your habit"
        val hour = intent.getIntExtra(EXTRA_HOUR, 9)
        val minute = intent.getIntExtra(EXTRA_MINUTE, 0)
        val repeatDaysCsv = intent.getStringExtra(EXTRA_REPEAT_DAYS) ?: "1,2,3,4,5,6,7"
        val mode = runCatching { ReminderMode.valueOf(intent.getStringExtra(EXTRA_MODE) ?: "FIXED_TIME") }
            .getOrDefault(ReminderMode.FIXED_TIME)
        val intervalMinutes = intent.getIntExtra(EXTRA_INTERVAL_MINUTES, 120)
        if (habitId == -1L) return

        val repeatDays = repeatDaysCsv.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        val todayDow = LocalDate.now().dayOfWeek.value
        val isScheduledToday = repeatDays.isEmpty() || todayDow in repeatDays

        // Interval reminders keep firing every N minutes regardless of day,
        // but we only show the notification on days the habit is scheduled.
        if (isScheduledToday) {
            NotificationHelper.showReminder(context, habitId, habitName)
        }

        // Self-reschedule, since AlarmManager alarms here are one-shot (needed
        // for arbitrary weekday selection and interval mode alike).
        val nextTrigger = when (mode) {
            ReminderMode.FIXED_TIME -> ReminderScheduler.nextFixedTriggerMillis(hour, minute, repeatDays)
            ReminderMode.INTERVAL -> ReminderScheduler.nextIntervalTriggerMillis(intervalMinutes)
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val nextIntent = Intent(context, HabitReminderReceiver::class.java).apply {
            putExtra(EXTRA_HABIT_ID, habitId)
            putExtra(EXTRA_HABIT_NAME, habitName)
            putExtra(EXTRA_HOUR, hour)
            putExtra(EXTRA_MINUTE, minute)
            putExtra(EXTRA_REPEAT_DAYS, repeatDaysCsv)
            putExtra(EXTRA_MODE, mode.name)
            putExtra(EXTRA_INTERVAL_MINUTES, intervalMinutes)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habitId.toInt(),
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        runCatching {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTrigger, pendingIntent)
        }
    }
}
