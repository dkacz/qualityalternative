# Sprint 27 Agent Content Inbox Validation Summary

Date: 2026-06-12

Scope: Google Drive Agent Inbox for user-controlled Markdown/EPUB packages, manifest review with operator-confirmed priority, private-document import, analytics/privacy, Settings UI, and visual e2e evidence.

## Automated Checks

- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest` - PASS
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew compileDebugAndroidTestKotlin` - PASS
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.VisualQaScreenshotTest#captureSprint27AgentInboxReviewScreens` - PASS
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:lintDebug` - PASS
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:processReleaseManifestForPackage` - PASS
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:assembleDebug` - PASS
- `git diff --check` - PASS

Result files:

- Unit XML: 42 XML suite files, 510 testcases, 0 failures, 0 errors.
- Connected XML: 1 test, 0 failures, 0 errors, 0 skipped.
- Review APK candidate was generated locally for GPT Pro R10, with committed checksum and manifest-check evidence under `evidence/sprint27_agent_content_inbox/apk/`.
- Review APK SHA-256: `9a51ec2a435c8cb8e8a0cdaa8e74212551127a04e57dda08d81115f59d3bf4e8`

## Targeted Coverage

- Manifest schema validation, unsafe names, format mismatch, rights class, priority, SHA-256 mismatch.
- Google Drive Agent Inbox client first-connect folder creation, stored-folder reuse, explicit parent package/file listing, bounded package/file pagination, and bounded file download.
- Review candidate construction for ready, invalid, duplicate, missing manifest, missing content, duplicate manifests, multiple content files, unsupported extra files, and truncated-package packages.
- Review duplicate handling uses the actual reviewed content SHA-256, not unverified manifest-declared SHA.
- Importer path from reviewed downloaded bytes to existing private user-document model, including content-addressed storage, same folder/name changed-bytes import isolation, changed-after-review rejection, and import-time content-size cap.
- ViewModel scan/import/reject/disconnect state transitions, explicit priority acceptance, duplicate handling, SHA mismatch review, bounded manifest/content download handling, analytics metadata privacy.
- ViewModel package-level `DOWNLOAD_UNAVAILABLE` handling for non-size manifest/content download failures, with scan continuation for remaining packages.
- Agent Inbox scan/import readiness gate on `UserDocumentRepository.observeReady()` and repository-level duplicate lookup by verified content fingerprint.
- Atomic Agent Inbox duplicate prevention through `addDocumentIfFingerprintAbsent`, Room repository write mutex, verified fingerprint fields on `UserDocumentDraft`, same-scan same-SHA sibling duplicate state, and import-time duplicate candidate state.
- Import-time failure finite-state handling for changed-after-review, import-time oversize, import download unavailable, and repository/local save rejection; failed rows become non-importable invalid candidates, stale reviewed fingerprints are cleared, and accepted priority state is removed.
- Local storage cleanup for post-write duplicate/rejected/exception results: concurrent same-SHA Agent Inbox packages leave one stored file, repository rejection leaves no stored file, and atomic-add exception leaves no stored file.
- Atomic local document write safety: Agent Inbox document storage writes into a scoped temporary file, verifies the temp file SHA-256 before promotion, atomically moves verified bytes into the deterministic final path when supported, cleans temp files in `finally`, and replaces stale mismatching final files with verified reviewed bytes.
- Preferences persistence for Agent Inbox Drive connection/scan failure state.
- Portable Profile privacy: raw Agent Inbox Drive folder id, scan errors, and private document SHA-256 fingerprints are omitted.
- Settings visual review states: disconnected, connected empty, READY priority requested, operator priority accepted, duplicate, invalid, local package removal, and dark mode.
- Imported private content visual states: actual Agent Inbox scan/import path for Markdown and EPUB, library rows with neutral `Agent Inbox document` provenance, intervention recommendation from imported Markdown, Markdown reader, and EPUB reader.
- Fixture target safety: debug-only resolver gate, debug-only fixture activity manifest registration, and release merged/packaged manifests with no fixture activities.

## Visual Evidence

- Raw screenshots: `evidence/sprint27_agent_content_inbox/visual_e2e/sprint27-agent-content-inbox-1781272063934/`
- Contact sheet: `evidence/sprint27_agent_content_inbox/visual_e2e/sprint27_agent_inbox_contact_sheet.png`
- Connected test XML/logcat: `evidence/sprint27_agent_content_inbox/android-results/connected_debug/`
- Standalone connected logcat copy: `evidence/sprint27_agent_content_inbox/android-results/connected_debug/logcat-VisualQaScreenshotTest-captureSprint27AgentInboxReviewScreens.txt`
- Unit test XML results: `evidence/sprint27_agent_content_inbox/unit-results/testDebugUnitTest/`
- Lint report: `evidence/sprint27_agent_content_inbox/lint/lint-results-debug.html`
- Release manifest evidence: `evidence/sprint27_agent_content_inbox/manifests/`

Screenshot set:

- `00_agent_inbox_disconnected_light.png`
- `01_agent_inbox_connected_empty_light.png`
- `02_agent_inbox_review_ready_light.png`
- `03_agent_inbox_priority_accepted_light.png`
- `04_agent_inbox_invalid_duplicate_light.png`
- `04b_agent_inbox_rejected_light.png`
- `05_agent_inbox_review_dark.png`
- `06_agent_inbox_library_imported_markdown_light.png`
- `07_agent_inbox_intervention_imported_markdown_light.png`
- `08_agent_inbox_reader_markdown_light.png`
- `09_agent_inbox_reader_epub_light.png`

## Notes

- Visual e2e uses debug-only ViewModel fixture hooks guarded by `BuildConfig.DEBUG` for static review-state seeding and a fake Drive client; screenshots `06`-`09` exercise the real `scanAgentInboxDrive` and `importAgentInboxCandidate` ViewModel paths before Library/intervention/reader capture.
- The screenshot flow does not require live Google Drive OAuth and does not store access tokens.
- GPT Pro R1 review was saved at `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R1.md` and returned `SCORE: 6/10`, `VERDICT: BLOCK`, `VISUAL REVIEW: REVISE`.
- GPT Pro R2 review was saved at `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R2.md` and returned `SCORE: 6/10`, `VERDICT: BLOCK`, `VISUAL REVIEW: PASS`.
- GPT Pro R3 review was saved at `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R3.md` and returned `SCORE: 6/10`, `VERDICT: BLOCK`, `VISUAL REVIEW: PASS`.
- R4 fixes the R3 blockers: content-addressed Agent Inbox storage, duplicate review only from actual content SHA, bounded Drive streaming downloads, package reject/remove action and analytics, first-connect folder creation instead of name search, and release-safe fixture gating.
- GPT Pro R4 review was saved at `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R4.md` and returned `SCORE: 7/10`, `VERDICT: REVISE`, `VISUAL REVIEW: REVISE`.
- R5 fixes the R4 findings: non-size Drive download failures become finite package-level `DOWNLOAD_UNAVAILABLE` invalid candidates; Agent Inbox scan/import waits for user-document readiness; duplicate prevention uses `findDocumentByFingerprintSha256`; the priority control is full-width and screenshot-asserted; the evidence bundle includes a standalone connected logcat copy.
- GPT Pro R5 review was saved at `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R5.md` and returned `SCORE: 8/10`, `VERDICT: BLOCK`, `VISUAL REVIEW: PASS`.
- R6 fixes the R5 blocker: Agent Inbox import now uses an atomic repository add-if-fingerprint-absent method backed by a Room write mutex, same-SHA package imports cannot both create user-document rows in the app process, and duplicate import results update the review list to visible `DUPLICATE` state.
- GPT Pro R6 review was saved at `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R6.md` and returned `SCORE: 8/10`, `VERDICT: BLOCK`, `VISUAL REVIEW: PASS`.
- R7 fixes the R6 blocker: import-time invalid/rejected/download-failure paths update the candidate to finite invalid state, clear stale reviewed fingerprints, clear accepted priority, and keep only the visible Remove path.
- GPT Pro R7 review was saved at `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R7.md` and returned `SCORE: 9/10`, `VERDICT: REVISE`, `VISUAL REVIEW: PASS`.
- R8 fixes the R7 finding: Agent Inbox storage deletes the just-written private file when the repository returns duplicate/rejected or throws after write, so failed or duplicate imports do not leave invisible local document bytes behind.
- GPT Pro R8 review was saved at `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R8.md` and returned `SCORE: 9/10`, `VERDICT: REVISE`, `VISUAL REVIEW: PASS`.
- R9 fixes the R8 finding: Agent Inbox document storage now writes verified content through a temp file plus SHA check before final promotion, cleans stale mismatching final files, and covers the stale-final replacement path with `fileStoreReplacesStaleMismatchingFinalFileWithVerifiedBytes`.
- GPT Pro R9 review was saved at `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R9.md` and returned `SCORE: 9/10`, `VERDICT: REVISE`, `VISUAL REVIEW: REVISE`.
- R10 fixes the R9 visual evidence mismatch: screenshots `06`-`09` now come from actual Agent Inbox fake-Drive scan/import flows, assert accepted priority before Markdown import, verify neutral imported provenance/fingerprint, and prove raw Drive content file names/package ids are not rendered in Library/intervention.
- GPT Pro R10 review was saved at `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R10.md` and returned `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`, with no fresh findings and no bundle gaps.
