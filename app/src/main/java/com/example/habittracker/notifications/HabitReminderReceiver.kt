package com.example.habittracker.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class HabitReminderReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_HABIT_ID = "habit_id"
        const val EXTRA_HABIT_NAME = "habit_name"
        const val EXTRA_HOUR = "hour"
        const val EXTRA_MINUTE = "minute"
        const val EXTRA_REPEAT_DAYS = "repeat_days"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1L)
        val habitName = intent.getStringExtra(EXTRA_HABIT_NAME) ?: "your habit"
        val hour = intent.getIntExtra(EXTRA_HOUR, 9)
        val minute = intent.getIntExtra(EXTRA_MINUTE, 0)
        val repeatDaysCsv = intent.getStringExtra(EXTRA_REPEAT_DAYS) ?: "1,2,3,4,5,6,7"
        if (habitId == -1L) return

        NotificationHelper.showReminder(context, habitId, habitName)

        // Self-reschedule for the next matching day, since AlarmManager alarms
        // are one-shot here (we need arbitrary weekday selection, which plain
        // repeating alarms can't express).
        val repeatDays = repeatDaysCsv.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        val nextTrigger = ReminderScheduler.nextTriggerMillis(hour, minute, repeatDays)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val nextIntent = Intent(context, HabitReminderReceiver::class.java).apply {
            putExtra(EXTRA_HABIT_ID, habitId)
            putExtra(EXTRA_HABIT_NAME, habitName)
            putExtra(EXTRA_HOUR, hour)
            putExtra(EXTRA_MINUTE, minute)
            putExtra(EXTRA_REPEAT_DAYS, repeatDaysCsv)
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
