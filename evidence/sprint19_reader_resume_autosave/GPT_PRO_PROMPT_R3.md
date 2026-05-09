You are doing a fresh-from-scratch adversarial audit of one scoped Android hotfix, with explicit recheck of prior BLOCK reviews.

Use only the shipped bundle as the audit base. If a claim cannot be proven from shipped files, label it `BUNDLE GAP`.

Read these files first, in order:
1. `evidence/sprint19_reader_resume_autosave/EVIDENCE.md`
2. `evidence/sprint19_reader_resume_autosave/GPT_PRO_REVIEW.md`
3. `evidence/sprint19_reader_resume_autosave/GPT_PRO_REVIEW_R2.md`
4. `evidence/sprint19_reader_resume_autosave/reader_resume_autosave_fix.diff`

Target scope:
- Sprint 19 Reader Resume Autosave Hotfix R3.
- Verify that reader progress cannot regress to an older page because of async save ordering, stale Room flow emissions, same-millisecond saves, same-percent-but-different-anchor cases, lifecycle pause/stop, Activity/ViewModel close, or Activity close/reopen.
- Verify that R1 and R2 blockers were actually fixed, not merely documented.

Known R2 blockers to recheck:
1. Reopen-before-pending-write race: the R2 fix did not prove/prevent an Activity close/reopen where the Activity reopens before the pending latest save reaches the repository. In that window, a stale page could be restored and then saved with a later timestamp.
2. Stale Room emission downgrade: `RoomReadingProgressRepository` assigned `progress.value = loadedProgress`, so a stale Room emission could temporarily downgrade repository state and allow a delayed older write.

Known R1 blockers to recheck:
1. Latest-page save was ViewModel-scoped and could be canceled before durable write.
2. Connected test waited on immediate ViewModel state, not durable repository state.
3. There was no adversarial delayed-latest-save plus ViewModel-close test.
4. Bundle omitted reader composable and Room schema/DAO files.
5. Build logs were claimed but not shipped.
6. Screenshots were visually identical without stage-specific assertion evidence.

Your job:
1. Inspect `Contracts.kt`, `MainViewModel.kt`, `AppContainer.kt`, `QualityAlternativeApp.kt`, `RoomReadingProgressRepository.kt`, `ReadingProgressDao.kt`, `ReadingProgressEntity.kt`, `QualityAlternativeDatabase.kt`, and the modified tests.
2. Check whether `ReadingProgressRepository.cachePendingProgress()` makes the newest visible anchor repository-visible before the async Room write completes, and whether this closes the immediate reopen race.
3. Check whether `RoomReadingProgressRepository` now monotonic-merges Room collector emissions and rejects older unfinished saves even after a stale Room row is emitted.
4. Check whether `progressPersistenceScope = appContainer.appScope` still covers ViewModel/Activity close in the realistic same-process case.
5. Check whether the connected test proves durable saved anchor by reading `readingProgressRepository.readingProgress()`, and whether R3 adds enough adversarial unit/instrumented coverage for R2 blockers.
6. Verify visual review using R3 screenshots and `screenshots/reader_resume_run_r3/reader_resume_stage_assertions.txt`.
7. Flag any release-blocking issue, missing test, or bundle gap. Be strict, but do not require unrelated release work.

Output format, exactly:

SCORE: n/10
VERDICT: PASS / REVISE / BLOCK
VISUAL REVIEW: PASS / REVISE / BLOCK
BLOCKERS: none or numbered list
R2 BLOCKER RECHECK: concise assessment
R1 BLOCKER RECHECK: concise assessment
READER RESUME AUTOSAVE: concise assessment
STALE WRITE / RACE CHECK: concise assessment
LIFECYCLE / REOPEN CHECK: concise assessment
TEST/EVIDENCE: concise assessment
BUNDLE GAPS: none or numbered list
PACKAGE HYGIENE: concise assessment

Only give SCORE 10/10, VERDICT PASS, and VISUAL REVIEW PASS if the fix and evidence are sufficient to proceed to a new release APK for this hotfix.
