# Validation Summary: v0.11.7-epub-loading-performance-alpha

## Build

- Version: `versionCode=23`, `versionName=0.11.7-alpha`
- APK: `release_artifacts/quality-alternative-v0.11.7-epub-loading-performance-alpha-debug.apk`
- SHA-256: `b6e5b93b66d1b9e44505ef04c8a549e26c7f7764fafc231fd8d9a89484eb68a4`

## Release Gate

- PASS: GPT Pro R3 review: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`.
- PASS: `testDebugUnitTest assembleDebugAndroidTest assembleDebug`.
- PASS: APK badging reports `versionCode=23` and `versionName=0.11.7-alpha`.
- PASS: APK signature verification reports `Verifies` and APK Signature Scheme v2 `true`.
- PASS: Emulator install reports `Success`.
- PASS: Installed package reports `versionCode=23` and `versionName=0.11.7-alpha`.
- PASS: Launch smoke captured `launch_smoke.png`.
- PASS: Emulator was shut down after validation; `adb_devices_after_emulator_shutdown.txt` shows no attached devices.

## Sprint 20 Evidence

- PASS: R3 unit evidence covers bounded EPUB retention, anchor stability, stale import cancellation, and stale reader-open cancellation.
- PASS: R3 connected visual evidence covers large EPUB import preparation and reader opening busy states.
- PASS: Package hygiene preserves the current R3 review bundle and Sprint 20 evidence trail.
