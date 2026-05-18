# GPT Pro Review Request: Sprint 22 Reading Time Remaining Hotfix R2

You are reviewing the R2 Android/Kotlin hotfix for Quality Alternative.

R1 GPT Pro review returned:

- `SCORE: 8/10`
- `VERDICT: PASS`
- `VISUAL REVIEW: PASS`
- no blockers

R1 non-blocking concerns were:

- duplicate analytics/autosave possible if background repair and reader-open repair race
- repair scheduling filtered attempted ids after `.take(1)`, so one failed/redundant candidate could starve older candidates
- autosave invocation was not asserted by unit tests
- bundle lacked several production context files

R2 specifically addresses those concerns.

## Audit Base

Use only this R2 bundle as the audit base. If a claim cannot be proven from shipped files, label it `BUNDLE GAP`.

## User-Reported Bug

The Home screen showed a long book as:

`41% read · 12 min left`

This is nonsensical for a long imported book. The suspected cause is a legacy capped document duration (`20 min`) that survived after reading-time estimation was improved, so remaining time was calculated from stale metadata.

## R2 Review Scope

Audit the hotfix and the R1 concern fixes:

- `app/src/main/java/com/qualityalternative/app/domain/service/Contracts.kt`
- `app/src/main/java/com/qualityalternative/app/data/local/UserDocumentDao.kt`
- `app/src/main/java/com/qualityalternative/app/data/RoomUserDocumentRepository.kt`
- `app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt`
- `app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt`
- `app/src/androidTest/java/com/qualityalternative/app/VisualQaScreenshotTest.kt`
- supporting production context files included in the bundle
- evidence under `evidence/sprint22_reading_time_remaining_hotfix/`

## What Changed

The fix adds a durable update path for user-document `durationMinutes`, then repairs unfinished private reader documents whose saved duration still looks like the old session cap (`<=20 min`). Repair runs from a combined document/progress stream and also on reader open. It records `READING_TIME_ESTIMATE_APPLIED` analytics and autosaves the portable profile after a repaired duration is persisted.

R2 additionally:

- guards duplicate analytics/autosave for the same content id inside one ViewModel lifetime
- filters already-attempted candidates before limiting the repair batch
- repairs up to three recent unfinished legacy documents per scheduling pass
- adds unit coverage for autosave invocation and duplicate background/open repair behavior
- adds fresh R2 visual E2E screenshots and connected-test logs
- includes extra production context files to reduce R1 bundle gaps

## Required Audit Questions

1. Does R2 fully address the reported `41% read · 12 min left` stale estimate regression?
2. Does repair stay bounded to relevant unfinished private-reader documents rather than parsing the whole library?
3. Are document/progress races and background/open duplicate repair behavior handled correctly?
4. Does reader-open repair keep `currentContent` and analytics metadata consistent with the repaired estimate?
5. Are persistence, portable-profile autosave, and analytics behavior acceptable?
6. Are R2 tests sufficient, including autosave and duplicate-repair coverage?
7. Does R2 visual evidence prove the Home card changes from `12 min left` to `1 hr 20 min left`?
8. Are there any release blockers or package hygiene problems?

## Required Output Format

Return:

- `SCORE: x/10`
- `VERDICT: PASS` or `FAIL`
- `VISUAL REVIEW: PASS` or `FAIL`
- `BLOCKERS`
- `R1 CONCERN RECHECK`
- `READING TIME REMAINING`
- `FLOW / RACE CHECK`
- `PERSISTENCE / AUTOSAVE / ANALYTICS`
- `TEST / EVIDENCE`
- `BUNDLE GAPS`
- `PACKAGE HYGIENE`

Give `10/10 PASS PASS` only if there are no blockers for a release APK.
