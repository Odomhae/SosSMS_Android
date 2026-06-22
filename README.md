# 안심콜 (Elder SOS App)

A single-screen-first Android app for elderly users. The home screen has one giant SOS
button: press it, a 3-second cancel countdown appears, and if not cancelled the app texts
the user's current location to up to 3 emergency contacts and auto-calls the highest-priority
contact. A second button lets the elder share their location on demand (non-emergency, no
countdown). No server, no login, no companion app — everything rides on the phone's existing
SMS and calling capability.

Full design spec: [`docs/superpowers/specs/2026-06-19-elder-sos-app-design.md`](docs/superpowers/specs/2026-06-19-elder-sos-app-design.md)

## Status

v1 feature set is implemented and passing unit tests / `assembleDebug`. Real-device
verification (permission flows, actual SMS/call behavior) is still outstanding — see
[Manual Testing](#manual-testing) below.

## Features (v1)

- **SOS button** — 3→2→1 cancel countdown, then sends an SMS (with a Google Maps location
  link, or a "location unavailable" fallback) to every configured contact and auto-calls
  the top-priority contact.
- **Share my location** — sends the same kind of SMS immediately, no countdown, no call.
- **Emergency contacts** — up to 3, with name/phone, reorderable via ▲▼ buttons (priority =
  call order). Drag-and-drop was intentionally **not** used here since it's unreliable with
  TalkBack for the target audience.
- **Permissions** — SMS, Call, and Location are requested just-in-time per button, each
  preceded by a plain-language rationale card; a denied permission shows a card with a
  button straight to the app's system Settings page.
- **Korean + English** — UI strings switch with the system locale (`values-ko/strings.xml`
  for Korean, `values/strings.xml` as the English default).
- **Accessibility** — ≥96dp touch targets, high-contrast SOS-red / Share-blue color pairs,
  TalkBack content descriptions on all interactive elements.

## Architecture

Kotlin + Jetpack Compose, package `com.odom.sosSms`.

```
ui/nav/BrainNavHost.kt        Compose Navigation: Home / SosCountdown / Contacts
ui/home/HomeScreen.kt         SOS + Share buttons, permission-flow state machine
ui/home/ShareLocationViewModel.kt
ui/sos/SosCountdownScreen.kt + SosViewModel.kt   countdown -> SMS -> auto-call state machine
ui/contacts/ContactsScreen.kt + ContactsViewModel.kt
ui/components/BigButton.kt    shared large-touch-target button
ui/theme/                     colors, type, high-contrast SOS/Share pairs

data/Contact.kt, data/ContactsRepository.kt   DataStore-backed contact list (max 3)
location/LocationProvider.kt, location/GeoLocation.kt   FusedLocationProviderClient wrapper
sms/SmsMessageBuilder.kt (pure), sms/SmsSender.kt       message formatting + SmsManager send
call/CallLauncher.kt          ACTION_CALL intent
permissions/PermissionRationale.kt   rationale card / denied+Settings card composables
```

ViewModels take their Android-framework-touching dependencies (location, SMS, call) as plain
function parameters rather than Context directly, so the countdown/CRUD logic is unit
testable without Robolectric or a device.

## Building & Testing

The Gradle wrapper needs JDK 11+ (this repo targets AGP 8.13.2 / compileSdk 36). If your
default `java` is older, point `JAVA_HOME` at a newer JDK (Android Studio ships one):

```bash
JAVA_HOME="C:\Program Files\Android\Android Studio\jbr" ./gradlew.bat testDebugUnitTest
JAVA_HOME="C:\Program Files\Android\Android Studio\jbr" ./gradlew.bat assembleDebug
```

Unit tests cover: `SmsMessageBuilder` formatting, the `SosViewModel` countdown/cancel/fire
state machine, `ContactsViewModel` CRUD + reorder + 3-contact cap, and `ContactsRepository`
DataStore round-trips. Repository/ViewModel tests run under Robolectric so
`Build.VERSION.SDK_INT` reflects a real API level — without it, AndroidX DataStore's file
rename falls back to a legacy path that doesn't overwrite existing files on Windows JVM test
runs (never an issue on a real device, since minSdk is 26).

## Manual Testing

Permission grant/denial flows and real SMS/call behavior can't be exercised by unit tests —
verify these on a real device with an active SIM using the checklist at
[`docs/testing/elder-sos-manual-checklist.md`](docs/testing/elder-sos-manual-checklist.md).

## Out of Scope (v2+ ideas)

Fall detection, continuous/periodic background location tracking, medication reminders /
vitals tracking, companion app or cloud sync for family members.
