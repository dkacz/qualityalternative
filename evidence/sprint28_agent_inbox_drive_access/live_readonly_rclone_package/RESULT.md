# Sprint 28 Live Readonly Rclone Result

Date: 2026-06-14

Branch: `codex/sprint28-agent-inbox-drive-access`

Device: `qaApi36` / `emulator-5554`

Build: `app/build/outputs/apk/debug/app-debug.apk`

## Setup

- Parent Drive folder created outside the app by rclone: `gdrive:QA_AGENT_INBOX_LIVE_SPIKE_20260614`
- Parent folder id pasted into the Android app: `<redacted-drive-folder-id>`
- Package folder uploaded by rclone: `rclone-readonly-package`
- Package folder id: `<redacted-drive-folder-id>`
- Manifest file id: `<redacted-drive-file-id>`
- Markdown file id: `<redacted-drive-file-id>`

## Result

PASS. In the live emulator flow, Codex drove the app through the controlled `drive.readonly` fallback consent and then Agent Inbox scanned the pasted folder id. The app found the rclone-created package and displayed `Rclone Readonly Inbox Smoke` as one package waiting for review.

The shipped proof for this result is the final app state after consent and scan: screenshot plus UI XML. The OAuth consent screen itself is not included because it exposed private account UI, and the filtered logcat captured after the run does not independently prove the consent flow.

## Evidence

- `rclone_listing_summary_redacted.md`: rclone-visible parent/package/file structure with raw Drive ids and owner removed.
- `live_readonly_rclone_success.png`: final Settings screenshot showing connected Agent Inbox and the rclone package.
- `window_live_readonly_success.xml`: UI dump proving the connected state and visible package title.
- `logcat_live_readonly_success.txt`: retained only as a raw post-run diagnostic; it is not used as proof of OAuth consent.
