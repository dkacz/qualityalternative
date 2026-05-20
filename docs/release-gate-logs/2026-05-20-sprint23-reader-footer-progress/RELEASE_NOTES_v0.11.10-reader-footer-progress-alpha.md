# Quality Alternative v0.11.10-reader-footer-progress-alpha

This hotfix follows `v0.11.9-reading-time-remaining-hotfix-alpha` and ships the Sprint 23 reader footer progress bar repair.

## What Changed

- Reader footer progress track now uses a deterministic fixed width instead of loose min/max constraints.
- The visible footer progress fill is driven by the same displayed percent used in labels such as `287/618 · 46%`.
- Footer progress now exposes `ProgressBarRangeInfo` for automated verification.
- E2E coverage now measures rendered fill width divided by rendered track width before and after reader-font repagination.
- Visual evidence confirms the footer bar remains aligned with the displayed percent in default and large reader text states.

## Review And Validation

- GPT Pro Sprint 23 R4 review: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`, blockers `none`.
- JVM regression covers `46 -> 0.46` progress-fraction mapping.
- Connected visual E2E asserts semantic progress and rendered fill/track ratio within tolerance.
- APK evidence includes `versionCode=26`, `versionName=0.11.10-alpha`, signature verification, SHA-256 hash, emulator install, cold launch, and shutdown confirmation.

## Changelog Versus `v0.11.9-reading-time-remaining-hotfix-alpha`

- Keeps the Sprint 22 reading-time remaining repair.
- Adds a focused reader footer progress-bar fix for percent/bar visual mismatch.
- Adds stronger UI regression coverage for footer progress geometry.
- Cleans the Sprint 23 evidence packet so the review bundle matches the shipped visual artifacts.
