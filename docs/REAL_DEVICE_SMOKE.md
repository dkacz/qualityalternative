# Real Device Smoke

Use this checklist to validate the Android interception alpha on one physical device before marking internal alpha as validated.

## Preconditions

- Install the latest debug build on a physical Android device.
- Complete onboarding inside the app.
- Select at least two supported distracting apps from the launch list.
- Enable Accessibility for `Quality Alternative`.
- Overlay permission is optional for this alpha and does not block the smoke run.

## Smoke Flow

1. Open the app and verify the readiness card says system intervention is ready.
2. Open the first selected distracting app from the launcher.
3. Confirm the intervention surface appears within roughly two seconds.
4. Tap `Open anyway` and confirm the user can continue into the target app without an immediate loop.
5. Re-open the same app and confirm the suppression window prevents a second instant re-prompt.
6. Trigger another intervention and choose `Pause 15 min`.
7. Re-open the same app during the delay window and confirm the app does not show the full intervention again.
8. After the delay expires, re-open the same app and confirm a fresh intervention appears.
9. Repeat the core flow on a second supported app from the launch list.
10. Open the app home screen and confirm recent history plus analytics-derived behavior still look coherent after the smoke run.

## Expected Outcomes

- Accessibility-triggered interception works on two real apps.
- `Open anyway` does not trap the user in a re-interception loop.
- Delay suppresses the intervention during the active window and allows it again after expiry.
- The home screen does not falsely claim readiness when Accessibility is off.
- No crash, blank screen, or stuck handoff occurs during app-to-app transitions.

## Sign-Off Template

- Device:
- Android version:
- App build / commit:
- App 1 result:
- App 2 result:
- `Open anyway` result:
- Delay result:
- Notes on OEM quirks or degraded behavior:
