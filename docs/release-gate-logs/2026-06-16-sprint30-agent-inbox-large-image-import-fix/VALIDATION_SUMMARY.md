# Sprint 30 Agent Inbox Large Image Import Fix Release Gate

Date: 2026-06-16

Release: `v0.11.18-agent-inbox-large-image-import-fix-alpha`

Branch: `codex/agent-inbox-large-image-import-fix`

Android version:

- `versionCode=34`
- `versionName=0.11.18-alpha`

## Result

Release gate status: PASS.

GPT Pro R2 returned `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW NOT APPLICABLE`, with no findings for the Sprint 30 lane.

## Checks

- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` - PASS.
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew connectedDebugAndroidTest` - PASS: 138 tests, 0 skipped, 0 failed, build successful in 18m 28s.
- `git diff --check` - PASS.
- `aapt dump badging` - PASS: `versionCode='34'`, `versionName='0.11.18-alpha'`.
- `apksigner verify --verbose --print-certs` - PASS: v2 signing verified with the Android debug certificate.
- `adb install -r` - PASS: `Success`.
- Explicit launch via `adb shell am start -n com.qualityalternative.app/.MainActivity` - PASS.
- Launch proof - PASS: `apk_install_evidence/launch_screenshot.png` shows the loaded onboarding UI after explicit launch.
- Activity proof - PASS: `apk_install_evidence/dumpsys_activity_activities_after_launch.txt` records `topResumedActivity=... com.qualityalternative.app/.MainActivity ...`.
- Package proof - PASS: `apk_install_evidence/dumpsys_package.txt` records `versionCode=34` and `versionName=0.11.18-alpha`.

## Release Artifact

- APK: `release_artifacts/quality-alternative-v0.11.18-agent-inbox-large-image-import-fix-alpha-debug.apk`
- SHA-256: `705c344ade36cd96753183c967f46908a647c4d3310c78f10adb268f0047ab8b`
- SHA file: `release_artifacts/quality-alternative-v0.11.18-agent-inbox-large-image-import-fix-alpha-debug.apk.sha256`
- Badging: `apk_badging.txt`
- Signature: `apk_signature_verify_verbose.txt`
- Install and launch evidence: `apk_install_evidence/`

## Evidence Paths

- Final Gradle build log: `final_gradle_build.log`
- Final full connected log: `connected_debug_android_test.log`
- Final connected XML: `connected_debug_android_test.xml`
- Final launch screenshot: `apk_install_evidence/launch_screenshot.png`
- GPT Pro R1 output: `evidence/sprint30_agent_inbox_large_image_import_fix/review/GPT_PRO_REVIEW_R1.md`
- GPT Pro R2 output: `evidence/sprint30_agent_inbox_large_image_import_fix/review/GPT_PRO_REVIEW_R2.md`

## Publication Status

Prepared for GitHub release publication.

- Release tag: `v0.11.18-agent-inbox-large-image-import-fix-alpha`
- Expected release URL: `https://github.com/dkacz/qualityalternative/releases/tag/v0.11.18-agent-inbox-large-image-import-fix-alpha`
- Integration method: commit this release gate on `codex/agent-inbox-large-image-import-fix`, tag the release commit, push branch and tag to `origin`, and publish the GitHub release from these release notes.
