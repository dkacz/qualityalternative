SCORE: 9/10
VERDICT: PASS
VISUAL REVIEW: PASS

BLOCKERS:

None.

R1/R2/R3/R4 RECHECK:

PASS: The R1 blocker remains fixed. Restore default backup in Settings routes to previewDefaultAccountLightProfileImport(), which loads the default backup with applyImmediately = false, stores a pending import plan, and shows the import preview instead of applying replacement immediately.

PASS: The R2 blocker remains fixed. DocumentReadingTimeEstimatorTest.kt is current, covers Markdown and EPUB extracted-text estimates at short, normal, 10,000-word, and capped huge-document sizes, and its unit XML is present with zero failures/errors.

PASS: The R3 blocker remains fixed. DocumentImportCandidateFactoryTest.kt is current, covers Markdown and EPUB import candidates through the shared estimator path, and its unit XML is present with zero failures/errors.

PASS: The R4 functional pass remains valid. The stale selected connected test name has been replaced with meditationInterventionShowsCalmAlternativeWhenPrimaryIsReading, the default-restore success screenshot is now named 10_default_backup_restore_success_dark.png, current screenshot output paths are Sprint 21 scoped, and final connected logcat does not contain the prior Failed to build unique file noise.

PACKAGE HYGIENE: R5 does not achieve perfect package hygiene because it includes a duplicate raw R4 review artifact under pro_review_harvest_r4/Sprint_21_R4_Review.md, byte-identical to GPT_PRO_REVIEW_R4.md, despite the R5 manifest saying duplicate raw harvest copies were excluded.

PROFILE RESTORE:

PASS: AppContainer.defaultProfileAutosaveUri is wired to AndroidAccountLightProfileAutosaveWriter.DEFAULT_PROFILE_BACKUP_URI, and the same Android writer implements both autosave write and backup read.

PASS: The user-visible default backup location is sane and explicit: Downloads/Quality Alternative/quality-alternative-profile.json, implemented through DEFAULT_PROFILE_BACKUP_DISPLAY_NAME and shown in Settings visual evidence.

PASS: The default backup is written under MediaStore.Downloads with relative path Downloads/Quality Alternative/, which is outside app-private storage and is suitable for clean-state and reinstall-style recovery.

PASS: The onboarding UI exposes Restore profile, and the connected test onboardingRestoreProfileLoadsDefaultBackupAfterCleanInstall writes a default shared backup, clears persistent app state, launches onboarding, restores from the default backup, and verifies restored settings.

BUNDLE GAP: The evidence still proves app-data-reset / clean-state restore, not a literal uninstall/reinstall cycle. The implementation design supports reinstall because the backup is in shared Downloads, but no literal uninstall/reinstall artifact is shipped.

SETTINGS DEFAULT RESTORE SAFETY:

PASS: Settings Restore default backup opens the same preview surface used by manual imports, with affected profile/library counts visible before replacement.

PASS: The connected test settingsDefaultBackupRestoreShowsPreviewBeforeReplace verifies that local reader font scale remains unchanged after preview, then requires the visible preview Replace action and the visible confirmation Replace action before the imported setting is applied.

PASS: Screenshots show the default backup preview, the explicit Replace confirmation card, and the post-restore success state.

Non-blocking hardening note: confirmAccountLightReplaceImport() still trusts the pending plan and does not independently check isAccountLightReplaceConfirming; the shipped UI path is correctly gated, but the ViewModel method itself remains less defensive than the product rule.

MEDIASTORE COLLISION:

PASS: AndroidAccountLightProfileAutosaveWriter recognizes the exact default filename and numeric collision forms matching quality-alternative-profile (1).json and timestamp-style quality-alternative-profile (timestamp).json.

PASS: Restore lookup orders matching MediaStore rows by DATE_ADDED DESC, _ID DESC, so the newest inserted matching backup wins even when Android creates collision-suffixed names.

PASS: The connected test readsNewestDefaultSharedProfileBackupWhenMediaStoreAddsCollisionSuffix creates duplicate MediaStore rows with a collision suffix and verifies that the newer backup is read.

BUNDLE GAP: The collision test uses a unique test stem, quality-alternative-profile-collision-..., rather than the exact production stem quality-alternative-profile.json; the source matcher is generic enough to cover the production filename, but the exact default collision filename is not directly exercised in connected evidence.

MEDITATION GONG:

PASS: No ToneGenerator, TONE_PROP_ACK, or startTone path remains in shipped app source.

PASS: Meditation completion calls MeditationGong.play(), which generates a short decaying multi-frequency PCM gong and plays it through AudioTrack.

PASS: The connected test meditationAlternativeOpensTimerAndCompletesWithGong switches to the one-minute timer, waits for completion, verifies the completion copy/action state, and captures the completed timer screen.

PASS: Connected XML reports this test passing in approximately 64 seconds, which is consistent with the one-minute completion path.

PASS: Visual evidence shows both the calm meditation alternative and the completed 0:00 state with copy indicating that the gong marks the end.

READING TIME:

PASS: ReadingTimeEstimator.estimateFromText() now clamps extracted-document estimates to 3..720 minutes.

PASS: DocumentReadingTimeEstimator routes Markdown and EPUB through extracted text before estimating, while PDF and unsupported formats remain on short defaults.

PASS: DocumentImportCandidateFactory.fromPickedDocument() uses the shared document estimator and carries the estimate source and word count into the import candidate.

PASS: Link/session defaults remain short: default link is 8 minutes, PDF fallback is 10 minutes, generic document fallback is 10 minutes, and session max remains 20 minutes.

PASS: Unit tests cover 10,000-word Markdown/EPUB imports as 45 minutes and huge extracted text as 720 minutes.

PASS: Visual evidence reading_time_import/14_long_document_import_multi_hour.png shows a Markdown import preview with 2 hr 15 min · auto.

TEST/EVIDENCE:

PASS: Unit XML is present for ReadingTimeEstimatorTest, DocumentReadingTimeEstimatorTest, DocumentImportCandidateFactoryTest, and MainViewModelTest, all with zero failures and zero errors.

PASS: Connected XML reports 8 selected connected tests with zero failures, zero errors, zero skips, and exit code 0.

PASS: Connected coverage includes default shared backup write/read, MediaStore collision read, onboarding clean-state restore, Settings preview-before-replace, long-document multi-hour import preview, meditation calm alternative, and meditation timer completion.

PASS: Current screenshots are present, uniquely hashed, and readable: onboarding restore entry, Settings preview/confirmation/success, meditation alternative/completion, and long-document import preview.

PASS: Final connected evidence no longer contains stale selected-test names, stale current screenshot directory names, or the prior Failed to build unique file MediaStore noise.

BUNDLE GAPS:

BUNDLE GAP: No literal uninstall/reinstall test artifact is included; the restore evidence is clean-state/app-data-reset plus source verification that the backup lives in shared Downloads.

BUNDLE GAP: The collision connected test uses a unique collision filename rather than the exact production default stem, although the production matcher supports the exact default collision forms.

No blocker-level bundle gap remains for profile restore, Settings restore safety, MediaStore collision restore logic, meditation completion behavior, or imported-document reading-time estimates.

PACKAGE HYGIENE:

PACKAGE HYGIENE: evidence/sprint21_profile_gong_reading_time/pro_review_harvest_r4/Sprint_21_R4_Review.md is a duplicate of GPT_PRO_REVIEW_R4.md; this contradicts the R5 manifest’s statement that duplicate raw harvest copies were excluded.

PACKAGE HYGIENE: Connected logcats remain raw and noisy with emulator/system lines, including unrelated launcher, EmojiCompat, HWUI, MediaProvider, wpa_supplicant, and Dialer/VVM messages. This does not obscure the selected test results, and the specific stale-name and prior MediaStore failure noise called out for R5 are gone.

PASS: No duplicate screenshots were found, and current screenshot names now accurately reflect the captured theme/state.