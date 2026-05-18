# GPT Pro Review Request: Sprint 22 Reading Time Remaining Hotfix R3

You are reviewing the R3 Android/Kotlin hotfix for Quality Alternative.

Previous GPT Pro results:

- R1: `SCORE: 8/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`
- R2: `SCORE: 9/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`

R2 had no blockers, but withheld 10/10 for residual non-blocking scheduler coverage and bundle evidence gaps:

- if the three newest candidates fail/no-op, older candidates might wait for a later emission
- connected test did not programmatically assert exact `1 hr 20 min left`
- bundle lacked ReadingProgress model and DocumentImportCandidateFactory
- release/debug APK build and signature evidence were not included

R3 specifically addresses those gaps.

## Audit Base

Use only this R3 bundle as the audit base. If a claim cannot be proven from shipped files, label it `BUNDLE GAP`.

## User-Reported Bug

The Home screen showed a long book as:

`41% read · 12 min left`

This is nonsensical for a long imported book. The cause is a legacy capped document duration (`20 min`) that survived after reading-time estimation was improved, so remaining time was calculated from stale metadata.

## R3 Changes To Recheck

- Background repair now scans a bounded candidate window of up to ten relevant unfinished private-reader legacy documents.
- It still limits successful repairs to three per pass.
- If newer candidates fail to load or no-op, the scheduler continues to later candidates in the same pass.
- New unit test proves three newest failing candidates do not prevent the fourth candidate from being repaired.
- Connected visual E2E now waits for and asserts the exact `1 hr 20 min left` label.
- Bundle includes ReadingProgress model, DocumentImportCandidateFactory, APK build logs, debug APK signature verification, SHA-256 hashes, and APK artifacts.

## Required Audit Questions

1. Does R3 fully address the reported `41% read · 12 min left` stale estimate regression?
2. Does repair stay bounded to relevant unfinished private-reader documents rather than parsing the whole library?
3. Does the bounded scan resolve the R2 scheduler edge without creating an excessive startup parse?
4. Are document/progress races and background/open duplicate repair behavior handled correctly?
5. Are persistence, portable-profile autosave, and analytics behavior acceptable?
6. Are tests sufficient, including autosave, duplicate-repair, failed-candidate scheduler behavior, and exact visual label coverage?
7. Does R3 visual evidence prove the Home card changes from `12 min left` to `1 hr 20 min left`?
8. Are APK/package hygiene and release-readiness evidence sufficient for a release APK?

## Required Output Format

Return:

- `SCORE: x/10`
- `VERDICT: PASS` or `FAIL`
- `VISUAL REVIEW: PASS` or `FAIL`
- `BLOCKERS`
- `R2 CONCERN RECHECK`
- `READING TIME REMAINING`
- `FLOW / RACE CHECK`
- `PERSISTENCE / AUTOSAVE / ANALYTICS`
- `TEST / EVIDENCE`
- `APK / RELEASE READINESS`
- `BUNDLE GAPS`
- `PACKAGE HYGIENE`

Give `10/10 PASS PASS` only if there are no blockers for a release APK.
