# Campus Pulse

Campus Pulse is a Firebase-powered Android notice board for students and staff. This version includes a refreshed Material 3 interface and was customized for Aman (Student ID: 2024eb02381).

## Highlights

- Separate student and staff dashboards
- Firebase email/password authentication
- Realtime campus notices
- Staff notice publishing
- Firebase Cloud Messaging support
- Inline form validation, loading feedback, notice counts, and empty states

## Firebase setup

The Android application ID is `com.aman.campuspulse`. Firebase client configuration is intentionally excluded from this public repository.

Register an Android app with that package name in your Firebase project, download its generated `google-services.json`, and save it locally as `app/google-services.json`. Do not commit that file.

## Open and run

Open the project root in Android Studio, allow Gradle to sync, and run the `app` configuration on an Android device or emulator (minimum Android 7.0 / API 24).
