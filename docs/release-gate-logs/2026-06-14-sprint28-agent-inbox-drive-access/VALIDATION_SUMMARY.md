# Sprint 28 Agent Inbox Drive Access Release Gate

Date: 2026-06-14

Release: `v0.11.16-agent-inbox-drive-access-alpha`

Branch: `codex/sprint28-agent-inbox-drive-access`

Android version:

- `versionCode=32`
- `versionName=0.11.16-alpha`

## Result

Release gate status: PASS.

GPT Pro R5 returned `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`, with no fresh findings and no bundle gaps for the controlled read-only Agent Inbox fallback.

## Checks

- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` - PASS.
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew connectedDebugAndroidTest` - PASS: 138 tests, 0 skipped, 0 failed, build successful in 34m 58s.
- `aapt dump badging` - PASS: `versionCode='32'`, `versionName='0.11.16-alpha'`.
- `apksigner verify --verbose --print-certs` - PASS: v2 signing verified with the Android debug certificate.
- `adb install -r` - PASS: `Success`.
- Explicit launch via `adb shell am start -n com.qualityalternative.app/.MainActivity` - PASS.
- Launch proof - PASS: `apk_install_evidence/launch_screenshot.png` shows the loaded onboarding UI after explicit launch.
- Activity proof - PASS: `apk_install_evidence/dumpsys_activity_activities_after_launch.txt` records `topResumedActivity=... com.qualityalternative.app/.MainActivity ...`.
- Package proof - PASS: `apk_install_evidence/dumpsys_package.txt` records `versionCode=32` and `versionName=0.11.16-alpha`.

## Release Artifact

- APK: `release_artifacts/quality-alternative-v0.11.16-agent-inbox-drive-access-alpha-debug.apk`
- SHA-256: `acb460d2ca6e8e1129607eed43171464eef192f7e470f4ef82dcda7286e5841d`
- SHA file: `release_artifacts/quality-alternative-v0.11.16-agent-inbox-drive-access-alpha-debug.apk.sha256`
- Badging: `apk_badging.txt`
- Signature: `apk_signature_verify_verbose.txt`
- Install and launch evidence: `apk_install_evidence/`

## Evidence Paths

- Final Gradle build log: `final_gradle_build.log`
- Final full connected log: `connected_debug_android_test.log`
- Final launch screenshot: `apk_install_evidence/launch_screenshot.png`
- GPT Pro R5 output: `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_R5_EVIDENCE_HYGIENE.md`
- Canonical visual E2E contact sheet: `evidence/sprint28_agent_inbox_drive_access/visual_e2e_readonly_r1/contact_sheet_readonly_r1.png`
- Live rclone fallback proof: `evidence/sprint28_agent_inbox_drive_access/live_readonly_rclone_package/RESULT.md`

## Publication Status

The APK artifact is built and verified locally. Git commit/tag/push and GitHub release publication are pending as the integration step after this release gate.
