package com.example.habittracker.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Real interstitial ad unit ID for this app. */
private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-2574875254966474/3004363438"
private const val RETRY_DELAY_MS = 15_000L
private const val LOAD_WATCHDOG_MS = 20_000L

/**
 * Full-screen interstitial ads, shown at natural break points (e.g. after
 * closing the Add Habit dialog, or every few habit completions) rather than
 * interrupting an in-progress action.
 */
object InterstitialAdManager {
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private var watchdog: Runnable? = null

    private val _isReady = MutableStateFlow(false)
    val isReadyFlow: StateFlow<Boolean> = _isReady

    fun preload(context: Context) {
        if (interstitialAd != null || isLoading) return
        isLoading = true
        val appContext = context.applicationContext

        watchdog?.let { mainHandler.removeCallbacks(it) }
        val watchdogRunnable = Runnable {
            if (isLoading) {
                Log.w("InterstitialAdManager", "Load timed out with no callback — resetting")
                isLoading = false
            }
        }
        watchdog = watchdogRunnable
        mainHandler.postDelayed(watchdogRunnable, LOAD_WATCHDOG_MS)

        InterstitialAd.load(
            appContext,
            INTERSTITIAL_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    watchdog?.let { mainHandler.removeCallbacks(it) }
                    interstitialAd = ad
                    isLoading = false
                    _isReady.value = true
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    watchdog?.let { mainHandler.removeCallbacks(it) }
                    Log.w("InterstitialAdManager", "Failed to load interstitial: ${error.message}")
                    interstitialAd = null
                    isLoading = false
                    _isReady.value = false
                    mainHandler.postDelayed({ preload(appContext) }, RETRY_DELAY_MS)
                }
            }
        )
    }

    fun isReady(): Boolean = interstitialAd != null

    private const val PREFS_NAME = "habit_tracker_ads"
    private const val KEY_ACTION_COUNT = "interstitial_action_count"
    private const val SHOW_EVERY_N_ACTIONS = 3

    /**
     * Call this after a natural break point (e.g. closing the Add Habit
     * dialog). Only actually shows an ad every [SHOW_EVERY_N_ACTIONS] calls,
     * so it doesn't interrupt someone adding several habits back to back.
     */
    fun maybeShowAfterAction(activity: Activity) {
        val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val count = prefs.getInt(KEY_ACTION_COUNT, 0) + 1
        prefs.edit().putInt(KEY_ACTION_COUNT, count).apply()
        if (count % SHOW_EVERY_N_ACTIONS == 0) {
            showIfReady(activity)
        }
    }

    /** Shows the ad if one is loaded; silently does nothing otherwise (never blocks the user). */
    fun showIfReady(activity: Activity, onDismissed: () -> Unit = {}) {
        val ad = interstitialAd
        if (ad == null) {
            preload(activity)
            onDismissed()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                _isReady.value = false
                preload(activity)
                onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
                _isReady.value = false
                preload(activity)
                onDismissed()
            }
        }

        ad.show(activity)
    }
}
