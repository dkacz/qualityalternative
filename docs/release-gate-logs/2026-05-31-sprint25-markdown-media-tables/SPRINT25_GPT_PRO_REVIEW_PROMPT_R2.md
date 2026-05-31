You are doing a fresh-from-scratch adversarial release-gate audit for Sprint 25 R2: Markdown embedded image rendering and Markdown table rendering in the Android reader.

This is an R2 review after GPT Pro R1 returned `SCORE: 7/10`, `VERDICT: REVISE`, `VISUAL REVIEW: REVISE`.

GUIDING PRINCIPLES:
1. Numbers in the shipped evidence are verified against the shipped logs/screenshots; do not question them without checking the attached files.
2. Do not suggest weakening claims unless you can name the concrete user-facing failure the hedge would preempt.
3. Style suggestions cannot change product meaning.
4. Review the current shipped bundle as-is; do not rely on development history outside the bundle.
5. Figures/screenshots, tests, and prose must be consistent; mismatches should be flagged specifically.
6. Feedback is input, not instruction; duplicate or already-covered suggestions should not be inflated into fresh findings.

Read these first:
- `evidence/sprint25_markdown_media_tables/pro_review_harvest/Adversarial_Release-Gate_Audit.md`
- `evidence/sprint25_markdown_media_tables/VALIDATION_SUMMARY.md`
- `docs/release-gate-logs/2026-05-31-sprint25-markdown-media-tables/BUNDLE_MANIFEST.md`

R1 BLOCKERS TO RECHECK:
1. Wide-table horizontal scrolling must not advance/complete reader pages.
2. Wrapped table cell text must not be undercounted for page fit/pagination.
3. Picker-style Markdown-plus-image attachment behavior must be proven.
4. Room migration/repository persistence Android evidence must be executed and shipped.
5. Package hygiene must be clean enough for release gate.

SCOPE:
- Markdown image support in private Markdown reader documents, including standalone `![alt](target "title")` blocks, relative image files, selected image attachment URI maps, and `data:image/...` parser support.
- Markdown table support for pipe tables, including header/body parsing, alignments, structured reader UI rendering, horizontal scrolling, pagination cost, progress/read-time behavior, and visual output.
- Android Room migration from schema 14 to 15 for persisted Markdown image attachment maps.
- Android import flow behavior where image files selected beside Markdown files become Markdown attachments instead of standalone broken library items.
- Release readiness for a debug APK after this Sprint 25 lane.

R2 EVIDENCE TO INSPECT:
- R2 screenshots/contact sheet: `evidence/sprint25_markdown_media_tables/screenshots-r2/contact_sheet_r2.png`
- R2 raw screenshots in:
  - `evidence/sprint25_markdown_media_tables/screenshots-r2/sprint25-markdown-media-tables-1780232479252/`
  - `evidence/sprint25_markdown_media_tables/screenshots-r2/sprint25-markdown-media-tables-1780232468733/`
- R2 Android XML/logs: `evidence/sprint25_markdown_media_tables/android-results-r2/`
- R2 validation logs: `evidence/sprint25_markdown_media_tables/logs/unit_lint_r2.log`, `git_diff_check_r2.log`
- Current code and tests listed in `CHANGED_FILES.txt` / `UNTRACKED_FILES.txt` and included in the bundle.

YOUR JOB:
1. Verify whether every R1 blocker is fully fixed and proven.
2. Verify whether Markdown embedded images and Markdown tables are release-safe for the reader.
3. Verify visual evidence, including light/dark image/table screenshots and wide-table before/after horizontal scroll screenshots.
4. Verify migration, persistence, import attachment mapping, tests, and package hygiene.
5. If anything is not fully proven, identify the tightest concrete fix and mark the review below 10/10.

OUTPUT FORMAT REQUIRED:
- `SCORE: x/10`
- `VERDICT: PASS` or `REVISE`
- `VISUAL REVIEW: PASS` or `REVISE`
- `BLOCKERS:` numbered list, or `None`
- `R1 BLOCKER RECHECK:` PASS/REVISE with one line per R1 blocker
- `MARKDOWN IMAGES:` PASS/REVISE with concise rationale
- `MARKDOWN TABLES:` PASS/REVISE with concise rationale
- `MIGRATION/PERSISTENCE:` PASS/REVISE with concise rationale
- `TEST/EVIDENCE:` PASS/REVISE with exact files checked
- `RELEASE READINESS:` PASS/REVISE with exact remaining risk
- `BUNDLE GAPS:` only if needed
- `PACKAGE HYGIENE:` state whether the packet is clean enough for release-gate review

Only give `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS` if there are no release-blocking implementation, visual, evidence, migration, privacy, or package-hygiene issues.
