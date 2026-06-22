# 안심콜 (Elder SOS App) — Design

## Overview

A single-screen-first Android app for elderly users. The home screen has one giant SOS button: press it, a 3-second cancel countdown appears, and if not cancelled the app texts the user's current location to up to 3 emergency contacts and auto-calls the highest-priority contact. A second button lets the elder share their location on demand (non-emergency, no countdown). No server, no login, no companion app — everything rides on the phone's existing SMS and calling capability.

**In scope (v1):**
- SOS button with cancel countdown, auto-call to primary contact
- On-demand "share my location" button (text only, no call)
- Self-managed emergency contact list (up to 3, with priority order)
- Korean + English UI (follows system locale)
- Accessibility-first UI (large touch targets, high contrast, font scaling, TalkBack support)

**Explicitly out of scope (v2+ ideas, not building now):**
- Fall detection
- Continuous or periodic background location tracking
- Medication reminders / vitals tracking
- Companion app or cloud sync for family members

## Screens & Flow

- **Home screen**: two giant buttons — SOS (red) and "내 위치 보내기" / "Share my location" (blue) — plus a small settings/contacts icon in a corner.
- **SOS countdown screen**: full-screen 3→2→1 countdown with a huge "취소" / "Cancel" button. If not cancelled: sends SMS (with location link) to all configured contacts, then auto-dials contact #1 (highest priority).
- **Share-location flow**: pressing the share button sends the SMS immediately, no countdown, no call — it's not an emergency.
- **Contacts/settings screen**: list of up to 3 contacts with big add/edit/delete controls and drag-to-reorder; order determines call priority (first = auto-called on SOS).

## Data & Permissions

- Contacts (`name`, `phone`, `order`) are stored locally via Jetpack DataStore. No cloud, no login, no account system.
- Permissions (SMS, Phone Call, Location) are requested just-in-time, only when the relevant button is first used — not all upfront. Each request is preceded by a plain-language explanation screen before the system permission dialog.
- If location is unavailable when SOS/share-location fires, the SMS still sends, with the message stating "위치 확인 불가" / "location unavailable" instead of failing silently.
- If SMS or Call permission is denied, the corresponding button shows a clear explanation that it won't work without that permission, with a button that opens the system Settings page for the app.

## Tech & Architecture

- Built on the existing Kotlin + Jetpack Compose scaffold (`com.odom.brain`).
- Compose Navigation between Home / SOS-Countdown / Contacts screens.
- `ContactsViewModel`: DataStore-backed CRUD for the contact list.
- `SosViewModel`: countdown timer state, `FusedLocationProviderClient` for last-known location, `SmsManager` for sending texts, `ACTION_CALL` intent for auto-dialing.
- Accessibility: touch targets ≥96dp, high-contrast color scheme, respects system font scaling, TalkBack content descriptions on all interactive elements.
- Localization: Korean + English string resources, switching by system locale.

## Testing

- Unit tests for ViewModel logic: contact CRUD, countdown timer behavior, SMS message formatting (including the "location unavailable" fallback).
- Manual test checklist for permission-grant/denial flows and actual SMS/call behavior (these require a real device with a SIM and cannot be fully automated).

## Open Questions / Risks

- Auto-dialing immediately after sending SMS could compound user anxiety if contact #1 doesn't answer — v1 leaves this as a single auto-call attempt with no retry/fallback to contact #2; revisit if real usage shows this is insufficient.
- `SmsManager` direct send (rather than launching the default SMS app) requires the `SEND_SMS` permission, which is flagged as a sensitive permission on the Play Store; app description and privacy policy should address this clearly.
