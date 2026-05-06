# Sprint 17 Slice 17.4 R20 Validation

Scope: Adaptive Reader Pagination Fit.

R20 is a bundle-provenance and package-hygiene rerun over the R19 implementation. No functional code changed after R19.

Implementation evidence:
- Reader page capacity is computed through `AdaptiveReaderPageFit` from the measured reader viewport, reader text scale, content padding, line height, per-block rendered cost, rendered short-block cap, and a bounded safety reserve.
- CODE admission uses a discrete short multi-line cost table for the full 2-11 rendered-line range:
  - 2 lines: 15 blocks on the first tall default page.
  - 3 lines: 10 blocks.
  - 4 lines: 8 blocks.
  - 5 lines: 6 blocks.
  - 6 lines: 5 blocks.
  - 7 lines: 4 blocks.
  - 8 lines: 4 blocks.
  - 9 lines: 3 blocks.
  - 10 lines: 3 blocks.
  - 11 lines: 3 blocks.
  - 12+ lines use the long-code cost path, preserving the previous 17-line chunk behavior.
- BODY/LIST/QUOTE admission charges the larger of viewport gap reserve and fixed rendered body padding cost. Short one-line BODY/LIST/QUOTE runs are guarded by `adaptiveReaderMaxBlocksPerPage(...)`.
- R20 keeps the R19 closure of the R18 blocker: 5-, 6-, 7-, and 9-line CODE have unit assertions, rendered footer-boundary assertions, page-fit summaries, and screenshots.
- Reader progress saves a source block index plus source text offset through `ReaderBlockLayout.sourcePositionForDisplayBlock(...)` and restores through `displayBlockIndexForSourcePosition(...)`.
- Annotation display resolves source selectors before legacy paragraph-index fallback.
- Room reading progress was migrated to schema 13 with `lastVisibleTextOffset`, and Portable Profile exports/imports that offset while still accepting older profiles that omit it.
- Portable Profile unknown-field warnings use `AllowedReadingProgressKeys`, so exported `lastVisibleTextOffset` does not generate a misleading ignored-field warning.

Validation commands:
- Raw log `r20_unit_validation.log` starts with the exact command line:
  - `COMMAND: JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18 ./gradlew testDebugUnitTest --rerun-tasks --tests com.qualityalternative.app.ui.ProgressSnapshotTest --tests com.qualityalternative.app.data.local.QualityAlternativeDatabaseMigrationTest --tests com.qualityalternative.app.data.AccountLightProfileExporterTest --tests com.qualityalternative.app.data.AccountLightProfileImporterTest`
  - Result: BUILD SUCCESSFUL.
- Raw log `r20_instrumentation_validation.log` starts with the exact command line:
  - `COMMAND: JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18 ./gradlew compileDebugAndroidTestKotlin connectedDebugAndroidTest --rerun-tasks -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#readerPaginationFitRespondsToViewportAndReaderTextSize,com.qualityalternative.app.data.RoomReadingProgressRepositoryTest,com.qualityalternative.app.data.local.QualityAlternativeDatabaseMigrationInstrumentedTest#migration12To13ValidatesRoomSchemaAndDefaultsReadingProgressOffset`
  - Result: BUILD SUCCESSFUL on `qaApi36(AVD) - 16`.
- `r20_emulator_reset.log` records the emulator geometry before instrumentation:
  - `Physical size: 1080x2400`
  - `Physical density: 420`

Visual evidence:
- `screenshots/sprint17-adaptive-pagination-1778073394700/01_tall_phone_default_text.png` is 1080x2400.
- `screenshots/sprint17-adaptive-pagination-1778073394700/02_tall_phone_large_text.png` is 1080x2400 with reader text scale 1.3.
- `screenshots/sprint17-adaptive-pagination-1778073394700/03_tall_phone_code_blocks.png` is 1080x2400 with 26 complete one-line fenced CODE blocks at default reader text scale and a real second page.
- `screenshots/sprint17-adaptive-pagination-1778073394700/04_tall_phone_large_code_blocks.png` is 1080x2400 with 22 complete one-line fenced CODE blocks at reader text scale 1.3.
- `screenshots/sprint17-adaptive-pagination-1778073394700/05_tall_phone_multiline_code_blocks.png` is 1080x2400 with four complete eight-line CODE blocks at default reader text scale.
- `screenshots/sprint17-adaptive-pagination-1778073394700/06_tall_phone_large_multiline_code_blocks.png` is 1080x2400 with one complete eight-line CODE block at reader text scale 1.3.
- `screenshots/sprint17-adaptive-pagination-1778073394700/07_tall_phone_short_multiline_code_blocks.png` is 1080x2400 with 15 complete two-line CODE blocks and a real second page.
- `screenshots/sprint17-adaptive-pagination-1778073394700/08_tall_phone_three_line_code_blocks.png` is 1080x2400 with 10 complete three-line CODE blocks and a real next page.
- `screenshots/sprint17-adaptive-pagination-1778073394700/09_tall_phone_five_line_code_blocks.png` is 1080x2400 with 6 complete five-line CODE blocks and a real next page.
- `screenshots/sprint17-adaptive-pagination-1778073394700/10_tall_phone_six_line_code_blocks.png` is 1080x2400 with 5 complete six-line CODE blocks and a real next page.
- `screenshots/sprint17-adaptive-pagination-1778073394700/11_tall_phone_seven_line_code_blocks.png` is 1080x2400 with 4 complete seven-line CODE blocks and a real next page.
- `screenshots/sprint17-adaptive-pagination-1778073394700/12_tall_phone_nine_line_code_blocks.png` is 1080x2400 with 3 complete nine-line CODE blocks and a real next page.
- `screenshots/sprint17-adaptive-pagination-1778073394700/13_tall_phone_oversized_short_line_code_blocks.png` is 1080x2400 with two complete 17-line CODE chunks from a 36-short-line fenced block.
- `screenshots/sprint17-adaptive-pagination-1778073394700/14_tall_phone_large_oversized_short_line_code_blocks.png` is 1080x2400 with one complete 15-line CODE chunk from a 40-short-line fenced block at reader text scale 1.3.
- `screenshots/sprint17-adaptive-pagination-1778073394700/15_tall_phone_large_oversized_short_line_code_tail_blocks.png` is 1080x2400 with the second 15-line CODE chunk plus the 10-line tail on page 2 at reader text scale 1.3.
- `screenshots/sprint17-adaptive-pagination-1778073394700/16_tall_phone_adjacent_whole_short_line_code_blocks.png` is 1080x2400 with a 19-line source CODE block split into complete 17-line and 2-line chunks on page 1; the adjacent 17-line source CODE block is on page 2.
- `screenshots/sprint17-adaptive-pagination-1778073394700/17_tall_phone_mixed_short_line_code_body_blocks.png` is 1080x2400 with two complete 17-line CODE chunks on page 1; the following one-line BODY tail is rejected to page 2.
- `screenshots/sprint17-adaptive-pagination-1778073394700/18_small_phone_default_text.png` is 720x1280 after emulator `wm size 720x1280` and `wm density 320`.
- `screenshots/sprint17-adaptive-pagination-1778073394700/page-fit-summaries.txt` records all 18 current summaries, including:
  - `07_tall_phone_short_multiline_code_blocks: ... blocks-15-pages-2`
  - `08_tall_phone_three_line_code_blocks: ... blocks-10-pages-3`
  - `09_tall_phone_five_line_code_blocks: ... blocks-6-pages-4`
  - `10_tall_phone_six_line_code_blocks: ... blocks-5-pages-5`
  - `11_tall_phone_seven_line_code_blocks: ... blocks-4-pages-6`
  - `12_tall_phone_nine_line_code_blocks: ... blocks-3-pages-8`
  - `17_tall_phone_mixed_short_line_code_body_blocks: ... blocks-2-pages-2`

R19 score-gap closure notes:
- R19 scored 9/10 with PASS/PASS and no blockers. Two non-blocking gaps prevented 10/10:
  - logs did not echo the exact test command filters.
  - the bundle included unnecessary R2/original prior review output directories.
- R20 closes both:
  - `r20_unit_validation.log` and `r20_instrumentation_validation.log` start with exact `COMMAND:` lines.
  - The R20 bundle includes only prior GPT Pro outputs R3-R19 as review context. It excludes `PRO_REVIEW_OUTPUT_SPRINT17_SLICE17_4_20260505_215139` and `PRO_REVIEW_OUTPUT_SPRINT17_SLICE17_4_R2_20260505_222930`.

Prior blocker closure notes:
- R18 5-/6-/7-/9-line CODE blocker: closed by R19/R20 unit proof and screenshots 09-12.
- R17 three-line CODE over-admission blocker: closed by unit proof and screenshot 08.
- R16 two-line CODE underfill blocker: closed by unit proof and screenshot 07.
- R15 mixed CODE+BODY blocker: closed by `[17,17,1]` unit proof and screenshot 17.
- R14 adjacent 19+17 and large `[15,15,10]` tail blockers: closed by unit proof and screenshots 14, 15, and 16.
- R13 single-block `[19,17]` split-tail: closed by `[17,17,2]` unit proof and screenshot 13.
- R12 many-short-line CODE whole-block path: closed by CODE-specific rendered-line splitting.
- R11 eight-line multi-line CODE clipping: closed by rendered multi-line CODE cost and whole-block allowance for page-contained medium CODE.
- R10 stale initial page after real viewport measurement: closed by `readerPageBoundarySignature(...)` and measured-page reconciliation.
- R9 default CODE proof gap: closed by 32 one-line CODE blocks, `pages-2`, and actual next-full-block measurement.
- R8 large CODE clipping and missing next-block proof: closed by rendered surface boundary checks plus real next-page block measurement.
- R7 default text and CODE underfill: closed by tall-phone page-fit calibration.
- R6 BODY/LIST/QUOTE over-admission: closed by rendered body padding cost, unit coverage, and the mixed CODE/BODY visual proof.
- R5 current-page-end marker issue: closed by debug marker and rendered block boundary checks.
- R4 safety reserve and profile warning issues: closed in source and tests.
- R3 source-position progress and selector-first annotation mapping: closed in source and tests.

Package hygiene:
- Superseded R19 logs, prompt, manifest, screenshots, and bundle zip were removed from the current evidence folder/root.
- The current evidence path contains only R20 validation, R20 logs, current diff, and one current screenshot directory.
- Prior GPT Pro outputs included in the R20 bundle are restricted to R3-R19.
