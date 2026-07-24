package com.example.habittracker.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.habittracker.ads.RewardedAdManager
import com.example.habittracker.premium.AllBadges
import com.example.habittracker.premium.Badge
import com.example.habittracker.premium.BadgeManager
import com.example.habittracker.premium.BadgeMetric

@Composable
fun AchievementsScreen(viewModel: HabitViewModel) {
    val habits by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var bestStreakEver by remember { mutableStateOf(0) }
    var totalCompletions by remember { mutableStateOf(0) }
    var claimedIds by remember { mutableStateOf(BadgeManager.getClaimedBadgeIds(context)) }
    val adReady by RewardedAdManager.isReadyFlow.collectAsState()

    LaunchedEffect(Unit) { RewardedAdManager.preload(context) }

    LaunchedEffect(habits) {
        bestStreakEver = habits.maxOfOrNull { viewModel.bestStreakBlocking(it.habit.id) } ?: 0
        totalCompletions = viewModel.totalCompletionsBlocking()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Achievements", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Unlock badges as you build streaks — watch a quick ad to claim each one.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(AllBadges) { badge ->
                val progress = when (badge.metric) {
                    BadgeMetric.BEST_STREAK -> bestStreakEver
                    BadgeMetric.TOTAL_COMPLETIONS -> totalCompletions
                    BadgeMetric.HABIT_COUNT -> habits.size
                }
                val isEligible = progress >= badge.threshold
                val isClaimed = badge.id in claimedIds

                BadgeCard(
                    badge = badge,
                    progress = progress,
                    isEligible = isEligible,
                    isClaimed = isClaimed,
                    adReady = adReady,
                    onClaim = {
                        val activity = context as? Activity ?: return@BadgeCard
                        RewardedAdManager.show(
                            activity,
                            onReward = {
                                BadgeManager.claim(context, badge.id)
                                claimedIds = BadgeManager.getClaimedBadgeIds(context)
                                Toast.makeText(context, "${badge.title} unlocked!", Toast.LENGTH_SHORT).show()
                            },
                            onUnavailable = {
                                Toast.makeText(context, "Ad not ready yet — try again in a few seconds.", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun BadgeCard(
    badge: Badge,
    progress: Int,
    isEligible: Boolean,
    isClaimed: Boolean,
    adReady: Boolean,
    onClaim: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isClaimed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (isClaimed) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isClaimed) {
                    Text(badge.emoji, style = MaterialTheme.typography.headlineSmall)
                } else if (isEligible) {
                    Icon(Icons.Filled.PlayCircle, contentDescription = "Ready to claim", tint = MaterialTheme.colorScheme.primary)
                } else {
                    Icon(Icons.Filled.Lock, contentDescription = "Locked", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                badge.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = if (isClaimed) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                badge.description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            when {
                isClaimed -> Text(
                    "Unlocked",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                isEligible -> TextButton(onClick = onClaim, enabled = adReady) {
                    Text(if (adReady) "Watch ad to claim" else "Loading ad\u2026")
                }
                else -> Text(
                    "$progress / ${badge.threshold}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
