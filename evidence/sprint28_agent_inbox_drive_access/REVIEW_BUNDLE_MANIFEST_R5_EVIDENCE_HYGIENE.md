# Sprint 28 GPT Pro Review Bundle Manifest R5 - Evidence Hygiene

Review lane: R5 re-review of the controlled `drive.readonly` Agent Inbox fallback after R4 returned `SCORE 9/10`, `VERDICT PASS`, `VISUAL REVIEW PASS` with a single evidence/package-hygiene finding.

Branch: `codex/sprint28-agent-inbox-drive-access`

Implementation state: local working tree on 2026-06-14. This bundle includes current source/test/docs/evidence needed to check whether R4's finding is closed and whether the read-only fallback remains releasable before the final APK gate.

## Primary Document

- `docs/SPRINT_28_AGENT_INBOX_DRIVE_ACCESS.md`

## Included Source And Test Surface

- `PRD.md`
- `docs/LANE_STATUS.md`
- `settings.gradle.kts`
- `build.gradle.kts`
- `app/build.gradle.kts`
- `gradle/libs.versions.toml`
- `gradlew`
- `gradlew.bat`
- `gradle/wrapper/`
- full `app/src/main/`
- full `app/src/test/`
- full `app/src/androidTest/`

## Included R5 Prompt And Review Context

- `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_PROMPT_R5_EVIDENCE_HYGIENE.md`
- `evidence/sprint28_agent_inbox_drive_access/REVIEW_BUNDLE_MANIFEST_R5_EVIDENCE_HYGIENE.md`
- `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_R4_READONLY_FALLBACK.md`
- `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_PROMPT_R4_READONLY_FALLBACK.md`
- `evidence/sprint28_agent_inbox_drive_access/REVIEW_BUNDLE_MANIFEST_R4_READONLY_FALLBACK.md`

R1/R2/R3 review outputs are included as historical regression context only. The active review target is the R5 evidence-hygiene closure for the read-only fallback.

## Included Current Evidence

- `evidence/sprint28_agent_inbox_drive_access/VALIDATION_SUMMARY.md`
- `evidence/sprint28_agent_inbox_drive_access/device_spike/RCLONE_PICKER_FOLDER_SPIKE.md`
- `evidence/sprint28_agent_inbox_drive_access/device_spike/live_picker_runtime_20260614/RESULT.md`
- `evidence/sprint28_agent_inbox_drive_access/live_readonly_rclone_package/RESULT.md`
- `evidence/sprint28_agent_inbox_drive_access/live_readonly_rclone_package/rclone_listing_summary_redacted.md`
- `evidence/sprint28_agent_inbox_drive_access/live_readonly_rclone_package/rclone-readonly-package/manifest.json`
- `evidence/sprint28_agent_inbox_drive_access/live_readonly_rclone_package/rclone-readonly-package/rclone-readonly-smoke.md`
- `evidence/sprint28_agent_inbox_drive_access/live_readonly_rclone_package/live_readonly_rclone_success.png`
- `evidence/sprint28_agent_inbox_drive_access/live_readonly_rclone_package/window_live_readonly_success.xml`
- `evidence/sprint28_agent_inbox_drive_access/live_readonly_rclone_package/logcat_live_readonly_success.txt`
- `evidence/sprint28_agent_inbox_drive_access/visual_e2e_readonly_r1/contact_sheet_readonly_r1.png`
- `evidence/sprint28_agent_inbox_drive_access/visual_e2e_readonly_r1/TEST-sprint28-readonly-visual.xml`
- `evidence/sprint28_agent_inbox_drive_access/visual_e2e_readonly_r1/logcat-sprint28-readonly-visual.txt`
- `evidence/sprint28_agent_inbox_drive_access/visual_e2e_readonly_r1/sprint28-agent-inbox-drive-access-1781460684272/`
- `evidence/sprint28_agent_inbox_drive_access/sprint28_r4_readonly_fallback_tracked_diff.patch`
- `evidence/sprint28_agent_inbox_drive_access/git_log_r4_readonly_fallback.txt`
- `evidence/sprint28_agent_inbox_drive_access/adb_devices_r4_readonly_fallback.txt`
- `evidence/sprint28_agent_inbox_drive_access/logs/git_diff_check_r4_readonly_fallback.log`
- `evidence/sprint28_agent_inbox_drive_access/sprint28_r5_evidence_hygiene_tracked_diff.patch`
- `evidence/sprint28_agent_inbox_drive_access/git_log_r5_evidence_hygiene.txt`
- `evidence/sprint28_agent_inbox_drive_access/adb_devices_r5_evidence_hygiene.txt`
- `evidence/sprint28_agent_inbox_drive_access/logs/git_diff_check_r5_evidence_hygiene.log`
- `evidence/sprint28_agent_inbox_drive_access/logs/full_local_gate_r4_readonly_fallback.log`
- `evidence/sprint28_agent_inbox_drive_access/logs/full_local_gate_r5_evidence_hygiene.log`
- `evidence/sprint28_agent_inbox_drive_access/logs/targeted_chrome_verified_host_rerun_r4.log`
- `evidence/sprint28_agent_inbox_drive_access/logs/targeted_sprint27_visual_rerun_r4_after_grant_seed.log`
- `evidence/sprint28_agent_inbox_drive_access/logs/targeted_sprint28_readonly_visual_rerun_after_seed_fix.log`
- `evidence/sprint28_agent_inbox_drive_access/logs/full_connected_debug_android_test_r4_readonly_fallback.log`
- `evidence/sprint28_agent_inbox_drive_access/logs/full_connected_debug_android_test_r5_evidence_hygiene.log`

The R4 full connected log is included only to explain the historical two-failure state before the Sprint 27 visual seed fix and targeted reruns. The R5 full connected log is the current fresh full connected run and should be treated as the canonical full connected evidence for this review.

## Included Historical Review Context

- `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_R1.md`
- `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_R2.md`
- `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_R3.md`
- `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_PROMPT_R3.md`
- `evidence/sprint28_agent_inbox_drive_access/REVIEW_BUNDLE_MANIFEST_R3.md`

These files are included only so the reviewer can check that the R5 fallback/evidence changes do not regress prior blockers. R2/R3 visual directories are excluded because the current visual surface is the R5 read-only run listed above.

## Intentional Exclusions

- Raw rclone listing JSON files are excluded. They contain raw Google Drive object ids and are replaced by `live_readonly_rclone_package/rclone_listing_summary_redacted.md`.
- Google account dumps, Google Play package dumps, OAuth screenshots, and emulator account artifacts from the live Picker investigation are excluded. The canonical non-private Picker failure summary is `device_spike/live_picker_runtime_20260614/RESULT.md`.
- The OAuth consent screenshot is excluded because it exposed private account UI. The live proof is final app state after consent/scan plus source/tests/logs that constrain authorization behavior.
- Previous Sprint 27 evidence and release bundles are excluded; the relevant Sprint 27 regression is represented by source/tests and `targeted_sprint27_visual_rerun_r4_after_grant_seed.log`.
- Build intermediates, APKs, and release artifacts are excluded because this lane is the pre-release Pro review. Final APK evidence belongs in the release gate after GPT Pro passes.
- Prior R2/R3 screenshot directories are excluded to avoid stale visual confusion. `VALIDATION_SUMMARY.md`, R3 review output, and `REVIEW_BUNDLE_MANIFEST_R3.md` preserve enough history for regression checking.

## Bundle Hygiene Notes

This packet is deliberately narrower than a full repo dump but includes complete source/test trees, current sprint docs, R4 review output, R5 closure evidence, current visual screenshots, raw Gradle logs, and the live rclone final-state proof. The canonical current artifacts are the R5 read-only review files, the R5 raw validation logs, the read-only visual run, and the live rclone fallback result. Older Picker-first or R3 artifacts should be treated as historical context only.
