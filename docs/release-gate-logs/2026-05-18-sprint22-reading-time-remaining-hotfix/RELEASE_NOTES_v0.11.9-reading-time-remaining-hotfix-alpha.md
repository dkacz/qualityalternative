# Quality Alternative v0.11.9-reading-time-remaining-hotfix-alpha

This hotfix follows `v0.11.8-profile-restore-gong-reading-time-alpha` and ships the Sprint 22 reading-time remaining repair.

## What Changed

- Home `Continue Reading` now repairs legacy imported reader documents that still carry the old capped `20 min` estimate.
- Long EPUB/Markdown documents no longer show nonsensical remaining-time labels such as `41% read · 12 min left` after progress exists.
- The repair persists the corrected `durationMinutes`, refreshes the in-memory document stream, records bounded analytics, and triggers portable profile autosave.
- Reader-open repair keeps `currentContent`, replacement routing, and manual continue analytics aligned with the corrected duration.
- Background repair is bounded to one startup cycle per ViewModel: at most ten relevant unfinished private-reader legacy candidates are scanned, and at most three are repaired.

## Review And Validation

- GPT Pro Sprint 22 R4 review: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`, blockers `none`.
- Unit evidence covers background repair, reader-open repair, autosave, duplicate analytics/autosave suppression, failed-candidate continuation, and no-cascade scheduler behavior.
- Connected visual evidence proves the Home card changes from `41% read · 12 min left` to `41% read · 1 hr 20 min left`.
- APK evidence includes `versionCode=25`, `versionName=0.11.9-alpha`, signature verification, SHA-256 hash, emulator install, cold launch, and shutdown confirmation.

## Changelog Versus `v0.11.8-profile-restore-gong-reading-time-alpha`

- Keeps the profile restore, meditation gong, and import-time reading estimate fixes from v0.11.8.
- Adds a migration-style repair for already-imported long books that still had stale short duration metadata.
- Prevents the Home continue card from using stale legacy duration when computing remaining reading time.
- Adds stricter scheduler bounds so repair cannot cascade across a large library on startup.
