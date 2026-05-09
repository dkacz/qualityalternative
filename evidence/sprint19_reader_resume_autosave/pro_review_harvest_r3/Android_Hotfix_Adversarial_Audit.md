SCORE: 8/10
VERDICT: BLOCK
VISUAL REVIEW: PASS

BLOCKERS:

The R3 connected “durable” assertion is not durable. durableSavedProgressPercentFor() and durableSavedProgressParagraphIndexFor() read app.appContainer.readingProgressRepository.readingProgress(), but RoomReadingProgressRepository.readingProgress() returns the in-memory MutableStateFlow, which is now updated by cachePendingProgress() before any Room write begins and by saveProgress() before dao.insertOrReplace(). This means the connected test can still pass before the Room row has been written.

RoomReadingProgressRepository does not make its monotonic merge atomic. The collector, cachePendingProgress(), and saveProgress() all perform read-modify-write assignments to progress.value; the collector does not use MutableStateFlow.update or the writeMutex. A stale Room emission computed from an older progress.value can therefore overwrite a newer cached anchor in an adversarial thread interleaving, which is the same downgrade class R2 was meant to eliminate.

R3 does not ship an integrated adversarial Activity close/reopen test with a delayed latest Room write, immediate Activity reopen before that write completes, and lifecycle pause/stop/dispose on the reopened Activity. The new ViewModel unit test covers a fresh ViewModel with a fake repository, and the Room test covers repository behavior, but the full Activity/lifecycle race remains unproven.

R2 BLOCKER RECHECK: Partially fixed, but not closed. cachePendingProgress() does make the newest visible anchor repository-visible synchronously, and the deterministic Room stale-emission test verifies that an older emitted row and older save do not downgrade state under normal ordering. However, the repository merge is not atomic, and the immediate Activity close/reopen-before-Room-write path is still not covered by an integrated instrumented test.

R1 BLOCKER RECHECK: R1 blocker 1 is materially fixed for the same-process case because MainViewModelFactory passes progressPersistenceScope = appContainer.appScope, so progress writes are no longer ViewModel-scoped. R1 blocker 2 is not actually fixed after R3 because the connected test reads repository-visible state, not a durable Room row. R1 blocker 3 is addressed by the delayed-latest-save plus ViewModel-close unit test. R1 blockers 4 and 5 are addressed by shipped reader composable, DAO/entity/database files, and build logs. R1 blocker 6 is improved by stage-specific assertion text, although the R3 screenshots themselves are byte-identical.

READER RESUME AUTOSAVE: The reader autosave path is substantially improved: page movement, back, pause/stop, and dispose all call the progress callback; ReadingProgress preserves percent, paragraph index, text offset, paragraph count, and monotonic timestamp; same-percent-but-different-paragraph saves are not collapsed; and ReaderScreen re-derives its initial page from restored progress when repository progress changes. Same-millisecond reader saves from one ViewModel are handled by nextReadingProgressUpdatedAtMillis().

STALE WRITE / RACE CHECK: Older unfinished saves are rejected when repository state already holds a newer unfinished anchor, and cachePendingProgress() closes the ordinary same-process immediate reopen window by exposing the latest anchor before Room persistence completes. The remaining race is repository-internal: stale Room collector emissions and pending-cache updates are not serialized or atomically merged, so the shipped source does not prove that a stale flow emission cannot temporarily downgrade progress.value under adversarial interleaving. The same-timestamp repository tie-breaker also compares paragraph index but not text offset; MainViewModel normally prevents this for reader saves, but the repository alone does not prove same-paragraph offset monotonicity.

LIFECYCLE / REOPEN CHECK: progressPersistenceScope = appContainer.appScope covers ViewModel and Activity close in the realistic same-process case, assuming the production Activity uses the shipped MainViewModelFactory. It does not cover process death, which is outside the scoped same-process hotfix. The immediate reopen-before-write scenario is improved by repository-visible pending cache, but not fully proven because the connected test does not delay the Room write and does not validate the actual Room row.

TEST/EVIDENCE: R3 ships passing unit evidence for 111 MainViewModelTest cases, including delayed older save, ViewModel close before write completion, and fresh ViewModel reopen before pending write completion. R3 ships passing connected evidence for three tests: the reader reopen flow and two Room repository tests. The Room tests are useful but deterministic; they do not exercise atomicity under concurrent collector/cache interleaving. The connected reader test checks page and paragraph anchor, but its “durable” helper reads in-memory repository state and therefore does not prove durable persistence.

BUNDLE GAPS:

BUNDLE GAP: Production MainActivity, QualityAlternativeApplication, manifest, and actual Activity ViewModel factory wiring are not shipped, so the Activity-to-appContainer.appScope production lifetime cannot be source-audited from the bundle.

BUNDLE GAP: The domain model file defining ReadingProgress, including isCompleted() and isUnfinished(), is not shipped, so completion/unfinished semantics are inferred from usage and tests rather than directly audited from source.

PACKAGE HYGIENE: The bundle is well scoped and includes the requested hotfix diff, target source files, modified tests, R1/R2 reviews, R3 logs, XML reports, screenshots, and stage assertion text. Build logs are now shipped. The R3 screenshots are byte-identical across all three stages, but the stage assertion file records distinct saved, pause/stop restore, and reopen assertions with matching page, percent, page-end paragraph, and repository-visible paragraph.