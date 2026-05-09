You are doing a fresh-from-scratch adversarial audit of one scoped Android hotfix, with explicit recheck of a prior BLOCK review.

Use only the shipped bundle as the audit base. If a claim cannot be proven from shipped files, label it `BUNDLE GAP`.

Read these files first, in order:
1. `evidence/sprint19_reader_resume_autosave/EVIDENCE.md`
2. `evidence/sprint19_reader_resume_autosave/GPT_PRO_REVIEW.md`
3. `evidence/sprint19_reader_resume_autosave/reader_resume_autosave_fix.diff`

Target scope:
- Sprint 19 Reader Resume Autosave Hotfix R2.
- Verify that reader progress cannot regress to an older page because of async save ordering, stale Room flow emissions, same-millisecond saves, same-percent-but-different-anchor cases, lifecycle pause/stop, Activity/ViewModel close, or Activity close/reopen.
- Verify that R1 blockers were actually fixed, not merely documented.

Known R1 blockers to recheck:
1. Latest-page save was ViewModel-scoped and could be canceled before durable write.
2. Connected test waited on immediate ViewModel state, not durable repository state.
3. There was no adversarial delayed-latest-save plus ViewModel-close test.
4. Bundle omitted reader composable and Room schema/DAO files.
5. Build logs were claimed but not shipped.
6. Screenshots were visually identical without stage-specific assertion evidence.

Your job:
1. Inspect `MainViewModel.kt`, `AppContainer.kt`, `QualityAlternativeApp.kt`, `RoomReadingProgressRepository.kt`, `ReadingProgressDao.kt`, `ReadingProgressEntity.kt`, `QualityAlternativeDatabase.kt`, and the three modified tests.
2. Check whether `progressPersistenceScope = appContainer.appScope` and the new unit test are sufficient for Activity/ViewModel close durability in the realistic same-process case.
3. Check whether the connected test now proves durable saved anchor by reading `readingProgressRepository.readingProgress()`.
4. Check whether the reader can respond if a newer persisted progress arrives after the reader screen has already opened.
5. Verify visual review using screenshots and `reader_resume_stage_assertions.txt`.
6. Flag any release-blocking issue, missing test, or bundle gap. Be strict, but do not require unrelated release work.

Output format, exactly:

SCORE: n/10
VERDICT: PASS / REVISE / BLOCK
VISUAL REVIEW: PASS / REVISE / BLOCK
BLOCKERS: none or numbered list
R1 BLOCKER RECHECK: concise assessment
READER RESUME AUTOSAVE: concise assessment
STALE WRITE / RACE CHECK: concise assessment
LIFECYCLE / REOPEN CHECK: concise assessment
TEST/EVIDENCE: concise assessment
BUNDLE GAPS: none or numbered list
PACKAGE HYGIENE: concise assessment

Only give SCORE 10/10, VERDICT PASS, and VISUAL REVIEW PASS if the fix and evidence are sufficient to proceed to a new release APK for this hotfix.
