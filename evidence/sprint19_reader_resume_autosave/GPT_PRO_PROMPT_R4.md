You are doing a fresh-from-scratch adversarial audit of one scoped Android hotfix, with explicit recheck of prior BLOCK reviews.

Use only the shipped bundle as the audit base. If a claim cannot be proven from shipped files, label it `BUNDLE GAP`.

Read these files first, in order:
1. `evidence/sprint19_reader_resume_autosave/EVIDENCE.md`
2. `evidence/sprint19_reader_resume_autosave/GPT_PRO_REVIEW.md`
3. `evidence/sprint19_reader_resume_autosave/GPT_PRO_REVIEW_R2.md`
4. `evidence/sprint19_reader_resume_autosave/GPT_PRO_REVIEW_R3.md`
5. `evidence/sprint19_reader_resume_autosave/reader_resume_autosave_fix.diff`

Target scope:
- Sprint 19 Reader Resume Autosave Hotfix R4.
- Verify that reader progress cannot regress to an older page because of async save ordering, stale Room flow emissions, same-millisecond saves, same-percent-but-different-anchor cases, lifecycle pause/stop, Activity/ViewModel close, or Activity close/reopen.
- Verify that R1, R2, and R3 blockers were actually fixed, not merely documented.

Known R3 blockers to recheck:
1. The connected “durable” assertion read repository memory, not the actual Room row.
2. `RoomReadingProgressRepository` monotonic merge was not atomic because collector/cache/save used read-modify-write assignments to `progress.value`.
3. No integrated Activity test delayed the latest Room write, closed/reopened immediately before the write completed, and exercised lifecycle pause/stop/dispose on the reopened Activity.
4. Bundle gaps: production `MainActivity`, `QualityAlternativeApplication`, manifest/factory wiring, and `ReadingProgress` model semantics were not shipped.

Known R2 blockers to recheck:
1. Reopen-before-pending-write race.
2. Stale Room emission downgrade.

Known R1 blockers to recheck:
1. Latest-page save was ViewModel-scoped and could be canceled before durable write.
2. Connected test waited on immediate ViewModel state, not durable repository state.
3. There was no adversarial delayed-latest-save plus ViewModel-close test.
4. Bundle omitted reader composable and Room schema/DAO files.
5. Build logs were claimed but not shipped.
6. Screenshots were visually identical without stage-specific assertion evidence.

Your job:
1. Inspect `AndroidManifest.xml`, `MainActivity.kt`, `QualityAlternativeApplication.kt`, `ReadingProgressModels.kt`, `Contracts.kt`, `MainViewModel.kt`, `AppContainer.kt`, `QualityAlternativeApp.kt`, `RoomReadingProgressRepository.kt`, `ReadingProgressDao.kt`, `ReadingProgressEntity.kt`, `QualityAlternativeDatabase.kt`, and the modified tests.
2. Check whether connected tests now assert the actual Room row via `ReadingProgressDao.findByContentId()` / `AppContainer.readingProgressRowForTests()`, not repository memory.
3. Check whether `RoomReadingProgressRepository` now uses atomic compare-and-set loops for collector merge and pending/save upsert, and whether older/same-timestamp earlier-offset writes are rejected.
4. Check whether `sprint19ReaderResumeUsesPendingLatestProgressAcrossImmediateReopenBeforeRoomWrite` proves the integrated Activity lifecycle race: delay latest Room write, close Activity, reopen before write completion, pause/stop reopened Activity, release write, and verify the actual Room row.
5. Check whether `progressPersistenceScope = appContainer.appScope` still covers ViewModel/Activity close in the realistic same-process case.
6. Verify visual review using R4 screenshots and both R4 stage assertion files.
7. Flag any release-blocking issue, missing test, or bundle gap. Be strict, but do not require unrelated release work or process-death support unless the shipped code claims it.

Output format, exactly:

SCORE: n/10
VERDICT: PASS / REVISE / BLOCK
VISUAL REVIEW: PASS / REVISE / BLOCK
BLOCKERS: none or numbered list
R3 BLOCKER RECHECK: concise assessment
R2 BLOCKER RECHECK: concise assessment
R1 BLOCKER RECHECK: concise assessment
READER RESUME AUTOSAVE: concise assessment
STALE WRITE / RACE CHECK: concise assessment
LIFECYCLE / REOPEN CHECK: concise assessment
TEST/EVIDENCE: concise assessment
BUNDLE GAPS: none or numbered list
PACKAGE HYGIENE: concise assessment

Only give SCORE 10/10, VERDICT PASS, and VISUAL REVIEW PASS if the fix and evidence are sufficient to proceed to a new release APK for this hotfix.
