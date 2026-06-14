# Sprint 28 GPT Pro Review Bundle Manifest R2

Review lane: Agent Inbox Drive access fix under Google Drive `drive.file`, plus Markdown image attachment parity for manual import and Agent Inbox Markdown packages.

Head commit: `c98cd6a` (`Add Sprint 28 Markdown image attachment flows`)

Included commit context: `git_log_r2.txt` in this directory lists the current Sprint 28 branch head and recent commits.

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
- `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_PROMPT_R1.md`
- `evidence/sprint28_agent_inbox_drive_access/sprint28_r2_tracked_diff.patch`
- `evidence/sprint28_agent_inbox_drive_access/git_log_r2.txt`
- `evidence/sprint28_agent_inbox_drive_access/adb_devices_r2.txt`
- `evidence/sprint28_agent_inbox_drive_access/logs/`
- `evidence/sprint28_agent_inbox_drive_access/android-results-r2/`
- `evidence/sprint28_agent_inbox_drive_access/visual_e2e/contact_sheet_r2.png`
- `evidence/sprint28_agent_inbox_drive_access/visual_e2e/sprint28-agent-inbox-drive-access-1781433607325/`

## Intentional Exclusions

- Previous Sprint 27 evidence and release bundles are excluded; Sprint 27 history is summarized in `docs/LANE_STATUS.md`, and this R2 review is scoped to Sprint 28.
- APK/release artifacts are excluded because Sprint 28 is not at release gate until GPT Pro returns `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`, and the live rclone/Picker device spike is resolved.
- Build intermediates outside the curated android-results/log evidence are excluded.
- The live rclone/Picker result is not claimed as complete. The checklist is included, and reviewers should treat live-spike proof as a pending release gate unless the shipped files prove otherwise.

## Bundle Hygiene Notes

This packet is intentionally broader than R1 because R1 identified bundle-gappable state/contract risks. R2 ships full source/test trees plus fresh visual evidence and validation logs so GPT Pro can audit Drive state hydration, Picker-grant persistence, Markdown image import, Agent Inbox image sidecars, reader rendering, analytics/privacy, and package hygiene without inferring from absent files.
