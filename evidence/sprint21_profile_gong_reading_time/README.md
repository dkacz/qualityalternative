# Sprint 21 Profile Restore, Meditation Gong, And Reading Time

## Scope

- Restore portable settings from the default shared backup after app data is cleared or the app is reinstalled.
- Replace meditation completion beeps with an in-app generated gong.
- Stop labeling long EPUB/Markdown documents as 20-minute reads by preserving full extracted reading time with a defensive cap.

## Implementation Notes

- The default profile backup now writes to `Downloads/Quality Alternative/quality-alternative-profile.json`.
- The restore path accepts Android MediaStore collision names such as `quality-alternative-profile (1).json` and chooses the newest inserted matching backup by MediaStore `DATE_ADDED` plus `_ID`, avoiding stale exact-name reads.
- Onboarding includes a `Restore profile` action so a fresh install can load the default backup before setup.
- Settings includes `Restore default backup` beside manual export/import, but now only shows the import preview first. Local data is not replaced until the user chooses `Replace` and confirms the destructive action.
- EPUB/Markdown extracted text estimates now clamp to `3..720` minutes. Link/session defaults remain short intervention estimates.
- Meditation completion now uses a short generated PCM gong through `AudioTrack` instead of `ToneGenerator` beeps.
- The meditation connected test now selects a one-minute reset, waits for timer completion, verifies the completion copy/action, and captures the completed-gong state.

## Verification

- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest --tests com.qualityalternative.app.data.ReadingTimeEstimatorTest --tests com.qualityalternative.app.data.DocumentReadingTimeEstimatorTest --tests com.qualityalternative.app.ui.DocumentImportCandidateFactoryTest --tests com.qualityalternative.app.ui.MainViewModelTest`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew assembleDebugAndroidTest`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.data.AndroidAccountLightProfileAutosaveWriterTest,com.qualityalternative.app.MainActivityTest#onboardingRestoreProfileLoadsDefaultBackupAfterCleanInstall,com.qualityalternative.app.MainActivityTest#settingsDefaultBackupRestoreShowsPreviewBeforeReplace,com.qualityalternative.app.MainActivityTest#longDocumentImportPreviewShowsMultiHourReadingTime,com.qualityalternative.app.MainActivityTest#meditationInterventionShowsCalmAlternativeWhenPrimaryIsReading,com.qualityalternative.app.MainActivityTest#meditationAlternativeOpensTimerAndCompletesWithGong`

The current unit and connected test XML/logcat outputs are preserved under `test-results/`.

## GPT Pro Review Trail

- R1 review: `GPT_PRO_REVIEW_R1.md` (`SCORE: 7/10`, `VERDICT: BLOCK`, `VISUAL REVIEW: REVISE`).
- R1 blocker fixed: Settings default restore no longer applies replace immediately; it opens the same preview and explicit replace confirmation flow as manual imports.
- R2 review: `GPT_PRO_REVIEW_R2.md` (`SCORE: 8/10`, `VERDICT: BLOCK`, `VISUAL REVIEW: REVISE`).
- R2 blocker fixed: stale `DocumentReadingTimeEstimatorTest` now expects 45 minutes for 10,000-word Markdown/EPUB imports and 720 minutes for huge extracted text; its XML result is included.
- R2 review items addressed: profile backup copy now says default Downloads folder, MediaStore collision freshness is documented as newest inserted row via `DATE_ADDED` plus `_ID`, and meditation runtime evidence includes timer completion.
- R3 review: `GPT_PRO_REVIEW_R3.md` (`SCORE: 8/10`, `VERDICT: BLOCK`, `VISUAL REVIEW: PASS`).
- R3 blocker fixed: stale `DocumentImportCandidateFactoryTest` now expects 45 minutes for 10,000-word Markdown/EPUB candidates and 720 minutes for huge extracted text; its XML result is included.
- R3 bundle gaps addressed: R4 ships `app/proguard-rules.pro`, `app/schemas`, a renamed meditation completion test, neutral Sprint 21 screenshot output paths, and a connected visual import preview showing a `2 hr 15 min` Markdown estimate.
- R4 review: `GPT_PRO_REVIEW_R4.md` (`SCORE: 9/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`).
- R4 package-hygiene items addressed for R5: the restored default-backup success screenshot is now named as dark-theme evidence, the selected meditation-alternative test name is no longer sprint-numbered, and current source screenshot helpers have neutral profile/reader evidence names.
- R5 robustness pass: repeated emulator runs exposed Android MediaStore exact-name saturation for `quality-alternative-profile.json`; the writer now falls back to a collision-name backup that the restore path already recognizes. Final R5 connected evidence was run after clearing stale emulator-only `Downloads/Quality Alternative` residue so logcat reflects current behavior rather than previous failed attempts.
- R5 review: `GPT_PRO_REVIEW_R5.md` (`SCORE: 9/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`).
- R5 package-hygiene item addressed for R6: duplicate raw harvest review copies were removed after copying the final text into canonical named review files. R6 keeps `GPT_PRO_REVIEW_R1.md` through `GPT_PRO_REVIEW_R5.md` as the review trail and excludes raw `pro_review_harvest_*` scratch directories.
- R6 review: `GPT_PRO_REVIEW_R6.md` (`SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`).
- Test environment note: the collision test still creates deliberate MediaStore duplicate-name rows with a unique collision filename and asserts that the newest inserted matching backup wins.

## Visual Evidence

- `screenshots/manual/onboarding_restore_profile_entry.png`
- `screenshots/current/profile_default_restore/08_default_backup_preview_light.png`
- `screenshots/current/profile_default_restore/09_default_backup_replace_confirm_light.png`
- `screenshots/current/profile_default_restore/10_default_backup_restore_success_dark.png`
- `screenshots/current/meditation_gong/12_meditation_calm_alternative.png`
- `screenshots/current/meditation_gong/13_meditation_gong_complete.png`
- `screenshots/current/reading_time_import/14_long_document_import_multi_hour.png`
