SCORE: 8/10
VERDICT: FAIL
VISUAL REVIEW: PASS

FRESH FINDINGS:

High / release-blocking — Reader disposal persistence is not exit-state safe.
Exact claim: ReaderScreen durably refreshes progress on reader disposal.
Why it is vulnerable: ReaderScreen calls persistVisibleReaderProgress() from DisposableEffect.onDispose, but MainViewModel.saveCurrentReadingProgress() depends on uiState.currentContent still being present and not already completed. On the normal back/skip path, ReaderScreen.BackHandler calls onBack = viewModel::skipReading; skipReading() eventually calls clearActiveSession(), which sets currentContent = null, so the disposal save can become a no-op. On the done/completion path, onDone = viewModel::finishReading; finishReading() saves a completed ReadingProgress row and moves to MainScreen.Feedback while leaving currentContent present, after which ReaderScreen.onDispose can call saveCurrentReadingProgress() with a non-completed, clamped 1..99 progress row. Because sameVisiblePosition() treats completed and non-completed progress as different, this can issue a later unfinished READING_PROGRESS_SAVED save after completion and potentially downgrade a completed item back to unfinished unless an unshipped repository layer prevents it.
Files checked: app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt lines 2326-2336, 2383-2390, 2465-2485, 2573-2575, and call site lines 657-679; app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt lines 2047-2090, 2160-2188, 2213-2236, 3251-3269, and sameVisiblePosition() lines 3618-3625.
Tightest fix: Make progress persistence completion-aware and exit-order safe in MainViewModel.saveCurrentReadingProgress(): reject any non-completed progress write when the current or stored row for the same content is completed, and explicitly persist the latest visible reader position before skipReading() clears currentContent. A focused regression test should cover completing from the last page and then waiting for reader disposal, asserting that the repository still contains progressPercent == 100 with completedAtMillis != null.

Medium — The visual evidence is labeled as lock-style evidence, but the shipped connected test exercises scenario close/reopen rather than an actual device lock/unlock lifecycle.
Exact claim: The evidence describes 09_session_progress_saved_before_lock.png and the scope describes lock/reopen durability.
Why it is vulnerable: The connected test sprint19ReaderSessionProgressPersistsLastViewedPageAfterReopen performs scenario?.close(), waits, relaunches the onboarded app, and reopens the same document. That proves close/reopen restoration after forward navigation and one backward swipe, but it does not itself exercise a physical lock/unlock path or explicitly drive an ON_PAUSE/ON_STOP lifecycle interruption while keeping the activity instance alive. The code does include an ON_PAUSE/ON_STOP observer, and the ViewModel unit test proves same-position durable refresh semantics, so this is an evidence-label mismatch rather than a navigation/restoration failure.
Files checked: evidence/sprint19_session_progress_hotfix/HOTFIX_EVIDENCE.md; app/src/androidTest/java/com/qualityalternative/app/MainActivityTest.kt lines 1661-1707; screenshots 09_session_progress_saved_before_lock.png and 10_session_progress_restored_after_reopen.png.
Tightest fix: Either rename the evidence to close/reopen wording or add one focused connected lifecycle test that explicitly moves the activity through pause/stop or uses a controlled lock/unlock-style device transition, then reopens and asserts the same source-anchored position.

TRACE CHECKS:

Evidence file read first: evidence/sprint19_session_progress_hotfix/HOTFIX_EVIDENCE.md.

Product contract checked: PRD.md lines 240-260 for meditation as a standing finite backup; PRD.md lines 291-314 for reader progress durability; PRD.md lines 329-331 for future optional AI annotation behavior.

Sprint contract checked: docs/SPRINT_19_AI_NOTE_ASSIST.md lines 46-48 and 152-167 for Slice 19.5A rules; lines 169-222 for AI sequencing after the hotfix gate.

Reader persistence code checked: QualityAlternativeApp.kt lines 2310-2341 for persistReaderProgress, lifecycle pause/stop observer, disposal save, and manual-navigation persistence; lines 2347-2360 for unconditional forward/back/TOC moveToPage() persistence.

ViewModel progress code checked: MainViewModel.kt lines 2047-2090 for same-visible-position durable saves and analytics suppression; lines 3618-3625 for sameVisiblePosition().

Same-position unit coverage checked: MainViewModelTest.kt test sameVisibleReaderProgressStillRefreshesDurableStoreForLifecycleStop, lines 964-1002, asserting two repository saves, latest updatedAtMillis == 5_200L, and exactly one READING_PROGRESS_SAVED event.

Connected session E2E checked: MainActivityTest.kt test sprint19ReaderSessionProgressPersistsLastViewedPageAfterReopen, lines 1661-1707, including three forward page advances, one backward swipeRight(), saved progress wait, scenario close/reopen, and restored page assertion.

Session screenshots checked: screenshots/09_session_progress_saved_before_lock.png and screenshots/10_session_progress_restored_after_reopen.png; both show page 3/12, 26%, and visible text from paragraphs 15-21. The files are byte-identical, which is visually consistent with same-page restoration but does not independently distinguish the before/after transition without the connected test sequence.

Meditation recommendation code checked: DefaultRecommendationEngine.kt lines 91-120, which caps backups at six and replaces the last capped backup with meditation when meditation is eligible but otherwise outside the finite backup cap.

Meditation unit coverage checked: DefaultRecommendationEngineTest.kt test generate_keepsMeditationBackupWhenPrimaryIsReading, lines 634-672.

Meditation connected E2E checked: MainActivityTest.kt test sprint19InterventionKeepsMeditationAlternativeWhenPrimaryIsReading, lines 1710-1723.

Meditation screenshot checked: screenshots/11_meditation_backup_alternative.png, which shows reading primary Neither Ask Nor Consent and visible backup 3-minute reset.

Validation logs checked: logs/unit.log shows :app:testDebugUnitTest BUILD SUCCESSFUL; logs/assemble.log shows assembleDebug and assembleDebugAndroidTest BUILD SUCCESSFUL; logs/connected_session_progress.log and logs/connected_meditation_backup.log each show one connected test run and BUILD SUCCESSFUL.

BLOCKERS:

Reader disposal persistence must be made completion-aware and exit-order safe before release, because the shipped code can no-op on back/skip disposal and can issue a non-completed progress save after reader completion disposal.

SESSION PROGRESS:

Forward page moves: Proven in code by moveToPage() calling persistReaderProgress(sourcePosition) unconditionally, and exercised by the connected test’s three advanceReaderPage() calls.

Backward page moves: Proven in code by the same moveToPage() path, and exercised by the connected test’s swipeRight() followed by a page decrement assertion.

Lifecycle pause/stop: Proven as a code path by LifecycleEventObserver handling ON_PAUSE and ON_STOP; same-position durable semantics are proven at ViewModel level by sameVisibleReaderProgressStillRefreshesDurableStoreForLifecycleStop. The connected visual evidence does not specifically prove a physical lock/unlock lifecycle.

Reader disposal: Not fully proven and release-blocked. The disposal hook exists, but effective durable persistence depends on ViewModel exit state and is vulnerable on back/skip and completion paths.

Same-position durable refresh: Proven at ViewModel level: the same visible position is saved twice, the second save refreshes updatedAtMillis, and the durable repository receives both saves.

Duplicate analytics behavior: Proven at ViewModel level for same-position lifecycle retries: only one READING_PROGRESS_SAVED event is emitted across two same-position saves.

MEDITATION BACKUP:

Meditation remains visible as a finite backup when reading is primary and meditation is eligible. The code preserves meditation inside the capped backup list, the unit test forces reading items to dominate the ranked inventory, the connected test asserts the meditation content id is in backups while primary is not meditation, and 11_meditation_backup_alternative.png visibly shows 3-minute reset in the finite backup list.

AI BOUNDARY:

The hotfix correctly avoids implementing AI before the release/hotfix gate. The shipped app source contains no Ask AI, OpenRouter, Gemini, LLM client, provider model, or API-key implementation. PRD.md and docs/SPRINT_19_AI_NOTE_ASSIST.md keep AI note assistance as a later, gated Slice 19.6+ effort and state that AI remains blocked until this hotfix is reviewed and released.

BUNDLE GAPS:

The concrete ReadingProgressRepository implementation is not included, so the bundle cannot prove that the storage layer would reject a late unfinished progress write after a completed progress write. The shipped ViewModel code should not rely on an unshipped lower layer to prevent completion downgrade.

The connected logs do not include test method names or instrumentation filter arguments; the source tests, log filenames, and screenshots support the intended trace, but future packets should include XML test results or the exact filtered Gradle commands for stronger evidence traceability.

PACKAGE HYGIENE:

The bundle is clean enough for this scoped review: it contains the PRD, sprint plan, relevant app source files, relevant unit and connected tests, focused logs, screenshots, diff, manifest, and review prompt, with no obvious unrelated APKs, credentials, prior review outputs, or stale screenshot folders.

Future packets should add the concrete reading-progress repository source or an explicit note that it is unchanged, Android test XML results or exact test-filter commands, and a lock/unlock or explicit pause/stop lifecycle evidence artifact if the evidence continues to use lock-oriented wording.