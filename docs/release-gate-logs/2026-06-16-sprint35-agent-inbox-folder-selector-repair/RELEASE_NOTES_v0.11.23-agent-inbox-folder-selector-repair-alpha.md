# v0.11.23 Agent Inbox Folder Selector Repair Alpha

This alpha repairs Agent Inbox folder selection after the Google file Picker path proved insufficient for externally populated/rclone package folders.

## Changes

- Removed the Agent Inbox Google file Picker authorization mode from production routing.
- Restored Android `OpenDocumentTree` as the primary folder selector for Agent Inbox.
- When a selected folder is backed by Google Drive, scanning now requests explicit Drive read access and scans only the selected folder id through the Drive API path.
- Existing legacy `picker_folder` grants are treated as needing repair and routed to the readonly folder-link consent path instead of continuing through `drive.file`.
- Agent Inbox import now uses readonly Drive authorization for Drive-backed candidates and does not fall back to `drive.file`.
- Added portable repo-level authoring instructions for agents and clarified that package folders must be fully built and validated before upload.

## Artifact

- Debug APK: `release_artifacts/quality-alternative-v0.11.23-agent-inbox-folder-selector-repair-alpha-debug.apk`
- Release unsigned APK: `release_artifacts/quality-alternative-v0.11.23-agent-inbox-folder-selector-repair-alpha-release-unsigned.apk`

## Review Gate

- GPT Pro R3: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`.
- Connected visual E2E passed on `emulator-5554` / `qaApi36(AVD) - 16`.
