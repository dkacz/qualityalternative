# Sprint 36 Agent Inbox Live Drive Picker Evidence

Date: 2026-06-16

Final debug APK under test:

- Path: `app/build/outputs/apk/debug/app-debug.apk`
- SHA-256: `83544a7efce11141c48cca25bed5ffb6a8da9e1429565c9c074b2fe35ba71348`
- Version: `0.11.23-alpha`, versionCode `39`
- Metadata: `logs/live_debug_apk_metadata.txt`

Live device/account:

- Device: `emulator-5554`, `qaApi36(AVD) - 16`
- Google account: `omareth@gmail.com`
- Evidence: `logs/live_emulator_account_state.txt`

Real Google Drive source:

- Folder: `QA-Agent-Inbox-Live-E2E-20260616-173729`
- Link: `live_drive_folder_link.txt`
- Remote listing before import: `logs/rclone_remote_listing.txt`, `logs/rclone_remote_lsjson.json`
- Remote listing after final import: `logs/rclone_remote_listing_after_import.json`
- Package folder: `codex-live-drive-e2e-package`
- Package files: `manifest.json`, `content.md`
- Package validation: `logs/package_validator.log`

Live E2E result:

- Fresh install/data-clear, onboarding, Settings, real Google account authorization, in-app Drive folder browser, folder selection, package scan, import, Library visibility, and reader rendering all passed.
- Screenshot sequence: `live_e2e/00_app_initial.png` through `live_e2e/14_reader_opened_imported_drive_document.png`
- Screenshot index: `logs/live_e2e_screenshot_index.txt`
- XML milestone index: `logs/live_e2e_xml_milestones.txt`
- Full logcat: `logs/live_e2e_logcat.txt`
- App-related logcat filter: `logs/live_e2e_app_related_logcat.txt`
- Crash/error sentinel result: `logs/live_e2e_health_sentinels.txt`

Key visual proof:

- `live_e2e/08_after_choose_folder_tap.png`: Google Play Services account chooser for `omareth@gmail.com`.
- `live_e2e/09_after_google_account_selected.png`: in-app Drive folder browser showing `QA-Agent-Inbox-Live-E2E-20260616-173729` with the target row's `Open` and `Select` controls fully above bottom navigation.
- `logs/live_09_after_google_account_selected.xml`: target folder bounds `[346,1235][867,1343]`, target `Open` bounds `[348,1425][461,1477]`, target `Select` bounds `[677,1425][811,1477]`, bottom navigation labels at y `2194-2233`.
- `live_e2e/10_after_drive_folder_selected.png`: `Agent Inbox folder connected`, `1 package waiting for review`, `Live Drive Agent Inbox Test`, and a clean `Import` button after snackbar clearance.
- `logs/live_10_after_drive_folder_selected.xml`: `Import` bounds `[745,1826][889,1878]`, bottom navigation labels at y `2194-2233`.
- `live_e2e/11_after_import_tap.png`: queue cleared with `No packages waiting for review.`
- `live_e2e/13_library_files_filter_after_import.png`: Library `Files` shows `Live Drive Agent Inbox Test` as `Your file · Agent Inbox document`.
- `live_e2e/14_reader_opened_imported_drive_document.png`: reader renders the imported Markdown content.

Automated checks:

- Targeted unit tests passed:
  - `AndroidGoogleDriveAgentInboxClientTest`
  - `AndroidHybridAgentInboxDriveClientTest`
  - `MainViewModelTest`
- Connected visual test passed:
  - `VisualQaScreenshotTest#captureSprint35AgentInboxFolderSelectorRepairStates`
  - Result XML: `logs/r3_TEST-drive-folder-browser-visual.xml`
  - Log: `logs/r3_connected_drive_folder_browser_visual.log`
  - Latest visual screenshots: `visual_e2e/sprint35-agent-inbox-folder-selector-repair-1781630925520/`

Log notes:

- `logs/live_e2e_health_sentinels.txt` contains no app crash and no old Agent Inbox failure strings.
- Full logcat contains emulator/system noise from Google keyboard `EmojiCompat` and Android `MediaProvider`; these are not `Process: com.qualityalternative.app` crashes and did not block the flow.

Review history:

- Initial GPT Pro review result: `gpt_pro_review_response.md`, `SCORE: 8/10`, `VERDICT: FAIL`.
- The blocker was clipped `Open`/`Select` controls in the folder browser plus a snackbar obscuring the scan/import screenshot.
- The current evidence is a rerun after the layout fix and after waiting for snackbar clearance. The final APK hash is now `83544a7efce11141c48cca25bed5ffb6a8da9e1429565c9c074b2fe35ba71348`, not the earlier failed `2a8eac...` build.

Known UX note:

- The top Google Drive card is now labeled `Drive sync not connected` to avoid contradicting Agent Inbox Drive connection state. Agent Inbox itself shows `DRIVE` when the folder is selected.
