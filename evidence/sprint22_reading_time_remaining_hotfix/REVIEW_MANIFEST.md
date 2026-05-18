# Sprint 22 Reading Time Remaining Hotfix Review Manifest

## Scope

Fix the Home `Continue Reading` card remaining-time regression where legacy imported EPUB/Markdown documents could keep the old capped `20 min` estimate and show values such as `41% read · 12 min left` for a long book.

## Implementation Summary

- Added a durable `UserDocumentRepository.updateEstimatedDuration(...)` path backed by `UserDocumentDao.updateDurationMinutes(...)`.
- Added a ViewModel repair path for unfinished private reader documents whose duration still looks like the old session cap (`<= 20 min`).
- Runs repair from a combined document/progress stream so it sees a consistent unfinished document snapshot.
- Also repairs immediately when a legacy private reader document is opened, so the current reader session and subsequent Home card use the corrected estimate.
- R2 after GPT Pro 8/10: guards duplicate analytics/autosave if background and reader-open repair overlap in one ViewModel lifetime.
- R2 after GPT Pro 8/10: filters already-attempted repair candidates before limiting the batch, and repairs up to three recent unfinished legacy documents per scheduling pass.
- R3 after GPT Pro 9/10: bounded scan now continues past failed/no-op newest candidates in the same pass, while still limiting costly work to at most ten candidates and three repairs.
- R3 after GPT Pro 9/10: connected visual test now programmatically asserts the exact corrected label `1 hr 20 min left`.
- R4 after GPT Pro 8/10 FAIL: background repair is now a single bounded startup cycle per ViewModel, preventing repair-induced repository emissions from cascading into additional scan windows.
- R4 after GPT Pro 8/10 FAIL: release artifact evidence now includes a locally signed release APK, release `apksigner verify` output, SHA-256 hashes, and emulator install/launch smoke evidence.
- Emits `READING_TIME_ESTIMATE_APPLIED` analytics with `repairSource`, previous duration, and repaired duration.
- Triggers portable profile autosave after a repaired duration is persisted.

## Validation

- R4 `testDebugUnitTest`: PASS. See `logs/testDebugUnitTest_r4.log`.
- R4 `assembleDebug assembleRelease`: PASS. See `logs/assembleDebugRelease_r4.log`.
- R4 signed release APK verification: PASS. See `logs/apksigner_verify_release_debugsigned_r4.log`.
- R4 signed release APK emulator install/launch smoke: PASS. See `logs/release_install_smoke_r4.log` and `screenshots/release_install_smoke_r4.png`.
- R4 connected screenshot test on final scheduler code: PASS. See `logs/connectedDebugAndroidTest_sprint22_reading_time_r4.log`.

## Visual Evidence

The connected screenshot test seeds a legacy long Markdown document with:

- persisted duration: `20 min`
- progress: `41%`
- real body: about `30,000` words

Screenshots:

- R4 visual evidence repeats the same flow after the strict single-cycle scheduler change:
  - `screenshots/final_connected_run_r4/00_home_continue_before_repair_assertion.png`: initial Home card with `41% read · 12 min left`.
  - `screenshots/final_connected_run_r4/01_home_continue_after_repair_wait.png`: repaired Home card with `41% read · 1 hr 20 min left`.
  - `screenshots/final_connected_run_r4/02_home_continue_repaired_remaining_time.png`: final repaired Home card evidence.

## Bundle Hygiene

This evidence directory preserves the final R4 audit trail, R1/R2 GPT Pro reviews plus R3 summary, final R4 screenshots/logs, and the hotfix diff. Superseded review ZIPs and duplicate APK binaries were removed; the canonical release APK is under `release_artifacts/`.
