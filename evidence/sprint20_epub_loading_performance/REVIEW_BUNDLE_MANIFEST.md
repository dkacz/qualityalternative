# Sprint 20 GPT Pro Review Bundle Manifest

## Review Scope

This bundle is scoped to the EPUB loading performance hotfix only.

## Included

- `docs/SPRINT_20_EPUB_LOADING_PERFORMANCE.md`
- `evidence/sprint20_epub_loading_performance/VALIDATION.md`
- `evidence/sprint20_epub_loading_performance/GPT_PRO_REVIEW.md`
- `evidence/sprint20_epub_loading_performance/GPT_PRO_REVIEW_R2.md`
- `evidence/sprint20_epub_loading_performance/CURRENT_DIFF.patch`
- `evidence/sprint20_epub_loading_performance/logs/testDebugUnitTest.log`
- `evidence/sprint20_epub_loading_performance/logs/testDebugUnitTest_r2.log`
- `evidence/sprint20_epub_loading_performance/logs/testDebugUnitTest_full_r2.log`
- `evidence/sprint20_epub_loading_performance/logs/testDebugUnitTest_r3.log`
- `evidence/sprint20_epub_loading_performance/logs/testDebugUnitTest_full_r3.log`
- `evidence/sprint20_epub_loading_performance/logs/connected_epub_visual.log`
- `evidence/sprint20_epub_loading_performance/logs/connected_epub_busy_states_r2.log`
- `evidence/sprint20_epub_loading_performance/logs/connected_epub_busy_states_r3.log`
- `evidence/sprint20_epub_loading_performance/screenshots/sprint20-epub-structured-reader-1778528644325/*.png`
- `evidence/sprint20_epub_loading_performance/screenshots/sprint20-epub-loading-performance-1778542290398/*.png`
- Changed and directly relevant production files:
  - `app/src/main/java/com/qualityalternative/app/data/EpubTextExtractor.kt`
  - `app/src/main/java/com/qualityalternative/app/data/DocumentReadingTimeEstimator.kt`
  - `app/src/main/java/com/qualityalternative/app/data/RoomUserDocumentRepository.kt`
  - `app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt`
  - `app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt`
  - `app/src/main/java/com/qualityalternative/app/ui/DocumentImportCandidateFactory.kt`
- Changed and directly relevant test files:
  - `app/src/androidTest/java/com/qualityalternative/app/VisualQaScreenshotTest.kt`
  - `app/src/test/java/com/qualityalternative/app/data/DocumentReadingTimeEstimatorTest.kt`
  - `app/src/test/java/com/qualityalternative/app/ui/DocumentImportCandidateFactoryTest.kt`
  - `app/src/test/java/com/qualityalternative/app/data/EpubTextExtractorTest.kt`
  - `app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt`

## Excluded

- Full repository history, prior sprint review bundles, APK artifacts, and screenshots unrelated to EPUB loading performance.
- Full broad connected-test suite; the bundle includes focused EPUB reader and busy-state visual runs plus full unit tests.
- Superseded R2 review ZIP and the older duplicate busy-state screenshot directory; R3 keeps the post-fix screenshot set only.
