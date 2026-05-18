SCORE: 9/10

VERDICT: PASS

VISUAL REVIEW: PASS

BLOCKERS

None proven from the R2 bundle.

The hotfix is release-acceptable for the reported stale 41% read · 12 min left regression. The remaining concerns are non-blocking coverage and bundle-evidence gaps, not code blockers.

R1 CONCERN RECHECK

R1 concern: duplicate analytics/autosave possible if background repair and reader-open repair race.
R2 status: addressed for analytics and autosave. durationRepairEventRecordedContentIds gates READING_TIME_ESTIMATE_APPLIED and profile autosave per content id inside one MainViewModel lifetime, and the new unit test openingAfterBackgroundDurationRepairDoesNotDuplicateRepairAnalyticsOrAutosave asserts one repair event and one autosave after opening the same stale document following background repair. See MainViewModel.kt:324-325, MainViewModel.kt:3409-3426, and MainViewModelTest.kt:1054-1111.

R1 concern: repair scheduling filtered attempted ids after .take(1), allowing one failed or redundant candidate to starve older candidates.
R2 status: substantially addressed. R2 filters attempted ids before limiting, sorts by unfinished-progress recency, and schedules up to three candidates per pass. See MainViewModel.kt:3343-3375. Residual non-blocking edge: if the three newest candidates all fail to load or produce no higher estimate, older candidates are not scheduled until a later document/progress emission, because failed/no-op attempts do not themselves emit a repaired document update. This is not a blocker for the reported Home continue-card case.

R1 concern: autosave invocation was not asserted by unit tests.
R2 status: addressed. The background-repair test asserts a single profile autosave write to the configured profile URI, and the duplicate-race test asserts no second autosave. See MainViewModelTest.kt:994-1027 and MainViewModelTest.kt:1094-1107.

R1 concern: bundle lacked several production context files.
R2 status: improved. R2 includes AppContainer.kt, CompositeContentRepository.kt, ReadingTimeEstimator.kt, DocumentReadingTimeEstimator.kt, AccountLightProfile.kt, AndroidAccountLightProfileAutosaveWriter.kt, ContentModels.kt, UserDocumentEntity.kt, QualityAlternativeDatabase.kt, and QualityAlternativeApp.kt. Some relevant definitions are still absent from the shipped source subset; those are listed under BUNDLE GAPS.

READING TIME REMAINING

R2 addresses the reported regression for the targeted legacy state: an unfinished private-reader user document whose persisted durationMinutes still looks like the old cap, <= ReadingTimeEstimator.MAX_SESSION_MINUTES, which is 20. The candidate predicate requires USER_DOCUMENT, repository-backed reader content, and durationMinutes <= 20. See ReadingTimeEstimator.kt:19-21 and MainViewModel.kt:4114-4122.

The background repair uses the actual reader text, computes a new estimate with ReadingTimeEstimator.estimateFromText, and persists only if the new estimate exceeds the stale value. See MainViewModel.kt:3361-3374, MainViewModel.kt:3387-3391, and MainViewModel.kt:3396-3408.

The Home and Library remaining-time displays consume item.durationMinutes through remainingMinutes(...), so once the repository emits the repaired ContentItem, the card’s remaining time changes from the stale value to the corrected value. See QualityAlternativeApp.kt:4907-4935, QualityAlternativeApp.kt:5110-5113, and QualityAlternativeApp.kt:5467-5469.

The visual fixture proves the intended case: a long imported Markdown document is seeded with durationMinutes = 20 and progressPercent = 41, then after repair the visible Home card changes from 41% read · 12 min left to 41% read · 1 hr 20 min left. See VisualQaScreenshotTest.kt:1994-2041 and the R2 screenshots under evidence/sprint22_reading_time_remaining_hotfix/screenshots/final_connected_run_r2/.

Non-blocking caveat: the first R2 screenshot intentionally captures the stale card before asynchronous repair finishes, so the fix does not prevent a brief initial stale-frame display on startup. It does repair the persisted estimate and the card thereafter.

FLOW / RACE CHECK

The background flow is bounded and relevant. It combines user documents with reading progress, filters to unfinished progress, filters to legacy private-reader user-document candidates, sorts by most recently updated progress, filters already-attempted ids before limiting, and schedules at most three repair jobs per pass. See MainViewModel.kt:369-380 and MainViewModel.kt:3343-3375.

The repair does not parse the whole library. Background parsing is limited to the selected candidates, and reader-open repair only uses the document already loaded for the reader. See MainViewModel.kt:3335-3391.

The document/progress race handling is acceptable. The combined stream makes the repair decision from a paired document/progress snapshot, rather than from independent observers. Duplicate background scheduling is guarded by durationRepairAttemptedContentIds. Duplicate repair event/autosave behavior is guarded by durationRepairEventRecordedContentIds.

Background/open race behavior is acceptable. A race can still cause duplicate estimation work, and in a tight race can still call updateEstimatedDuration more than once with the same value, but R2 prevents duplicate repair analytics and duplicate autosave in the same ViewModel lifetime. The production repository also avoids a second DAO write if the persisted duration already equals the safe estimate. See RoomUserDocumentRepository.kt:153-177.

Reader-open repair keeps the active reader state consistent. openLibraryItem(...) and openReplacementSession(...) both replace stale content with repairedContent, then use that repaired item for currentContent and screenForReplacement(...). Manual-continue analytics metadata also uses repairedContent.analyticsMetadata(). See MainViewModel.kt:1342-1375 and MainViewModel.kt:3289-3311.

PERSISTENCE / AUTOSAVE / ANALYTICS

Persistence is acceptable. R2 adds UserDocumentRepository.updateEstimatedDuration(...), UserDocumentDao.findById(...), and UserDocumentDao.updateDurationMinutes(...). The Room implementation clamps the estimate to MIN_SESSION_MINUTES..MAX_DOCUMENT_MINUTES, updates durationMinutes and updatedAtMillis, and updates the repository’s in-memory state flow. See Contracts.kt:107-111, UserDocumentDao.kt:17-31, and RoomUserDocumentRepository.kt:153-177.

No schema migration is required for this hotfix because durationMinutes already exists in UserDocumentEntity and in the Room table definition. See UserDocumentEntity.kt:14-28 and QualityAlternativeDatabase.kt:320-337.

Autosave behavior is acceptable. After a repaired duration is persisted and the repair event is admitted by the per-content guard, R2 invokes autosaveAccountLightProfileAfterPortableMutation(...). The exporter reads userDocumentRepository.userDocuments(), and portable user documents export durationMinutes, so the repaired value is included in the portable profile. See MainViewModel.kt:3426, MainViewModel.kt:3526-3532, AccountLightProfile.kt:480-486, and AccountLightProfile.kt:1585-1593.

Analytics behavior is acceptable. R2 records READING_TIME_ESTIMATE_APPLIED with estimateSource, repairSource, previousDurationMinutes, and repaired durationMinutes. For private-reader user documents, analyticsMetadata() does not include document body text or URI because externalUrl is absent for private-reader Markdown/EPUB documents. See MainViewModel.kt:3413-3425 and MainViewModel.kt:4510-4520.

TEST / EVIDENCE

Unit evidence passes. The bundled testDebugUnitTest_r2.log reports BUILD SUCCESSFUL.

Connected visual evidence passes. The bundled connectedDebugAndroidTest_sprint22_reading_time_r2.log reports one focused screenshot test completed successfully on qaApi36.

New R2 unit coverage is materially stronger than R1:

unfinishedLegacyDocumentDurationIsRepairedBeforeContinueCardUsesRemainingTime verifies background repair of a stale unfinished document from 20 to 24, confirms repository state, verifies repair analytics metadata, and asserts profile autosave invocation. See MainViewModelTest.kt:971-1028.

openingLegacyDocumentRepairsDurationFromLoadedReaderText verifies reader-open repair from loaded text and asserts currentContent.durationMinutes is repaired. See MainViewModelTest.kt:1030-1052.

openingAfterBackgroundDurationRepairDoesNotDuplicateRepairAnalyticsOrAutosave verifies that opening after background repair does not duplicate repair analytics or autosave. See MainViewModelTest.kt:1054-1111.

Visual review passes. The R2 screenshot sequence shows:

00_home_continue_before_repair_assertion.png: 41% read · 12 min left.

01_home_continue_after_repair_wait.png: 41% read · 1 hr 20 min left.

02_home_continue_repaired_remaining_time.png: 41% read · 1 hr 20 min left.

Non-blocking test limitations remain: there is no direct unit test for the three-candidate batch limit, attempted-before-limit scheduling behavior, parser failure, completed-document exclusion, external/PDF exclusion, or exact programmatic assertion of 1 hr 20 min left. The visual screenshot proves the exact text, while the connected test asserts disappearance of 12 min left and durationMinutes > 20.

BUNDLE GAPS

BUNDLE GAP: The shipped source subset does not include the ReadingProgress model or its isUnfinished() / isCompleted() definitions, although the included code and tests depend on them. The connected and unit logs reduce risk, but the exact unfinished/completed predicate cannot be audited from source in this bundle alone.

BUNDLE GAP: The shipped source subset references DocumentImportCandidateFactory, but its definition is not included. This does not block review of the R2 repair path, but it prevents a complete source audit of the original improved import-time estimation pipeline from the bundle alone.

BUNDLE GAP: The bundle includes debug unit and connected-test logs, but not a release-variant build log or release APK artifact. No release-specific blocker is proven, but release assembly itself is not evidenced.

BUNDLE GAP: The bundle is not a complete compileable project snapshot; it is a focused review packet. The included Gradle logs show the full working tree compiled and tested, but the source files in the zip alone are not sufficient to independently reproduce that compile.

PACKAGE HYGIENE

Package hygiene is acceptable for a review bundle. The changed production files are limited to the repository contract, DAO, Room repository, and ViewModel repair flow; the changed test files are the ViewModel unit tests and the focused visual screenshot test. git_diff_files_r2.txt lists only those six modified source/test files, and git_diff_stat_r2.txt shows a focused hotfix-sized change set.

The evidence directory is useful and scoped: R2 includes the R1 review, R2 prompt, manifest, validation summary, patch, status/stat files, debug unit log, focused connected-test log, and R2 screenshots.

Non-blocking hygiene note: the bundle retains both the prior final_connected_run/ screenshots and the R2 final_connected_run_r2/ screenshots. This is not confusing because the manifest distinguishes them, but the R2 directory is the authoritative visual evidence for this review.