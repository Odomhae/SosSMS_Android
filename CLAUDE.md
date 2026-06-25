# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

안심콜 (Elder SOS App) — a single-screen-first Android app for elderly users. One giant SOS
button starts a 3-second cancel countdown; if not cancelled, the app texts the user's current
location to up to 3 emergency contacts and auto-calls the highest-priority contact. A second
button shares location on demand with no countdown. No server, no login, no companion app —
everything rides on the phone's existing SMS and calling capability.

Full design spec: `docs/superpowers/specs/2026-06-19-elder-sos-app-design.md`
Manual test checklist (permissions/SMS/call — can't be exercised by unit tests): `docs/testing/elder-sos-manual-checklist.md`

Kotlin + Jetpack Compose, package `com.odom.sosSms`. `minSdk = 26`, `targetSdk`/`compileSdk = 36`.

## Build & Test Commands

The Gradle wrapper needs JDK 11+. If the default `java` is older, point `JAVA_HOME` at a newer
JDK (Android Studio ships one):

```bash
JAVA_HOME="C:\Program Files\Android\Android Studio\jbr" ./gradlew.bat testDebugUnitTest
JAVA_HOME="C:\Program Files\Android\Android Studio\jbr" ./gradlew.bat assembleDebug
```

Run a single test class or method:

```bash
./gradlew.bat testDebugUnitTest --tests "com.odom.sosSms.ui.sos.SosViewModelTest"
./gradlew.bat testDebugUnitTest --tests "com.odom.sosSms.ui.sos.SosViewModelTest.specific test name"
```

Unit tests (`app/src/test`) cover `SmsMessageBuilder`, the `SosViewModel` countdown/cancel/fire
state machine, `ContactsViewModel` CRUD/reorder/3-contact cap, and `ContactsRepository` DataStore
round-trips. Repository/ViewModel tests run under **Robolectric** so `Build.VERSION.SDK_INT`
reflects a real API level — without it, AndroidX DataStore's file rename falls back to a legacy
path that doesn't overwrite existing files on Windows JVM test runs (never an issue on a real
device, since `minSdk` is 26). Don't remove the Robolectric runner from these tests.

Instrumented tests live in `app/src/androidTest` and require a device/emulator
(`./gradlew.bat connectedAndroidTest`).

There is no lint/format command configured beyond the default Gradle/Android lint
(`./gradlew.bat lint`).

## Architecture

```
ui/nav/BrainNavHost.kt        Compose Navigation: Home / SosCountdown / Contacts
ui/home/HomeScreen.kt         SOS + Share buttons, permission-flow state machine
ui/home/ShareLocationViewModel.kt
ui/sos/SosCountdownScreen.kt + SosViewModel.kt   countdown -> SMS -> auto-call state machine
ui/contacts/ContactsScreen.kt + ContactsViewModel.kt
ui/components/BigButton.kt    shared large-touch-target button
ui/theme/                     colors, type, high-contrast SOS/Share color pairs

data/Contact.kt, data/ContactsRepository.kt   DataStore-backed contact list (max 3, tab/newline-encoded)
location/LocationProvider.kt, location/GeoLocation.kt   FusedLocationProviderClient wrapper +
                               location-enabled check / Play Services settings resolution
sms/SmsMessageBuilder.kt (pure), sms/SmsSender.kt       message formatting + SmsManager send
call/CallLauncher.kt          ACTION_CALL intent
permissions/PermissionRationale.kt   rationale card / denied+Settings card composables
```

**Dependency injection pattern**: ViewModels (`SosViewModel`, `ShareLocationViewModel`,
`ContactsViewModel`) take their Android-framework-touching dependencies (location lookup, SMS
send, call launch) as plain suspend/lambda function parameters in their constructors rather than
a `Context` directly. This is what makes the countdown/CRUD state machines unit-testable without
Robolectric for the pure logic, and is the pattern to follow for any new ViewModel that needs
framework access.

**Permission flow**: `HomeScreen.kt` implements a generic permission-request queue
(`PermissionFlowStep`) that walks through a list of permissions one at a time, showing a
plain-language rationale card before each system prompt, and falling back to a "denied → open
Settings" card for permissions marked mandatory. SOS requires SMS + Call as mandatory (location
is best-effort); Share Location requires SMS as mandatory. A `LaunchedEffect(Unit)` in
`HomeScreen` also runs this same queue once on first composition (`STARTUP_PERMISSIONS`, no
mandatory set) to prime SMS/Call/Location permissions as soon as the app opens, instead of
waiting for the user to press a button. When adding a new permission-gated action, extend this
queue rather than writing a parallel one-off flow.

**GPS-enable flow**: Both SOS and Share Location pass `needsLocationCheck = true` to `startFlow`.
Once permissions are settled, `HomeScreen` calls `LocationProvider.isLocationEnabled()`; if
location services are off, it calls `LocationProvider.checkLocationSettings()` (Play Services
`SettingsClient`) and, on a `ResolvableApiException`, launches the one-tap "turn on location"
system dialog via `ActivityResultContracts.StartIntentSenderForResult`. The flow proceeds to the
action either way (turned on or dismissed) since location remains best-effort, never blocking.

**Localization**: UI strings switch with the system locale — `values-ko/strings.xml` for Korean,
`values/strings.xml` as the English default. Any new user-facing string needs entries in both.

**Accessibility constraints** (non-negotiable for this app's target audience): ≥96dp touch
targets, high-contrast SOS-red / Share-blue color pairs (`ui/theme/Color.kt`), TalkBack content
descriptions on all interactive elements. Drag-and-drop was intentionally avoided for contact
reordering (unreliable with TalkBack) in favor of explicit ▲▼ buttons — don't reintroduce
drag-and-drop for this UI.

## Out of Scope (v2+ ideas — do not implement unless asked)

Fall detection, continuous/periodic background location tracking, medication reminders/vitals
tracking, companion app or cloud sync for family members.
