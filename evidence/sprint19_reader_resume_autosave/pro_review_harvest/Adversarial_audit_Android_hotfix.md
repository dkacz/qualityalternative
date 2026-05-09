SCORE: 7/10

VERDICT: BLOCK

VISUAL REVIEW: REVISE

BLOCKERS:

The Activity close/reopen path is not proven durable, and a stale resume path remains: saveCurrentReadingProgress() updates uiState.readingProgress synchronously, then performs readingProgressRepository.saveProgress(progress) in viewModelScope.launch. A latest-page save can therefore be visible in the same-process UI before it is durably written; if the Activity/ViewModel is closed while that write is pending or queued behind another save, the newer anchor can be lost and reopen can fall back to older persisted progress.

The connected test’s “saved progress” wait is no longer a durable-save wait. savedProgressParagraphIndexFor() reads activity.mainViewModel.uiState.readingProgress, which the hotfix now updates immediately before the repository write begins. This means the test can pass the pre-close wait without proving that Room contains the last visible page.

The release evidence does not include an adversarial lifecycle-close case with a delayed latest save followed by Activity/ViewModel destruction and reopen. That is the exact failure class most relevant to “read for a while, lock/return or reopen resumes stale.”

READER RESUME AUTOSAVE: The ViewModel fix materially improves resume correctness for active same-process reading: progress is updated immediately in UI state, paragraph anchor and text offset are retained, and same-percent/different-anchor saves are no longer collapsed by rounded percentage alone. However, because durable persistence remains asynchronous and ViewModel-scoped, the fix is not logically sufficient for the full reopen complaint.

STALE WRITE / RACE CHECK: The monotonic updatedAtMillis generation in MainViewModel addresses same-millisecond unfinished saves from the reader, and RoomReadingProgressRepository rejects older unfinished writes when a newer unfinished row is already reflected in repository state. Completion-over-unfinished is also guarded in the repository. The remaining race is not older-write replacement after the newer write has landed; it is cancellation or non-completion of the latest asynchronous save before Activity close/reopen.

LIFECYCLE / REOPEN CHECK: Pause/stop and reopen are covered only under non-adversarial timing. The connected test moves to CREATED, resumes, closes, and relaunches, but it first waits on ViewModel state rather than the repository/Room row. There is no shipped test that delays the final repository write, closes the Activity/ViewModel, and verifies that reopen still resumes to the newer paragraph anchor.

TEST/EVIDENCE: Unit XML reports 109 passing MainViewModelTest cases, including delayedOlderReaderProgressSaveCannotOverwriteNewerVisiblePosition. Connected XML reports the scoped reader resume test and Room stale-write test passing. The new Room test directly covers “newer unfinished row rejects later older unfinished save.” The connected reader test now checks paragraph anchor, which is an improvement over percent-only evidence, but its persistence assertion is weakened by the new immediate UI upsert.

BUNDLE GAPS:

EVIDENCE.md claims assembleDebugAndroidTest and assembleDebug passed, but no build logs for those commands are shipped.

The bundle does not include the reader composable/MainActivity source that triggers autosaves on page change, dispose, pause, or stop, so lifecycle trigger behavior cannot be source-audited from shipped files.

The bundle does not include the Room DAO/entity/database source, so SQL conflict behavior and schema details cannot be source-audited; the shipped connected Room test partially mitigates this but does not replace source review.

PACKAGE HYGIENE: The bundle is well scoped and includes the requested patch, modified source/test files, XML logs, and visual artifacts. Evidence quality is weakened by overclaiming unshipped assemble results and by three screenshot files that are byte-identical; the screenshots visually show page 3/12, 26%, and visible text beginning at “Session progress paragraph 15,” but as artifacts they do not independently prove that three distinct lifecycle/reopen stages were captured.