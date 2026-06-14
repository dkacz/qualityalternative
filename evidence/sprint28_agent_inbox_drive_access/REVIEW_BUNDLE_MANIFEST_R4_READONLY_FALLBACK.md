# Sprint 28 GPT Pro Review Bundle Manifest R4 - Readonly Fallback

Review lane: controlled `drive.readonly` fallback for Agent Inbox Drive access after the live Google Picker runtime failed before folder selection.

Branch: `codex/sprint28-agent-inbox-drive-access`

Implementation state: local working tree on 2026-06-14. The bundle includes the current source/test/docs/evidence needed to audit the fallback before final release.

## Primary Document

- `docs/SPRINT_28_AGENT_INBOX_DRIVE_ACCESS.md`

## Included Source And Test Surface

- `PRD.md`
- `docs/LANE_STATUS.md`
- `settings.gradle.kts`
- `build.gradle.kts`
- `app/build.gradle.kts`
- `gradle/libs.versions.toml`
- full `app/src/main/java/`
- full `app/src/test/java/`
- full `app/src/androidTest/java/`

## Included Current Evidence

- `evidence/sprint28_agent_inbox_drive_access/VALIDATION_SUMMARY.md`
- `evidence/sprint28_agent_inbox_drive_access/device_spike/RCLONE_PICKER_FOLDER_SPIKE.md`
- `evidence/sprint28_agent_inbox_drive_access/device_spike/live_picker_runtime_20260614/RESULT.md`
- `evidence/sprint28_agent_inbox_drive_access/live_readonly_rclone_package/RESULT.md`
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

## Included Historical Review Context

- `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_R1.md`
- `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_R2.md`
- `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_R3.md`
- `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_PROMPT_R3.md`
- `evidence/sprint28_agent_inbox_drive_access/REVIEW_BUNDLE_MANIFEST_R3.md`

These files are included only to let the reviewer check that the R4 fallback does not regress prior blockers. R3 visual and Android result directories are not included because the current visual surface is the R4 read-only run listed above.

## Intentional Exclusions

- Raw rclone listing JSON files are excluded from the GPT Pro bundle. The live PASS can be audited from the redacted result summary, final app screenshot, UI dump, and logcat; exact Drive object ids are not needed for this review.
- Google account dumps, package dumps, OAuth screenshots, and emulator account artifacts from the live Picker investigation are excluded. The canonical Picker failure summary is `device_spike/live_picker_runtime_20260614/RESULT.md`.
- Previous Sprint 27 evidence and release bundles are excluded; Sprint 27 history is summarized in `docs/LANE_STATUS.md`.
- Build intermediates, APKs, and release artifacts are excluded because this lane is the pre-release fallback review. Final APK evidence belongs in the release gate after GPT Pro passes.
- Prior R2/R3 screenshot directories are excluded to avoid stale visual confusion. `VALIDATION_SUMMARY.md` and `REVIEW_BUNDLE_MANIFEST_R3.md` preserve enough history for regression checking.

## Bundle Hygiene Notes

This packet is deliberately narrower than a full repo dump but still includes complete source and test trees. The canonical current artifacts are the R4 read-only visual run and live rclone fallback result. Any older Picker-first or R3 evidence should be treated as historical context only, not as proof that the fallback is visually or functionally ready.
