# Sprint 29 Agent Inbox Folder Selector Release Gate

Date: 2026-06-15

Release: `v0.11.17-agent-inbox-folder-selector-alpha`

Branch: `codex/sprint29-agent-inbox-folder-selector`

Android version:

- `versionCode=33`
- `versionName=0.11.17-alpha`

## Result

Release gate status: PASS.

GPT Pro R2 returned `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`, with no fresh findings and no bundle gaps for the Agent Inbox folder selector lane.

## Checks

- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` - PASS.
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew connectedDebugAndroidTest` - PASS: 138 tests, 0 skipped, 0 failed, build successful in 18m 18s.
- `aapt dump badging` - PASS: `versionCode='33'`, `versionName='0.11.17-alpha'`.
- `apksigner verify --verbose --print-certs` - PASS: v2 signing verified with the Android debug certificate.
- `adb install -r` - PASS: `Success`.
- Explicit launch via `adb shell am start -n com.qualityalternative.app/.MainActivity` - PASS.
- Launch proof - PASS: `apk_install_evidence/launch_screenshot.png` shows the loaded onboarding UI after explicit launch.
- Activity proof - PASS: `apk_install_evidence/dumpsys_activity_activities_after_launch.txt` records `topResumedActivity=... com.qualityalternative.app/.MainActivity ...`.
- Package proof - PASS: `apk_install_evidence/dumpsys_package.txt` records `versionCode=33` and `versionName=0.11.17-alpha`.

## Release Artifact

- APK: `release_artifacts/quality-alternative-v0.11.17-agent-inbox-folder-selector-alpha-debug.apk`
- SHA-256: `753362b76fdd0110fd15668a1215cbe6e1291b674efca9dc9c94e61c8d9b0fec`
- SHA file: `release_artifacts/quality-alternative-v0.11.17-agent-inbox-folder-selector-alpha-debug.apk.sha256`
- Badging: `apk_badging.txt`
- Signature: `apk_signature_verify_verbose.txt`
- Install and launch evidence: `apk_install_evidence/`

## Evidence Paths

- Final Gradle build log: `final_gradle_build.log`
- Final full connected log: `connected_debug_android_test.log`
- Final connected XML: `connected_debug_android_test.xml`
- Final launch screenshot: `apk_install_evidence/launch_screenshot.png`
- GPT Pro R2 output: `evidence/sprint29_agent_inbox_folder_selector/GPT_PRO_REVIEW_R2.md`
- Canonical visual E2E contact sheet: `evidence/sprint29_agent_inbox_folder_selector/visual_e2e_selector_r2/contact_sheet_selector_r2.png`

## Publication Status

Published to GitHub Releases.

- Release commit: `9e88d7c0b081b43ace7b01f54ecb35f9c5e34ae9`
- Release tag: `v0.11.17-agent-inbox-folder-selector-alpha`
- Release URL: `https://github.com/dkacz/qualityalternative/releases/tag/v0.11.17-agent-inbox-folder-selector-alpha`
- Published assets: `quality-alternative-v0.11.17-agent-inbox-folder-selector-alpha-debug.apk` and `quality-alternative-v0.11.17-agent-inbox-folder-selector-alpha-debug.apk.sha256`.
- Integration method: committed on `codex/sprint29-agent-inbox-folder-selector`, tagged the release commit, pushed branch and tag to `origin`, and published the GitHub release from these release notes.
