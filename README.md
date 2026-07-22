# Habit Tracker

A simple Android habit tracker built with Kotlin + Jetpack Compose + Room.

## Features
- Add / delete habits
- Tap a habit to mark it done for today
- Automatic streak calculation (consecutive days completed)
- Data persists locally on-device via Room (SQLite)
- Material 3 UI with dynamic color on Android 12+

## How to open this project
1. Open **Android Studio** (Koala or newer recommended).
2. Choose **File > Open**, and select this `HabitTracker` folder.
3. Android Studio will generate the Gradle wrapper automatically on first sync
   (it detects there's no `gradlew`/`gradle-wrapper.jar` and offers to create one —
   click "OK" / "Sync Now" when prompted). Alternatively, run
   `gradle wrapper` from a terminal in this folder if you have Gradle installed.
4. Let Gradle sync (it will download dependencies — needs internet access).
5. Click **Run ▶** to install on an emulator or connected device (minSdk 26 / Android 8.0+).

## Project structure
```
app/src/main/java/com/example/habittracker/
├── MainActivity.kt              # Entry point, wires ViewModel + Compose UI
├── data/
│   ├── Habit.kt                 # Room entities: Habit, CompletionRecord
│   ├── HabitDao.kt               # Database queries
│   ├── HabitDatabase.kt          # Room database setup
│   └── HabitRepository.kt        # Business logic (streaks, toggling completion)
├── ui/
│   ├── HabitViewModel.kt          # UI state, combines habits + completions
│   ├── HabitViewModelFactory.kt
│   ├── HabitListScreen.kt         # Main screen: list, add dialog, delete dialog
│   └── theme/Theme.kt             # Material 3 theme
```

## AdMob ads
A banner ad now sits at the bottom of the screen, using **Google's test
IDs** — safe to build and run right now with zero setup, but they only ever
show placeholder "Test Ad" banners and never earn revenue.

Before publishing, swap in your own IDs from https://apps.admob.com:
1. **App ID** — in `AndroidManifest.xml`, replace the value of the
   `com.google.android.gms.ads.APPLICATION_ID` meta-data tag.
2. **Banner ad unit ID** — in `ui/BannerAd.kt`, replace
   `TEST_BANNER_AD_UNIT_ID` with your real banner ad unit ID.

Don't swap in real IDs until you're ready to test with real ad traffic —
Google can flag accounts for invalid traffic if test devices repeatedly
request real ads. While developing, add your device as a test device via
`RequestConfiguration` if you want to preview real ad units safely.

Want interstitial (full-screen) or rewarded ads too? Those need a slightly
different pattern (load ahead of time, then show at a natural break point
like after marking a few habits done) — happy to add either.

## Extending it
- **Reminders**: add `AlarmManager` or `WorkManager` scheduled notifications per habit.
- **Weekly/custom frequency**: add a `frequency` field to `Habit` and adjust the
  streak logic in `HabitRepository.currentStreak`.
- **Charts/history view**: `HabitDao.getCompletedDaysForHabit` already returns
  all completed days per habit — feed that into a calendar heatmap Composable.
- **Custom colors per habit**: `colorHex` is already stored on `Habit`; wire it
  into `HabitRow`'s icon tint in `HabitListScreen.kt`.
