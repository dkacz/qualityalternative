# Sprint 26 Final Release Review Bundle Manifest

Bundle target: GPT Pro final release gate for `v0.11.14-custom-targets-website-interventions-alpha`.

## Primary Files

- `PRD.md`
- `docs/SPRINT_26_CUSTOM_TARGETS_WEBSITE_INTERVENTIONS.md`
- `docs/LANE_STATUS.md`
- `docs/release-gate-logs/2026-06-07-sprint26-custom-targets-website-interventions/VALIDATION_SUMMARY.md`
- `docs/release-gate-logs/2026-06-07-sprint26-custom-targets-website-interventions/RELEASE_NOTES_v0.11.14-custom-targets-website-interventions-alpha.md`
- `evidence/sprint26_custom_targets_website_interventions/SPRINT26_FINAL_RELEASE_REVIEW_PROMPT.md`

## Release Evidence

- `docs/release-gate-logs/2026-06-07-sprint26-custom-targets-website-interventions/final_gradle_build_r2_scrubbed.log`
- `docs/release-gate-logs/2026-06-07-sprint26-custom-targets-website-interventions/final_gradle_build_r2.status.txt`
- `docs/release-gate-logs/2026-06-07-sprint26-custom-targets-website-interventions/connected_debug_android_test_r2_scrubbed.log`
- `docs/release-gate-logs/2026-06-07-sprint26-custom-targets-website-interventions/connected_debug_android_test_r2.status.txt`
- `docs/release-gate-logs/2026-06-07-sprint26-custom-targets-website-interventions/connected_debug_android_test_r2_targeted_scrubbed.log`
- `docs/release-gate-logs/2026-06-07-sprint26-custom-targets-website-interventions/connected_debug_android_test_r2_targeted.status.txt`
- `docs/release-gate-logs/2026-06-07-sprint26-custom-targets-website-interventions/connected_results/androidTest-results-debug/TEST-qaApi36(AVD) - 16-_app-.xml`
- `docs/release-gate-logs/2026-06-07-sprint26-custom-targets-website-interventions/unit_results/testDebugUnitTest/`
- `docs/release-gate-logs/2026-06-07-sprint26-custom-targets-website-interventions/apk_badging.txt`
- `docs/release-gate-logs/2026-06-07-sprint26-custom-targets-website-interventions/apk_signature_verify.txt`
- `docs/release-gate-logs/2026-06-07-sprint26-custom-targets-website-interventions/apk_signature_verify_verbose.txt`
- `docs/release-gate-logs/2026-06-07-sprint26-custom-targets-website-interventions/apk_signature_verify.status.txt`
- `docs/release-gate-logs/2026-06-07-sprint26-custom-targets-website-interventions/apk_install_evidence/`
- `docs/release-gate-logs/2026-06-07-sprint26-custom-targets-website-interventions/screenshots/`
- `release_artifacts/quality-alternative-v0.11.14-custom-targets-website-interventions-alpha-debug.apk.sha256`

## Per-Slice Review Trail

- `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_PLAN_REVIEW_R2.md`
- `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_1_R4_REVIEW.md`
- `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_2_R2_REVIEW.md`
- `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R7_REVIEW.md`
- `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_4_R3_REVIEW.md`
- `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_5_R2_REVIEW.md`
- `evidence/sprint26_custom_targets_website_interventions/SLICE26_5_EVIDENCE.md`

## Source / Diff Included

- `app/build.gradle.kts`
- `app/src/main/`
- `app/src/test/`
- `app/src/androidTest/`
- `evidence/sprint26_custom_targets_website_interventions/SPRINT26_FINAL_RELEASE_R2_DIFF.patch`

## Excluded On Purpose

- The 38 MB APK binary is not duplicated in the GPT Pro ZIP. APK readiness is proven by badging, signature verification, SHA-256, install readback, and launch screenshot. The binary remains in `release_artifacts/` for the actual GitHub release.
- Superseded older slice bundles and repeated stale screenshot directories are not included. The final bundle includes the latest passing review outputs, full app source/test trees for this lane, unit XML, canonical R2 release-gate logs, and the dated final screenshot artifacts.
- The first failed connected gate is not included in the ZIP except through `VALIDATION_SUMMARY.md`; it remains on disk in the release-gate directory as an audit trail, but the canonical final gate is R2. This is intentional package hygiene, not missing evidence.
- Raw logs with local absolute paths are not included in the GPT Pro R2 review ZIP. The bundle uses scrubbed canonical copies that replace the local repository root with `$REPO`.
