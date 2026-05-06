# Sprint 17 Slice 17.5 Validation

Scope: cross-page annotation selection and compact range controls.

## Automated Checks

- PASS: `JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18 ./gradlew testDebugUnitTest --tests com.qualityalternative.app.ui.ProgressSnapshotTest`
- PASS: `JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18 ./gradlew connectedDebugAndroidTest --rerun-tasks -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#readerAnnotationEditorSavesEditsAndShowsPreview,com.qualityalternative.app.MainActivityTest#readerAnnotationControlsExpandAndReopenAcrossPages`

Logs:

- `logs/unit_validation.log`
- `logs/instrumentation_validation.log`

## Visual Evidence

Screenshots are in `screenshots/sprint17-cross-page-annotation-1778079063962/`:

- `01_compact_controls_first_page_light.png`
- `02_after_end_later_clicks_light.png`
- `03_compact_controls_later_page_light.png`
- `04_long_quote_scroll_region_light.png`
- `05_reopened_cross_page_quote_light.png`

## Implementation Notes

- Range changes are source-offset based and page-aware. Expanding the end or start can move the reader viewport to the page containing the changed boundary without dismissing the overlay.
- Selection source offsets now handle both full-source split chunks and local display chunks, so saved selectors remain absolute to the source text.
- Start/end controls are a compact icon-first mini bar in the sheet header with accessibility descriptions and stable test tags; they no longer consume a standalone overlay row.
- R1 GPT Pro blocker recheck: each compact visual arrow now keeps a 44dp outer touch target while rendering a 22dp visual icon button. The instrumentation test asserts all four range-control hit targets are at least 44dp wide and tall.
- The annotation sheet grows up to most of the reader viewport for long selected quotes; the quote body then scrolls inside the sheet instead of truncating.
- Existing note create/edit flow was revalidated after the selection changes.
