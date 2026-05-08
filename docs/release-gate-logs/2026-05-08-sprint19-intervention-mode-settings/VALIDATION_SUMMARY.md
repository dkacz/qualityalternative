# Sprint 19 Intervention Mode Settings Release Gate

Release candidate: `v0.11.4-intervention-mode-settings-alpha`

Android package: `com.qualityalternative.app`

Version: `versionCode 20`, `versionName 0.11.4-alpha`

Previous release: `v0.11.3-session-progress-meditation-alpha`

## GPT Pro Gate

- Review: Sprint 19 Slice 19.5B Intervention Mode Settings
- URL: https://chatgpt.com/c/69fdcdba-2918-8394-8d5d-0d7d736b0b6f
- Harvested file: `evidence/sprint19_intervention_mode_settings/GPT_PRO_REVIEW.md`
- Result: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`
- Blockers: None

## Validation

- JVM unit tests, debug APK build, and Android test APK build: `testDebugUnitTest_assembleDebug_androidTest.log`, `BUILD SUCCESSFUL`
- Connected Settings Mode visual E2E: `connected_intervention_mode.log`, `BUILD SUCCESSFUL`
- APK badging: `apk_badging.txt`, `versionCode='20'`, `versionName='0.11.4-alpha'`
- APK signature: `apk_signature.txt`, Android debug signer verified
- Emulator install smoke: `adb_install.log`, `Success`
- Emulator launch smoke: `adb_start_activity.log` and `adb_launch_focus.txt`, `com.qualityalternative.app/.MainActivity`

## APK

- Artifact: `release_artifacts/quality-alternative-v0.11.4-intervention-mode-settings-alpha-debug.apk`
- SHA-256: `fc139465669db35f7a8c4744a54b4fe637381236f00708c36b7754583e1c7e71`
- SHA file: `release_artifacts/quality-alternative-v0.11.4-intervention-mode-settings-alpha-debug.apk.sha256`

## Changes Versus v0.11.3

- Settings Mode now reflects and persists the actual Soft/Firm intervention behavior.
- Soft mode provides immediate Open anyway.
- Firm mode preserves the visible five-second wait.
- Portable profile export/import includes the intervention mode.
- Analytics metadata distinguishes Soft and Firm behavior.
- AI note assistance remains intentionally excluded until after this APK release.
