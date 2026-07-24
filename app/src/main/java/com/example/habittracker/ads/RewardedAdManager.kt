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
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Real rewarded ad unit ID for this app. */
private const val REWARDED_AD_UNIT_ID = "ca-app-pub-2574875254966474/5367782831"
private const val RETRY_DELAY_MS = 15_000L
private const val LOAD_WATCHDOG_MS = 20_000L

object RewardedAdManager {
    private var rewardedAd: RewardedAd? = null
    private var isLoading = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private var watchdog: Runnable? = null

    // Lets screens show a live "loading..." vs "ready" state instead of a
    // dead-end failure when the user taps before the ad has finished loading.
    private val _isReady = MutableStateFlow(false)
    val isReadyFlow: StateFlow<Boolean> = _isReady

    // Surfaced on-screen (not just logcat) so a failure reason is visible
    // even when debugging from a phone with no computer attached.
    private val _lastError = MutableStateFlow<String?>(null)
    val lastErrorFlow: StateFlow<String?> = _lastError

    fun preload(context: Context) {
        if (rewardedAd != null || isLoading) return
        isLoading = true
        _lastError.value = null
        val appContext = context.applicationContext

        // Safety net: if neither onAdLoaded nor onAdFailedToLoad ever fires
        // (rare, but possible on a flaky connection), don't let isLoading
        // stay stuck true forever — that would silently block all future
        // attempts, even ones triggered by reopening the screen.
        watchdog?.let { mainHandler.removeCallbacks(it) }
        val watchdogRunnable = Runnable {
            if (isLoading) {
                Log.w("RewardedAdManager", "Load timed out with no callback — resetting")
                isLoading = false
                _lastError.value = "Load timed out (no response from ad server)"
            }
        }
        watchdog = watchdogRunnable
        mainHandler.postDelayed(watchdogRunnable, LOAD_WATCHDOG_MS)

        RewardedAd.load(
            appContext,
            REWARDED_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    watchdog?.let { mainHandler.removeCallbacks(it) }
                    rewardedAd = ad
                    isLoading = false
                    _isReady.value = true
                    _lastError.value = null
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    watchdog?.let { mainHandler.removeCallbacks(it) }
                    Log.w("RewardedAdManager", "Failed to load rewarded ad: ${error.message}")
                    rewardedAd = null
                    isLoading = false
                    _isReady.value = false
                    _lastError.value = "${error.code}: ${error.message}"
                    // Auto-retry in the background so the ad is likely ready
                    // by the time the user taps again, without them needing
                    // to do anything.
                    mainHandler.postDelayed({ preload(appContext) }, RETRY_DELAY_MS)
                }
            }
        )
    }

    fun isReady(): Boolean = rewardedAd != null

    /**
     * Shows the loaded rewarded ad if available. [onReward] fires only if the
     * user watched enough of the ad to earn the reward. Either way, a new ad
     * is preloaded afterward for next time.
     */
    fun show(activity: Activity, onReward: () -> Unit, onUnavailable: () -> Unit) {
        val ad = rewardedAd
        if (ad == null) {
            onUnavailable()
            preload(activity)
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                _isReady.value = false
                preload(activity)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                rewardedAd = null
                _isReady.value = false
                _lastError.value = "Failed to show: ${adError.message}"
                preload(activity)
            }
        }

        ad.show(activity) { onReward() }
    }
}
