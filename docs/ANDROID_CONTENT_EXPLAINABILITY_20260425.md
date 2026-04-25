# Android content explainability polish - 2026-04-25

Status: implemented and locally validated on `codex/android-content-explainability-polish`.

## Scope

This slice improves the intervention UX by making the primary recommendation explain itself without changing the finite replacement model.

Mapped PRD scope:

- `FR5 Recommendation Selection`: recommendation fit should be legible to the user through topic, duration, priority, and source signals.
- `FR6 Intervention UI`: the intervention still shows exactly one primary recommendation and at most two backups, with no feed or larger browsing stack.
- Content strategy: existing `whyThisNow` metadata is surfaced as user-facing context; no scraping, rehosting, runtime copyright blocking, or open-web retrieval is introduced.

## Implemented

- Added `RecommendationExplainer`, a deterministic domain helper that builds a short headline plus fit chips from:
  - item `whyThisNow`
  - individual priority picks
  - preferred topic overlap
  - preferred duration bucket fit
  - source type
- Added a compact `Why this` panel to the primary intervention card.
- Added unit coverage for editorial, saved-link, and meditation explanation cases.
- Added visual QA assertion that every captured intervention exposes the `Why this` panel.

## Review Fixes

- Compressed the intervention primary card, explanation panel, and backup rows so two finite backups or the explicit no-backups state remain visible above the bottom action row on the baseline visual device.
- Removed scroll reliance from the intervention alternatives area.
- Rewrote shipped `whyThisNow` metadata to remove internal curation phrasing such as "the user", "the product", and "backup".
- Rewrote the remaining primary-card-visible description that used internal third-person phrasing.
- Added a unit guard that rejects internal/editorial curation language in surfaced `whyThisNow` and description copy.
- Strengthened visual QA assertions to prove backup rows or the empty-backup message are above `intervention-bottom-actions`.

## Explicit Non-Goals

- No ranking behavior changes.
- No additional recommendation list or discovery feed.
- No runtime copyright gate.
- No expansion beyond Android MVP soft-intervention behavior.

## Validation

- `git diff --check`: pass.
- `RecommendationExplainerTest`: pass.
- `starter_packs.json` JSON validation: pass.
- `testDebugUnitTest`: pass.
- `lintDebug`: pass.
- `connectedDebugAndroidTest` on `emulator-5554`: pass, 50/50 tests.
- `VisualQaScreenshotTest#captureCoreContentScreensInLightAndDark`: pass.
- `VisualQaScreenshotTest#captureSprint10ReaderProgressStreakAndMeditationScreens`: pass.
- Fresh visual QA contact sheet: `docs/visual-qa/2026-04-25-android-content-explainability/contact_sheet.png`.
