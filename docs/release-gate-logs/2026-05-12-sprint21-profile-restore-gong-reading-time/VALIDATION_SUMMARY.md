# Validation Summary: v0.11.8-profile-restore-gong-reading-time-alpha

## Build

- Version: `versionCode=24`, `versionName=0.11.8-alpha`
- APK: `release_artifacts/quality-alternative-v0.11.8-profile-restore-gong-reading-time-alpha-debug.apk`
- SHA-256: `ccd1f9385ea986565c9e16961880ca519f5aa56c6d38b67be7fde0affd5fcd0a`

## Release Gate

- PASS: GPT Pro Sprint 21 R6 review: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`.
- PASS: `testDebugUnitTest assembleDebugAndroidTest assembleDebug`.
- PASS: APK badging reports `versionCode=24` and `versionName=0.11.8-alpha`.
- PASS: APK signature verification reports `Verifies` and APK Signature Scheme v2 `true`.
- PASS: Emulator install reports `Success`.
- PASS: Installed package reports `versionCode=24` and `versionName=0.11.8-alpha`.
- PASS: Launch smoke captured `launch_smoke.png`.
- PASS: Emulator was shut down after validation; `adb_devices_after_emulator_shutdown.txt` shows no attached devices.

## Sprint 21 Evidence

- PASS: R6 GPT Pro review accepted the profile restore, Settings preview safety, MediaStore collision handling, meditation gong, and long reading-time behavior.
- PASS: R6 connected visual evidence covers onboarding restore, Settings default restore preview/confirmation/success, meditation completion, and long-document import preview.
- PASS: Package hygiene preserves the current R6 bundle and named review trail without raw harvest duplicates.
