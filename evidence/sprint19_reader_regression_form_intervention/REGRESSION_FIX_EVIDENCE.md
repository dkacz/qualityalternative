# Sprint 19 Regression Fix Evidence

Date: 2026-05-07

Scope: Sprint 19 slices 19.1-19.3 before AI work.

## Fix Summary

- EPUB source blocks are globally indexed across spine documents, so chapter 2/3 no longer reuse `sourceBlockIndex = 0`.
- Reader progress resolves source positions by stable source block identity and preserves a source anchor when reader font size repaginates the page.
- Annotation start-backward movement can cross page/source boundaries without confusing later chapters with the beginning of the book.
- Form intervention disables `Open anyway` and the close icon until a visible 5-second wait completes.
- Form intervention now records shown, unlock blocked, unlock enabled, unlock used, completed, and abandoned analytics.
- Portable Profile autosave tests now assert reader progress persists both the source-position field stored as `lastVisibleParagraphIndex` and `lastVisibleTextOffset`.

## R1 Pro Review Closure

R1 review: `GPT_PRO_REVIEW_R1.md` returned `SCORE: 7/10`, `VERDICT: FAIL`, `VISUAL REVIEW: FAIL`.

R2 closes those blockers as follows:

- Profile/Drive progress persistence: `MainViewModelTest.configuredProfileAutosaveRunsAfterPortableProfileMutations` asserts autosaved profile JSON contains `lastVisibleParagraphIndex` and `lastVisibleTextOffset`; `AccountLightProfileExporterTest.exportSettingsOnlyProfileJson_includesPortableLibraryAndReadingStateWithoutRawUris` asserts exported profile preserves both fields without raw Drive/content URIs.
- Font-size repagination evidence: the EPUB fixture now has enough material to change from `5/7 · 76%` at default reader text to `8/10 · 76%` at large reader text.
- Annotation evidence: screenshots cover before movement, after cross-chapter start-back movement, saved highlight, and reopened selector.
- Form intervention analytics: unit tests cover unlock blocked, unlock enabled, unlock used, completed, and abandoned events; visual evidence also asserts disabled close/open while waiting.
- Raw validation: logs are included under `logs/`.

R2 review: `GPT_PRO_REVIEW_R2.md` returned `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS`.

Harvested source:

- `/Users/omare/Documents/qualityalternative/PRO_REVIEW_OUTPUT_SPRINT19_REGRESSION_R2_20260507_2305/Sprint_19_Review_Gate.md`

## Local Validation

- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest --tests com.qualityalternative.app.ui.MainViewModelTest --tests com.qualityalternative.app.data.AccountLightProfileExporterTest --tests com.qualityalternative.app.data.AccountLightProfileImporterTest --tests com.qualityalternative.app.data.EpubTextExtractorTest --tests com.qualityalternative.app.ui.ProgressSnapshotTest`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew assembleDebug assembleDebugAndroidTest`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#sprint19EpubProgressAndAnnotationStartStayAnchoredInLaterChapter`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#sprint19FormInterventionShowsFiveSecondUnlockBeforeOpenAnyway`

All listed R2 validation commands passed. Raw logs:

- `logs/unit_targeted_r2.log`
- `logs/assemble_debug_r2.log`
- `logs/instrumentation_reader_annotation_r2.log`
- `logs/instrumentation_form_intervention_r2.log`

## Visual Evidence

- `screenshots/reader/01_chapter_three_progress_not_one_percent.png`
- `screenshots/reader/02_chapter_three_large_font_progress_stable.png`
- `screenshots/reader/03_annotation_before_start_back.png`
- `screenshots/reader/04_annotation_start_back_without_book_start_jump.png`
- `screenshots/reader/05_annotation_saved_cross_chapter_highlight.png`
- `screenshots/reader/06_annotation_reopened_cross_chapter_selector.png`
- `screenshots/form_intervention/07_form_intervention_waiting_locked.png`
- `screenshots/form_intervention/08_form_intervention_unlock_ready.png`

## Release Boundary

AI note assistance is intentionally not included in this regression-fix scope. It remains queued for Sprint 19 after the regression APK release.
