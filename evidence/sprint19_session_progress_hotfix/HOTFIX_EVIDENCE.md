# Sprint 19 Session Progress Hotfix Evidence

Date: 2026-05-08

Scope:

- Fix late tester regression where reading for a while, locking/reopening, and returning to the app could restore the pre-session reader position instead of the last viewed page.
- Restore meditation as a visible finite backup alternative when reading content dominates the intervention recommendations.
- Keep AI note assistance blocked until this hotfix is reviewed and released.

Implementation summary:

- `ReaderScreen` now persists source-anchored progress on every page move, including backward moves.
- `ReaderScreen` also refreshes progress on lifecycle pause/stop and reader disposal using the latest visible source position.
- `ReaderScreen` now saves the latest visible position before invoking reader back/skip, so the save captures the active content before `skipReading()` clears the session.
- `MainViewModel.saveCurrentReadingProgress` now refreshes durable storage for same-visible-position lifecycle saves while avoiding duplicate `READING_PROGRESS_SAVED` analytics events.
- `MainViewModel.saveCurrentReadingProgress` now ignores late non-completed lifecycle/disposal writes after a content item has been completed, preventing completion downgrade.
- `RoomReadingProgressRepository.saveProgress` now enforces completion dominance at the final write boundary: an unfinished write cannot replace an already completed row.
- Reactivating completed content deletes the old completed progress row before a deliberate reread, so completion dominance does not block intentional new progress.
- `DefaultRecommendationEngine` keeps an eligible meditation reset in the finite backup list when the primary recommendation is not meditation.
- `PRD.md` and the Sprint 19 plan record both product rules.

Validation:

- Unit: `testDebugUnitTest` for same-position lifecycle progress refresh and meditation backup retention passed.
- Unit: `testDebugUnitTest` for late incomplete lifecycle save after completion passed; completed progress remains `100%` with `completedAtMillis`.
- Unit: `testDebugUnitTest` for delayed unfinished lifecycle save interleaved with in-flight completion passed; the final row remains completed after the delayed unfinished save resumes.
- Build: `assembleDebug assembleDebugAndroidTest` passed.
- Connected E2E: `sprint19ReaderSessionProgressPersistsLastViewedPageAfterReopen` passed; it now covers forward navigation, one backward move, explicit Activity pause/stop via `CREATED`, resume, scenario close, and reopen.
- Connected E2E: `sprint19InterventionKeepsMeditationAlternativeWhenPrimaryIsReading` passed.

Visual evidence:

- `screenshots/09_session_progress_saved_before_pause_stop.png`: reader is on page `3/12`, `26%`, showing paragraphs 15-21 after forward navigation and one backward move.
- `screenshots/10_session_progress_restored_after_pause_stop.png`: after Activity pause/stop and resume, reader remains on page `3/12`, `26%`.
- `screenshots/11_session_progress_restored_after_reopen.png`: after scenario close/reopen, reader restores to page `3/12`, `26%` and the same visible source text.
- `screenshots/12_meditation_backup_alternative.png`: intervention primary is reading content and the finite backup list visibly includes `3-minute reset`.

GPT Pro review trail:

- `reviews/GPT_PRO_REVIEW_R1.md` returned `SCORE: 8/10`, `VERDICT: FAIL`, `VISUAL REVIEW: PASS`.
- R1 blocker closed in R2: disposal persistence is now exit-order safe for back/skip and completion-aware for late lifecycle/disposal saves.
- R1 evidence-label mismatch closed in R2: screenshots and test now explicitly cover Activity pause/stop/resume.
- `reviews/archive/GPT_PRO_REVIEW_R2_PARTIAL.md` was a dead partial lane without `SCORE` or `VERDICT`; it is archived and is not treated as completed review evidence.
- `reviews/GPT_PRO_REVIEW_R3.md` returned `SCORE: 8/10`, `VERDICT: FAIL`, `VISUAL REVIEW: PASS`.
- R3 blocker closed in R4: completion dominance now lives in `RoomReadingProgressRepository.saveProgress`, the final write boundary, with a delayed-save regression test.
- `reviews/GPT_PRO_REVIEW_R4.md` returned `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`, `BLOCKERS: None`.

Logs:

- `logs/unit.log`
- `logs/assemble.log`
- `logs/connected_session_progress.log`
- `logs/connected_meditation_backup.log`

Known boundary:

- This hotfix does not implement AI note assistance.
- This hotfix does not alter Google Drive authorization or annotation sync behavior.
