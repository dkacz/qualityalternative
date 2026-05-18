SCORE: 10/10

VERDICT: PASS

VISUAL REVIEW: PASS

BLOCKERS

None for a GitHub alpha APK release.

R3 BLOCKER RECHECK

Resolved. The release-readiness blocker is addressed by R4 evidence: assembleDebugRelease_r4.log shows successful assembleDebug assembleRelease; apk_r4/sprint22-reading-time-hotfix-release-debugsigned.apk is present; apksigner_verify_release_debugsigned_r4.log reports Verifies with APK Signature Scheme v3 and one Android Debug signer; SHA-256 hashes are present and match the bundled APK files; release_install_smoke_r4.log shows successful install, cold launch, and running process evidence; release_install_smoke_r4.png shows the launched app.

Resolved. The cascade blocker is addressed in MainViewModel.kt by legacyReadingTimeBackgroundRepairCycleStarted, which is checked before candidate selection and set to true before the repair coroutine launches. Repair-induced userDocumentRepository.observeUserDocuments() emissions therefore cannot schedule another background scan window in the same MainViewModel lifetime. The unit test backgroundRepairDoesNotCascadeBeyondSingleBoundedStartupCycle seeds twelve stale unfinished documents and proves that only the first three receive updated durations.

READING TIME REMAINING

R4 fully addresses the reported 41% read · 12 min left stale estimate regression for the targeted legacy private-reader document case. The repair predicate is restricted to unfinished USER_DOCUMENT items that use repository-backed reader content and still have a duration at or below ReadingTimeEstimator.MAX_SESSION_MINUTES (20). The repair then re-estimates from actual reader text and persists only when the recovered estimate is greater than the stale duration.

The visible Home-card behavior follows directly from the repaired durationMinutes. For the connected fixture, roughly 30,000 words estimate to 134 minutes at 225 words per minute; at 41% progress, the remaining time rounds up to 80 minutes, which formats as 1 hr 20 min left. The R4 visual test asserts both removal of 12 min left and presence of 1 hr 20 min left.

FLOW / RACE CHECK

Repair remains bounded to relevant unfinished private-reader documents rather than parsing the whole library. The scheduler receives user documents plus reading progress, filters to unfinished legacy private-reader candidates, sorts by newest unfinished progress, excludes attempted and in-flight IDs, takes at most ten candidates, and performs at most three successful repairs. Metadata filtering may inspect the in-memory document list, but expensive reader-body parsing is limited to the bounded candidate window.

The document/progress race is handled acceptably because scheduling is driven by a combined document/progress stream rather than by independent document-only emissions. A document is not repaired by the background path unless its paired progress snapshot is unfinished.

Background/open duplicate behavior is acceptable. durationRepairEventRecordedContentIds prevents duplicate READING_TIME_ESTIMATE_APPLIED analytics and duplicate profile autosave within a MainViewModel lifetime. A stale object passed to openLibraryItem after background repair may still trigger a redundant estimate/update attempt, but the production Room repository is idempotent because it skips the DAO update when the persisted duration already equals the safe estimate. This is not a release blocker.

PERSISTENCE / AUTOSAVE / ANALYTICS

Persistence is acceptable. R4 adds UserDocumentRepository.updateEstimatedDuration, UserDocumentDao.findById, and UserDocumentDao.updateDurationMinutes; the Room implementation clamps repaired values to the document range, updates durationMinutes and updatedAtMillis, and refreshes the repository state flow. No schema migration is required because durationMinutes already exists in UserDocumentEntity and the user_documents table.

Autosave behavior is acceptable. A successful repaired-duration persistence path invokes portable-profile autosave after the analytics guard admits the repair event. The included unit coverage verifies an autosave write when background repair changes a stale document and verifies no second autosave when opening the same document after background repair.

Analytics behavior is acceptable. R4 records READING_TIME_ESTIMATE_APPLIED with estimateSource, repairSource, previousDurationMinutes, and repaired durationMinutes. The event is guarded per content ID for the ViewModel lifetime, preventing duplicate repair analytics for background/open overlap.

TEST / EVIDENCE

Sufficient. testDebugUnitTest_r4.log reports BUILD SUCCESSFUL. The relevant unit coverage includes background repair before Home remaining-time use, reader-open repair from loaded text, duplicate analytics/autosave suppression after background repair, failed-candidate continuation to the fourth candidate, and no-cascade scheduling with twelve stale documents.

Sufficient. connectedDebugAndroidTest_sprint22_reading_time_r4.log reports one focused connected test completed successfully on qaApi36. The connected test seeds a legacy long Markdown document with duration 20, progress 41%, and a long body; it programmatically asserts that the Home card no longer contains 12 min left and does contain 1 hr 20 min left.

VISUAL REVIEW

Pass. The R4 screenshots prove the visual transition:

00_home_continue_before_repair_assertion.png shows 41% read · 12 min left.

01_home_continue_after_repair_wait.png shows 41% read · 1 hr 20 min left.

02_home_continue_repaired_remaining_time.png continues to show 41% read · 1 hr 20 min left.

APK / RELEASE READINESS

Sufficient for a GitHub alpha APK release. The bundle includes both debug and locally debug-signed release APK artifacts, successful debug/release assembly evidence, release and debug APK signature verification logs, SHA-256 hashes, and emulator install/launch smoke evidence.

The release APK is intentionally signed with the Android Debug certificate. That is acceptable for the stated alpha/GitHub installable release evidence and is not Play Store production signing. The release verifier output shows APK Signature Scheme v3 verification; build.gradle.kts declares minSdk = 29, so v3-only signing is compatible with the supported Android API range.

The release smoke log contains an initial uninstall cleanup failure, but the subsequent install succeeds, launch reports Status: ok and LaunchState: COLD, and pidof com.qualityalternative.app returns a running process ID. The smoke screenshot confirms the launched app UI.

BUNDLE GAPS

No bundle gap blocks the requested decision.

Non-blocking scope note: the bundle is a focused review packet rather than a complete source checkout. Some app-level implementation files, such as the full activity/manifest path and the Android document body loader implementation, are not included for independent source audit. The requested hotfix behavior is nevertheless supported by the shipped ViewModel/repository code, unit tests, connected visual test, APK artifacts, signature logs, and install/launch smoke evidence.

PACKAGE HYGIENE

Acceptable. The changed source/test files are limited to the repository contract, DAO, Room repository, MainViewModel, MainViewModelTest, and the focused visual QA test. The patch is scoped to stale reading-time repair and its evidence.

Acceptable. The APK evidence directory is clearly labeled for R4, includes the signed release APK and debug APK, and includes SHA-256 hashes that match the bundled files. Prior R2/R3 screenshots and logs remain in the evidence tree, but the R4 manifest clearly identifies final_connected_run_r4 and the R4 logs as authoritative.

Acceptable. No keystore, local signing secret, local.properties, obvious environment secret, or private key file is present in the shipped bundle. The .idsig file is consistent with the incremental install evidence and is not a hygiene issue.