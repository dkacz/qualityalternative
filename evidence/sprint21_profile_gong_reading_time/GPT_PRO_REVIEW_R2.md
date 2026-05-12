SCORE: 8/10
VERDICT: BLOCK
VISUAL REVIEW: REVISE

BLOCKERS:

The included app/src/test/java/com/qualityalternative/app/data/DocumentReadingTimeEstimatorTest.kt is stale and contradicts the Sprint 21 R2 reading-time implementation. It still expects 10,000-word Markdown and EPUB imports to estimate as 20 minutes, while ReadingTimeEstimator.estimateFromText() now correctly returns 45 minutes at 225 words per minute. This test is included in the shipped bundle but is absent from the unit XML, so the evidence is selective and the included test suite is not release-clean.

R1 BLOCKER RECHECK:

PASS on the R1 product blocker. MainViewModel.previewDefaultAccountLightProfileImport() now calls loadDefaultAccountLightProfileImport(applyImmediately = false), stores a pending import plan, and exposes accountLightImportPreview without applying applyReplace.

PASS in QualityAlternativeApp.kt. The Settings Restore default backup button is wired to viewModel::previewDefaultAccountLightProfileImport, while onboarding uses the separate immediate clean-state restore path, restoreDefaultAccountLightProfileFromOnboarding.

PASS in MainActivityTest.kt. settingsDefaultBackupRestoreShowsPreviewBeforeReplace clicks the Settings default restore button, waits for preview, asserts readerFontScale remains at the local default before confirmation, opens the Replace confirmation, then applies Replace and verifies the restored value.

PROFILE RESTORE:

PASS for implementation trace. AppContainer.defaultProfileAutosaveUri now points to AndroidAccountLightProfileAutosaveWriter.DEFAULT_PROFILE_BACKUP_URI, and the same writer is wired as both autosave writer and backup reader through MainViewModelFactory.

PASS for clean-state flow. onboardingRestoreProfileLoadsDefaultBackupAfterCleanInstall saves settings to the default shared backup, clears persistent app state, launches onboarding, clicks Restore profile, and verifies restored onboarding completion, reader font scale, and meditation duration.

REVISE on reinstall evidence. The connected test clears app persistence through the test reset path; it does not demonstrate a literal uninstall/reinstall cycle. The source path is plausible for reinstall recovery, but the evidence proves clean-state/data-reset behavior rather than package reinstall behavior.

SETTINGS DEFAULT RESTORE SAFETY:

PASS for destructive-safety behavior. Settings default restore now previews first, preserves local settings before confirmation, and requires the user to click Replace and then the explicit confirmation action before local portable settings are replaced.

PASS for UI evidence. The screenshots show the preview card, the Replace confirmation card, and the post-restore success state.

REVISE for copy accuracy. The default path is shown as Downloads/Quality Alternative/quality-alternative-profile.json, but the status line still says “STORES THE PROFILE BACKUP IN APP STORAGE,” which is misleading now that the default destination is a shared Downloads path.

MEDIASTORE COLLISION:

PASS for implementation. AndroidAccountLightProfileAutosaveWriter accepts exact names and Android collision names such as quality-alternative-profile (1).json, and reads the newest matching MediaStore row by descending _ID.

PASS for connected evidence. readsNewestDefaultSharedProfileBackupWhenMediaStoreAddsCollisionSuffix passed on qaApi36(AVD) - 16, and logcat shows MediaStore creating and reading a collision-suffixed file.

REVISE on strict “newest” semantics. The implementation projects DATE_MODIFIED but sorts by _ID DESC, so the evidence proves newest inserted MediaStore row wins, not necessarily newest by modification timestamp. That likely matches the Android collision scenario, but the wording should be tightened or the sort should use the intended freshness field.

MEDITATION GONG:

PASS for source behavior. The old ToneGenerator / startTone path is removed from the shipped source, and meditation completion now calls MeditationGong.play().

PASS for gong implementation plausibility. MeditationGong generates a decaying multi-frequency PCM tone in app code and plays it through AudioTrack.

REVISE for evidence depth. The connected tests open the meditation alternative and timer, but they do not wait for timer completion or verify the completion copy/audio path at runtime.

READING TIME:

PASS for core estimator. ReadingTimeEstimator.estimateFromText() now clamps extracted text estimates to 3..720 minutes, with unit XML proving 10,000 words becomes 45 minutes and a huge text caps at 720.

PASS for import call chain. DocumentImportCandidateFactory calls DocumentReadingTimeEstimator.estimate(), and Markdown/EPUB routes extract body text before calling ReadingTimeEstimator.estimateFromText().

PASS for link/session defaults. MAX_SESSION_MINUTES remains 20; DEFAULT_LINK_MINUTES remains 8; PDF and fallback document defaults remain 10.

REVISE for stale test coverage. DocumentReadingTimeEstimatorTest still asserts the old 20-minute cap for 10,000-word Markdown/EPUB imports and was not included in the unit XML. This is the release blocker.

TEST/EVIDENCE:

PASS: Unit XML shows ReadingTimeEstimatorTest and MainViewModelTest passing with zero failures/errors.

PASS: Connected XML shows 7 selected connected tests passing with zero failures/errors and exit code 0.

PASS: Connected coverage now includes clean-state onboarding default restore, Settings preview-before-replace, default shared backup write/read, and MediaStore collision read.

REVISE: The README verification command intentionally excludes DocumentReadingTimeEstimatorTest, which is included in the bundle and stale.

REVISE: No visual or connected evidence demonstrates a long Markdown/EPUB import displaying a multi-hour estimate after import.

REVISE: No test evidence directly exercises meditation timer completion or the generated gong runtime path.

BUNDLE GAPS:

BUNDLE GAP: The bundle is not self-contained for rebuilding. It lacks the Gradle wrapper, version catalog, many referenced application/source files, resources, Room database classes, domain model files, fixture classes, and repository implementations.

BUNDLE GAP: The included DocumentReadingTimeEstimatorTest.kt has no corresponding XML result despite being directly relevant to this sprint’s import call-chain requirement.

BUNDLE GAP: The evidence proves app data reset/clean state, not an actual uninstall/reinstall.

BUNDLE GAP: Repository-level persistence for long document durations cannot be fully verified from shipped source because the concrete user-document repository and domain model definitions are not included.

PACKAGE HYGIENE:

PACKAGE HYGIENE: GPT_PRO_REVIEW_R1.md and pro_review_harvest_r1/Adversarial_Audit_Sprint_21.md are byte-identical duplicates.

PACKAGE HYGIENE: Screenshot directories still use stale sprint names such as sprint16-portable-profile-* and sprint19-reader-form-regression-* inside Sprint 21 R2 evidence. They appear current by timestamp, but the naming remains misleading.

PACKAGE HYGIENE: The Settings screenshots contain misleading default-backup status copy: “STORES THE PROFILE BACKUP IN APP STORAGE” under the Downloads path.

PACKAGE HYGIENE: Connected logcat files contain substantial unrelated emulator/system noise and repeated resource close/release warnings; not a product blocker, but noisy release evidence.

PACKAGE HYGIENE: The default backup test environment is visibly polluted with many prior MediaStore collisions, for example quality-alternative-profile (24).json, which helps exercise collision handling but should be called out as test-environment residue rather than ordinary default-path behavior.