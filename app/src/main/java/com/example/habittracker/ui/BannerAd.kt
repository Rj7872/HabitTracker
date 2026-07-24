package com.example.habittracker.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * Test banner ad unit ID from Google — safe to ship during development,
 * never serves real ads. Replace with your own banner ad unit ID from the
 * AdMob console before release (Apps -> your app -> Ad units).
 */
private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"

@Composable
fun BannerAd(modifier: Modifier = Modifier, adUnitId: String = TEST_BANNER_AD_UNIT_ID) {
    // Surfaced on-screen (not just logcat) so a failure reason is visible
    // even when debugging from a phone with no computer attached.
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                adListener = object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        errorMessage = "Ad failed: ${error.code} ${error.message}"
                    }

                    override fun onAdLoaded() {
                        errorMessage = null
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        }
    )

    errorMessage?.let {
        Text(
            it,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(4.dp)
        )
    }
}
