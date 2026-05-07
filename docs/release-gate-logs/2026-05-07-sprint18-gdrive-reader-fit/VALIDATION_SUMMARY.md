# Sprint 18 Google Drive Reader Fit Release Validation

Release candidate: `v0.11.0-gdrive-reader-fit-alpha`

Previous release: `v0.10.0-reader-settings-sync-polish-alpha`

Branch: `codex/sprint18-gdrive-selection-regression-fixes`

Implementation commit: `045919a`

## GPT Pro Gate

- Sprint 18 R3: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`
- R3 confirmed Google Drive E2E, annotation selection, reader pagination, release readiness, no bundle gaps, and clean-enough package hygiene.

## Validation

- Full unit validation and full Android validation:
  `JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18 ./gradlew :app:testDebugUnitTest :app:connectedDebugAndroidTest`
- Canonical connected Android result: 105/105 tests passed on `qaApi36(AVD) - 16`
- Targeted annotation editor regression rerun passed after updating the test to match compact range controls.
- One earlier full connected run failed because the emulator went offline during Visual QA; the app test suite was rerun on the same AVD using software rendering and passed fully.
- Debug APK build:
  `JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18 ./gradlew :app:assembleDebug`

## APK

- Asset: `quality-alternative-v0.11.0-gdrive-reader-fit-alpha-debug.apk`
- Package: `com.qualityalternative.app`
- APK `versionCode`: `16`
- APK `versionName`: `0.11.0-alpha`
- SHA-256: `879b1c89c7dbb825c738913b98f0a19efeab8899c21559b3657ebec3b4c5ba2d`

## Signature And Install

- `apksigner verify --verbose --print-certs` passed.
- APK Signature Scheme v2: `true`
- Signer: Android Debug certificate
- Emulator install smoke: `adb install -r app/build/outputs/apk/debug/app-debug.apk` returned `Success`.
- Installed package reported `versionCode=16`, `versionName=0.11.0-alpha`.
- Emulator was shut down immediately after install/version smoke to free RAM.

## Changelog Versus `v0.10.0-reader-settings-sync-polish-alpha`

- Google Drive annotation sync now has a proven live authorization and write/readback path, including rclone verification of the JSON-LD annotation note.
- Google Drive `Connect` no longer routes OAuth cancellation into the Android folder picker; local destination change remains separate.
- Onboarding no longer advertises account login while no account system exists.
- Annotation selection preserves stable source block indexes so start can move backward across source/page boundaries.
- Reader pagination now uses measured rendered text height and keeps visible text above the footer, including compact viewport and code-heavy cases.
- Sprint 18 release evidence includes GPT Pro R3 10/10 review, current connected logs, visual screenshots, APK signature proof, and install smoke.

