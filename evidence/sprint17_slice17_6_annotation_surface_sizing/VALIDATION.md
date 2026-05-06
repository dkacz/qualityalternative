# Sprint 17 Slice 17.6 R2 Validation

Scope: annotation note surface sizing for long selected quotes, long notes, and keyboard-visible editing.

## R1 Recheck

- R1 GPT Pro review: `R1_GPT_PRO_REVIEW.md`
- R1 score/verdict: 8/10, FAIL, VISUAL REVIEW FAIL.
- R1 blocker: keyboard-visible long-note screenshot clipped the top of the editor; Note header, compact range controls, and close action were partially off-screen.
- R2 fix: the activity now uses `adjustResize`, the overlay detects when the root has already been resized for IME before applying any extra keyboard offset, and range buttons use a smaller compact footprint.

## Automated Checks

- PASS: `JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18 ./gradlew testDebugUnitTest --tests com.qualityalternative.app.ui.ProgressSnapshotTest`
- PASS: `JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18 ./gradlew connectedDebugAndroidTest --rerun-tasks -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#readerAnnotationEditorSavesEditsAndShowsPreview,com.qualityalternative.app.MainActivityTest#readerAnnotationControlsExpandAndReopenAcrossPages,com.qualityalternative.app.MainActivityTest#readerAnnotationEditorContainsLongQuoteAndLongNoteWithinViewport`

Logs:

- `logs/unit_validation.log`
- `logs/instrumentation_validation.log`

## Visual Evidence

Annotation surface screenshots are in `screenshots/sprint17-annotation-surface-1778083622313/`:

- `01_long_quote_surface_light.png`
- `02_long_note_surface_light.png`

Cross-page regression screenshots are in `screenshots/sprint17-cross-page-annotation-1778083604958/`:

- `01_compact_controls_first_page_light.png`
- `02_after_end_later_clicks_light.png`
- `03_compact_controls_later_page_light.png`
- `04_long_quote_scroll_region_light.png`
- `05_reopened_cross_page_quote_light.png`

## Implementation Notes

- The annotation editor now applies IME-aware bottom padding only when the root has not already resized for the keyboard, and caps the sheet to the available viewport so the surface can grow without pushing or repaginating the underlying reader.
- MainActivity declares `windowSoftInputMode="adjustResize"` so keyboard editing resizes the usable reader viewport instead of panning the whole annotation sheet off the top of the screen.
- Range arrows use a compact icon footprint in the header row and no longer steal a standalone row or dominate the note surface.
- Long selected quotes use an internal scroll region with a bounded maximum height, leaving note input and actions visible.
- Long note input is height-bounded and scrolls internally; Save/Cancel remain visible above the keyboard in the captured long-note state.
- The shared multiline field now adapts its minimum height when a constrained overlay passes a smaller maximum height, avoiding invalid min/max constraints while preserving the default 92dp input height elsewhere.
- Annotation note entry tests now tap the note field before typing and assert Save is enabled before committing, matching the real user interaction and guarding against focus regressions.
- Existing annotation create/edit and cross-page selection reopen flows were revalidated together with the new long quote/long note surface test.
