package com.example.habittracker.premium

import android.content.Context

enum class BadgeMetric { BEST_STREAK, TOTAL_COMPLETIONS }

data class Badge(
    val id: String,
    val emoji: String,
    val title: String,
    val description: String,
    val metric: BadgeMetric,
    val threshold: Int
)

val AllBadges = listOf(
    Badge("streak_3", "\uD83C\uDF31", "Sprout", "Reach a 3-day streak on any habit", BadgeMetric.BEST_STREAK, 3),
    Badge("streak_7", "\uD83D\uDD25", "On Fire", "Reach a 7-day streak on any habit", BadgeMetric.BEST_STREAK, 7),
    Badge("streak_14", "\u26A1", "Unstoppable", "Reach a 14-day streak on any habit", BadgeMetric.BEST_STREAK, 14),
    Badge("streak_30", "\uD83C\uDF1F", "Monthly Master", "Reach a 30-day streak on any habit", BadgeMetric.BEST_STREAK, 30),
    Badge("streak_100", "\uD83D\uDC51", "Centurion", "Reach a 100-day streak on any habit", BadgeMetric.BEST_STREAK, 100),
    Badge("total_10", "\u2705", "Getting Started", "Complete habits 10 times total", BadgeMetric.TOTAL_COMPLETIONS, 10),
    Badge("total_50", "\uD83C\uDFC5", "Dedicated", "Complete habits 50 times total", BadgeMetric.TOTAL_COMPLETIONS, 50),
    Badge("total_200", "\uD83C\uDFC6", "Habit Legend", "Complete habits 200 times total", BadgeMetric.TOTAL_COMPLETIONS, 200)
)

private const val PREFS_NAME = "habit_tracker_premium"
private const val KEY_CLAIMED_BADGES = "claimed_badges"

/**
 * A badge becomes *eligible* once the user's progress crosses its threshold,
 * but stays locked in the gallery until "claimed" by watching a rewarded ad —
 * that's the premium hook. Claims persist locally via SharedPreferences.
 */
object BadgeManager {

    fun getClaimedBadgeIds(context: Context): Set<String> =
        prefs(context).getString(KEY_CLAIMED_BADGES, "")
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?.toSet()
            ?: emptySet()

    fun claim(context: Context, badgeId: String) {
        val current = getClaimedBadgeIds(context)
        val updated = current + badgeId
        prefs(context).edit().putString(KEY_CLAIMED_BADGES, updated.joinToString(",")).apply()
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
