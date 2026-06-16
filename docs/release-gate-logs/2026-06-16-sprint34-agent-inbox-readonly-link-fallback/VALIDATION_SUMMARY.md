# Sprint 34 Agent Inbox Readonly Link Fallback Validation

## Scope

Address GPT Pro R1 findings on the Sprint 33 Agent Inbox Drive file-picker hotfix before asking for a 10/10 re-review.

## R1 Findings Addressed

- The typed/manual readonly folder-id path is now reachable from Settings through a visible `Drive folder link or id` field and `Use Drive link` action.
- Authorization tests now assert the literal Google OAuth scope strings for `drive.file` and `drive.readonly`.
- The stale unreachable Google Drive document-tree post-persist branch was removed.
- The R2 review bundle should include the scope constants, concrete Drive client, Drive client tests, and Google Drive document-tree URI predicate source so those are no longer bundle gaps.

## Local Gates

- PASS: `JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18/libexec/openjdk.jdk/Contents/Home ./gradlew testDebugUnitTest lintDebug assembleRelease assembleDebug`
- PASS: `git diff --check`
- PASS: debug APK metadata reports `versionCode=38`, `versionName=0.11.22-alpha`.

## Artifacts

- APK: `release_artifacts/quality-alternative-v0.11.22-agent-inbox-readonly-link-fallback-alpha-debug.apk`
- SHA-256: `2bd452f4b37b5e92fa203940096474da7d35b092bb820d306e19e1bc2c280264`
- Build log: `docs/release-gate-logs/2026-06-16-sprint34-agent-inbox-readonly-link-fallback/final_gradle_build.log`
- ADB evidence: `docs/release-gate-logs/2026-06-16-sprint34-agent-inbox-readonly-link-fallback/adb_devices.txt`

## Connected Visual/E2E

Not run locally. `adb devices -l` reported no attached devices, and no local emulator binary was available in the standard SDK paths checked during the prior release gate.

## Residual Risk

The code now exposes both the supported `drive.file` Picker route and the controlled readonly typed-folder route. The remaining proof gap is live-device behavior on a signed-in Android device.
