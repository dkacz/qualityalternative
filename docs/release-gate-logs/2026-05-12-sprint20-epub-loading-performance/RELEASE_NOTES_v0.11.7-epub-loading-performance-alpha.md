# Quality Alternative v0.11.7-epub-loading-performance-alpha

This hotfix follows `v0.11.6-reader-resume-autosave-alpha` and ships the Sprint 20 EPUB loading performance repair.

## What Changed

- EPUB import now avoids retaining non-reader assets such as large images, fonts, and binary payloads in memory.
- Readable XHTML/HTML entry retention is bounded per entry and across the whole EPUB so oversized files fail fast instead of hanging the app.
- EPUB anchor extraction now maps table-of-contents anchors in a single readable pass, including inline page breaks, without shifting later reader targets.
- Large private reader documents now open through a visible non-blocking loading overlay instead of doing heavy text extraction on the UI path.
- Import preparation now has a clear busy state and ignores stale file-picker/import completions after navigation or cancellation.
- Reader-open races now ignore stale success and failure callbacks when another book is opened or the user leaves the flow.

## Review And Validation

- GPT Pro EPUB loading performance review R3: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`, blockers `none`.
- Unit evidence covers bounded EPUB retention, large readable-entry failures, aggregate text limits, anchor stability, stale import cancellation, and stale reader-open cancellation.
- Connected visual evidence covers the import preparing state and reader opening overlay for large EPUB flows.
- Package hygiene keeps only the current R3 review bundle and Sprint 20 evidence trail.

## Changelog Versus `v0.11.6-reader-resume-autosave-alpha`

- Keeps the reader resume autosave durability repair shipped in v0.11.6.
- Adds bounded EPUB parsing so large EPUBs no longer freeze the app through asset-heavy retention.
- Adds reader/import busy states so long EPUB operations are visible and cancellable from the user flow.
- Preserves EPUB reader correctness by rechecking anchor mapping after the performance changes.
