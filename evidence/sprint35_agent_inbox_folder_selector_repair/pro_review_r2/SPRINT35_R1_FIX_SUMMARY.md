# Sprint 35 R1 Fix Summary

GPT Pro R1 returned `SCORE: 8/10`, `VERDICT: REVISE`, `VISUAL REVIEW: REVISE`.

## R1 Findings Addressed

1. Production dependency wiring was missing from the review bundle.
   - R2 includes `app/src/main/java/com/qualityalternative/app/data/AppContainer.kt`.
   - The relevant production binding is `val agentInboxDriveClient = AndroidHybridAgentInboxDriveClient(context = context)`.

2. Scope constants were missing from the review bundle.
   - R2 includes `app/src/main/java/com/qualityalternative/app/domain/service/ReadingAnnotationDriveSync.kt`.
   - This proves annotations still use `drive.file` while Agent Inbox uses `drive.readonly`.

3. App-side package validation/model definitions were missing from the review bundle.
   - R2 includes `AgentInboxManifest.kt`, `AgentInboxDocumentStore.kt`, `AgentInboxPackageImporter.kt`, `AgentInboxReviewCandidate.kt`, and `ContentModels.kt`.

4. Visual access-lost evidence was not production-state-equivalent.
   - `MainViewModel.seedAgentInboxDriveAccessLostForTests()` now mirrors production access-lost recovery by clearing `agentInboxDriveFolderDraft` and `agentInboxDriveFolderDraftError`.
   - A Sprint 35-named visual E2E was added: `VisualQaScreenshotTest#captureSprint35AgentInboxFolderSelectorRepairStates`.
   - New screenshots and contact sheet were captured under `evidence/sprint35_agent_inbox_folder_selector_repair/visual_e2e/`.

5. Release evidence was stale/too aggregate.
   - Final Gradle gate was rerun with `--rerun-tasks`.
   - Unit-test XML reports are included.
   - APK hash files, APK badging, install, package dump, and direct launch evidence are included.

## Current Claim

The feature is not removed. Agent Inbox now uses a real Android folder picker (`OpenDocumentTree`) for folder selection. Local/system folders scan through SAF without a Google token. Google Drive-backed document-tree selections and explicit Drive folder ids scan/import through `drive.readonly`, scoped to the selected folder id. Legacy `picker_folder` grants are treated as repair state and cannot continue through `drive.file`.
