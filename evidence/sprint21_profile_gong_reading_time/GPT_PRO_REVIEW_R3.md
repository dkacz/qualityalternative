SCORE: 8/10
VERDICT: BLOCK
VISUAL REVIEW: PASS

BLOCKERS:

app/src/test/java/com/qualityalternative/app/ui/DocumentImportCandidateFactoryTest.kt is stale and contradicts the shipped reading-time implementation. It still expects 10,000-word Markdown and EPUB imports to estimate as 20 minutes, while DocumentImportCandidateFactory.fromPickedDocument() now routes through DocumentReadingTimeEstimator.estimate(), which routes extracted text to ReadingTimeEstimator.estimateFromText() and returns 45 minutes at 225 words per minute. This test class is also absent from the shipped unit XML. This is the same release-cleanliness failure pattern as the R2 blocker, now in a different directly relevant import-path test.

R1/R2 BLOCKER RECHECK:

PASS: The R1 Settings default-restore blocker remains fixed. previewDefaultAccountLightProfileImport() calls loadDefaultAccountLightProfileImport(applyImmediately = false), stores a pending plan, exposes accountLightImportPreview, and does not call applyReplace() before user confirmation.

PASS: The Settings UI button is wired to viewModel::previewDefaultAccountLightProfileImport; onboarding uses the separate immediate clean-state restore path, restoreDefaultAccountLightProfileFromOnboarding().

PASS: settingsDefaultBackupRestoreShowsPreviewBeforeReplace verifies that readerFontScale remains local/default after preview, then requires the preview Replace action and the explicit confirmation Replace action before replacement.

PASS: The R2 named blocker is fixed for DocumentReadingTimeEstimatorTest.kt; the test now expects 45 minutes for 10,000-word Markdown/EPUB extraction and 720 minutes for huge extracted text, and its XML is shipped with tests="3" failures="0" errors="0".

BLOCK: R3 leaves DocumentImportCandidateFactoryTest.kt stale and unreported in XML, so the included test suite is still not release-clean.

PROFILE RESTORE:

PASS: The default backup URI is wired through AppContainer.defaultProfileAutosaveUri to AndroidAccountLightProfileAutosaveWriter.DEFAULT_PROFILE_BACKUP_URI, and MainViewModelFactory wires the same writer as both accountLightProfileAutosaveWriter and accountLightProfileBackupReader.

PASS: The default path is user-visible and sane: Downloads/Quality Alternative/quality-alternative-profile.json.

PASS: The restore reader uses the default shared Downloads MediaStore location, not app-private storage, and onboarding exposes a visible Restore profile entry point for a clean-state restore.

PASS with evidence limitation: onboardingRestoreProfileLoadsDefaultBackupAfterCleanInstall proves app-data clean-state recovery after resetting persistent app state. The source path supports reinstall recovery because the backup is in shared Downloads, but the shipped evidence does not perform a literal uninstall/reinstall cycle.

SETTINGS DEFAULT RESTORE SAFETY:

PASS: Settings Restore default backup now previews first and preserves local settings before confirmation.

PASS: The preview card shows imported scope and warnings; clicking Replace opens a separate confirmation card; only the confirmation Replace action applies applyReplace().

PASS: The connected test asserts the local reader font scale remains unchanged before confirmation and changes only after confirmed replacement.

PASS: The current screenshots show the preview, replace-confirmation state, and post-restore success state.

Minor hardening note: confirmAccountLightReplaceImport() itself does not internally require uiState.isAccountLightReplaceConfirming; the user-facing UI path is gated correctly, but the ViewModel method could be made state-defensive.

MEDIASTORE COLLISION:

PASS: AndroidAccountLightProfileAutosaveWriter accepts exact default names and Android collision names such as quality-alternative-profile (1).json.

PASS: The MediaStore query intentionally sorts by DATE_ADDED DESC, _ID DESC, matching the R3 requirement that newest inserted matching backup wins because DATE_MODIFIED was unreliable for emulator collision cases.

PASS: The connected collision test passed, and logcat shows a collision-suffixed inserted file being read: quality-alternative-profile-collision-... (1).json.

PASS: README wording now correctly describes “newest inserted” via DATE_ADDED plus _ID, rather than implying modification-time freshness.

MEDITATION GONG:

PASS: No ToneGenerator, TONE_PROP_ACK, or startTone path remains in the shipped app source.

PASS: Completion now calls MeditationGong.play(), which generates a decaying multi-frequency PCM gong and plays it through AudioTrack.

PASS: The connected meditation test now selects the one-minute reset, waits for timer completion, verifies the completion copy/action state, and captures 13_meditation_gong_complete.png.

PASS: Connected XML reports meditationAlternativeOpensThreeMinuteTimer passing with a runtime of about 63 seconds, which is consistent with the one-minute completion wait.

PACKAGE HYGIENE: The test name still says OpensThreeMinuteTimer even though the test now switches to a one-minute reset for completion evidence.

READING TIME:

PASS for implementation: ReadingTimeEstimator.estimateFromText() now clamps extracted text estimates to 3..720 minutes, with MAX_DOCUMENT_MINUTES = 720.

PASS for import path: DocumentReadingTimeEstimator.estimate() extracts Markdown text and EPUB text, then calls ReadingTimeEstimator.estimateFromText().

PASS for UI import path: DocumentImportCandidateFactory.fromPickedDocument() uses the shared document estimator and stores the resulting duration, source, and word count in DocumentImportCandidate.

PASS for defaults: DEFAULT_LINK_MINUTES remains 8, MAX_SESSION_MINUTES remains 20, and PDF/fallback document defaults remain 10.

BLOCK for tests/evidence: DocumentImportCandidateFactoryTest.kt still expects the old 20-minute cap for 10,000-word Markdown/EPUB imports and has no XML result. This directly undermines the release evidence for the imported-document UI candidate path.

BUNDLE GAP: No connected or screenshot evidence shows a long Markdown/EPUB import displaying a multi-hour estimate; source and unit coverage support the behavior, but the visual/import-flow evidence remains absent.

TEST/EVIDENCE:

PASS: Unit XML includes ReadingTimeEstimatorTest, DocumentReadingTimeEstimatorTest, and MainViewModelTest, all with zero failures/errors.

PASS: Connected XML includes 7 selected connected tests with zero failures/errors, including clean-state default restore, Settings preview-before-replace, MediaStore collision handling, default shared backup write/read, and meditation completion.

PASS: Connected exit code is 0.

BLOCK: The shipped unit XML is selective and omits DocumentImportCandidateFactoryTest.kt, a directly relevant reading-time import-path test that is stale and would fail against the shipped implementation.

REVISE: The README verification command excludes DocumentImportCandidateFactoryTest, so the evidence does not represent a release-clean targeted unit suite for the full reading-time import chain.

REVISE: Connected logcat evidence is materially noisy with emulator/system logs, resource-release warnings, and stale screenshot directory names, although the relevant test outcomes are clear.

BUNDLE GAPS:

BUNDLE GAP: DocumentImportCandidateFactoryTest.kt is stale and absent from unit XML; this is the release blocker.

BUNDLE GAP: Evidence proves clean-state/data-reset restore, not a literal uninstall/reinstall cycle.

BUNDLE GAP: No visual or connected import-flow evidence demonstrates a long Markdown/EPUB file producing a multi-hour estimate in the app UI.

BUNDLE GAP: The bundle includes migration tests and Gradle references to schema assets, but app/schemas is not shipped; the selected evidence is sufficient for this slice, but the full shipped test tree is not fully evidenced.

BUNDLE GAP: proguard-rules.pro is referenced by app/build.gradle.kts but is not included in the bundle.

PACKAGE HYGIENE:

PACKAGE HYGIENE: BUNDLE_MANIFEST_R3.md has the title # Sprint 21 R2 Bundle Manifest, which is misleading in an R3 review packet.

PACKAGE HYGIENE: Evidence directories are now mostly neutral/current, but connected logcat still shows screenshot output paths under stale sprint16-portable-profile-* and sprint19-reader-form-regression-* names.

PACKAGE HYGIENE: The connected meditation test name remains stale relative to its new completion behavior.

PACKAGE HYGIENE: The MediaStore test environment is visibly polluted with prior default backup collisions, for example quality-alternative-profile (26).json; this supports collision-path exercising but should be labeled as test-environment residue.

PACKAGE HYGIENE: No duplicate screenshots were found in the current evidence set, and the previous misleading profile-backup copy has been corrected to say the default Downloads folder.