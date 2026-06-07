# Sprint 26 Final Release Gate Validation Summary

Release candidate: `v0.11.14-custom-targets-website-interventions-alpha`

Generated: 2026-06-07 23:21:32 CEST

## Scope

Sprint 26 adds configurable installed-app targets outside the standard distractor list and supported Chrome website/domain interventions. The release gate also verifies bedtime integration, soft/firm preservation, privacy-safe analytics/profile export behavior, screenshot evidence, and APK install readiness.

## Version And APK

- Android `versionCode`: `30`
- Android `versionName`: `0.11.14-alpha`
- APK: `release_artifacts/quality-alternative-v0.11.14-custom-targets-website-interventions-alpha-debug.apk`
- APK SHA-256: `0d863923fc39be5ef9032a13c1d312ed9ceca74ccb2130eb362e38b63bdf77bc`
- Badging: `apk_badging.txt`
- Signature: `apk_signature_verify.txt`, `apk_signature_verify_verbose.txt`, `apk_signature_verify.status.txt`
- Emulator install/readback: `apk_install_evidence/adb_install.log`, `apk_install_evidence/dumpsys_package.txt`
- Launch smoke screenshot: `apk_install_evidence/launch_screenshot.png`

## Automated Gates

- Final JVM/lint/build gate: PASS
  - Command log: `final_gradle_build_r2_scrubbed.log` (canonical review log with local absolute paths replaced by `$REPO`)
  - Raw command log retained on disk only: `final_gradle_build_r2.log`
  - Status: `final_gradle_build_r2.status.txt` (`exit_code=0`)
  - Tasks: `:app:testDebugUnitTest :app:lintDebug :app:assembleDebug`

- Targeted regression rerun for the three previously failing connected scenarios: PASS
  - Command log: `connected_debug_android_test_r2_targeted_scrubbed.log` (canonical review log)
  - Raw command log retained on disk only: `connected_debug_android_test_r2_targeted.log`
  - Status: `connected_debug_android_test_r2_targeted.status.txt` (`exit_code=0`)
  - Covered the firm-mode open-anyway wait checks and the core visual screenshot finite-choice layout check.

- Full connected Android gate: PASS
  - Command log: `connected_debug_android_test_r2_scrubbed.log` (canonical review log)
  - Raw command log retained on disk only: `connected_debug_android_test_r2.log`
  - Status: `connected_debug_android_test_r2.status.txt` (`exit_code=0`)
  - XML result: `connected_results/androidTest-results-debug/TEST-qaApi36(AVD) - 16-_app-.xml`
  - Result summary: `136` tests, `0` failures, `0` errors, `0` skipped.
  - Unit XML results: `unit_results/testDebugUnitTest/`

## Important Prior Red Gate

`connected_debug_android_test.log` and `connected_debug_android_test.status.txt` remain on disk in this release-gate directory as the first release-gate attempt. They are intentionally excluded from the GPT Pro R2 review ZIP to avoid treating stale red evidence as canonical. That attempt failed because two legacy tests still assumed firm-mode open-anyway friction while Sprint 26 intentionally preserves `SOFT` as the default, and one visual assertion did not recognize the separate meditation alternative card as a valid finite choice.

The final canonical connected result is `connected_debug_android_test_r2.log` with `exit_code=0`.

## Visual Evidence

Canonical final screenshots are under `screenshots/`:

- `sprint26-custom-targets-1780866735053/`: custom installed-app target settings, persistence, removal, soft intervention, firm wait, bedtime intervention.
- `sprint26-custom-targets-1780866544320/`: website rule settings, private/public IP rejection, exact rule, wildcard/apex toggle, pause, edit, delete, browser support matrix.
- `sprint26-custom-targets-1780866592442/`: Chrome website soft intervention, firm wait, bedtime emergency unlock.
- `sprint26-chrome-verified-host-1780865934601/`: nonmatching host, typed-but-not-loaded host, matching verified-host adapter.
- `sprint26-chrome-verified-host-1780865957572/`: unsupported/unreadable negative state with no intervention.

Manual visual check from local rendered screenshots: PASS. The meditation alternative is visually separate from "Other options"; soft/firm/bedtime website screens are readable; bottom action controls do not overlap finite choices; unsupported/unreadable Chrome states remain non-intervening.

## Package Hygiene

- The repository release-gate directory intentionally keeps the failed first connected run as non-canonical audit history, but the GPT Pro R2 review bundle intentionally excludes that stale failed log and includes only this summary's explanation plus the canonical R2 logs/results.
- The APK binary is stored in `release_artifacts/`; the review bundle should rely on metadata, signature, install logs, and SHA-256 rather than duplicating the 38 MB APK unless binary inspection is explicitly required.
- Release gate artifacts are contained in this dated release-gate directory.
