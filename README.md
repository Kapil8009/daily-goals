# Daily Goals

Daily Goals is a private, offline-first Android productivity app for planning each day, completing goals, and carrying unfinished work forward without losing daily history.

## Features

- Create, edit, reschedule, complete, and delete daily goals.
- Add an optional description, priority, date, and time.
- See today's completion percentage and completed/incomplete counts update live.
- Automatically carry unfinished goal series through missed days and into today.
- Prevent duplicate carry-forward rows with a database-level unique constraint.
- Browse incomplete goals grouped by date with search, date, and priority filters.
- Browse history by calendar date with completed/incomplete and priority filters.
- View today/week percentages, lifetime totals, and current/best completion streaks.
- Receive an optional daily reminder at a chosen time; Android 13+ permission is requested only after an explanation.
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
app/src/main/java/com/example/dailygoals/
├── data/
│   ├── local/             # Room entity, DAO, converters, database
│   └── repository/        # Goal operations, settings, JSON backup
├── domain/
│   ├── model/             # Priority, settings, statistics models
│   └── usecase/           # Validation, percentages, streak calculations
├── notifications/         # Notification channel, worker, scheduler
├── ui/
│   ├── components/        # Reusable goal, progress, and empty-state UI
│   ├── navigation/        # Navigation graph and bottom navigation
│   ├── screens/           # Today, editor, incomplete, history, stats, settings
│   └── theme/             # Light/dark Material 3 theme
└── viewmodel/             # GoalViewModel and reactive application state
```

Room schemas are exported to `app/schemas/` for future migration testing. JVM tests cover validation, completion math, carry planning, and statistics. Instrumented repository tests cover persistence, editing, deletion, completion, duplicate prevention, disabled carry-forward, and multiple-day carry-forward.

## Carry-Forward Design

Every user-created goal gets a stable `seriesId`. Room enforces a unique index on `(seriesId, date)`, so two copies of the same goal series cannot exist on one date even if synchronization runs concurrently or repeatedly.

At launch/resume and inside reminder work, the repository finds the latest row in every series. If that row is incomplete and not cancelled, it creates one historical row for every missed date through today. Completing the latest row stops future carry-forward. Deleting is a soft cancellation, which hides the selected row while retaining the series terminator needed to prevent an older incomplete row from reappearing.

## How to Run

1. Install the latest stable Android Studio compatible with AGP 9.4.
2. In Android Studio, choose **Open** and select this repository root.
3. Let Gradle sync and install Android SDK Platform 37 if prompted.
4. Create or select an Android 8.0+ emulator, or connect a physical device with USB debugging enabled.
5. Select the `app` run configuration and click **Run**.

The permanent package, namespace, and application ID are `com.katiyar.dailygoals`.

## Command-Line Builds

Set `ANDROID_HOME` or create an uncommitted `local.properties` containing `sdk.dir=/absolute/path/to/Android/Sdk`.

Run all checks and builds:

```bash
./gradlew build
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
11. Test install/upgrade, notifications, backup/import, dark mode, date rollover, and carry-forward on release-signed builds.
12. Promote through closed/open testing as required, then submit the production release for review.

## Privacy and Permissions

`POST_NOTIFICATIONS` is the only runtime permission: only Android 13+ users who opt into reminders see its system dialog. WorkManager contributes non-runtime permissions required for reliable scheduled work (`RECEIVE_BOOT_COMPLETED`, `WAKE_LOCK`, `FOREGROUND_SERVICE`, and `ACCESS_NETWORK_STATE`). The app has no `INTERNET` permission, and network access is not required. File export/import uses the Storage Access Framework without broad storage access. The app does not request contacts, location, camera, microphone, SMS, or phone access.

## Release Checklist

- Confirm ownership of the `com.katiyar.dailygoals` application ID before the first Play Console upload; application IDs cannot be changed after publication.
- Add final Play Store icon and screenshots.
- Host the privacy policy.
- Add and test Room migrations before changing the database schema version.
- Test on API 26, 33, and the target API.
- Run `./gradlew build bundleRelease`.
- Inspect the signed bundle with Android Studio's APK Analyzer and test it through Play internal testing.

## License

MIT. See [LICENSE](LICENSE).
