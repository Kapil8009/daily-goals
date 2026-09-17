# Daily Goals

Daily Goals Version 2 is a private, offline-first Android productivity app for one-time goals, recurring daily targets, progress tracking, and helpful reminders without losing history.

## Version 2 Features

- Create one-time goals or recurring target-based goals.
- Set daily targets in minutes, hours, pages, repetitions, or a custom unit.
- Track recurring progress with quick additions (`+15 min`, `+30 min`, `+1 hour`) or manual entry.
- Automatically complete today's occurrence when progress reaches its target.
- Cancel that goal's remaining reminder work immediately after completion.
- Generate a fresh zero-progress occurrence on the next scheduled day while preserving prior history.
- Repeat every day or on selected weekdays.
- Configure each goal's start/end time, once/15/30/60/120-minute or custom reminder interval, sound profile, vibration, and smart text.
- Use status-aware messages for not started, in progress, almost complete, and completed states.
- Add progress directly from recurring-goal notifications and snooze reminders with Dismiss.
- Pause indefinitely, pause until a chosen resume date, resume, or delete a recurring goal while optionally keeping history.
- View recurring completion rate, average progress, completed days, and current/best streaks.
- Export and import Version 2 recurring definitions and daily history; Version 1 JSON backups remain importable.

Example: create **Study**, choose **Recurring**, set **3 Hours**, **Every day**, and reminders every **30 minutes** from **9:00 AM–10:00 PM**. Progress updates from `0 / 3h` to `1h / 3h`, then `2h 30m / 3h`. Adding the last 30 minutes completes today's instance and cancels further Study reminders. The next scheduled day starts at `0 / 3h` while the completed date stays in History.

## Features

- Create, edit, reschedule, complete, and delete daily goals.
- Add an optional description, priority, date, and time.
- See today's completion percentage and completed/incomplete counts update live.
- Automatically carry unfinished goal series through missed days and into today.
- Prevent duplicate carry-forward rows with a database-level unique constraint.
- Browse incomplete goals grouped by date with search, date, and priority filters.
- Browse history by calendar date with completed/incomplete and priority filters.
- View today/week percentages, lifetime totals, and current/best completion streaks.
- Receive optional per-goal smart reminders; Android 13+ permission is requested only after an explanation.
- Choose system, light, or dark theme.
- Export and import local JSON backups through Android's permission-free system file picker.
- Read an accurate privacy policy inside the app.
- Work completely offline with no login and no `INTERNET` permission.

## Tech Stack

- Kotlin 2.3.21
- Jetpack Compose with the stable 2026.08 Compose BOM
- Material 3
- Room 2.8.5
- MVVM with `ViewModel`, coroutines, `Flow`, and `StateFlow`
- Navigation Compose 2.10.1
- Preferences DataStore 1.2.1
- WorkManager 2.11.2
- Gradle 9.7.1 with Kotlin DSL and Android Gradle Plugin 9.4.0
- Minimum SDK 26 (Android 8.0); compile/target SDK 37

## Screenshots

Add final device screenshots before publishing:

| Today | Add goal | Incomplete |
| --- | --- | --- |
| `docs/screenshots/today.png` | `docs/screenshots/add-goal.png` | `docs/screenshots/incomplete.png` |

| History | Statistics | Settings |
| --- | --- | --- |
| `docs/screenshots/history.png` | `docs/screenshots/statistics.png` | `docs/screenshots/settings.png` |

## Project Structure

```text
app/src/main/java/com/katiyar/dailygoals/
├── data/
│   ├── local/             # Normal, recurring, and daily-instance Room data
│   └── repository/        # Goal operations, settings, migration-safe JSON backup
├── domain/
│   ├── model/             # Priority, settings, statistics models
│   └── usecase/           # Validation, percentages, streak calculations
├── notifications/         # Per-goal workers, channels, actions, midnight sync
├── ui/
│   ├── components/        # Reusable goal, progress, and empty-state UI
│   ├── navigation/        # Navigation graph and bottom navigation
│   ├── screens/           # Today, editor, incomplete, history, stats, settings
│   └── theme/             # Light/dark Material 3 theme
└── viewmodel/             # GoalViewModel and reactive application state
```

Room schemas are exported to `app/schemas/`. `MIGRATION_1_2` preserves the Version 1 `goals` table, adds normal-goal reminder columns, and creates normalized `recurring_goals` and `daily_goal_instances` tables. Instrumented tests validate the migration and repository behavior. JVM tests cover validation, completion math, recurrence rules, reminder windows/content, scheduling intervals, and statistics.

## Carry-Forward Design

Every user-created goal gets a stable `seriesId`. Room enforces a unique index on `(seriesId, date)`, so two copies of the same goal series cannot exist on one date even if synchronization runs concurrently or repeatedly.

At launch/resume and inside reminder work, the repository finds the latest row in every series. If that row is incomplete and not cancelled, it creates one historical row for every missed date through today. Completing the latest row stops future carry-forward. Deleting is a soft cancellation, which hides the selected row while retaining the series terminator needed to prevent an older incomplete row from reappearing.

Recurring goals never enter this carry-forward pipeline. Each definition has independent scheduling and reminder settings. A daily snapshot stores that date's title, target, unit, progress, and completion state. Room's unique `(recurringGoalId, date)` index prevents duplicate occurrences. Missed days do not accumulate target amounts; synchronization creates only the requested active date.

## Notification Design

Daily Goals uses persistent one-time WorkManager requests per goal rather than continuously running timers. A separate midnight reconciliation request creates the next day's eligible recurring instances, applies normal carry-forward, and schedules new work. App launch/resume performs the same idempotent reconciliation, so date changes and missed days recover safely.

Notification channels are separated by sound profile and vibration preference. **Default**, **Soft reminder**, **Bell**, and **Chime** use the device's system-provided legal notification/alarm/ringtone profiles; **None** is silent. Android channel settings take precedence after a channel is created. WorkManager survives process death and device restart, but Android Doze and manufacturer battery restrictions may delay exact delivery; the app intentionally does not request exact-alarm permission or run a continuous background service.

## How to Run

1. Install the latest stable Android Studio compatible with AGP 9.4.
2. In Android Studio, choose **Open** and select this repository root.
3. Let Gradle sync and install Android SDK Platform 37 if prompted.
4. Create or select an Android 8.0+ emulator, or connect a physical device with USB debugging enabled.
5. Select the `app` run configuration and click **Run**.

The permanent package, namespace, and application ID are `com.katiyar.dailygoals`.

## Command-Line Builds

Set `ANDROID_HOME` or create an uncommitted `local.properties` containing `sdk.dir=/absolute/path/to/Android/Sdk`.

On Linux or macOS, if the project was downloaded from GitHub and the wrapper is not executable, run `chmod +x gradlew` once.

Run all checks and builds:

```bash
./gradlew build
```

Run JVM tests and compile the on-device migration/repository suite:

```bash
./gradlew test assembleDebugAndroidTest
```

Build a debug APK:

```bash
./gradlew assembleDebug
```

The APK is created at `app/build/outputs/apk/debug/app-debug.apk`.

Build the unsigned release artifacts:

```bash
./gradlew assembleRelease
./gradlew bundleRelease
```

The APK and Android App Bundle are created under `app/build/outputs/apk/release/` and `app/build/outputs/bundle/release/`.

## Signing a Release

Never commit a keystore, passwords, or signing credentials. Create a private upload key using Android Studio (**Build > Generate Signed Bundle / APK**) or `keytool`, keep it outside the repository, and configure signing from local environment variables or an ignored `keystore.properties` file. For Play uploads, choose **Android App Bundle** and generate a signed `release` bundle.

## Google Play Store Deployment

1. Create and verify a Google Play Console developer account.
2. Choose a permanent unique application ID, update the package, and create the app in Play Console.
3. Complete the store listing: title, short/full descriptions, category, contact details, and support URL.
4. Replace/validate the supplied adaptive icon and add a 512×512 Play Store icon.
5. Capture phone screenshots for the Today, editor, Incomplete, History, Statistics, and Settings screens.
6. Host `PRIVACY_POLICY.md` at a public HTTPS URL and enter that URL in Play Console.
7. Complete the Data Safety form based on the final build. This source has no developer-operated data collection and no Internet permission; re-evaluate if analytics, crash reporting, ads, or cloud features are added.
8. Generate a signed Android App Bundle using Android Studio and enroll in Play App Signing.
9. Upload the `.aab` to an internal testing release.
10. Complete content rating, target audience, ads declaration, app access, and current testing/release requirements.
11. Test the Version 1→2 upgrade, install/upgrade, notification actions/channels, battery-restricted delivery, backup/import, dark mode, date rollover, recurring reset, and carry-forward on release-signed builds.
12. Promote through closed/open testing as required, then submit the production release for review.

## Privacy and Permissions

`POST_NOTIFICATIONS` is the only runtime permission: only Android 13+ users who enable reminders after an in-app explanation see its system dialog. A denial state links to the app's Android notification settings. WorkManager contributes non-runtime permissions required for persistent scheduled work (`RECEIVE_BOOT_COMPLETED`, `WAKE_LOCK`, `FOREGROUND_SERVICE`, and `ACCESS_NETWORK_STATE`). The app has no `INTERNET` permission, and network access is not required. File export/import uses the Storage Access Framework without broad storage access. The app does not request contacts, location, camera, microphone, SMS, or phone access.

## Release Checklist

- Confirm ownership of the `com.katiyar.dailygoals` application ID before the first Play Console upload; application IDs cannot be changed after publication.
- Add final Play Store icon and screenshots.
- Host the privacy policy.
- Keep both exported Room schemas and run the Version 1→2 migration test before release.
- Test on API 26, 33, and the target API.
- Run `./gradlew build bundleRelease`.
- Inspect the signed bundle with Android Studio's APK Analyzer and test it through Play internal testing.

## License

MIT. See [LICENSE](LICENSE).
