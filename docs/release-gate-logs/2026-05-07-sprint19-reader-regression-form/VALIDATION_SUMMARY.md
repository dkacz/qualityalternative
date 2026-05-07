# Sprint 19 Reader Regression And Form Intervention Release Validation

Release candidate: `v0.11.2-reader-regression-form-alpha`

Previous release: `v0.11.1-reader-progress-hotfix-alpha`

Branch: `codex/sprint19-ai-note-assist-plan`

## GPT Pro Gate

- Sprint 19 regression gate R2: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`
- Harvested review: `PRO_REVIEW_OUTPUT_SPRINT19_REGRESSION_R2_20260507_2305/Sprint_19_Review_Gate.md`
- Evidence copy: `evidence/sprint19_reader_regression_form_intervention/GPT_PRO_REVIEW_R2.md`
- Sprint 19 final release gate: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`
- Final release gate harvest: `PRO_REVIEW_OUTPUT_SPRINT19_FINAL_RELEASE_GATE_20260507_2325/Sprint_19_Review.md`
- Final release gate evidence copy: `evidence/sprint19_final_release_gate/FINAL_GPT_PRO_REVIEW.md`

## Validation

- Unit validation:
  `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:testDebugUnitTest --rerun-tasks`
- Build validation:
  `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:assembleDebug :app:assembleDebugAndroidTest`
- Connected reader/progress/annotation E2E:
  `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#sprint19EpubProgressAndAnnotationStartStayAnchoredInLaterChapter`
- Connected form-intervention E2E:
  `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#sprint19FormInterventionShowsFiveSecondUnlockBeforeOpenAnyway`

All listed validation commands passed.

## APK

- Asset: `quality-alternative-v0.11.2-reader-regression-form-alpha-debug.apk`
- Package: `com.qualityalternative.app`
- APK `versionCode`: `18`
- APK `versionName`: `0.11.2-alpha`
- SHA-256: `a027802ed0f648be722cb41136ed30bf0879c939de6023b86f5bb1d85c2e75b3`

## Signature, Install, And Launch

- `apksigner verify --verbose --print-certs` passed.
- APK Signature Scheme v2: `true`
- Signer: Android Debug certificate
- Emulator install smoke: `adb install -r release_artifacts/quality-alternative-v0.11.2-reader-regression-form-alpha-debug.apk` returned `Success`.
- Installed package reported `versionCode=18`, `versionName=0.11.2-alpha`.
- Launch smoke focused `com.qualityalternative.app/.MainActivity`.
- Emulator was shut down immediately after install/version/launch smoke to free RAM.

## Changelog Versus `v0.11.1-reader-progress-hotfix-alpha`

- Fixed EPUB chapter source indexing so later chapter annotation/progress anchors no longer collide with beginning-of-book indexes.
- Proved Chapter Three progress displays as a later-material percent and remains stable across reader text size repagination.
- Proved annotation start-backward movement can cross chapters without jumping to the book start, then save and reopen the selected quote.
- Added a visible 5-second form-intervention wait before `Open anyway`, with disabled open/close during the wait and analytics for unlock and abandonment.
- Preserved the AI boundary: no AI note-assist implementation or provider configuration is included in this APK.

## Evidence Files

- `unit_debug_rerun.log`
- `assemble_debug.log`
- `connected_reader_annotation.log`
- `connected_form_intervention.log`
- `apk_badging.txt`
- `apk_signature.txt`
- `apk_sha256.txt`
- `adb_install.log`
- `adb_installed_package.txt`
- `adb_launch_focus.txt`
- `adb_devices_after_emulator_shutdown.txt`
- `launch_smoke.png`
