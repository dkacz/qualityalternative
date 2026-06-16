# Sprint 33 Agent Inbox Drive File Picker Validation

## Scope

Fix the device-reported Google Play Services `INTERNAL_ERROR` shown when reconnecting Agent Inbox to a Google Drive folder after `v0.11.20-agent-inbox-drive-picker-alpha`.

## Local Gates

- PASS: `JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18/libexec/openjdk.jdk/Contents/Home ./gradlew testDebugUnitTest lintDebug assembleRelease assembleDebug`
- PASS: `git diff --check`
- PASS: debug APK metadata reports `versionCode=37`, `versionName=0.11.21-alpha`.

## Artifacts

- APK: `release_artifacts/quality-alternative-v0.11.21-agent-inbox-drive-file-picker-alpha-debug.apk`
- SHA-256: `8390cf0fb2a09c0301e11cb9f850a0da3031e2c42f24db6ad8b76064b07760da`
- Build log: `docs/release-gate-logs/2026-06-16-sprint33-agent-inbox-drive-file-picker/final_gradle_build.log`
- ADB evidence: `docs/release-gate-logs/2026-06-16-sprint33-agent-inbox-drive-file-picker/adb_devices.txt`

## Connected Visual/E2E

Not run locally. `adb devices -l` reported no attached devices, and the local machine did not expose an Android emulator binary in the standard SDK paths checked during the release gate.

## Residual Risk

The APK should remove the Play Services `INTERNAL_ERROR` caused by combining `drive.readonly` with Google Picker folder parameters. If the picker opens but packages still do not appear, the next likely issue is Drive `drive.file` visibility for externally created/rclone-added folder children, which needs a separate device-confirmed slice.
