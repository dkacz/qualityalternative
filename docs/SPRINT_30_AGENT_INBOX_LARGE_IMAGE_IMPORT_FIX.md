# Sprint 30 - Agent Inbox Large Image Import Fix

## Goal

Fix the Agent Inbox Markdown image sidecar import path so packages below the advertised 5 MiB per-image limit do not fail opaquely with `LOCAL_IMPORT_REJECTED`, and make any future import failure diagnosable from logs and candidate UI state.

## Scope

- Preserve the existing package structure and validator contract.
- Keep the documented 5 MiB per-image limit unless validation proves the app cannot honor it.
- Make import-time exceptions visible without leaking raw Drive ids, local file paths, or package ids into remote-safe analytics.
- Reduce avoidable peak memory allocations on Drive/document-tree downloads and local document persistence.

## Implementation State

- Added `AgentInboxImportFailureDetail` to `AgentInboxReviewCandidate`.
- Added `IMAGE_WRITE_FAILED` for image sidecar persistence failures.
- `MainViewModel.importAgentInboxCandidate` now:
  - passes known expected byte sizes to Agent Inbox downloads,
  - logs import-time caught exceptions with `Log.e`,
  - stores exception class/message on failed candidates,
  - maps image sidecar write failures to `IMAGE_WRITE_FAILED`,
  - preserves specific details for import-time download failures.
- `QualityAlternativeApp` now shows failure class/message in Agent Inbox candidate detail text when available.
- `AgentInboxDriveClient.downloadFile` now accepts optional `expectedBytes`.
- Google Drive and document-tree Agent Inbox clients now use expected/content length to avoid unhinted `ByteArrayOutputStream` growth when bounded file size is known.
- `FileAgentInboxDocumentStore` now verifies the content SHA from the already-held bytes instead of re-reading the temp file, and wraps image sidecar `IOException`/`OutOfMemoryError` as `AgentInboxImageAttachmentWriteException`.
- GPT Pro R1 returned `SCORE: 8`, `VERDICT: REVISE`; the remaining finding was that image sidecar temp-file creation happened before the wrapping/cleanup scope.
- R1 fix: sidecar temp-file plan creation now happens inside the same cleanup/wrap scope as sidecar writes, nested image-write wrappers unwrap to the root cause for UI detail, and a regression test forces sidecar temp creation failure to verify `IMAGE_WRITE_FAILED` mapping plus cleanup.
- GPT Pro R2 returned `SCORE: 10`, `VERDICT: PASS`, `VISUAL REVIEW: NOT APPLICABLE`; no scoped findings remain and release readiness is approved.
- `AccessibilityInterceptionTest` now writes Chrome evidence under app-scoped external files instead of a stale public Downloads folder, fixing an unrelated connected-test permission failure on reruns with changed app UIDs.

## Validation

- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:testDebugUnitTest --tests com.qualityalternative.app.data.AgentInboxPackageImporterTest --tests com.qualityalternative.app.ui.MainViewModelTest` - PASS.
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:testDebugUnitTest` - PASS.
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:lintDebug :app:compileDebugAndroidTestKotlin :app:assembleDebug` - PASS.
- Initial `connectedDebugAndroidTest` found one unrelated stale public-Downloads evidence permission failure in `AccessibilityInterceptionTest#chromeVerifiedHostAdapterHarnessAcceptsOnlyLoadedMatchingHost`.
- Targeted rerun of `AccessibilityInterceptionTest#chromeVerifiedHostAdapterHarnessAcceptsOnlyLoadedMatchingHost` after moving evidence to app-scoped external files - PASS.
- Fresh full rerun after the R1 fix, `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew connectedDebugAndroidTest` - PASS: 138/138 tests, 0 skipped, 0 failed on `qaApi36(AVD) - 16`.
- `git diff --check` - PASS.

## Evidence

- Bug report and fix record: `docs/AGENT_INBOX_LARGE_IMAGE_IMPORT_BUG.md`
- Relevant unit XML: `evidence/sprint30_agent_inbox_large_image_import_fix/logs/TEST-AgentInboxPackageImporterTest.xml`
- Relevant ViewModel XML: `evidence/sprint30_agent_inbox_large_image_import_fix/logs/TEST-MainViewModelTest.xml`
- Full connected XML: `evidence/sprint30_agent_inbox_large_image_import_fix/logs/TEST-full-connected-debug-android-test.xml`
- GPT Pro R1 output: `evidence/sprint30_agent_inbox_large_image_import_fix/review/GPT_PRO_REVIEW_R1.md`
- GPT Pro R2 output: `evidence/sprint30_agent_inbox_large_image_import_fix/review/GPT_PRO_REVIEW_R2.md`
- Current patch for review: `evidence/sprint30_agent_inbox_large_image_import_fix/review/current_patch.diff`
