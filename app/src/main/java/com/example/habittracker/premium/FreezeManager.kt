package com.example.habittracker.premium

import android.content.Context

private const val PREFS_NAME = "habit_tracker_premium"
private const val KEY_FREEZE_COUNT = "freeze_count"

/**
 * Streak Freezes are a consumable earned by watching a rewarded ad. Spending
 * one protects a habit's streak on a day it wasn't actually completed.
 */
object FreezeManager {

    fun getFreezeCount(context: Context): Int =
        prefs(context).getInt(KEY_FREEZE_COUNT, 0)

    fun addFreeze(context: Context) {
        val current = getFreezeCount(context)
        prefs(context).edit().putInt(KEY_FREEZE_COUNT, current + 1).apply()
    }

    /** Returns true if a freeze was available and has now been spent. */
    fun useFreeze(context: Context): Boolean {
        val current = getFreezeCount(context)
        if (current <= 0) return false
        prefs(context).edit().putInt(KEY_FREEZE_COUNT, current - 1).apply()
        return true
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
