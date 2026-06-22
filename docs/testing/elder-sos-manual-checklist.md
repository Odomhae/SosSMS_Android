# Elder SOS — Manual Test Checklist

Permission grant/denial flows and real SMS/call behavior can't be exercised by unit tests
(no SIM/telephony in the JVM test environment). Verify these on a real device with an active SIM.

## Setup

- [ ] Fresh install (no permissions pre-granted)
- [ ] At least 2 real phone numbers available to receive test SMS/calls

## Permission flows

- [ ] First tap on SOS shows the SMS rationale card before the system dialog
- [ ] Denying SMS shows the "won't be sent without this permission" card with an Open Settings button
- [ ] Open Settings button navigates to the app's system settings page
- [ ] Granting SMS, then denying CALL_PHONE, shows the call-denied card and does **not** proceed to the countdown screen
- [ ] Granting SMS + CALL_PHONE but denying location still proceeds to the countdown screen (location is optional)
- [ ] First tap on "Share my location" shows only the SMS (+ location) rationale, never asks for CALL_PHONE
- [ ] Re-tapping SOS/Share after a previous denial re-shows the denied card (not a silent no-op)

## SOS flow

- [ ] Pressing SOS shows a 3 → 2 → 1 countdown
- [ ] Pressing Cancel during the countdown returns to Home and sends nothing
- [ ] Letting the countdown finish sends one SMS to each configured contact with a Google Maps link
- [ ] With location services off/unavailable, the SMS still sends with the "위치 확인 불가 / location unavailable" text instead of failing
- [ ] After SMS send, the phone dialer opens (or call places) to contact #1 (the top-priority contact)
- [ ] With zero contacts configured, SOS completes without crashing and does not attempt a call

## Share-location flow

- [ ] Pressing "Share my location" sends immediately with no countdown and no call
- [ ] Sent confirmation (toast) appears after the SMS attempt completes

## Contacts screen

- [ ] Add up to 3 contacts; the Add button disappears/disables at 3 and a max-reached message shows
- [ ] Edit an existing contact and confirm the change persists after closing/reopening the app
- [ ] Delete a contact and confirm the remaining contacts keep their relative order
- [ ] Move Up/Move Down correctly reorders priority (contact #1 is the one auto-called)
- [ ] Move Up is disabled on the first row; Move Down is disabled on the last row
- [ ] TalkBack reads each control's label correctly (rows, priority, move/edit/delete buttons, big SOS/Share/Cancel buttons)

## Localization

- [ ] Switching the system language to Korean shows all Korean strings (안심콜, SOS, 내 위치 보내기, etc.)
- [ ] Switching back to English (or any other locale) falls back to the English strings
