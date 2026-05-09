# Quality Alternative v0.11.6-reader-resume-autosave-alpha

This hotfix follows `v0.11.5-meditation-calm-alternative-alpha` and ships the reader resume autosave repair before Sprint 19 AI note-assist work continues.

## What Changed

- Reader progress writes now survive Activity/ViewModel close by using the application persistence scope.
- Reader position updates are visible immediately for same-process reopen while the Room write is still pending.
- Room progress hydration now monotonic-merges newer in-memory anchors instead of allowing stale database emissions to downgrade the current page.
- Older unfinished saves, same-timestamp earlier paragraphs, and same-timestamp earlier text offsets are rejected before Room upsert.
- Reader restore recalculates from updated repository progress after the reader is already open.
- Connected regression coverage now verifies the actual Room row, not only ViewModel or repository memory.
- A new integrated lifecycle test covers delayed latest Room write, Activity close, immediate reopen before the write completes, pause/stop on the reopened Activity, write release, and actual Room-row catch-up.

## Review And Validation

- GPT Pro reader resume autosave review R4: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`, blockers `none`.
- Unit evidence: 111 `MainViewModelTest` cases passing.
- Connected evidence: 4 Android tests passing across reader resume, immediate pending-write reopen, stale Room emission, older write rejection, and same-timestamp offset ordering.
- Visual evidence confirms stable reader page restore and pending-write reopen behavior.

## Changelog Versus `v0.11.5-meditation-calm-alternative-alpha`

- Keeps the dedicated calm meditation alternative shipped in v0.11.5.
- Adds a reader resume durability hotfix for lock/reopen sessions.
- Reduces the risk of losing the latest reading page after screen lock, pause/stop, or rapid Activity recreation.
- Keeps Sprint 19 AI note assistance gated until after this APK release.
