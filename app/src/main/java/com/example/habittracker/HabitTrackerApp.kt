package com.example.habittracker

import android.app.Application
import com.google.android.gms.ads.MobileAds

class HabitTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
    }
}
