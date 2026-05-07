# Sprint 18 Reader Progress Percent Hotfix

Generated: 2026-05-07

## Scope

- Reader progress percent is now source-anchored instead of page-count anchored.
- Restored Reader sessions show the saved progress percent after Reader text size changes, while the page count may legitimately change.
- The annotation note editor no longer shows user-facing technical selection text such as `Selection block ... steps ...`.

## Validation

- PASS: `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:testDebugUnitTest`
- PASS: `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew connectedDebugAndroidTest --rerun-tasks -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#readerProgressPercentRestoresFromSourceAnchorAfterReaderFontChange,com.qualityalternative.app.MainActivityTest#readerAnnotationControlsExpandAndReopenAcrossPages`
- GPT Pro R3 E2E visual review: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`.
- Connected test report: `test_reports/connected_hotfix.xml` reports 2 tests, 0 failures, 0 errors, 0 skipped on `qaApi36(AVD) - 16`.
- Unit XML reports copied under `test_reports/unit/` include the progress-specific `ProgressSnapshotTest` plus persistence/profile-adjacent coverage from `MainViewModelTest`, `AccountLightProfileExporterTest`, and `AccountLightProfileImporterTest`.
- Emulator was shut down after screenshots and connected validation; `logs/adb_devices_after_emulator_shutdown.txt` records no attached devices.

## Screenshots

- `screenshots/sprint18-progress-hotfix-1778150244743/01_default_font_saved_progress.png`: default Reader text after advancing and saving progress; footer shows `2/4 · 49%`.
- `screenshots/sprint18-progress-hotfix-1778150244743/02_large_font_restored_same_progress.png`: Reader reopened at 130% text; page count changes to `4/8`, but footer still shows saved `49%`.
- `screenshots/sprint17-cross-page-annotation-1778150248407/01_compact_controls_first_page_light.png`: note popup no longer shows the technical `Selection block ... steps ...` summary.
- `screenshots/sprint17-cross-page-annotation-1778150248407/02_compact_controls_later_page_light.png`: cross-page selection controls remain compact after moving to later page.
- `screenshots/sprint17-cross-page-annotation-1778150248407/03_long_quote_scroll_region_light.png`: long selected quote remains in the scroll region.
- `screenshots/sprint17-cross-page-annotation-1778150248407/04_reopened_cross_page_quote_light.png`: saved cross-page quote reopens without the technical summary.

## Files

- `reader_progress_percent_hotfix.diff`: current implementation diff.
- `logs/unit_debug.log`: unit-test output.
- `logs/logcat-com.qualityalternative.app.MainActivityTest-readerProgressPercentRestoresFromSourceAnchorAfterReaderFontChange.txt`: runtime progress/font evidence log.
- `logs/logcat-com.qualityalternative.app.MainActivityTest-readerAnnotationControlsExpandAndReopenAcrossPages.txt`: runtime annotation popup evidence log.
- `logs/adb_devices_after_emulator_shutdown.txt`: emulator shutdown evidence.
- `test_reports/unit/*.xml`: per-test unit XML evidence for progress and persistence/profile-adjacent coverage.
- `GPT_PRO_REVIEW_R3.md`: final GPT Pro E2E review with visual screenshots, scoring `10/10 PASS/PASS`.
