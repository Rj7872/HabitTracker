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

/**
 * Test rewarded ad unit ID from Google — safe to ship during development,
 * never serves real ads. Replace with your own rewarded ad unit ID from the
 * AdMob console before release (Apps -> your app -> Ad units).
 */
private const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
private const val RETRY_DELAY_MS = 15_000L

object RewardedAdManager {
    private var rewardedAd: RewardedAd? = null
    private var isLoading = false
    private val mainHandler = Handler(Looper.getMainLooper())

    // Lets screens show a live "loading..." vs "ready" state instead of a
    // dead-end failure when the user taps before the ad has finished loading.
    private val _isReady = MutableStateFlow(false)
    val isReadyFlow: StateFlow<Boolean> = _isReady

    fun preload(context: Context) {
        if (rewardedAd != null || isLoading) return
        isLoading = true
        val appContext = context.applicationContext
        RewardedAd.load(
            appContext,
            TEST_REWARDED_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading = false
                    _isReady.value = true
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w("RewardedAdManager", "Failed to load rewarded ad: ${error.message}")
                    rewardedAd = null
                    isLoading = false
                    _isReady.value = false
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
                preload(activity)
            }
        }

        ad.show(activity) { onReward() }
    }
}
