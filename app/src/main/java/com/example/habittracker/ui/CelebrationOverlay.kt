package com.example.habittracker.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

private val starColors = listOf(
    Color(0xFFFFD54F), Color(0xFFFF8A65), Color(0xFF4FC3F7), Color(0xFF81C784), Color(0xFFBA68C8)
)

/**
 * Brief full-screen celebration: a burst of scattered stars plus a centered
 * "Nice work!" badge that scales in, holds, then fades out on its own.
 * Doesn't intercept touches — purely decorative and self-dismissing.
 */
@Composable
fun CelebrationOverlay(onFinished: () -> Unit) {
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }
    val starOffsets = remember {
        List(12) { Triple(Random.nextFloat() * 2 - 1, Random.nextFloat() * 2 - 1, starColors.random()) }
    }

    LaunchedEffect(Unit) {
        scale.animateTo(1f, tween(280, easing = FastOutSlowInEasing))
        delay(650)
        alpha.animateTo(0f, tween(350))
        onFinished()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        starOffsets.forEach { (dx, dy, color) ->
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = color,
                modifier = Modifier
                    .offset(x = (dx * 130).dp, y = (dy * 220).dp)
                    .scale(scale.value)
                    .alpha(alpha.value)
                    .size(22.dp)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            Text("\uD83C\uDF89", style = MaterialTheme.typography.displayMedium)
            Text(
                "Nice work!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
