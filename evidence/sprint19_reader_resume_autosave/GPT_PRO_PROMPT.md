You are doing a fresh-from-scratch adversarial audit of one scoped Android hotfix.

GUIDING PRINCIPLES:
1. Use only the shipped bundle as the audit base.
2. If a claim cannot be proven from shipped files, label it `BUNDLE GAP`.
3. Do not inflate already-covered concerns into fresh findings.
4. Treat evidence quality and package hygiene as part of the review.
5. The user-reported bug is concrete: after reading for a while, locking/returning or reopening can resume at a stale pre-session page. Review against that exact risk.

Read the full attached `evidence/sprint19_reader_resume_autosave/EVIDENCE.md` first, then inspect the patch and named source/test files.

Target scope:
- Sprint 19 Reader Resume Autosave Hotfix.
- Verify that reader progress cannot regress to an older page because of async save ordering, stale Room flow emissions, same-millisecond saves, same-percent-but-different-anchor cases, lifecycle pause/stop, or Activity close/reopen.
- Verify that the visual evidence actually supports the claim that resume returns to the same page/visible text after reopen.

Known prior bug classes to actively test against:
- Rounded progress percent is not enough: several pages can share the same percent.
- Older unfinished progress writes can finish after newer writes and overwrite the newer anchor.
- Same-process UI state can be newer than the Room flow and then be overwritten by a stale emission.
- Lifecycle/dispose saves can happen around pause/stop and reopen.
- Completion must still win over unfinished lifecycle saves.

Your job:
1. Inspect `reader_resume_autosave_fix.diff`, `MainViewModel.kt`, `RoomReadingProgressRepository.kt`, and the three modified tests.
2. Check whether the code fix is logically sufficient for the user complaint, or whether a stale resume path remains.
3. Check the test evidence:
   - `logs/unit_main_view_model.xml`
   - `logs/connected_reader_resume_and_room_progress.xml`
   - screenshots under `screenshots/reader_resume_run/`
4. Verify visual review: the screenshots should show the same reader page after save, after pause/stop restore, and after full reopen.
5. Flag any release-blocking issue, missing test, or bundle gap. Be strict, but do not require unrelated release work.

Output format, exactly:

SCORE: n/10
VERDICT: PASS / REVISE / BLOCK
VISUAL REVIEW: PASS / REVISE / BLOCK
BLOCKERS: none or numbered list
READER RESUME AUTOSAVE: concise assessment
STALE WRITE / RACE CHECK: concise assessment
LIFECYCLE / REOPEN CHECK: concise assessment
TEST/EVIDENCE: concise assessment
BUNDLE GAPS: none or numbered list
PACKAGE HYGIENE: concise assessment

Only give SCORE 10/10, VERDICT PASS, and VISUAL REVIEW PASS if the fix and evidence are sufficient to proceed to a new release APK for this hotfix.
