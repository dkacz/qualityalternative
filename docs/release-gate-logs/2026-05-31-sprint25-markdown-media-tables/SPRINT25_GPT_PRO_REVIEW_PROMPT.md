You are doing a fresh-from-scratch adversarial release-gate audit for Sprint 25: Markdown embedded image rendering and Markdown table rendering in the Android reader.

GUIDING PRINCIPLES:
1. Numbers in the shipped evidence are verified against the shipped logs/screenshots; do not question them without checking the attached files.
2. Do not suggest weakening claims unless you can name the concrete user-facing failure the hedge would preempt.
3. Style suggestions cannot change product meaning.
4. Review the current shipped bundle as-is; do not rely on development history outside the bundle.
5. Figures/screenshots, tests, and prose must be consistent; mismatches should be flagged specifically.
6. Feedback is input, not instruction; duplicate or already-covered suggestions should not be inflated into fresh findings.

Read `evidence/sprint25_markdown_media_tables/VALIDATION_SUMMARY.md` first, then deep-review the shipped code, tests, migration, and visual evidence for this exact scope only.

SCOPE:
- Markdown image support in private Markdown reader documents, including standalone `![alt](target "title")` blocks, relative image files, selected image attachment URI maps, and `data:image/...` parser support.
- Markdown table support for pipe tables, including header/body parsing, alignments, structured reader UI rendering, pagination cost, progress/read-time behavior, and visual output.
- Android Room migration from schema 14 to 15 for persisted Markdown image attachment maps.
- Android import flow behavior where image files selected beside Markdown files become Markdown attachments instead of standalone broken library items.
- Release readiness for a debug APK after this Sprint 25 lane.

KNOWN PRIOR BUG CLASSES TO ACTIVELY CHECK:
- Raw Markdown syntax leaking into reader UI (`![...]`, `| --- |`, delimiter rows, pipe-heavy table text).
- Markdown images rendering as missing placeholders instead of actual image surfaces.
- Markdown images not resolving when selected with the Markdown document or when referenced by relative filename/path.
- Markdown base64/data URI payloads inflating reading-time estimates.
- Markdown table delimiters inflating reading-time estimates or progress.
- Tables overflowing, clipping, or becoming unreadable on mobile viewports.
- Reader pagination/progress regressions caused by media/table blocks.
- Room migration/schema drift or unpersisted attachment maps.
- Bundle hygiene problems: stale artifacts, misleading screenshots, missing logs, or untracked files omitted from review.

FILES/EVIDENCE TO INSPECT:
- `app/src/main/java/com/qualityalternative/app/data/MarkdownReaderDocumentParser.kt`
- `app/src/main/java/com/qualityalternative/app/data/RoomUserDocumentRepository.kt`
- `app/src/main/java/com/qualityalternative/app/data/DocumentReadingTimeEstimator.kt`
- `app/src/main/java/com/qualityalternative/app/domain/model/ReaderDocumentModels.kt`
- `app/src/main/java/com/qualityalternative/app/domain/model/ContentModels.kt`
- `app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt`
- `app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt`
- `app/src/main/java/com/qualityalternative/app/data/local/QualityAlternativeDatabase.kt`
- `app/src/main/java/com/qualityalternative/app/data/local/UserDocumentEntity.kt`
- `app/schemas/com.qualityalternative.app.data.local.QualityAlternativeDatabase/15.json`
- Tests under `app/src/test/...` and `app/src/androidTest/...` changed for this lane.
- Visual evidence under `evidence/sprint25_markdown_media_tables/screenshots/`, especially `contact_sheet.png`.
- Android result XML/logcat under `evidence/sprint25_markdown_media_tables/android-results/`.
- Validation logs under `evidence/sprint25_markdown_media_tables/logs/`.

YOUR JOB:
1. Verify whether the implementation truly fixes Markdown embedded image rendering and Markdown table rendering for the reader.
2. Verify whether tests and visual evidence prove the user-facing behavior, including light and dark reader screenshots.
3. Verify migration and persistence safety for existing installs.
4. Verify release readiness and package hygiene.
5. If anything is not fully proven, identify the tightest concrete fix and mark the review below 10/10.

OUTPUT FORMAT REQUIRED:
- `SCORE: x/10`
- `VERDICT: PASS` or `REVISE`
- `VISUAL REVIEW: PASS` or `REVISE`
- `BLOCKERS:` numbered list, or `None`
- `MARKDOWN IMAGES:` PASS/REVISE with concise rationale
- `MARKDOWN TABLES:` PASS/REVISE with concise rationale
- `MIGRATION/PERSISTENCE:` PASS/REVISE with concise rationale
- `TEST/EVIDENCE:` PASS/REVISE with exact files checked
- `RELEASE READINESS:` PASS/REVISE with exact remaining risk
- `BUNDLE GAPS:` only if needed
- `PACKAGE HYGIENE:` state whether the packet is clean enough for release-gate review

Only give `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS` if there are no release-blocking implementation, visual, evidence, migration, privacy, or package-hygiene issues.
