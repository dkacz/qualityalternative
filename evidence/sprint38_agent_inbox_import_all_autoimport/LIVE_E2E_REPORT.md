# Sprint 38 Agent Inbox Import All + Autoimport Evidence

Date: 2026-06-17

## Candidate Under Test

- Branch: `codex/agent-inbox-import-all-autoimport`
- Version: `versionCode=41`, `versionName=0.11.25-alpha`
- Debug APK under live test: `app/build/outputs/apk/debug/app-debug.apk`
- Debug APK SHA-256: `d68a4619a047bfe2944c90dceb34eba705a3f01451a9e105faca842346a56504`
- Release APK candidate: `apk/app-release-unsigned.apk`
- Release APK SHA-256: `2c94a4aae0cb91b151f0583971f1e7114bc4c7bad6ddae7a852e7a93184d1e4c`

## Live Device And Account

- Device: `emulator-5554`
- Google account present on emulator: `omareth@gmail.com`
- Evidence: `logs/live_adb_devices.txt`, `logs/live_emulator_account_state.txt`

## Real Google Drive Source

- Drive folder: `QA-Agent-Inbox-Sprint38-Import-All-Autoimport-20260617-182500`
- Drive folder link: `live_drive_folder_link.txt`
- External package folders:
  - `codex-sprint38-import-all-one`
  - `codex-sprint38-import-all-two`
  - `codex-sprint38-autoimport-startup`
- Each package contains direct `manifest.json` and `content.md`.
- Package validators:
  - `logs/package_validator_import_all_one.log`
  - `logs/package_validator_import_all_two.log`
  - `logs/package_validator_autoimport_startup.log`
- Remote listings:
  - `logs/rclone_remote_listing_initial.txt`
  - `logs/rclone_remote_listing_after_autoimport_upload.txt`
  - `logs/rclone_remote_listing_final.txt`

## Live Import All Result

The signed-in emulator flow proved batch import from real Drive:

1. Fresh onboarding and Settings path: `live_e2e/live_e2e_00_app_initial.png` through `live_e2e/live_e2e_07g_agent_inbox_button_visible.png`.
2. Real Google Play Services account chooser: `live_e2e/live_e2e_10_after_choose_folder_visible_tap.png`.
3. In-app Drive folder browser showing the real Sprint 38 folder with `Open` and `Select`: `live_e2e/live_e2e_11_after_google_account_selected.png`.
4. Folder selected and scan completed with `2 packages waiting for review` and visible `Import all`: `live_e2e/live_e2e_12_after_drive_folder_selected_scan.png`.
5. Queue screenshots show both external package titles and manifest metadata:
   - `live_e2e/live_e2e_13_import_all_queue_scrolled.png`
   - `live_e2e/live_e2e_14_import_all_queue_high_priority.png`
6. `Sprint 38 Import All One` shows `PRIORITY REQUESTED`; it was not accepted before batch import.
7. After `Import all`, the queue is empty: `live_e2e/live_e2e_15_after_import_all_tap.png`.
8. Library `Files` shows both imported documents as `Your file · Agent Inbox document`: `live_e2e/live_e2e_17_library_files_after_import_all.png`.
9. Reader renders the imported Drive Markdown body for `Sprint 38 Import All Two`: `live_e2e/live_e2e_18_reader_import_all_two.png`.

Matching UIAutomator XML files are in `logs/live_*.xml`.

## Live Autoimport Result

The signed-in emulator flow proved opt-in autoimport on app start:

1. Autoimport was explicitly enabled after Agent Inbox folder connection: `live_e2e/live_e2e_22_autoimport_toggle_on.png`.
2. The new package `codex-sprint38-autoimport-startup` was uploaded to Drive only after autoimport was enabled; see `logs/rclone_upload_autoimport_startup.log` and `logs/rclone_remote_listing_after_autoimport_upload.txt`.
3. The app was force-stopped and relaunched using `logs/adb_launch_autoimport_restart.log`.
4. On relaunch, no Google chooser/consent screen appeared; Home showed `Agent Inbox autoimport imported 1 package.`: `live_e2e/live_e2e_23_after_autoimport_restart.png`.
5. Library `Files` shows `Sprint 38 Autoimport Startup` as `Your file · Agent Inbox document`: `live_e2e/live_e2e_25_library_files_after_autoimport_restart.png`.
6. Reader renders the autoimported Drive Markdown body: `live_e2e/live_e2e_26_reader_autoimport_startup.png`.

## Automated Checks

- Targeted unit tests: `logs/targeted_unit_tests.log`
- Unit XML:
  - `logs/TEST-MainViewModelTest.xml`
  - `logs/TEST-GoogleDriveAuthorizationTest.xml`
- Connected visual test: `logs/connected_visual.log`
- Connected visual XML: `logs/TEST-connected-visual.xml`
- Fresh connected visual screenshots: `visual_e2e_latest/sprint35-agent-inbox-folder-selector-repair-1781716267384/`
- Full local release gate: `logs/local_release_gate.log`

## Log Health

- Full live logcat: `logs/live_e2e_logcat.txt`
- App-related logcat filter: `logs/live_e2e_app_related_logcat.txt`
- Sentinel result: `logs/live_e2e_health_sentinels.txt`
- Sentinel result reports no fatal exception, no app process crash marker, no old Agent Inbox failure strings, and no Drive authorization failure string.

## GPT Pro Review

- Review URL: `gpt_pro_review_url_r1.txt`
- Review response: `gpt_pro_review_response_r1.md`
- Result: `SCORE: 10/10`, `VERDICT: PASS`, `BLOCKERS: None`.

## Release Artifacts

- Installable debug APK: `../../release_artifacts/quality-alternative-v0.11.25-agent-inbox-import-all-autoimport-alpha-debug.apk`
- Unsigned release APK: `../../release_artifacts/quality-alternative-v0.11.25-agent-inbox-import-all-autoimport-alpha-release-unsigned.apk`
