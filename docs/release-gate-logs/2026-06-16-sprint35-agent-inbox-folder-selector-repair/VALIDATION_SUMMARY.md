# Sprint 35 Agent Inbox Folder Selector Repair Validation

## Scope

Repair Agent Inbox folder selection without removing the feature. Google file Picker is no longer used for Agent Inbox folder selection; Android folder selection is primary, and Drive-backed selections scan through explicit readonly Drive access limited to the selected folder id.

## Local Gates

- `testDebugUnitTest`
- `lintDebug`
- `assembleRelease`
- `assembleDebug`
- `git diff --check`
- Targeted unit coverage for Google Drive authorization, MainViewModel Agent Inbox state transitions, preferences persistence, hybrid document-tree/Drive scanning, and Google Drive Agent Inbox client behavior.
- Connected screenshot E2E: `VisualQaScreenshotTest#captureSprint35AgentInboxFolderSelectorRepairStates` on `emulator-5554` / `qaApi36(AVD) - 16`.
- Debug APK install passed on `emulator-5554` / `qaApi36(AVD) - 16`; `dumpsys package` reports `versionCode=39` and `versionName=0.11.23-alpha`.
- Direct launch intent for `com.qualityalternative.app/.MainActivity` returned successfully after install.
- GPT Pro R2 finding addressed: legacy `picker_folder` grants are now repair-only below the UI as well as in the visible Compose path.

## Evidence

- Final build log: `final_gradle_build.log`
- Sanitized final build log for reviewer packet: `final_gradle_build_sanitized.log`
- Connected screenshot XML: `../../../../evidence/sprint35_agent_inbox_folder_selector_repair/logs/TEST-sprint35-agent-inbox-folder-selector-repair.xml`
- Connected screenshot logcat: `../../../../evidence/sprint35_agent_inbox_folder_selector_repair/logs/logcat-sprint35-agent-inbox-folder-selector-repair.txt`
- Screenshot contact sheet: `../../../../evidence/sprint35_agent_inbox_folder_selector_repair/visual_e2e/contact_sheet_sprint35_agent_inbox_folder_selector_repair.png`
- APK hashes:
  - `apk_debug_sha256.txt`
  - `apk_release_unsigned_sha256.txt`
- APK metadata:
  - `apk_debug_badging.txt`
- Unit XML reports:
  - `unit-test-results/`
- Install/launch evidence:
  - `adb_install_debug.status.txt`
  - `dumpsys_package_after_install.txt`
  - `adb_direct_launch_after_install.txt`

## Review Gate

GPT Pro R3 returned `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS`.
