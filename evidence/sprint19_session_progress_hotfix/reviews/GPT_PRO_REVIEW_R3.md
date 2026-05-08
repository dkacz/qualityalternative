SCORE: 8/10
VERDICT: FAIL
VISUAL REVIEW: PASS

FRESH FINDINGS:

High / release-blocking — Exact claim: late disposal/lifecycle writes cannot downgrade completed reader progress. Why it is vulnerable: MainViewModel.saveCurrentReadingProgress() rejects incomplete writes only at call time through hasCompletedProgressForActiveRead(), then launches an asynchronous repository write with the already-created unfinished ReadingProgress. finishReading() also launches asynchronously, saves the completed row, and only later updates uiState.currentReadingProgress to completed. A lifecycle pause/stop save that is invoked while completion is in progress, before uiState reflects completion, can pass the gate and then write an unfinished row after the completed save. RoomReadingProgressRepository.saveProgress() does not re-check completion dominance before replacing the row, and upsertProgress() replaces the in-memory progress entry for the same contentId. The shipped unit test incompleteLifecycleSaveAfterCompletionDoesNotDowngradeCompletedProgress proves only the post-completion call case after advanceUntilIdle(), not the in-flight lifecycle/disposal ordering case. Files checked: app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt lines 2047-2092, 2163-2191, 3457-3470; app/src/main/java/com/qualityalternative/app/data/RoomReadingProgressRepository.kt lines 47-52; app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt lines 1005-1045. Tightest fix: make completion dominance atomic at the final write boundary, either by re-checking current/stored progress inside the saveCurrentReadingProgress coroutine immediately before readingProgressRepository.saveProgress(progress), or by making ReadingProgressRepository.saveProgress() refuse any unfinished update when the current row for the same content is completed unless the content is explicitly reactivated. Add a focused regression test with a delayed repository save in which finishReading() and a lifecycle/disposal progress save interleave, asserting that the final row remains progressPercent == 100 with completedAtMillis != null.

TRACE CHECKS:

Evidence read first: evidence/sprint19_session_progress_hotfix/HOTFIX_EVIDENCE.md.

Product contract checked: PRD.md lines 256-260 for finite backup and meditation visibility; lines 301-302 for source-anchored durable reader progress; lines 329-331 for AI remaining optional and gated.

Sprint contract checked: docs/SPRINT_19_AI_NOTE_ASSIST.md lines 152-167 for Slice 19.5A deliverables and acceptance; lines 170-305 for AI as a later second-part sequence.

Reader source-position persistence checked: app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt lines 2296-2324 for source-anchored progress position; lines 2325-2336 for lifecycle pause/stop and disposal persistence; lines 2338-2341 for manual-navigation retry persistence; lines 2347-2359 for unconditional page-move persistence; lines 2383-2393 for back/skip save-before-exit ordering; lines 2468-2487 and 2501-2507 for forward/back/complete navigation dispatch; lines 2571-2578 for TOC navigation using the same persisted moveToPage() path.

ViewModel progress durability checked: app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt lines 2047-2092 for progress save, same-position durable refresh, and analytics suppression; lines 2163-2191 for completion save; lines 3457-3470 for completed-progress gating; lines 3636-3643 for same-visible-position comparison.

Repository behavior checked: app/src/main/java/com/qualityalternative/app/data/RoomReadingProgressRepository.kt lines 47-52 for unconditional replace/upsert behavior.

Same-position and duplicate analytics unit coverage checked: app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt test sameVisibleReaderProgressStillRefreshesDurableStoreForLifecycleStop, lines 962-1002, asserting two repository saves, latest updatedAtMillis == 5_200L, and exactly one READING_PROGRESS_SAVED event.

Completion-downgrade unit coverage checked: app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt test incompleteLifecycleSaveAfterCompletionDoesNotDowngradeCompletedProgress, lines 1005-1045. It covers the post-completion call case but not the interleaved in-flight write case.

Connected session E2E checked: app/src/androidTest/java/com/qualityalternative/app/MainActivityTest.kt test sprint19ReaderSessionProgressPersistsLastViewedPageAfterReopen, lines 1663-1719. It performs three forward advances, one backward swipeRight(), waits for saved progress, moves the Activity to Lifecycle.State.CREATED, resumes, then closes/reopens and asserts return to the last viewed page.

Connected meditation E2E checked: app/src/androidTest/java/com/qualityalternative/app/MainActivityTest.kt test sprint19InterventionKeepsMeditationAlternativeWhenPrimaryIsReading, lines 1722-1735.

Meditation engine checked: app/src/main/java/com/qualityalternative/app/domain/service/DefaultRecommendationEngine.kt lines 91-120, which preserves a meditation backup within the capped finite backup list when primary is not meditation and meditation is otherwise eligible.

Meditation unit coverage checked: app/src/test/java/com/qualityalternative/app/domain/service/DefaultRecommendationEngineTest.kt test generate_keepsMeditationBackupWhenPrimaryIsReading, lines 634-672.

Logs checked: evidence/sprint19_session_progress_hotfix/logs/unit.log shows :app:testDebugUnitTest BUILD SUCCESSFUL; logs/assemble.log shows assembleDebug and assembleDebugAndroidTest BUILD SUCCESSFUL; logs/connected_session_progress.log shows one connected test run and BUILD SUCCESSFUL; logs/connected_meditation_backup.log shows one connected test run and BUILD SUCCESSFUL.

Screenshots checked: screenshots/09_session_progress_saved_before_pause_stop.png, screenshots/10_session_progress_restored_after_pause_stop.png, and screenshots/11_session_progress_restored_after_reopen.png all show page 3/12, 26%, and visible paragraphs 15-21 for Sprint 19 Session Progress; screenshots/12_meditation_backup_alternative.png shows reading primary Neither Ask Nor Consent and visible finite backup 3-minute reset.

BLOCKERS:

Completion-downgrade prevention is not fully closed for interleaved lifecycle/disposal saves during asynchronous completion. A lifecycle/disposal progress save can pass the completed-progress gate before finishReading() has updated completed state, then write an unfinished row after the completed save because the repository write path is not completion-dominant.

SESSION PROGRESS:

Forward page moves: proven in code by moveToPage() calling persistReaderProgress(sourcePosition) unconditionally, and exercised by the connected test’s three advanceReaderPage() calls.

Backward page moves: proven in code by the same moveToPage() path, and exercised by the connected test’s swipeRight() followed by a page decrement assertion.

Lifecycle pause/stop: proven in code by the LifecycleEventObserver handling ON_PAUSE and ON_STOP; connected evidence drives Activity CREATED then RESUMED and returns to the last viewed page.

Reader disposal: the disposal hook exists and calls persistVisibleReaderProgress(), but completion-downgrade prevention for interleaved disposal/lifecycle saves is not fully proven because the final write path is not completion-aware.

Back/skip exit ordering: proven for the Reader BackHandler; it calls persistVisibleReaderProgress() before onBack(), and onBack maps to viewModel::skipReading.

Completion-downgrade prevention: partially proven for direct post-completion saves, not proven for in-flight lifecycle/disposal saves during asynchronous completion; this is the release blocker.

Same-position durable refresh: proven by unit test; the same visible position is saved twice and the durable progress timestamp advances.

Duplicate analytics behavior: proven by unit test for same-position lifecycle retry; only one READING_PROGRESS_SAVED event is emitted across the two same-position saves.

MEDITATION BACKUP:

Meditation remains visible as a finite backup when reading is primary and meditation is eligible. The engine preserves meditation inside the capped backup list, the unit test forces reading items to dominate the ranked inventory while asserting a meditation backup remains, the connected test asserts primary is not meditation and meditation is in backups, and screenshots/12_meditation_backup_alternative.png visibly shows 3-minute reset in the finite backup list.

AI BOUNDARY:

The hotfix correctly avoids implementing AI before the release/hotfix gate. The shipped app source contains no OpenRouter client, Gemini/model integration, Ask AI UI, provider credential path, or AI response persistence. PRD.md and docs/SPRINT_19_AI_NOTE_ASSIST.md keep AI note assistance as a later gated sequence rather than part of this hotfix.

BUNDLE GAPS:

None material to the scoped decision. The connected Gradle logs do not print method names, but the log filenames, shipped Android test source, and captured screenshots are sufficient to trace the scoped E2E claims.

PACKAGE HYGIENE:

The bundle is clean enough for this scoped review: it includes the PRD, sprint plan, relevant source files, focused unit and connected tests, logs, screenshots, diff, manifest, and evidence summary, with no bundled APKs, credentials, or unrelated Google Drive/AI implementation artifacts.

evidence/sprint19_session_progress_hotfix/GPT_PRO_REVIEW_R2_PARTIAL.md is stale and redundant; it is correctly described as a dead partial lane in HOTFIX_EVIDENCE.md, but should be removed from a future release packet to avoid ambiguity.

Future packets should add XML connected-test results or exact filtered Gradle commands, and should include a concurrency-focused completion-downgrade regression test once the blocker is fixed.