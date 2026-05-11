# Sprint 20 - EPUB Loading Performance

## Problem

Large EPUB files can make the app feel frozen during import or reader open. The risky paths are:

- document picker candidate creation extracting reading time on the UI path,
- reader opening parsing private EPUB content synchronously,
- EPUB extraction retaining every ZIP entry, including binary images, before choosing readable spine files,
- anchor mapping recalculating readable text from the beginning of a chapter for every anchor.

## Scope

- Keep EPUB import and reader open responsive for large files.
- Ignore non-reading binary EPUB resources during extraction.
- Keep private Markdown and EPUB reader behavior intact.
- Fail safely for pathological text entries instead of exhausting memory.
- Add targeted unit tests for binary-heavy EPUBs, anchor-heavy EPUBs, import preparation state, and background reader loading.
- Add R2 regression coverage for inline/pagebreak anchors, retained-size guard failures, stale reader-open races, and visual busy states.

## Acceptance

- Picking EPUBs prepares candidates off the main UI path.
- Opening a private EPUB loads reader content off the main UI path.
- Opening a private EPUB shows a lightweight loading overlay instead of freezing the previous screen.
- EPUB parser retains only package, TOC, and readable HTML entries.
- Binary-heavy EPUB fixtures still extract readable spine text.
- Anchor-heavy EPUB fixtures preserve TOC-to-block mapping.
- Inline/pagebreak anchors inside paragraphs do not shift later TOC anchors.
- Oversized retained EPUB entries and aggregate retained readable text fail safely.
- Slow EPUB opens cannot overwrite newer UI state after navigation or another open request.
- Slow failed EPUB opens cannot clear a newer open or mark stale content unavailable.
- Canceled document imports cannot repopulate the import form when candidate extraction finishes late.
- The reader-opening overlay is the only visible opening message; no duplicate snackbar competes with it.
- Automated tests cover the changed logic where the local Java runtime allows execution.
- Visual evidence captures both import preparation and reader-opening overlay states.
