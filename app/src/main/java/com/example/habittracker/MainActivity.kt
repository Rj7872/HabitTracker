package com.example.habittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.habittracker.data.HabitDatabase
import com.example.habittracker.data.HabitRepository
import com.example.habittracker.ui.RootScreen
import com.example.habittracker.ui.HabitViewModel
import com.example.habittracker.ui.HabitViewModelFactory
import com.example.habittracker.ui.isDynamicColorEnabled
import com.example.habittracker.ui.theme.HabitTrackerTheme

class MainActivity : ComponentActivity() {

    private val viewModel: HabitViewModel by viewModels {
        val dao = HabitDatabase.getInstance(applicationContext).habitDao()
        HabitViewModelFactory(HabitRepository(dao))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var dynamicColor by androidx.compose.runtime.remember {
                mutableStateOf(isDynamicColorEnabled(applicationContext))
            }
            HabitTrackerTheme(dynamicColor = dynamicColor) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RootScreen(viewModel = viewModel, onDynamicColorChanged = { dynamicColor = it })
                }
            }
        }
    }
}
