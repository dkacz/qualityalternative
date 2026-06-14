# Rclone + Picker Folder Spike

Purpose: prove whether the documented Google Picker folder grant under `drive.file` exposes Agent Inbox package folders added later by an external user-controlled tool such as rclone.

## Preconditions

- Install a debug or alpha build containing Sprint 28 R3 implementation commit `482478d` or later.
- Use a Google account authorized for the app's OAuth client.
- Prepare a Drive folder intended to be the Agent Inbox.
- Ensure the package producer can upload to that same folder through rclone.

## Procedure

For the ready-to-run operator flow and fixture, use:

- `evidence/sprint28_agent_inbox_drive_access/device_spike/LIVE_RCLONE_PICKER_SPIKE_RUNBOOK.md`
- `evidence/sprint28_agent_inbox_drive_access/device_spike/rclone_picker_live_package/`
- `evidence/sprint28_agent_inbox_drive_access/device_spike/LIVE_ENVIRONMENT_PROBE_20260614.txt`

1. Open app Settings.
2. In `Agent Inbox`, tap `Select folder`.
3. In Google Picker, select the intended Agent Inbox folder.
4. Confirm Settings shows `Google Drive Agent Inbox folder selected`.
5. Using rclone, add a new child package folder under the selected folder.
6. The package folder must contain:
   - `manifest.json`
   - exactly one `content.md`, `content.markdown`, or `content.epub`
7. Return to Settings and tap `Scan now`.
8. Record whether the app shows the package for review.

## Expected Result For Picker-First Release

- The app sees the rclone-added package under the selected folder.
- The package appears in the finite review list.
- Raw Drive folder id, package id, file id, and file name do not appear in remote-safe analytics or Portable Profile.

## Failure Result

If the Picker cannot produce a selected folder grant, the scan reports an access-lost state, or the scan does not see a valid package that is visibly present in Drive, keep this sprint unreleased and evaluate the documented `drive.readonly` fallback path with a PRD/privacy update and GPT Pro review.

## Result

Failed before folder selection on the available live emulator. On 2026-06-14, `qaApi36` was signed into a Google account, the current debug APK was freshly installed, and `rclone` prepared `gdrive:QA_AGENT_INBOX_LIVE_SPIKE_20260614`. Tapping Agent Inbox `Select folder` launched the Google Play Services account chooser, but after selecting the account the flow returned to Quality Alternative with `No Agent Inbox folder was selected`; no Google Drive folder Picker appeared and no selected folder id was returned. The same result occurred before and after app reinstall. Evidence is recorded in `live_picker_runtime_20260614/RESULT.md`.

Because this environment cannot produce the required Picker folder grant, the sprint must not proceed to release on the Picker-first path.

## Fallback Follow-Up

The controlled `drive.readonly` fallback was implemented after this failure. In that flow, the user pastes the Agent Inbox Drive folder URL/id, grants explicit read-only Drive consent, and the app scans only the saved folder id. The app does not search Drive for inbox folders.

Fallback evidence:

- Deterministic visual E2E: `evidence/sprint28_agent_inbox_drive_access/visual_e2e_readonly_r1/contact_sheet_readonly_r1.png`
- Live rclone result: `evidence/sprint28_agent_inbox_drive_access/live_readonly_rclone_package/RESULT.md`
- Live rclone screenshot: `evidence/sprint28_agent_inbox_drive_access/live_readonly_rclone_package/live_readonly_rclone_success.png`

Fallback result: PASS on the signed-in `qaApi36` emulator. After explicit Google read-only consent, the app scanned the pasted folder id and displayed the package uploaded by rclone as one package waiting for review.
