# GPT Pro Review Request: Sprint 22 Reading Time Remaining Hotfix

You are reviewing an Android/Kotlin hotfix for Quality Alternative.

Guiding principles:

1. Use only the shipped bundle as the audit base.
2. If a claim cannot be proven from shipped files, label it `BUNDLE GAP`.
3. Prioritize correctness, regression risk, visual evidence quality, test coverage, analytics/privacy, and package hygiene.
4. Do not inflate suggestions that duplicate existing coverage into blockers.

## User-Reported Bug

The Home screen showed a long book as:

`41% read · 12 min left`

This is nonsensical for a long imported book. The suspected cause is a legacy capped document duration (`20 min`) that survived after reading-time estimation was improved, so remaining time was calculated from stale metadata.

## Review Scope

Audit only this hotfix:

- `app/src/main/java/com/qualityalternative/app/domain/service/Contracts.kt`
- `app/src/main/java/com/qualityalternative/app/data/local/UserDocumentDao.kt`
- `app/src/main/java/com/qualityalternative/app/data/RoomUserDocumentRepository.kt`
- `app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt`
- `app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt`
- `app/src/androidTest/java/com/qualityalternative/app/VisualQaScreenshotTest.kt`
- evidence under `evidence/sprint22_reading_time_remaining_hotfix/`

## What Changed

The fix adds a durable update path for user-document `durationMinutes`, then repairs unfinished private reader documents whose saved duration still looks like the old session cap (`<=20 min`). Repair runs from a combined document/progress stream and also on reader open. It records `READING_TIME_ESTIMATE_APPLIED` analytics and autosaves the portable profile after a repaired duration is persisted.

## Required Audit Questions

1. Does the implementation fully address the reported `41% read · 12 min left` stale estimate regression?
2. Does the repair avoid expensive parsing of the whole library on startup and limit itself to relevant unfinished private-reader documents?
3. Are races between document and progress flows handled correctly?
4. Does reader-open repair keep `currentContent` and analytics metadata consistent with the repaired estimate?
5. Are persistence, portable-profile autosave, and analytics behavior acceptable?
6. Are tests sufficient, especially the unit tests and connected visual evidence?
7. Does the visual evidence prove the Home card changes from `12 min left` to `1 hr 20 min left`?
8. Any release blockers or package hygiene problems?

## Required Output Format

Return:

- `SCORE: x/10`
- `VERDICT: PASS` or `FAIL`
- `VISUAL REVIEW: PASS` or `FAIL`
- `BLOCKERS`
- `READING TIME REMAINING`
- `FLOW / RACE CHECK`
- `PERSISTENCE / AUTOSAVE / ANALYTICS`
- `TEST / EVIDENCE`
- `BUNDLE GAPS`
- `PACKAGE HYGIENE`

Give `10/10 PASS PASS` only if there are no blockers for a release APK.
