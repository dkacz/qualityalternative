# Sprint 16 Slice 16.5 Adaptive Reader Evidence

Generated on 2026-05-05 against the `qaApi36` Android emulator.

## Verification

- `./gradlew testDebugUnitTest`
- `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#readerFontSizeSettingUsesAppLevelPreference,com.qualityalternative.app.MainActivityTest#crossPageAnnotationSelectionPersistsAcrossPagedSourceChunks,com.qualityalternative.app.MainActivityTest#homeReadNowOpensLibraryAndPaginatedReaderWithoutIntervention,com.qualityalternative.app.MainActivityTest#epubReaderUsesKindlePagingAndTableOfContentsNavigation,com.qualityalternative.app.MainActivityTest#readerAnnotationEditorSavesEditsAndShowsPreview,com.qualityalternative.app.MainActivityTest#annotationLibraryListsSavedNotesAndOpensSourceFragment`
- Compact viewport check at `720x1280`, density `320`: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#readerFontSizeSettingUsesAppLevelPreference`

All commands passed. The emulator viewport was reset to its physical size and density after the compact check.

## Coverage

- App-level reader font size setting persists and drives in-reader typography.
- Page budget adapts to measured reader viewport width/height, screen density via dp conversion, and app-level font size.
- Split block size adapts from the same page budget so long source material can fill the available page without a scroll fallback.
- Small-font page evidence fills the reader surface on both the standard emulator and compact `720x1280` override.
- Cross-page annotation selector highlights the saved source range across page chunks with visible page continuity (`anchor37` followed by `anchor38`) and no skipped source tokens.
- Annotation Library returns to the source using explicit selectors when present and paragraph fallback for older annotations.
- EPUB table of contents and minimal paginated reader chrome remain covered.

## Screenshots

- `screenshots/sprint16-adaptive-reader-1777972477107/00_reader_font_setting_xl.png`
- `screenshots/sprint16-adaptive-reader-1777972477107/01_reader_xl_font_light.png`
- `screenshots/sprint16-adaptive-reader-1777972477107/02_reader_small_font_light.png`
- `screenshots/sprint16-adaptive-reader-1777972649251_compact_720x1280/00_reader_font_setting_xl.png`
- `screenshots/sprint16-adaptive-reader-1777972649251_compact_720x1280/01_reader_xl_font_light.png`
- `screenshots/sprint16-adaptive-reader-1777972649251_compact_720x1280/02_reader_small_font_light.png`
- `screenshots/sprint16-adaptive-reader-1777972450142/03_cross_page_annotation_page_one_light.png`
- `screenshots/sprint16-adaptive-reader-1777972450142/04_cross_page_annotation_page_two_light.png`
- `screenshots/sprint14-reader-pagination-1777972593291/02_reader_page_one_light.png`
- `screenshots/sprint14-reader-pagination-1777972593291/03_reader_next_page_light.png`
- `screenshots/sprint14-reader-annotation-1777972613038/01_reader_annotation_editor_light.png`
- `screenshots/sprint14-annotation-library-1777972609320/04_annotation_fragment_jump_dark.png`
