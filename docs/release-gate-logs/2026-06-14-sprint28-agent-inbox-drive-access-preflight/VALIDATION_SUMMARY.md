# Sprint 28 Agent Inbox Drive Access Preflight

Date: 2026-06-14

Branch: `codex/sprint28-agent-inbox-drive-access`

Head commit: `661fb36`

Status: PREP ONLY. This is not the final release gate and not an alpha publication record. Final release still waits for the signed-in live rclone/Picker spike documented in `evidence/sprint28_agent_inbox_drive_access/device_spike/LIVE_RCLONE_PICKER_SPIKE_RUNBOOK.md`.

## Context

GPT Pro R3 passed the deterministic implementation and visual lane with `SCORE 10/10`, `VERDICT PASS`, and `VISUAL REVIEW PASS`.

The remaining release blocker is live proof that a Google Picker-selected Drive folder under `drive.file` exposes package folders added later by rclone. The local emulator has Google Play services but no signed-in Google account, so that proof cannot be executed in this environment.

## Checks Run

- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` - PASS.
- APK copied to `release_artifacts/quality-alternative-sprint28-agent-inbox-drive-access-preflight-debug.apk`.
- SHA-256 recorded in `apk_sha256.txt`.
- `aapt dump badging` - PASS: `versionCode='31'`, `versionName='0.11.15-alpha'`.
- `apksigner verify --verbose --print-certs` - PASS: v2 signing verified with the Android debug certificate.
- `adb install -r` - PASS: `Success`.
- Explicit launch via `adb shell am start -n com.qualityalternative.app/.MainActivity` - PASS.
- Launch proof - PASS: `apk_install_evidence/launch_after_explicit_start_screenshot.png` shows the loaded app onboarding UI.
- Activity proof - PASS: `apk_install_evidence/dumpsys_activity_activities_after_explicit_launch.txt` records `topResumedActivity=ActivityRecord{... com.qualityalternative.app/.MainActivity ...}`.

## Important Non-Pass Items

- Full connected release gate was not rerun here; Sprint 28 already has focused connected visual E2E and full local unit/lint/build evidence, while final release gate is intentionally held until the live rclone/Picker spike passes.
- Android version was not bumped for this preflight. The APK still reports `versionCode=31`, `versionName=0.11.15-alpha`.
- No alpha publication was performed.

## Evidence Paths

- Gradle build log: `preflight_gradle_build.log`
- APK badging: `apk_badging.txt`
- APK signature verification: `apk_signature_verify_verbose.txt`
- Install and launch evidence: `apk_install_evidence/`
- Local ignored APK candidate: `release_artifacts/quality-alternative-sprint28-agent-inbox-drive-access-preflight-debug.apk`
- Tracked APK checksum: `apk_sha256.txt`
