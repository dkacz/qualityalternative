# Sprint 28 Live rclone + Picker Spike Runbook

Purpose: execute the remaining release gate after GPT Pro R3 `10/10 PASS/PASS`.

## Current Blocker

On 2026-06-14, local validation had:

- `qaApi36` emulator attached over ADB.
- Google Play services installed at `/product/priv-app/PrebuiltGmsCore/PrebuiltGmsCore.apk`.
- `rclone v1.73.2` with a configured `gdrive:` remote.
- No signed-in Google account on the emulator: `adb shell cmd account list` returned no accounts.

The spike therefore needs a signed-in Android device or emulator using the OAuth client accepted by this app.

## Fixture

Use the package directory:

- `evidence/sprint28_agent_inbox_drive_access/device_spike/rclone_picker_live_package/`

It contains:

- `manifest.json`
- `content.md`

The manifest SHA-256 is bound to `content.md`:

- `7aafb776a9cd2f5e1447ccffb587409f86d3eadcd654e88bf223224fab908bbf`

## Procedure

1. Install a build containing Sprint 28 R3 implementation commit `482478d` or later.
2. Sign into Android with the Google account authorized for the app OAuth client.
3. Open Quality Alternative Settings.
4. In `Agent Inbox`, tap `Select folder`.
5. In Google Picker, select the Drive folder that will receive agent packages.
6. Confirm the app shows `Google Drive Agent Inbox folder selected`.
7. Upload the fixture as a new child folder under that same selected Drive folder:

```bash
cd /Users/omare/Documents/qualityalternative && QA_AGENT_INBOX_RCLONE_PARENT='gdrive:REPLACE_WITH_SELECTED_AGENT_INBOX_FOLDER_PATH' && QA_PACKAGE='sprint28-live-picker-rclone-package-20260614' && rclone copy evidence/sprint28_agent_inbox_drive_access/device_spike/rclone_picker_live_package "$QA_AGENT_INBOX_RCLONE_PARENT/$QA_PACKAGE" --progress
```

8. Return to Quality Alternative Settings and tap `Scan now`.
9. Record whether `Sprint 28 rclone Picker proof` appears for review.
10. If visible, capture a screenshot of the review state and record the folder/package upload target without raw Drive ids.

## Pass Criteria

- The app sees the package uploaded by rclone after the Picker grant.
- The package appears in the finite Agent Inbox review list.
- The app can import it without access-lost errors.
- Raw Drive folder id, package id, file id, and file name remain absent from remote-safe analytics and Portable Profile export.

## Fail Criteria

- The package is visibly present in Drive but absent from the app scan.
- The scan returns access-lost after a valid Picker folder selection.
- Import exposes raw Drive identifiers in user-visible provenance, remote-safe analytics, or Portable Profile.

If this fails, do not release the Sprint 28 APK. Prepare the documented `drive.readonly` fallback with PRD/privacy update and another GPT Pro review.
