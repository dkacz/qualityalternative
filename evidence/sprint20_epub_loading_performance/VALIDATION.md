# Sprint 20 EPUB Loading Performance Validation

## Local Checks

- `git diff --check` - PASS.
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest --tests com.qualityalternative.app.data.EpubTextExtractorTest --tests com.qualityalternative.app.data.DocumentReadingTimeEstimatorTest --tests com.qualityalternative.app.ui.DocumentImportCandidateFactoryTest --tests com.qualityalternative.app.ui.MainViewModelTest` - PASS.
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest` - PASS.
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.VisualQaScreenshotTest#captureSprint15EpubStructuredDocumentSmokeScreens` - PASS on `qaApi36`.
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest --rerun-tasks --tests com.qualityalternative.app.data.EpubTextExtractorTest --tests com.qualityalternative.app.ui.MainViewModelTest` - PASS, 28 tasks executed, see `logs/testDebugUnitTest_r2.log`.
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest --rerun-tasks` - PASS, 28 tasks executed, see `logs/testDebugUnitTest_full_r2.log`.
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.VisualQaScreenshotTest#captureSprint20EpubLoadingBusyStatesScreens` - PASS on `qaApi36`, see `logs/connected_epub_busy_states_r2.log`.
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest --rerun-tasks --tests com.qualityalternative.app.data.EpubTextExtractorTest --tests com.qualityalternative.app.ui.MainViewModelTest` - PASS after R2 race fixes, 28 tasks executed, see `logs/testDebugUnitTest_r3.log`.
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest --rerun-tasks` - PASS after R2 race fixes, 28 tasks executed, see `logs/testDebugUnitTest_full_r3.log`.
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.VisualQaScreenshotTest#captureSprint20EpubLoadingBusyStatesScreens` - PASS on `qaApi36` after removing duplicate opening snackbar, see `logs/connected_epub_busy_states_r3.log`.
- Emulator cleanup - PASS, `adb devices` returned no attached devices after `adb emu kill`.

## Targeted Test Command

```bash
./gradlew testDebugUnitTest --tests com.qualityalternative.app.data.EpubTextExtractorTest --tests com.qualityalternative.app.data.DocumentReadingTimeEstimatorTest --tests com.qualityalternative.app.ui.DocumentImportCandidateFactoryTest --tests com.qualityalternative.app.ui.MainViewModelTest
```

## Implementation Evidence

- EPUB ZIP extraction now retains only `container.xml`, `.opf`, `.ncx`, and readable HTML/XHTML entries.
- Non-reading binary EPUB resources are skipped instead of copied into memory.
- Retained EPUB text has bounded per-entry and total retained-size guards.
- EPUB anchor mapping now injects invisible markers before one readable pass, so inline/pagebreak anchors do not make later TOC anchors drift.
- Document picker EPUB candidate preparation runs on an IO coroutine with visible preparation state.
- Reader opening loads repository-backed private documents on the document work dispatcher instead of the main state path.
- Reader opening uses request tokens so stale slow parses cannot overwrite a newer reader/open state.
- Reader opening request tokens are invalidated by top-level navigation and stale failure side effects are skipped before marking documents unavailable or clearing active state.
- Reader opening now has a lightweight overlay while EPUB parsing is in progress.
- Reader opening no longer also emits a bottom snackbar with the same text; the overlay is the single opening message.
- Import preparation request tokens are invalidated by cancel/navigation, and form edits preserve `isPreparing` until the candidate result arrives or is ignored.
- User document repository caches one parsed reader document per URI/fingerprint to avoid immediate reparsing churn.

## Visual Evidence

- `evidence/sprint20_epub_loading_performance/screenshots/sprint20-epub-structured-reader-1778528644325/01_epub_structured_reader_light.png`
- `evidence/sprint20_epub_loading_performance/screenshots/sprint20-epub-structured-reader-1778528644325/02_epub_structured_reader_mid_light.png`
- `evidence/sprint20_epub_loading_performance/screenshots/sprint20-epub-structured-reader-1778528644325/03_epub_structured_reader_mid_dark.png`
- `evidence/sprint20_epub_loading_performance/screenshots/sprint20-epub-loading-performance-1778542290398/01_epub_import_preparing_light.png`
- `evidence/sprint20_epub_loading_performance/screenshots/sprint20-epub-loading-performance-1778542290398/02_reader_opening_overlay_light.png`

## R1 GPT Pro Blocker Recheck

- Inline/pagebreak anchor drift: fixed with marker-based block mapping and covered by `extractDocumentMapsLaterTocAnchorAfterInlinePagebreakWithoutDrift`.
- Retained-size guard evidence: covered by `extractThrowsWhenReadableEntryExceedsSafetyLimit` and `extractThrowsWhenAggregateReadableTextExceedsSafetyLimit`.
- Fresh unit execution evidence: covered by `logs/testDebugUnitTest_r2.log` and `logs/testDebugUnitTest_full_r2.log`, both run with `--rerun-tasks`.
- Busy-state visual evidence: covered by the Sprint 20 screenshots for import preparation and reader-opening overlay.
- Synchronous single-import path: `prepareUserDocumentImport` now prepares on the document work dispatcher and shows the same preparing state as batch import.

## R2 GPT Pro Blocker Recheck

- Slow reader open after navigation: fixed by invalidating reader-open request ids in top-level navigation and covered by `openLibraryItemIgnoresSlowPrivateReaderLoadAfterNavigationAway`.
- Stale reader-open failure side effects: fixed by checking request id before failure side effects and covered by `staleFailedPrivateReaderLoadDoesNotClearNewerOpen`.
- Import preparation after cancel/navigation: fixed by passing request ids through batch import completion and invalidating them on cancel/navigation; covered by `cancelAddDocumentImportPreparationIgnoresLateBatchResult` and `cancelSingleDocumentImportPreparationIgnoresLateCandidateResult`.
- Form edits during preparation: `updateAddDocumentForm` now preserves `isPreparing` and keeps save disabled; covered by `addDocumentEditsDoNotClearPreparingStateBeforeCandidateReturns`.
- Duplicate opening snackbar: removed by keeping `latestMessage` null while the overlay is visible; refreshed visual screenshot captured in `screenshots/sprint20-epub-loading-performance-1778542290398/`.

## GPT Pro R3 Gate

- `SCORE: 10/10`
- `VERDICT: PASS`
- `VISUAL REVIEW: PASS`
- Blockers: none.
- Review path: `evidence/sprint20_epub_loading_performance/GPT_PRO_REVIEW_R3.md`
