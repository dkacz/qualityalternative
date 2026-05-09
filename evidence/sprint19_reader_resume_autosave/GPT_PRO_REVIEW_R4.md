SCORE: 10/10

VERDICT: PASS

VISUAL REVIEW: PASS

BLOCKERS: none

R3 BLOCKER RECHECK: Fixed. The connected tests now read the actual Room row through AppContainer.readingProgressRowForTests() and ReadingProgressDao.findByContentId(). RoomReadingProgressRepository now uses compare-and-set loops for collector merge and cache/save upsert paths. The integrated Activity test delays the latest unfinished Room save, closes the Activity, reopens before Room write completion, exercises pause/stop on the reopened Activity, releases the write, and verifies the actual Room row catches up.

R2 BLOCKER RECHECK: Fixed. cachePendingProgress() makes the latest same-process anchor repository-visible before the Room write completes, and the R4 Activity test proves immediate reopen before the delayed Room write restores the latest page rather than the older durable row. Stale Room emissions are protected by monotonic CAS merge, and the repository test injects an older Room row plus older save without downgrading state.

R1 BLOCKER RECHECK: Fixed. Production MainViewModelFactory passes progressPersistenceScope = appContainer.appScope; the connected durable assertion is now Room-row based; the delayed-latest-save plus ViewModel-close unit test is present; reader composable, Room DAO/entity/database, manifest, application, factory wiring, build logs, screenshots, and stage assertion evidence are shipped.

READER RESUME AUTOSAVE: The shipped code preserves percent, paragraph index, text offset, paragraph count, and monotonic updatedAtMillis; same-percent but different-anchor progress is not collapsed; page movement, back, pause/stop, and dispose all call the progress persistence path; restored progress is re-derived from repository state when current progress changes.

STALE WRITE / RACE CHECK: The repository rejects older unfinished writes and same-timestamp earlier paragraph/offset writes before DAO upsert. Collector, cache, and save paths no longer perform non-atomic read-modify-write replacement of progress state. Completion-over-unfinished protection remains intact.

LIFECYCLE / REOPEN CHECK: Production wiring supports the realistic same-process Activity/ViewModel-close case because the persistence job runs in AppContainer.appScope, which is process-level and not Activity/ViewModel-scoped. The R4 integrated test covers delayed latest Room write, Activity close, immediate reopen before write completion, pause/stop on the reopened Activity, release of the delayed write, and actual Room-row verification. Process death is not claimed and is outside this scoped hotfix.

TEST/EVIDENCE: Sufficient. R4 unit XML reports 111 tests with zero failures, including the delayed older-save, ViewModel-close, and pending-reopen unit cases. R4 connected XML reports four tests with zero failures, including both Activity reader tests and both Room repository race tests. R4 screenshot assertions record stable page 3/12 with durable paragraph 20, and pending-reopen page 4/12 while durable Room remains at paragraph 20 until the delayed write is released and catches up to paragraph 27.

BUNDLE GAPS: none

PACKAGE HYGIENE: The bundle contains the requested production files, modified tests, R1/R2/R3 reviews, R4 diff, unit and connected XML, Gradle logs, logcat files, screenshots, and both R4 stage assertion files. The pending-reopen screenshots are byte-identical across stages, but that is consistent with the intended visible-page stability and is independently disambiguated by the shipped stage assertion file showing the durable Room row changing only after release.
