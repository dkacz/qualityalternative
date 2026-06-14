# v0.11.16 Agent Inbox Drive Access Alpha

This alpha fixes the Agent Inbox Drive access gap for packages created outside the app by tools such as rclone, Codex, or Claude Code, and completes the Markdown image attachment path for manual imports and Agent Inbox Markdown packages.

## Highlights

- Added a controlled Agent Inbox Drive fallback: paste a Drive folder URL/id, grant explicit read-only Drive consent, and scan only that saved folder id.
- Preserved the historical Picker-folder `drive.file` path while preventing legacy app-created folder state from hydrating as connected without a durable grant marker.
- Stopped Agent Inbox scans from silently creating or discovering folders; inaccessible folders now become finite reconnect states.
- Added live rclone evidence proving the app can see a package uploaded outside the app after read-only consent.
- Fixed Markdown image follow-up selection so images can be added after a Markdown file is already selected.
- Added bounded Agent Inbox Markdown sidecar images with reviewed filenames, size limits, rollback cleanup, and reader rendering.
- Kept privacy boundaries: raw Drive ids, file ids, package ids, content names, tokens, and raw failures stay out of Portable Profile and remote-safe analytics.

## Validation

- GPT Pro R5: `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`.
- Full connected Android gate before release bump: `connectedDebugAndroidTest` PASS, 138 tests, 0 failures.
- Final release-gate validation is recorded in this folder's `VALIDATION_SUMMARY.md`.

## Artifact

- APK: `quality-alternative-v0.11.16-agent-inbox-drive-access-alpha-debug.apk`
