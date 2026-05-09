SCORE: 8/10

VERDICT: BLOCK

VISUAL REVIEW: PASS

BLOCKERS:

The R2 fix is sufficient for the same-process ViewModel-close case only after the app-scope save is allowed to complete, but it does not prove or prevent the stricter Activity close/reopen race where the Activity reopens before the pending latest save has reached the repository. In that window, the reopened reader can initially restore from the older repository value; if the stale page is then saved on pause/stop/dispose with a later updatedAtMillis, RoomReadingProgressRepository.saveProgress() will accept it as newer and can overwrite the pending latest anchor.

RoomReadingProgressRepository rejects older writes only against its current in-memory progress.value, while its Room collector assigns progress.value = loadedProgress without a monotonic merge. A stale Room emission can temporarily downgrade repository state; the ViewModel active override protects the currently open reader UI, but the repository write guard itself is not robust against a delayed older write that arrives while repository state has been downgraded. No shipped test simulates stale Room flow emission plus delayed older save.

R1 BLOCKER RECHECK: R1 blockers 1, 2, 3, 4, 5, and 6 are materially addressed: production MainViewModelFactory now passes progressPersistenceScope = appContainer.appScope; the connected test waits on readingProgressRepository.readingProgress() and checks the paragraph anchor; the new unit test covers delayed latest save plus ViewModel close before release; QualityAlternativeApp.kt, DAO/entity/database files, logs, and stage assertion evidence are shipped. The remaining gap is stricter than R1: reopen while the latest app-scope write is still pending, plus stale Room emission interaction.

READER RESUME AUTOSAVE: The reader autosave path now preserves paragraph index and text offset, updates in-memory UI state immediately, handles same-percent/different-anchor saves, persists on page movement, pause/stop, dispose, and back, and can remap the reader when state.currentReadingProgress changes after the screen is already open because restoredProgress, initialPageIndex, hasManualReaderNavigation, and currentPageIndex are keyed to the restored progress paragraph.

STALE WRITE / RACE CHECK: Same-millisecond saves from the same ViewModel are handled by nextReadingProgressUpdatedAtMillis(), and delayed older saves are rejected when repository state already reflects the newer unfinished progress. The repository still trusts mutable in-memory state that can be replaced by Room flow emissions, so the no-regression guarantee is not complete under adversarial stale-flow ordering.

LIFECYCLE / REOPEN CHECK: Pause/stop/dispose save triggers are present in ReaderScreen, and app-scope persistence prevents ViewModel cancellation from killing the latest save in the normal same-process close case. The connected reopen test is non-adversarial because it waits for the durable anchor before lifecycle close and full reopen. There is no shipped test for Activity close, immediate reopen before the delayed latest save completes, then stale-page pause/stop/dispose.

TEST/EVIDENCE: Unit XML shows 110 passing MainViewModelTest cases, including the new delayed older-save and ViewModel-close durability test. Connected XML shows the scoped reader resume test and Room stale-write test passing. The connected test now reads readingProgressRepository.readingProgress() rather than immediate ViewModel state, and the repository updates that state after dao.insertOrReplace(), so the durable-anchor assertion is materially stronger than R1. Missing adversarial coverage remains for pending-save reopen and stale Room emission downgrade.

BUNDLE GAPS: none

PACKAGE HYGIENE: The R2 bundle is well scoped and includes the requested source files, modified tests, diff, unit and connected XML, Gradle logs, screenshots, and reader_resume_stage_assertions.txt. Screenshots 09 and 10 are byte-identical and screenshot 11 is visually the same page, but the stage assertion file records separate saved, pause/stop restore, and reopen assertions with matching page, progress, page-end paragraph, and durable saved paragraph.