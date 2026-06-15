# Sprint 29 Agent Inbox Folder Selector - R2 Review Bundle Manifest

Branch: `codex/sprint29-agent-inbox-folder-selector`

## Review Goal

Close GPT Pro R1 and reach:

- `SCORE: 10/10`
- `VERDICT: PASS`
- `VISUAL REVIEW: PASS`

## R2 Changes Since R1

- Document-tree access loss during manifest/content/image stream opening now propagates to the outer access-lost handler and clears the folder grant.
- Import-time document-tree access loss now clears the folder grant instead of leaving a stale ready/invalid package row.
- Added ViewModel tests for scan-time and import-time document-tree access loss.
- Connected document-tree UI badge now says `Folder`; historical Drive grants still say `Drive`.
- Access-lost visual copy now says `Choose the folder again`.
- Visual test is renamed to Sprint 29 and selects `Documents` through Android DocumentsUI before capturing the connected state.
- Portable Profile exporter test now proves raw `content://.../tree/...` Agent Inbox folder URIs are omitted from JSON.
- R2 bundle includes missing R1 source context for sidecar review/import/storage and Portable Profile export.

## Validation Evidence

- Targeted unit tests:
  - `testDebugUnitTest --tests com.qualityalternative.app.ui.MainViewModelTest --tests com.qualityalternative.app.data.PreferencesSettingsRepositoryTest --tests com.qualityalternative.app.data.AccountLightProfileExporterTest`
- Full local gate:
  - `testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:lintDebug :app:processReleaseManifestForPackage :app:assembleDebug`
  - Log: `logs/full_local_gate_r2.log`
  - Status: `logs/full_local_gate_r2.status.txt`
- Focused visual E2E:
  - `connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.VisualQaScreenshotTest#captureSprint29AgentInboxFolderSelectorStates`
  - XML: `visual_e2e_selector_r2/TEST-sprint29-selector-visual-r2.xml`
  - Logcat: `visual_e2e_selector_r2/logcat-sprint29-selector-visual-r2.txt`
  - Contact sheet: `visual_e2e_selector_r2/contact_sheet_selector_r2.png`
- Full connected Android gate:
  - `connectedDebugAndroidTest`
  - Result: 138/138 tests, 0 skipped, 0 failed.
  - Log: `logs/full_connected_debug_android_test_r2.log`
  - XML: `logs/TEST-full-connected-debug-android-test-r2.xml`
- Diff hygiene:
  - `git diff --check`
  - Log: `logs/git_diff_check_r2.log`
  - Status: `logs/git_diff_check_r2.status.txt`

## Source Files Included

- `PRD.md`
- `docs/SPRINT_29_AGENT_INBOX_FOLDER_SELECTOR.md`
- `docs/LANE_STATUS.md`
- `sprint29_selector_r2_code_docs.diff`
- `app/src/main/java/com/qualityalternative/app/domain/service/AgentInboxDrive.kt`
- `app/src/main/java/com/qualityalternative/app/data/AndroidDocumentTreeAgentInboxClient.kt`
- `app/src/main/java/com/qualityalternative/app/data/AndroidGoogleDriveAgentInboxClient.kt`
- `app/src/main/java/com/qualityalternative/app/data/AppContainer.kt`
- `app/src/main/java/com/qualityalternative/app/data/PreferencesSettingsRepository.kt`
- `app/src/main/java/com/qualityalternative/app/data/AgentInboxManifest.kt`
- `app/src/main/java/com/qualityalternative/app/data/AgentInboxReviewCandidate.kt`
- `app/src/main/java/com/qualityalternative/app/data/AgentInboxPackageImporter.kt`
- `app/src/main/java/com/qualityalternative/app/data/AgentInboxDocumentStore.kt`
- `app/src/main/java/com/qualityalternative/app/data/AccountLightProfile.kt`
- `app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt`
- `app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt`
- `app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt`
- `app/src/test/java/com/qualityalternative/app/data/PreferencesSettingsRepositoryTest.kt`
- `app/src/test/java/com/qualityalternative/app/data/AccountLightProfileExporterTest.kt`
- `app/src/test/java/com/qualityalternative/app/data/AgentInboxReviewCandidateFactoryTest.kt`
- `app/src/test/java/com/qualityalternative/app/data/AgentInboxPackageImporterTest.kt`
- `app/src/androidTest/java/com/qualityalternative/app/VisualQaScreenshotTest.kt`

## R1 Context Included

- `GPT_PRO_REVIEW_R1.md`
- `GPT_PRO_REVIEW_PROMPT_R1.md`
- `REVIEW_BUNDLE_MANIFEST_R1.md`
