# Sprint 28 GPT Pro Review Bundle Manifest R3

Review lane: R2-fix adversarial audit for Agent Inbox Drive access under Google Drive `drive.file`, with Markdown image sidecar safety and visual evidence.

Implementation head commit: `482478d` (`Fix Sprint 28 Markdown sidecar review blockers`)

Included commit context: `git_log_r3.txt` in this directory starts at the R3 implementation head and lists recent Sprint 28 commits. The prompt/manifest commit is packaging metadata and is not part of the implementation scoring surface.

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

## Included Evidence

- `evidence/sprint28_agent_inbox_drive_access/VALIDATION_SUMMARY.md`
- `evidence/sprint28_agent_inbox_drive_access/device_spike/RCLONE_PICKER_FOLDER_SPIKE.md`
- `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_R1.md`
- `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_R2.md`
- `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_PROMPT_R1.md`
- `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_PROMPT_R2.md`
- `evidence/sprint28_agent_inbox_drive_access/sprint28_r3_tracked_diff.patch`
- `evidence/sprint28_agent_inbox_drive_access/git_log_r3.txt`
- `evidence/sprint28_agent_inbox_drive_access/adb_devices_r3.txt`
- `evidence/sprint28_agent_inbox_drive_access/logs/targeted_r2_fixes.log`
- `evidence/sprint28_agent_inbox_drive_access/logs/full_local_gate_r3_candidate.log`
- `evidence/sprint28_agent_inbox_drive_access/logs/connected_sprint28_visual_r3_candidate.log`
- `evidence/sprint28_agent_inbox_drive_access/logs/git_diff_check_r3.log`
- `evidence/sprint28_agent_inbox_drive_access/android-results-r3/`
- `evidence/sprint28_agent_inbox_drive_access/visual_e2e_r3/contact_sheet_r3.png`
- `evidence/sprint28_agent_inbox_drive_access/visual_e2e_r3/sprint28-agent-inbox-drive-access-1781437194813/`

## Intentional Exclusions

- Previous Sprint 27 evidence and release bundles are excluded; Sprint 27 history is summarized in `docs/LANE_STATUS.md`, and this R3 review is scoped to Sprint 28.
- APK/release artifacts are excluded because Sprint 28 is not at release gate until GPT Pro returns `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`, and the live rclone/Picker device spike is resolved.
- Build intermediates are excluded. The R3 Android result directory keeps only XML, exit code, textproto, testlog, and focused logcat evidence.
- The live rclone/Picker result is not claimed as complete. The checklist is included, and reviewers should treat live-spike proof as a pending release gate unless the shipped files prove otherwise.

## Bundle Hygiene Notes

This packet is narrower than the R2 bundle where possible: it includes full source/test trees for auditability, but R3 evidence is limited to the current R2-fix logs, focused connected visual result, raw screenshots, contact sheet, and scoped source/docs patch. Older R1/R2 review outputs are included only so the reviewer can re-check the exact prior findings and confirm the new R3 changes close them without regressing earlier blockers.
