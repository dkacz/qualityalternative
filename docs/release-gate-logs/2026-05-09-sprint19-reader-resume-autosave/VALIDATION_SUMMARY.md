# Validation Summary: v0.11.6-reader-resume-autosave-alpha

## Build

- Version: `versionCode=22`, `versionName=0.11.6-alpha`
- APK: `release_artifacts/quality-alternative-v0.11.6-reader-resume-autosave-alpha-debug.apk`
- SHA-256: `25200016fd5cab9c5158e85755c91098713eecf62e25f8b0aa0336a76d659c50`

## Release Gate

- PASS: GPT Pro R4 review: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`.
- PASS: `testDebugUnitTest assembleDebugAndroidTest assembleDebug`.
- PASS: APK badging reports `versionCode=22` and `versionName=0.11.6-alpha`.
- PASS: APK signature verification reports `Verifies` and APK Signature Scheme v2 `true`.
- PASS: Emulator install reports `Success`.
- PASS: Installed package reports `versionCode=22` and `versionName=0.11.6-alpha`.
- PASS: Launch smoke captured `launch_smoke.png`.
- PASS: Emulator was shut down after validation; `adb_devices_after_emulator_shutdown.txt` shows no attached devices.

## Prior Hotfix Evidence

- PASS: R4 connected regression: 4 tests, 0 failures.
- PASS: R4 unit evidence: 111 `MainViewModelTest` cases, 0 failures.
- PASS: R4 visual evidence covers stable resume and pending-write immediate reopen.
