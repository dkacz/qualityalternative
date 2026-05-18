# GPT Pro Review Request: Sprint 22 Reading Time Remaining Hotfix R4

You are reviewing the R4 Android/Kotlin hotfix for Quality Alternative.

Previous GPT Pro results:

- R1: `SCORE: 8/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`
- R2: `SCORE: 9/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`
- R3: `SCORE: 8/10`, `VERDICT: FAIL`, `VISUAL REVIEW: PASS`

R3 blockers were:

- release APK readiness was not proven because only an unsigned release APK was included
- background repair was bounded per scheduling emission, not per startup/background repair cycle, so repository emissions caused by repairs could cascade into additional scan windows

R4 specifically fixes those blockers.

## Audit Base

Use only this R4 bundle as the audit base. If a claim cannot be proven from shipped files, label it `BUNDLE GAP`.

## User-Reported Bug

The Home screen showed a long book as:

`41% read · 12 min left`

This is nonsensical for a long imported book. The cause is a legacy capped document duration (`20 min`) that survived after reading-time estimation was improved, so remaining time was calculated from stale metadata.

## R4 Changes To Recheck

- Background repair is now a single bounded startup cycle per `MainViewModel` lifetime.
- The cycle scans at most ten relevant unfinished private-reader legacy candidates.
- The cycle applies at most three successful repairs.
- Repair-induced repository emissions cannot schedule additional windows in the same startup cycle.
- Unit coverage proves the scheduler does not cascade beyond the first three repairs when twelve stale legacy documents exist.
- The prior failed-candidate test still proves three newest failures do not block repair of the fourth candidate.
- Connected visual E2E was rerun after the scheduler change and still proves `12 min left` becomes `1 hr 20 min left`.
- Release-readiness evidence now includes:
  - `assembleDebug assembleRelease` build log
  - signed release APK artifact: `apk_r4/sprint22-reading-time-hotfix-release-debugsigned.apk`
  - release `apksigner verify` output
  - debug APK verification output
  - SHA-256 hashes
  - emulator install/launch smoke log and screenshot

The release APK is locally signed with the Android debug key for this alpha/GitHub installable release evidence; do not treat this as Play Store production signing.

## Required Audit Questions

1. Does R4 fully address the reported `41% read · 12 min left` stale estimate regression?
2. Does repair stay bounded to relevant unfinished private-reader documents rather than parsing the whole library?
3. Does the single-cycle guard resolve the R3 cascade blocker?
4. Are document/progress races and background/open duplicate repair behavior handled correctly?
5. Are persistence, portable-profile autosave, and analytics behavior acceptable?
6. Are tests sufficient, including autosave, duplicate-repair, failed-candidate continuation, no-cascade scheduling, and exact visual label coverage?
7. Does R4 visual evidence prove the Home card changes from `12 min left` to `1 hr 20 min left`?
8. Are APK/package hygiene and installable alpha release evidence sufficient for a GitHub APK release?

## Required Output Format

Return:

- `SCORE: x/10`
- `VERDICT: PASS` or `FAIL`
- `VISUAL REVIEW: PASS` or `FAIL`
- `BLOCKERS`
- `R3 BLOCKER RECHECK`
- `READING TIME REMAINING`
- `FLOW / RACE CHECK`
- `PERSISTENCE / AUTOSAVE / ANALYTICS`
- `TEST / EVIDENCE`
- `APK / RELEASE READINESS`
- `BUNDLE GAPS`
- `PACKAGE HYGIENE`

Give `10/10 PASS PASS` only if there are no blockers for a GitHub alpha APK release.
