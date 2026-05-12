SCORE: 7/10
VERDICT: BLOCK
VISUAL REVIEW: REVISE
BLOCKERS:

Settings Restore default backup calls previewDefaultAccountLightProfileImport, which validates the default backup and immediately runs applyReplace(plan) without showing affected scope or requiring confirmation. That is acceptable for a fresh onboarding restore, but it is destructive from Settings and conflicts with the PRD requirement that replace import show affected scope before mutation.

PROFILE RESTORE:

The implementation trace is coherent: AppContainer now points the default profile autosave URI to qualityalternative://profile-backup/default; AndroidAccountLightProfileAutosaveWriter handles that sentinel by writing to MediaStore Downloads under Quality Alternative; the same class implements AccountLightProfileBackupReader; MainViewModelFactory wires the writer as both writer and reader; onboarding and Settings both invoke previewDefaultAccountLightProfileImport.

The reader plausibly fixes the stale-file bug by querying MediaStore.Downloads, accepting exact and Android-collision names such as quality-alternative-profile (1).json, sorting by DATE_MODIFIED DESC, and falling back to direct public Downloads file reads.

The onboarding flow has an obvious Restore profile entry point, and the connected UI test exercises a clean-state restore through the app flow.

The Settings entry point exists, but its direct destructive replace behavior is the blocker.

MEDITATION GONG:

The old ToneGenerator/TONE_PROP_ACK path is removed from the changed code, and completion now calls MeditationGong.play(), which generates a short decaying PCM sound through AudioTrack.

The implementation is plausibly gong-like and guarded by hasPlayedGong, but the bundle contains no dedicated test or runtime/audio evidence for the sound path.

READING TIME:

ReadingTimeEstimator.estimateFromText now clamps extracted text estimates to 3..720 minutes instead of 3..20, and the unit test covers short text, a 10,000-word document, normal text, and a huge-text 720-minute cap.

Portable user-document validation/export ranges were raised from 1..240 to 1..720, which aligns the portable profile with long document metadata.

The patch leaves MAX_SESSION_MINUTES = 20, DEFAULT_LINK_MINUTES = 8, and the short defaults unchanged, so the submitted diff does not show accidental expansion of link/session estimates.

TEST/EVIDENCE:

Positive evidence exists for default shared backup write/read, ViewModel default restore, onboarding restore after reset, and reading-time estimator behavior.

Collision behavior is implemented in code but not covered by a shipped test that creates both quality-alternative-profile.json and quality-alternative-profile (1).json and proves the newer collision file wins.

No shipped execution logs or test result artifacts prove the README verification commands actually passed.

Visual evidence demonstrates the onboarding restore entry and Settings default-backup entry, but does not demonstrate post-restore success, MediaStore collision recovery, long document reading-time display, or the meditation timer/gong copy.

BUNDLE GAPS:

BUNDLE GAP: No full app source, Gradle files, manifest, SDK configuration, or permission declarations are shipped, so buildability and Android storage-permission behavior cannot be fully verified from the bundle.

BUNDLE GAP: No executed test logs, connected-test result XML, emulator API level, or device configuration are included.

BUNDLE GAP: No connected collision test proves restore selects the newest Android-collision backup over a stale exact-name backup.

BUNDLE GAP: No shipped importer implementation proves from source that EPUB and Markdown extraction paths definitely call ReadingTimeEstimator.estimateFromText; the estimator and portable profile ranges are proven, but the importer call chain is not.

BUNDLE GAP: No visual evidence covers long reading-time labels or the meditation completion screen.

PACKAGE HYGIENE:

The bundle contains stale-looking screenshot directories named sprint16 and sprint17 inside Sprint 21 evidence, and one Settings screenshot is byte-identical across two directories. The screenshots are not failed artifacts, and several do show the changed Settings restore entry, but the reuse and duplicate paths should be cleaned up or clearly labeled before release review.