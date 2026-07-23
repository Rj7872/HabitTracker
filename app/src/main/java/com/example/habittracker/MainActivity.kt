package com.example.habittracker

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.habittracker.data.HabitDatabase
import com.example.habittracker.data.HabitRepository
import com.example.habittracker.ui.RootScreen
import com.example.habittracker.ui.HabitViewModel
import com.example.habittracker.ui.HabitViewModelFactory
import com.example.habittracker.ui.OnboardingScreen
import com.example.habittracker.ui.isDynamicColorEnabled
import com.example.habittracker.ui.isOnboardingComplete
import com.example.habittracker.ui.setOnboardingComplete
import com.example.habittracker.ui.theme.HabitTrackerTheme

class MainActivity : ComponentActivity() {

    private val viewModel: HabitViewModel by viewModels {
        val dao = HabitDatabase.getInstance(applicationContext).habitDao()
        HabitViewModelFactory(application, HabitRepository(dao))
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op either way */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            var dynamicColor by remember {
                mutableStateOf(isDynamicColorEnabled(applicationContext))
            }
            HabitTrackerTheme(dynamicColor = dynamicColor) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var showOnboarding by remember { mutableStateOf(!isOnboardingComplete(applicationContext)) }
                    if (showOnboarding) {
                        OnboardingScreen(onContinue = {
                            setOnboardingComplete(applicationContext)
                            showOnboarding = false
                        })
                    } else {
                        RootScreen(viewModel = viewModel, onDynamicColorChanged = { dynamicColor = it })
                    }
                }
            }
        }
    }
}
