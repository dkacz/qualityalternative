# Sprint 38 Review Bundle Manifest

Purpose: adversarial GPT Pro review of Agent Inbox `Import all` and opt-in autoimport on app startup.

Required first-read files:

- `docs/AGENT_INBOX_LIVE_REVIEW_GATE.md`
- `PRD.md`
- `evidence/sprint38_agent_inbox_import_all_autoimport/LIVE_E2E_REPORT.md`
- `evidence/sprint38_agent_inbox_import_all_autoimport/source_diff.patch`
- `evidence/sprint38_agent_inbox_import_all_autoimport/logs/live_e2e_health_sentinels.txt`

Key evidence:

- `live_e2e/`: signed-in emulator screenshots from fresh onboarding through real Drive folder selection, batch import, Library, reader, autoimport restart, Library, and reader.
- `logs/live_*.xml`: UIAutomator dumps matching the live screenshots.
- `live_drive_package/`: local source package folders uploaded externally to Drive.
- `logs/package_validator_*.log`: validator output for each package.
- `logs/rclone_remote_listing*.txt`, `logs/rclone_remote_lsjson*.json`: remote Drive state.
- `logs/targeted_unit_tests.log`, `logs/TEST-MainViewModelTest.xml`, `logs/TEST-GoogleDriveAuthorizationTest.xml`: targeted unit evidence.
- `logs/connected_visual.log`, `logs/TEST-connected-visual.xml`, `visual_e2e_latest/`: connected visual test evidence.
- `logs/local_release_gate.log`: `testDebugUnitTest`, `lintDebug`, and `assembleRelease`.
- `logs/apk_hashes.txt`, `apk/app-release-unsigned.apk`: release APK candidate and SHA-256.

Known limitation:

- The user physical phone was not attached to ADB. The live proof uses the signed-in emulator with real Google Play Services, a real Google account, and a real Google Drive folder/package workflow.
