package com.example.habittracker

import android.app.Application
import com.example.habittracker.ads.InterstitialAdManager
import com.example.habittracker.ads.RewardedAdManager
import com.google.android.gms.ads.MobileAds

class HabitTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this) {
            RewardedAdManager.preload(this)
            InterstitialAdManager.preload(this)
        }
    }
}
