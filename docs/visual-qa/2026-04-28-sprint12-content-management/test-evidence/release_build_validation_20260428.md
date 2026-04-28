# Release Build Validation

Release: `v0.6.0-content-management-alpha`

Timestamp: 2026-04-28 14:03 Europe/Warsaw

## Command

- `export JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18/libexec/openjdk.jdk/Contents/Home && ./gradlew testDebugUnitTest connectedDebugAndroidTest assembleDebug assembleRelease`

## Result

- Gradle result: `BUILD SUCCESSFUL in 5m 23s`
- Gradle tasks: 129 actionable tasks, 51 executed, 78 up-to-date
- Connected Android result: `Finished 64 tests on qaApi36(AVD) - 16`
- Unit XML: 188 tests, 0 failures, 0 errors, 0 skipped
- Connected Android XML: 64 tests, 0 failures, 0 errors, 0 skipped

## APK Metadata

- Debug APK versionCode: 7
- Debug APK versionName: `0.6.0-alpha`
- Release APK versionCode: 7
- Release APK versionName: `0.6.0-alpha`
- Release build output: `app-release-unsigned.apk`
- Published installable alpha APK output: `app-debug.apk`
- GitHub Release publishes only the installable alpha APK and its checksum, matching previous alpha-release convention.

## Evidence Files

- Unit XML: `release-test-results/unit/`
- Connected Android XML: `release-test-results/android/`
- Debug APK metadata: `apk-metadata/debug-output-metadata.json`
- Release APK metadata: `apk-metadata/release-output-metadata.json`
