# Sprint 27 Agent Content Inbox Release Gate

Date: 2026-06-12

Release: `v0.11.15-agent-content-inbox-alpha`

Android version:

- `versionCode=31`
- `versionName=0.11.15-alpha`

## Result

Release gate status: PASS.

GPT Pro R10 returned `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`, with no fresh findings, no bundle gaps, and package hygiene clean enough for release-gate audit.

## Checks

- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` - PASS.
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew connectedDebugAndroidTest` - PASS on R2: 137 tests, 0 skipped, 0 failed, build successful in 48m 21s.
- `aapt dump badging` - PASS: `versionCode='31'`, `versionName='0.11.15-alpha'`.
- `apksigner verify --verbose --print-certs` - PASS: v2 signing verified, debug certificate recorded.
- `adb install -r` - PASS: `Success`.
- Launch proof - PASS: `MainActivity` launched and `launch_after_load_screenshot.png` shows the loaded onboarding UI after the splash screen.
- Emulator shutdown proof - PASS: `adb_emu_kill.log` recorded `OK: killing emulator`, and `adb_devices_after_shutdown.txt` contains no attached device rows.

## Superseded Connected Gate

The first full connected run is retained on disk as a superseded failure log:

- `connected_debug_android_test.log`
- `connected_debug_android_test.status.txt`

It failed 3/137 after the release-version bump:

- `MainActivityTest.readerAnnotationControlsExpandAndReopenAcrossPages`
- `MainActivityTest.annotationDriveSyncSettingsShowsConnectFailureConnectedAndRetryStates`
- `VisualQaScreenshotTest.captureSprint22ReadingTimeRemainingRepair`

Follow-up evidence:

- `connected_debug_android_test_failed_targeted_rerun.log` proved the two `MainActivityTest` failures were repaired.
- `connected_debug_android_test_sprint22_rerun.log` proved the Sprint 22 visual timeout repair passed.
- `connected_debug_android_test_r2.log` is the canonical final full connected gate and passed 137/137.

## Release Artifact

- APK: `release_artifacts/quality-alternative-v0.11.15-agent-content-inbox-alpha-debug.apk`
- SHA-256: `10f2d54f7dc06c561afa32a83bcc7c5790c211f17cd320d469d93e6c957278f6`
- SHA file: `release_artifacts/quality-alternative-v0.11.15-agent-content-inbox-alpha-debug.apk.sha256`
- Badging: `apk_badging.txt`
- Signature: `apk_signature_verify_verbose.txt`
- Install and launch evidence: `apk_install_evidence/`

## Evidence Paths

- Final Gradle build log: `final_gradle_build.log`
- Final full connected log: `connected_debug_android_test_r2.log`
- Final loaded launch screenshot: `apk_install_evidence/launch_after_load_screenshot.png`
- Emulator shutdown log: `adb_emu_kill.log`
- Devices after shutdown: `adb_devices_after_shutdown.txt`
- GPT Pro R10 output: `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R10.md`
- Canonical visual E2E contact sheet: `evidence/sprint27_agent_content_inbox/visual_e2e/sprint27_agent_inbox_contact_sheet.png`
