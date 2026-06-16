# Sprint 30 Agent Inbox Large Image Import Fix - Review Bundle Manifest

## Review Question

Audit whether the Sprint 30 implementation fixes the Agent Inbox large Markdown image sidecar import bug described in `docs/AGENT_INBOX_LARGE_IMAGE_IMPORT_BUG.md`.

## Primary Files

- `docs/AGENT_INBOX_LARGE_IMAGE_IMPORT_BUG.md`
- `docs/SPRINT_30_AGENT_INBOX_LARGE_IMAGE_IMPORT_FIX.md`
- `docs/AGENT_INBOX_PACKAGE_AUTHORING.md`
- `docs/LANE_STATUS.md`
- `AGENTS.md`

## Changed Implementation Files

- `app/src/main/java/com/qualityalternative/app/domain/service/AgentInboxDrive.kt`
- `app/src/main/java/com/qualityalternative/app/data/AgentInboxManifest.kt`
- `app/src/main/java/com/qualityalternative/app/data/AgentInboxReviewCandidate.kt`
- `app/src/main/java/com/qualityalternative/app/data/AgentInboxPackageImporter.kt`
- `app/src/main/java/com/qualityalternative/app/data/AgentInboxDocumentStore.kt`
- `app/src/main/java/com/qualityalternative/app/data/AndroidGoogleDriveAgentInboxClient.kt`
- `app/src/main/java/com/qualityalternative/app/data/AndroidDocumentTreeAgentInboxClient.kt`
- `app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt`
- `app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt`
- `app/src/androidTest/java/com/qualityalternative/app/AccessibilityInterceptionTest.kt`

## Changed Test Files

- `app/src/test/java/com/qualityalternative/app/data/AgentInboxPackageImporterTest.kt`
- `app/src/test/java/com/qualityalternative/app/data/AgentInboxManifestValidatorTest.kt`
- `app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt`
- `app/src/androidTest/java/com/qualityalternative/app/VisualQaScreenshotTest.kt`
- `tools/validate_agent_inbox_package.py`

## Validation Evidence

- `evidence/sprint30_agent_inbox_large_image_import_fix/logs/TEST-AgentInboxPackageImporterTest.xml`
- `evidence/sprint30_agent_inbox_large_image_import_fix/logs/TEST-MainViewModelTest.xml`
- `evidence/sprint30_agent_inbox_large_image_import_fix/logs/TEST-full-connected-debug-android-test.xml`
- `evidence/sprint30_agent_inbox_large_image_import_fix/review/GPT_PRO_REVIEW_R1.md`
- `evidence/sprint30_agent_inbox_large_image_import_fix/review/GPT_PRO_REVIEW_R1_URL.txt`
- `evidence/sprint30_agent_inbox_large_image_import_fix/review/GPT_PRO_REVIEW_R2.md`
- `evidence/sprint30_agent_inbox_large_image_import_fix/review/GPT_PRO_REVIEW_R2_URL.txt`
- `evidence/sprint30_agent_inbox_large_image_import_fix/review/current_patch.diff`
- `evidence/sprint30_agent_inbox_large_image_import_fix/review/current_patch_stat.txt`

## Commands Already Run

- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:testDebugUnitTest --tests com.qualityalternative.app.data.AgentInboxPackageImporterTest --tests com.qualityalternative.app.ui.MainViewModelTest`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:testDebugUnitTest`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:lintDebug :app:compileDebugAndroidTestKotlin :app:assembleDebug` (rerun after R1 fix)
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.AccessibilityInterceptionTest#chromeVerifiedHostAdapterHarnessAcceptsOnlyLoadedMatchingHost`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew connectedDebugAndroidTest` (rerun after R1 fix)
- `git diff --check`

## R1 Review And Fix

- GPT Pro R1 output: `evidence/sprint30_agent_inbox_large_image_import_fix/review/GPT_PRO_REVIEW_R1.md`
- R1 score/verdict: `SCORE: 8`, `VERDICT: REVISE`, `VISUAL REVIEW: NOT APPLICABLE`
- R1 finding: image sidecar temp-file creation happened before the sidecar write wrapping/cleanup scope, so pre-write `File.createTempFile(...)` failures could still collapse to generic `LOCAL_IMPORT_REJECTED` and leave temp files.
- R1 fix: `FileAgentInboxDocumentStore.writeImageAttachmentsAtomically` now builds sidecar plans and temp files inside the cleanup/wrap scope, avoids rewrapping an existing `AgentInboxImageAttachmentWriteException`, and `AgentInboxReviewCandidate.toAgentInboxImportFailureDetail()` unwraps nested image-write wrappers to the root cause.
- R1 regression: `AgentInboxPackageImporterTest.importCandidateMapsSidecarTempCreationFailureToImageWriteFailure` forces sidecar temp creation failure, asserts `IMAGE_WRITE_FAILED` mapping, root-cause message preservation, and empty storage cleanup.
- GPT Pro R2 output: `evidence/sprint30_agent_inbox_large_image_import_fix/review/GPT_PRO_REVIEW_R2.md`
- R2 score/verdict: `SCORE: 10`, `VERDICT: PASS`, `VISUAL REVIEW: NOT APPLICABLE`

## Bundle Hygiene

This packet intentionally excludes APK outputs, full Gradle build directories, stale Sprint 27-29 review bundles, screenshots unrelated to Sprint 30, and prior release artifacts. The current patch plus the listed source files and XML test evidence are the canonical audit base for this review lane.
