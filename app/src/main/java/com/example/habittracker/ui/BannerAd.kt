package com.example.habittracker.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * Test banner ad unit ID from Google — safe to ship during development,
 * never serves real ads. Replace with your own banner ad unit ID from the
 * AdMob console before release (Apps -> your app -> Ad units).
 */
private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"

@Composable
fun BannerAd(modifier: Modifier = Modifier, adUnitId: String = TEST_BANNER_AD_UNIT_ID) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
