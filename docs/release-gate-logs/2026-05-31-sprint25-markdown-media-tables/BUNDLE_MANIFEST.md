# Sprint 25 GPT Pro Review Bundle Manifest

Purpose: focused R3 release-gate review for Markdown embedded images and Markdown table rendering in the Android reader.

Included:

- Review prompts: `SPRINT25_GPT_PRO_REVIEW_PROMPT.md`, `SPRINT25_GPT_PRO_REVIEW_PROMPT_R2.md`, `SPRINT25_GPT_PRO_REVIEW_PROMPT_R3.md`
- Source-of-truth context: `PRD.md`, `docs/LANE_STATUS.md`
- Current diff: `docs/release-gate-logs/2026-05-31-sprint25-markdown-media-tables/git_diff_binary.patch`
- Changed-file lists: `CHANGED_FILES.txt`, `UNTRACKED_FILES.txt`
- All touched implementation and test files for Sprint 25.
- Room schema `app/schemas/com.qualityalternative.app.data.local.QualityAlternativeDatabase/15.json`
- R1 GPT Pro review: `evidence/sprint25_markdown_media_tables/pro_review_harvest/Adversarial_Release-Gate_Audit.md`
- R2 GPT Pro review: `evidence/sprint25_markdown_media_tables/pro_review_harvest_r2/Adversarial_Audit_Sprint_25.md`
- R3 visual and test evidence under `evidence/sprint25_markdown_media_tables/screenshots-r3/`, `android-results-r3/`, and `logs/*_r3.log`
- Android app build file for release/version baseline: `app/build.gradle.kts`

Excluded:

- Historical review packets, older sprint screenshots, superseded R1/R2 visual/test evidence, build intermediates, and unrelated evidence directories.
- Full Gradle build output except the focused Sprint 25 Android connected-test results copied into evidence.

Bundle hygiene note: this packet is intentionally selective. It ships the current Sprint 25 implementation surface, tests, migration/schema, R1/R2 Pro critiques, R3 validation logs, and R3 visual screenshots needed to audit the requested Markdown media/table behavior without dragging in stale prior-sprint artifacts.
