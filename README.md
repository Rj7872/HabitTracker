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
This app uses **real AdMob IDs** (App ID, and ad unit IDs for banner,
interstitial, and rewarded ads) — not Google's placeholder test IDs.

- **Banner** — bottom of the Home screen (`ui/BannerAd.kt`)
- **Rewarded** — "Watch ad for +1 Freeze" in Settings, and badge claims in
  Achievements (`ads/RewardedAdManager.kt`)
- **Interstitial** — shown after every 3rd time you add a new habit, so it
  doesn't interrupt someone setting up several habits in a row
  (`ads/InterstitialAdManager.kt`)

⚠️ **Important while developing**: tapping/watching real ads repeatedly
from the same device without genuine ad interest counts as **invalid
traffic** in Google's eyes, and can get your AdMob account flagged or
suspended. Register your device as a test device (via
`RequestConfiguration.setTestDeviceIds(...)` in `HabitTrackerApp.kt`) if
you need to keep testing after this point — that shows Google-labeled test
ads on your real ad units without counting against you, and is much safer
than clicking your own live ads repeatedly.

Also worth setting up before you publish: **app-ads.txt** on the website
tied to your Play Store developer account, listing your AdMob publisher ID
as an authorized seller — AdMob will show a warning banner until this is
in place.

## Extending it
- **Reminders**: add `AlarmManager` or `WorkManager` scheduled notifications per habit.
- **Weekly/custom frequency**: add a `frequency` field to `Habit` and adjust the
  streak logic in `HabitRepository.currentStreak`.
- **Charts/history view**: `HabitDao.getCompletedDaysForHabit` already returns
  all completed days per habit — feed that into a calendar heatmap Composable.
- **Custom colors per habit**: `colorHex` is already stored on `Habit`; wire it
  into `HabitRow`'s icon tint in `HabitListScreen.kt`.
