# Sprint 22 Reading Time Remaining Hotfix R2 Validation

## Automated Checks

- Unit tests: `./gradlew testDebugUnitTest` passed.
  - Log: `logs/testDebugUnitTest_r2.log`
- Connected visual E2E: `VisualQaScreenshotTest#captureSprint22ReadingTimeRemainingRepair` passed on `qaApi36`.
  - Log: `logs/connectedDebugAndroidTest_sprint22_reading_time_r2.log`

## Visual Proof

The R2 connected test seeds the same legacy long imported document with:

- persisted stale duration: `20 min`
- progress: `41%`
- generated long body: about `30,000` words

Screenshots show:

- before repair: `41% read · 12 min left`
- after repair: `41% read · 1 hr 20 min left`

## R1 Concern Fixes

- Duplicate background/open repair analytics and autosave are guarded per content id.
- Already-attempted candidates are filtered before limiting the batch.
- Background repair can process up to three recent unfinished legacy user documents per scheduling pass.
- Unit coverage asserts autosave invocation and no duplicate analytics/autosave when opening after background repair.

## R3 Additions After GPT Pro 9/10

- Background repair now scans a bounded window of up to ten relevant candidates, so failed or no-op newest candidates do not prevent the next candidate from being repaired in the same pass.
- New unit test covers three newest candidate load failures followed by repair of the fourth candidate.
- Connected visual E2E now asserts the exact corrected Home label: `1 hr 20 min left`.
- Debug and release APK assembly logs are included.
- Debug APK signature verification and SHA-256 hashes are included.

## R4 Additions After GPT Pro 8/10 FAIL

- Background repair now starts only one bounded startup cycle per ViewModel, preventing repair-induced repository emissions from opening additional scan windows.
- New unit test proves twelve stale documents only repair the first three in that single startup cycle.
- A release APK is locally signed for installable alpha evidence and verified with `apksigner`.
- The signed release APK is installed and launched on emulator; a smoke screenshot is included.
- Final connected visual E2E was rerun after the scheduler change.
