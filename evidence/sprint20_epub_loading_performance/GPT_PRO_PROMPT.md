# GPT Pro Review Request - Sprint 20 EPUB Loading Performance R3

You are reviewing a focused Android hotfix for Quality Alternative.

Read `evidence/sprint20_epub_loading_performance/REVIEW_BUNDLE_MANIFEST.md` first, then inspect the changed files, diff, validation logs, and screenshots in the bundle.

This is R3 after:

- R1 score `7/10 FAIL`: inline/pagebreak anchor drift, missing retained-size guard tests, up-to-date-only unit evidence, missing busy-state visual evidence, possible synchronous single-import path, and stale-reader-open race risk.
- R2 score `8/10 FAIL`: incomplete stale reader-open navigation guard, unguarded stale reader-open failure side effects, import-preparation cancellation/stale-state risk, and duplicate opening snackbar under the overlay.

Recheck R1 and R2 blockers explicitly.

## User Problem

The user reported that EPUB loading takes a long time and larger EPUBs can freeze the app.

## Intended Fix

- EPUB import candidate preparation runs off the main UI path.
- Reader opening for repository-backed private documents runs off the main UI path and shows a lightweight loading overlay.
- EPUB extraction retains only package/TOC/readable HTML entries instead of every ZIP entry.
- Large binary resources inside EPUBs are ignored instead of copied into memory.
- Retained text has safety bounds to avoid pathological memory use.
- Anchor-heavy EPUBs avoid repeated full-prefix reparsing without drifting later TOC anchors after inline/pagebreak anchors.
- Single-file and batch document import preparation run off the UI path with a visible preparing state.
- Reader opening is request-token guarded so a stale slow parse or stale failed parse cannot overwrite/clear newer UI state, including after top-level navigation.
- Import preparation is request-token guarded so stale batch or single-file results cannot repopulate AddDocument after cancel/navigation; edits during preparation preserve the busy state.
- The reader-opening overlay is the only opening message; the duplicate snackbar was removed.
- One parsed reader document is cached per URI/fingerprint to avoid immediate reparsing churn.

## Review Scope

Deep-review only Sprint 20 EPUB loading performance and the listed changed files. Do not review unrelated older sprint behavior unless this patch regresses it.

## Required Checks

1. Verify the implementation plausibly prevents UI freezes for binary-heavy or larger EPUBs.
2. Check whether any heavy work can still run on the main state/UI path.
3. Check EPUB correctness risks: spine order, nav/NCX TOC, anchors, skipped binary resources, bounds, and fallback behavior.
4. Check the new UI states for import preparation and reader opening.
5. Review the included unit and connected visual evidence.
6. Flag any missing evidence as `BUNDLE GAP`; do not infer unavailable live behavior.
7. Check package hygiene for stale/noisy files in this review packet.
8. Recheck every R1 and R2 blocker and say whether it is resolved.

## Output Format

Return exactly these sections:

- `SCORE: x/10`
- `VERDICT: PASS` or `VERDICT: FAIL`
- `VISUAL REVIEW: PASS` or `VISUAL REVIEW: FAIL`
- `BLOCKERS`
- `R1 BLOCKER RECHECK`
- `R2 BLOCKER RECHECK`
- `EPUB PERFORMANCE`
- `ASYNC UI / FREEZE RISK`
- `EPUB CORRECTNESS`
- `TEST/EVIDENCE`
- `BUNDLE GAPS`
- `PACKAGE HYGIENE`
- `RELEASE READINESS`

Use `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS` only if there are no blockers and the patch is ready to commit as a hotfix slice.
