# Rclone + Picker Folder Spike

Purpose: prove whether the documented Google Picker folder grant under `drive.file` exposes Agent Inbox package folders added later by an external user-controlled tool such as rclone.

## Preconditions

- Install a debug or alpha build containing Sprint 28 R3 implementation commit `482478d` or later.
- Use a Google account authorized for the app's OAuth client.
- Prepare a Drive folder intended to be the Agent Inbox.
- Ensure the package producer can upload to that same folder through rclone.

## Procedure

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

If the scan reports an access-lost state or does not see a valid package that is visibly present in Drive, keep this sprint unreleased and evaluate the documented `drive.readonly` fallback path with a PRD/privacy update and GPT Pro review.

## Result

Pending. On 2026-06-14, Codex had a local `qaApi36` emulator for deterministic visual E2E and `rclone v1.73.2` available on macOS, but `adb shell cmd account list` returned no Google account on the emulator. The live spike still requires a signed-in production OAuth/rclone scenario that can prove whether a Picker-selected Drive folder under `drive.file` exposes child packages added later by rclone.
