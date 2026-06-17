# Sprint 37 Agent Inbox Drive Authorization Repair Evidence

Date: 2026-06-17

## Failure Under Repair

- User-provided failing screenshot: `user_failure_screenshot_20260617_1616.jpg`.
- Failure shown there: Agent Inbox folder not selected, `GOOGLE DRIVE AUTHORIZATION HIT A GOOGLE PLAY SERVICES ERROR`, empty in-app `My Drive` panel with `No folders on this level.`, and snackbar `Agent Inbox connection failed.`

## Candidate APK Under Live Test

- APK: `app/build/outputs/apk/debug/app-debug.apk`
- Metadata: `logs/live_debug_apk_metadata.txt`
- SHA-256: `83696744691fa2c0fcc7a2bff93ac35c90d1212a283fbad0895025949766cee2`
- versionCode: `40`
- versionName: `0.11.24-alpha`

## Live Device / Account

- Device available to Codex: `emulator-5554`.
- Google account present on the emulator: `omareth@gmail.com`.
- Device/account evidence: `logs/live_adb_devices.txt`, `logs/live_emulator_account_state.txt`.
- The user's physical phone from the screenshot was not attached to ADB during this run, so the live proof is a signed-in emulator with real Google Play Services and real Google Drive.

## Real Google Drive Source

- Drive folder: `QA-Agent-Inbox-Sprint37-Auth-Repair-20260617-143008`
- Drive folder link: `live_drive_folder_link.txt`
- External package folder: `codex-sprint37-drive-auth-repair-package`
- Package files: `manifest.json`, `content.md`
- Package priority in manifest: `high`
- Package validation: `logs/package_validator.log`
- Remote listings before/after import: `logs/rclone_remote_listing.txt`, `logs/rclone_remote_lsjson.json`, `logs/rclone_remote_listing_after_import.json`

## Live E2E Result

The signed-in emulator flow passed on the current candidate APK:

1. Fresh app data after install: `live_e2e/live_e2e_00_app_initial.png` through `live_e2e/live_e2e_10_agent_inbox_before_choose.png`.
2. Real Google Play Services account chooser: `live_e2e/live_e2e_11_after_choose_folder_tap.png`.
3. In-app Drive folder browser showing the real Sprint 37 Drive folder with visible `Open` and `Select`: `live_e2e/live_e2e_12_after_google_account_selected.png`.
4. Folder selected and scan completed with `1 package waiting for review`: `live_e2e/live_e2e_13_after_drive_folder_selected.png`.
5. Manifest priority accepted: `live_e2e/live_e2e_14_priority_accepted.png`.
6. Import cleared the queue with `No packages waiting for review.`: `live_e2e/live_e2e_15_after_import_tap.png`.
7. Library `Files` shows `Sprint 37 Drive Auth Repair Test` as `Your file · Agent Inbox document` and `Priority`: `live_e2e/live_e2e_17_library_files_filter_after_import.png`.
8. Reader renders the imported Markdown body: `live_e2e/live_e2e_18_reader_opened_imported_drive_document.png`.

Machine-readable UI dumps for the same milestones are in `logs/live_*.xml`.

## Automated Checks

- Targeted unit tests: `logs/targeted_unit_tests.log`
- Connected visual regression test: `logs/connected_visual.log`
- Connected visual result XML: `logs/TEST-visual.xml`
- Visual regression screenshot proving the auth-failure panel no longer leaves an empty `My Drive`: `visual_e2e/sprint35-agent-inbox-folder-selector-repair-1781706377433/00a_agent_inbox_drive_authorization_failed_light.png`

## Log Health

- Full live logcat: `logs/live_e2e_logcat.txt`
- App-related logcat filter: `logs/live_e2e_app_related_logcat.txt`
- Sentinel result: `logs/live_e2e_health_sentinels.txt`
- Sentinel result passed for no app crash and no old failure strings:
  - `Package is missing manifest.json`
  - `Package could not be saved`
  - `Agent Inbox package could not be imported`
  - `Google Drive authorization hit a Google Play services error`
  - `Agent Inbox connection failed`

## Code Fix Summary

- `AGENT_INBOX_BROWSE_READONLY` no longer opens the in-app `My Drive` browser before authorization succeeds.
- Agent Inbox Drive authorization failure now closes and resets the folder browser state instead of leaving a blank root browser visible.
- Agent Inbox readonly connect/browse authorization no longer forces `Prompt.CONSENT`; Google Identity can reuse existing grants and only prompt when needed.
- Regression tests assert both the authorization request shape and the failure-state UI behavior.
