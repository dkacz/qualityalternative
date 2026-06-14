# Sprint 28 Live Picker Runtime Result

Date: 2026-06-14

## Environment

- Branch: `codex/sprint28-agent-inbox-drive-access`
- Build: `app/build/outputs/apk/debug/app-debug.apk`
- Device: `qaApi36` / `emulator-5554`
- Google account: present on emulator during the live run; private account dumps are intentionally excluded from the repository evidence.
- Google Play services: `25.26.35 (260400-783060121)` observed during the live run.
- Play Store package: installed as `com.android.vending` but had no launchable activity on this AVD during the live run.
- rclone folder prepared: `gdrive:QA_AGENT_INBOX_LIVE_SPIKE_20260614`.

## Attempt

1. Reinstalled the current debug APK to avoid stale app state.
2. Completed first-run onboarding with default choices and skipped non-Drive runtime permissions.
3. Opened Settings -> Agent Inbox.
4. Tapped `Select folder`.
5. Google Play Services showed the account chooser.
6. Selected the signed-in Google account.

## Observed Result

- The Google Drive folder Picker did not appear.
- The app returned to Settings and showed `No Agent Inbox folder was selected`.
- Fresh logcat shows the Google authorization flow completed account selection and returned to the app; no folder selection UI and no selected folder id were observed.
- Repeating the flow before and after app reinstall produced the same result.

## Evidence

- `00_select_folder_visible.png`: Agent Inbox `Select folder` state before the live attempt.
- `02_no_folder_selected.png`: app state after Google account selection returned without a folder id.
- `logcat_picker_flow.txt`: fresh logcat for the clean install Picker attempt.
- Private account dumps, package dumps, and OAuth/account screenshots are intentionally excluded from the repository evidence.

## Decision

This environment does not prove that a Picker-selected `drive.file` folder grant exposes later rclone-added package children, because it never reaches a folder Picker selection. The Sprint 28 release gate remains blocked for the Picker-first path. The next implementation step is the controlled `drive.readonly` fallback with PRD/privacy updates, explicit user-facing copy, targeted tests, visual E2E screenshots, and another GPT Pro review that includes visual evidence.
