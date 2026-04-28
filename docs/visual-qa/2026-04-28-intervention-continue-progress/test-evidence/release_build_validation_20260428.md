# Release Build Validation: v0.6.1-intervention-progress-alpha

Date: 2026-04-28

## Commands

- `git diff --check`
  - Result: PASS
- `JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18/libexec/openjdk.jdk/Contents/Home ./gradlew testDebugUnitTest connectedDebugAndroidTest assembleDebug`
  - Result: PASS
  - Build time: 5m 2s
- `adb install -r release_artifacts/v0.6.1-intervention-progress-alpha/quality-alternative-v0.6.1-intervention-progress-alpha-debug.apk`
  - Result: PASS

## Test Summary

- Android connected tests: 65 tests, 0 failures, 0 errors, 0 skipped.
- MainActivityTest: 20 tests, 0 failures.
- VisualQaScreenshotTest: 7 tests, 0 failures.
- DefaultRecommendationEngineTest: 14 tests, 0 failures.

## APK

- Path: `release_artifacts/v0.6.1-intervention-progress-alpha/quality-alternative-v0.6.1-intervention-progress-alpha-debug.apk`
- SHA-256: `ec1547f4b976ccfd814d856eeb72c94d6965b19bc613a1ac053d1703c7110912`
- versionCode: 8
- versionName: `0.6.1-alpha`
- applicationId: `com.qualityalternative.app`
- Signing: Android debug certificate, v2 signature verified.

