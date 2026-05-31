# Sprint 25 Markdown Media And Tables Validation

Status: GPT Pro R3 passed `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS`; Sprint 25 is ready for APK release preparation.

## Scope

- Markdown reader now parses standalone Markdown images into media blocks.
- Markdown reader now resolves sibling/relative image files and selected image attachment URI maps for Markdown documents.
- Markdown reader now parses pipe Markdown tables into structured headers, rows, and alignments.
- Reader UI now renders images and tables as structured UI, not raw Markdown syntax.
- Reading-time estimation and reader pagination ignore image payload noise and table delimiter syntax.
- Reader page gestures now ignore table-horizontal-scroll gestures consumed by child table scrollables.
- Reader table page fit now measures wrapped cell text and splits long table rows by visual weight.
- Markdown picker-style image candidates are mapped into the Markdown document attachment map and filtered from standalone imports.
- Reader page gestures now ignore only child-consumed horizontal drags, so ordinary text taps and ordinary text swipes still advance pages.

## Automated Validation

- PASS: `:app:testDebugUnitTest`
- PASS: `:app:lintDebug`
- PASS: `:app:compileDebugAndroidTestKotlin`
- PASS: `VisualQaScreenshotTest#captureSprint25MarkdownMediaAndTableScreens` on `qaApi36(AVD) - 16`
- PASS: `VisualQaScreenshotTest#captureSprint25WideMarkdownTableHorizontalScrollDoesNotAdvanceReaderPage` on `qaApi36(AVD) - 16`
- PASS: `VisualQaScreenshotTest#captureSprint25OrdinaryTextNavigationStillWorksAfterTableGestureGuard` on `qaApi36(AVD) - 16`
- PASS: `RoomUserDocumentRepositoryTest` on `qaApi36(AVD) - 16`
- PASS: `QualityAlternativeDatabaseMigrationInstrumentedTest` on `qaApi36(AVD) - 16`
- PASS: `git diff --check`

## Visual Evidence

- R3 contact sheet: `evidence/sprint25_markdown_media_tables/screenshots-r3/contact_sheet_r3.png`
- R3 raw screenshot directories:
  - `evidence/sprint25_markdown_media_tables/screenshots-r3/sprint25-markdown-media-tables-1780234757329/`
  - `evidence/sprint25_markdown_media_tables/screenshots-r3/sprint25-markdown-media-tables-1780234748974/`
  - `evidence/sprint25_markdown_media_tables/screenshots-r3/sprint25-markdown-media-tables-1780234735123/`
- R3 Android test XML: `evidence/sprint25_markdown_media_tables/android-results-r3/TEST-qaApi36(AVD) - 16-_app-.xml`

The visual E2E test captures:

- Intervention card for a private Markdown media/table document.
- Light reader page showing a rendered Markdown image and a rendered Markdown table.
- Dark reader page showing the same image/table rendering.
- Wide-table before/after horizontal scroll screenshots proving hidden right columns become visible and the reader remains on page `1/1`.
- Ordinary text-reader before/tap/swipe screenshots proving text taps advance from page `1/6` to `2/6`, and text swipes also advance from page `1/6` to `2/6`.
- Assertions that raw Markdown image syntax, raw table pipes, and raw delimiter rows are not visible in the reader.

## GPT Pro R1 Remediation

R1 review result: `SCORE: 7/10`, `VERDICT: REVISE`, `VISUAL REVIEW: REVISE`.

R1 blockers addressed before R2:

- Wide-table horizontal scroll/page-swipe conflict fixed and covered by visual/instrumented evidence.
- Wrapped table cell height/page-fit undercount fixed by measured table row heights and visual-weight splitting.
- Picker-style Markdown image attachment mapping covered by unit tests.
- Room repository and migration instrumented tests executed and copied into R2 evidence.

R2 review result: `SCORE: 8/10`, `VERDICT: REVISE`, `VISUAL REVIEW: PASS`.

R2 blocker addressed before R3:

- The child-consumption gesture guard is now narrowed to consumed horizontal drags, not all child-consumed pointer events.
- `VisualQaScreenshotTest#captureSprint25OrdinaryTextNavigationStillWorksAfterTableGestureGuard` proves ordinary rendered text tap and ordinary rendered text swipe still advance reader pages after the table-scroll guard.

R3 review result: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`.

R3 review file: `evidence/sprint25_markdown_media_tables/pro_review_harvest_r3/GPT_PRO_REVIEW_R3.md`.
