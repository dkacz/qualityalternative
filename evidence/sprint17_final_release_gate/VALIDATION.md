# Sprint 17 Final Release Gate Validation

Release candidate: `v0.10.0-reader-settings-sync-polish-alpha`

Previous release baseline: `v0.9.0-portable-profile-alpha`

## Sprint 17 Commits

- `16b73ba` Define Sprint 17 reader settings polish contract
- `9b16111` Add Sprint 17 typography controls
- `713b2aa` Add Sprint 17 default sync destinations
- `cb813c4` Repair Google Drive authorization flow
- `a90ffe5` Add Sprint 17 adaptive reader pagination fit
- `5754bbe` Add cross-page annotation range controls
- `3dfd2c9` Improve annotation note surface sizing

The current release candidate also includes the final gate version bump to `versionCode=15`, `versionName=0.10.0-alpha`, and a test-fixture hardening change that keeps reader progress tests multi-page after adaptive pagination improvements.

## Per-Slice GPT Pro Gates

- Slice 17.0 R2: `reviews/slice17_0_contract_r2_gpt_pro.md` — `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`
- Slice 17.1 R2: `reviews/slice17_1_typography_r2_gpt_pro.md` — `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`
- Slice 17.2 R4: `reviews/slice17_2_defaults_r4_gpt_pro.md` — `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`
- Slice 17.3 R3: `reviews/slice17_3_drive_r3_gpt_pro.md` — `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`
- Slice 17.4 R20: `reviews/slice17_4_pagination_r20_gpt_pro.md` — `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`
- Slice 17.5 R2: `reviews/slice17_5_selection_r2_gpt_pro.md` — `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`
- Slice 17.6 R2: `reviews/slice17_6_note_surface_r2_gpt_pro.md` — `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`

## Final Local Validation

- PASS: `JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18 ./gradlew testDebugUnitTest`
- PASS: `JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18 ./gradlew connectedDebugAndroidTest`
- PASS: `JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18 ./gradlew assembleDebug`
- PASS: `apksigner verify --verbose --print-certs app/build/outputs/apk/debug/app-debug.apk`
- PASS: `adb install -r app/build/outputs/apk/debug/app-debug.apk`

Logs:

- `logs/full_unit_validation.log`
- `logs/full_connected_validation.log`
- `logs/connected_reader_progress_rerun.log`
- `logs/assemble_debug.log`
- `docs/release-gate-logs/2026-05-06-sprint17-reader-sync-polish/apk_badging.txt`
- `docs/release-gate-logs/2026-05-06-sprint17-reader-sync-polish/apksigner_verify.txt`
- `docs/release-gate-logs/2026-05-06-sprint17-reader-sync-polish/adb_install.txt`
- `docs/release-gate-logs/2026-05-06-sprint17-reader-sync-polish/adb_installed_version.txt`

## APK Candidate

- APK path: `release_artifacts/quality-alternative-v0.10.0-reader-settings-sync-polish-alpha-debug.apk`
- Package: `com.qualityalternative.app`
- `versionCode`: `15`
- `versionName`: `0.10.0-alpha`
- SHA-256: `581c3dfd69b54add1e74438b88a5ee20fdea180672bdf790a0ebcc0401ebfde9`
- Signature: Android Debug certificate, APK Signature Scheme v2 verified.
- Emulator install smoke: `Success`; installed package reports `versionCode=15`, `versionName=0.10.0-alpha`.

## Visual Evidence Index

- Typography controls: `docs/visual-qa/sprint17-slice17-1-typography/sprint17-typography-settings-1777999457068/*.png`
- Defaults and Settings IA: `docs/visual-qa/sprint17-slice17-2-default-destinations/sprint17-default-settings-1778004534886/*.png`
- Google Drive auth states: `docs/visual-qa/sprint17-slice17-3-drive-auth/sprint17-drive-auth-1778007685417/*.png`
- Adaptive pagination: `evidence/sprint17_slice17_4_adaptive_reader_pagination/screenshots/sprint17-adaptive-pagination-1778073394700/*.png`
- Cross-page annotation selection: `evidence/sprint17_slice17_5_cross_page_annotation_selection/screenshots/sprint17-cross-page-annotation-1778079063962/*.png`
- Annotation note surface sizing: `evidence/sprint17_slice17_6_annotation_surface_sizing/screenshots/sprint17-annotation-surface-1778083622313/*.png`

## Notes

- `sprint17_release_candidate.diff` is the working-tree diff versus `v0.9.0-portable-profile-alpha`, including the final version bump and release-gate test fixture hardening.
- The debug APK itself is intentionally excluded from the GPT Pro bundle because binary contents are not useful review material; the bundle includes badging, signature, install, version, and checksum evidence instead.
