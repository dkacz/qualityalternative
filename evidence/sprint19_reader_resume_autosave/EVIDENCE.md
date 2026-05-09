# Sprint 19 Reader Resume Autosave Hotfix Evidence

## User-Visible Problem

Reader resume could reopen at a stale pre-session page after reading, locking the screen, and returning later. The previous regression test only waited for the saved progress percent, which was too coarse: different visible pages can round to the same percent.

## Fix Summary

- Reader progress durable writes now launch in the app-level persistence scope in production, so Activity/ViewModel close does not cancel the latest page save.
- Reader progress saves now use a monotonic `updatedAtMillis`, so rapid page changes in the same millisecond still get deterministic ordering.
- The in-memory `MainUiState.readingProgress` is updated immediately when the reader position changes, so same-process reopen/continue sees the newest visible anchor instead of waiting for a repository flow.
- Repository flow hydration keeps a newer active reader progress override when an older Room emission arrives late.
- `ReadingProgressRepository.cachePendingProgress()` now makes the latest visible anchor repository-visible synchronously before the Room write starts, so an immediate same-process reopen cannot restore from the older durable row while the latest app-scope write is still pending.
- `RoomReadingProgressRepository` now monotonic-merges Room collector emissions with its current in-memory progress instead of replacing the list with potentially stale database rows. R4 makes the merge a compare-and-set loop so stale collector/cache/save interleavings cannot overwrite a newer anchor.
- `RoomReadingProgressRepository` rejects older unfinished progress writes when a newer unfinished position is already stored.
- The reader screen re-evaluates restored progress when repository progress changes after the screen opens, so a late durable save can still remap the page while the reader is open.
- The connected resume test now waits for the actual Room row through `ReadingProgressDao.findByContentId()`, not only the rounded percent, immediate ViewModel state, or repository memory.

## GPT Pro R1 Blocker Recheck

The first GPT Pro review is preserved at `GPT_PRO_REVIEW.md` and scored `7/10`, `VERDICT: BLOCK`, `VISUAL REVIEW: REVISE`.

R1 blocker fixes:

- Durable save cancellation: fixed by passing `AppContainer.appScope` into `MainViewModel` as `progressPersistenceScope`.
- UI-state-only connected assertion: fixed by adding Room DAO helpers and waiting on `ReadingProgressDao.findByContentId()` via `AppContainer.readingProgressRowForTests()`.
- Missing adversarial close case: fixed by `latestReaderProgressSaveSurvivesViewModelCloseBeforeRepositoryWriteCompletes`, which delays the latest save, closes the ViewModel before the repository write completes, releases the write, then verifies a fresh ViewModel reopens at the latest paragraph anchor.
- Missing lifecycle source: fixed in the R2 review bundle by shipping `QualityAlternativeApp.kt`.
- Missing Room source: fixed in the R2 review bundle by shipping `ReadingProgressDao.kt`, `ReadingProgressEntity.kt`, and `QualityAlternativeDatabase.kt`.
- Missing build logs: fixed by shipping command logs under `logs/`.
- Screenshot ambiguity: fixed by adding `screenshots/reader_resume_run_r2/reader_resume_stage_assertions.txt`, which records stage-specific page/progress/durable-anchor values.

## GPT Pro R2 Blocker Recheck

The second GPT Pro review is preserved at `GPT_PRO_REVIEW_R2.md` and scored `8/10`, `VERDICT: BLOCK`, `VISUAL REVIEW: PASS`.

R2 blocker fixes:

- Immediate reopen before pending latest save completes: fixed by `cachePendingProgress()` plus `reopenedReaderUsesPendingLatestProgressBeforeRepositoryWriteCompletes`, which delays the latest save, closes the original ViewModel, opens a fresh ViewModel before releasing the write, and verifies the fresh reader restores the latest paragraph anchor.
- Stale Room emission downgrade: fixed by monotonic repository merge and `cachePendingProgress_survivesStaleRoomEmissionAndOlderWrite`, which injects an older Room row after a newer pending anchor and then verifies both the stale emission and a delayed older save cannot move progress backward.
- R2 visual review was already PASS; R3 repeats the connected screenshot evidence to prove the user-visible page remains stable after durable save, lifecycle pause/stop, and full Activity reopen.

## GPT Pro R3 Blocker Recheck

The third GPT Pro review is preserved at `GPT_PRO_REVIEW_R3.md` and scored `8/10`, `VERDICT: BLOCK`, `VISUAL REVIEW: PASS`.

R3 blocker fixes:

- Connected durable assertion was not actually durable: fixed by adding `ReadingProgressDao.findByContentId()` and `AppContainer.readingProgressRowForTests()`, then changing the connected resume test to assert the actual Room row.
- Repository monotonic merge was not atomic: fixed by compare-and-set loops around collector merge and pending/save upsert. The repository no longer performs read-modify-write `progress.value = ...` assignments for these paths.
- Missing integrated Activity close/reopen-before-Room-write test: fixed by `sprint19ReaderResumeUsesPendingLatestProgressAcrossImmediateReopenBeforeRoomWrite`, which delays the next unfinished Room insert, advances to a latest page, closes the Activity, reopens immediately before releasing the Room write, performs pause/stop on the reopened Activity, then releases the write and verifies the actual Room row catches up to the latest anchor.
- Same-timestamp same-paragraph tie-breaker now includes `lastVisibleTextOffset`, and the Room repository test asserts that an earlier offset cannot replace a later offset at the same timestamp and paragraph.

## Validation

- PASS R2: `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest --rerun-tasks`
  - Log: `logs/test_debug_unit_full_r2.log`
  - XML report: `logs/unit_main_view_model_r2.xml`
  - Includes `delayedOlderReaderProgressSaveCannotOverwriteNewerVisiblePosition`
  - Includes `latestReaderProgressSaveSurvivesViewModelCloseBeforeRepositoryWriteCompletes`
- PASS R3: `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest --rerun-tasks`
  - Log: `logs/test_debug_unit_full_r3.log`
  - XML report: `logs/unit_main_view_model_r3.xml`
  - `tests="111" failures="0" errors="0"`
  - Includes `delayedOlderReaderProgressSaveCannotOverwriteNewerVisiblePosition`
  - Includes `latestReaderProgressSaveSurvivesViewModelCloseBeforeRepositoryWriteCompletes`
  - Includes `reopenedReaderUsesPendingLatestProgressBeforeRepositoryWriteCompletes`
- PASS R4: `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest --rerun-tasks`
  - Log: `logs/test_debug_unit_full_r4.log`
  - XML report: `logs/unit_main_view_model_r4.xml`
  - `tests="111" failures="0" errors="0"`
  - Includes `delayedOlderReaderProgressSaveCannotOverwriteNewerVisiblePosition`
  - Includes `latestReaderProgressSaveSurvivesViewModelCloseBeforeRepositoryWriteCompletes`
  - Includes `reopenedReaderUsesPendingLatestProgressBeforeRepositoryWriteCompletes`
- PASS R2: `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ANDROID_HOME=/opt/homebrew/share/android-commandlinetools ANDROID_SDK_ROOT=/opt/homebrew/share/android-commandlinetools ./gradlew connectedDebugAndroidTest --rerun-tasks -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#sprint19ReaderSessionProgressPersistsLastViewedPageAfterReopen,com.qualityalternative.app.data.RoomReadingProgressRepositoryTest#saveProgress_keepsNewerUnfinishedPositionWhenOlderSaveArrivesLater`
  - Log: `logs/connected_reader_resume_and_room_progress_r2.log`
  - XML report: `logs/connected_reader_resume_and_room_progress_r2.xml`
  - Device: `qaApi36(AVD) - 16`
  - Test evidence covers page forward, one page back, durable saved anchor match, lifecycle pause/stop restore, full Activity close/reopen restore, and Room stale-write rejection.
- PASS R3: `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ANDROID_HOME=/opt/homebrew/share/android-commandlinetools ANDROID_SDK_ROOT=/opt/homebrew/share/android-commandlinetools ./gradlew connectedDebugAndroidTest --rerun-tasks -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#sprint19ReaderSessionProgressPersistsLastViewedPageAfterReopen,com.qualityalternative.app.data.RoomReadingProgressRepositoryTest#saveProgress_keepsNewerUnfinishedPositionWhenOlderSaveArrivesLater,com.qualityalternative.app.data.RoomReadingProgressRepositoryTest#cachePendingProgress_survivesStaleRoomEmissionAndOlderWrite`
  - Log: `logs/connected_reader_resume_and_room_progress_r3.log`
  - XML report: `logs/connected_reader_resume_and_room_progress_r3.xml`
  - `tests="3" failures="0" errors="0"`
  - Device: `qaApi36(AVD) - 16`
  - Test evidence covers page forward, one page back, durable saved anchor match, lifecycle pause/stop restore, full Activity close/reopen restore, older-save rejection, and stale Room emission merge protection.
- PASS R4: `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ANDROID_HOME=/opt/homebrew/share/android-commandlinetools ANDROID_SDK_ROOT=/opt/homebrew/share/android-commandlinetools ./gradlew connectedDebugAndroidTest --rerun-tasks -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#sprint19ReaderSessionProgressPersistsLastViewedPageAfterReopen,com.qualityalternative.app.MainActivityTest#sprint19ReaderResumeUsesPendingLatestProgressAcrossImmediateReopenBeforeRoomWrite,com.qualityalternative.app.data.RoomReadingProgressRepositoryTest#saveProgress_keepsNewerUnfinishedPositionWhenOlderSaveArrivesLater,com.qualityalternative.app.data.RoomReadingProgressRepositoryTest#cachePendingProgress_survivesStaleRoomEmissionAndOlderWrite`
  - Log: `logs/connected_reader_resume_and_room_progress_r4.log`
  - XML report: `logs/connected_reader_resume_and_room_progress_r4.xml`
  - `tests="4" failures="0" errors="0"`
  - Device: `qaApi36(AVD) - 16`
  - Test evidence covers actual Room-row durable anchor, lifecycle pause/stop restore, full Activity close/reopen restore, delayed latest Room write, immediate Activity reopen before Room write completion, pause/stop on reopened Activity, older-save rejection, stale Room emission merge protection, and same-timestamp earlier-offset rejection.
- PASS R2: `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew assembleDebugAndroidTest assembleDebug`
  - Log: `logs/assemble_debug_and_android_test_r2.log`
- PASS R3: `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew assembleDebugAndroidTest assembleDebug`
  - Log: `logs/assemble_debug_and_android_test_r3.log`
- PASS R4: `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew assembleDebugAndroidTest assembleDebug`
  - Log: `logs/assemble_debug_and_android_test_r4.log`

## Visual Evidence

The R2, R3, and R4 screenshots show the same resumed page after durable save, after pause/stop restore, and after full reopen: page `3/12`, progress `26%`, visible text starts at "Session progress paragraph 15".

The R3 stage assertion file independently records:

```text
saved_before_pause_stop: page=3/12, progress=26, pageEndParagraph=20, durableSavedParagraph=20
restored_after_pause_stop: page=3/12, progress=26, pageEndParagraph=20, durableSavedParagraph=20
restored_after_reopen: page=3/12, progress=26, pageEndParagraph=20, durableSavedParagraph=20
```

The R4 pending-write stage assertion file independently records that the user-visible reopen uses page `4/12` while the actual durable Room row is still on paragraph `20`, and then the actual Room row catches up to paragraph `27` after the delayed write is released:

```text
pending_latest_before_room_write: page=4/12, progress=34, pageEndParagraph=27, durableSavedParagraph=20
immediate_reopen_before_room_write: page=4/12, progress=34, pageEndParagraph=27, durableSavedParagraph=20
room_write_released_after_reopen: page=4/12, progress=34, pageEndParagraph=27, durableSavedParagraph=27
```

- `screenshots/reader_resume_run_r2/09_session_progress_saved_before_pause_stop.png`
- `screenshots/reader_resume_run_r2/10_session_progress_restored_after_pause_stop.png`
- `screenshots/reader_resume_run_r2/11_session_progress_restored_after_reopen.png`
- `screenshots/reader_resume_run_r2/reader_resume_stage_assertions.txt`
- `screenshots/reader_resume_run_r3/09_session_progress_saved_before_pause_stop.png`
- `screenshots/reader_resume_run_r3/10_session_progress_restored_after_pause_stop.png`
- `screenshots/reader_resume_run_r3/11_session_progress_restored_after_reopen.png`
- `screenshots/reader_resume_run_r3/reader_resume_stage_assertions.txt`
- `screenshots/reader_resume_run_r4/09_session_progress_saved_before_pause_stop.png`
- `screenshots/reader_resume_run_r4/10_session_progress_restored_after_pause_stop.png`
- `screenshots/reader_resume_run_r4/11_session_progress_restored_after_reopen.png`
- `screenshots/reader_resume_run_r4/reader_resume_stage_assertions.txt`
- `screenshots/reader_pending_reopen_run_r4/12_pending_latest_before_room_write.png`
- `screenshots/reader_pending_reopen_run_r4/13_immediate_reopen_before_room_write.png`
- `screenshots/reader_pending_reopen_run_r4/14_room_write_released_after_reopen.png`
- `screenshots/reader_pending_reopen_run_r4/reader_resume_stage_assertions.txt`

## Review Inputs

- Patch: `reader_resume_autosave_fix.diff`
- Main files:
  - `app/src/main/AndroidManifest.xml`
  - `app/src/main/java/com/qualityalternative/app/MainActivity.kt`
  - `app/src/main/java/com/qualityalternative/app/QualityAlternativeApplication.kt`
  - `app/src/main/java/com/qualityalternative/app/domain/model/ReadingProgressModels.kt`
  - `app/src/main/java/com/qualityalternative/app/domain/service/Contracts.kt`
  - `app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt`
  - `app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt`
  - `app/src/main/java/com/qualityalternative/app/data/AppContainer.kt`
  - `app/src/main/java/com/qualityalternative/app/data/RoomReadingProgressRepository.kt`
  - `app/src/main/java/com/qualityalternative/app/data/local/ReadingProgressDao.kt`
  - `app/src/main/java/com/qualityalternative/app/data/local/ReadingProgressEntity.kt`
  - `app/src/main/java/com/qualityalternative/app/data/local/QualityAlternativeDatabase.kt`
  - `app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt`
  - `app/src/androidTest/java/com/qualityalternative/app/MainActivityTest.kt`
  - `app/src/androidTest/java/com/qualityalternative/app/data/RoomReadingProgressRepositoryTest.kt`
