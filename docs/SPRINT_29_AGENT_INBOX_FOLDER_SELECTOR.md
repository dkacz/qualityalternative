# Sprint 29 - Agent Inbox Folder Selector

## Goal

Replace the Sprint 28 pasted-folder fallback with a normal folder selector for Agent Inbox. The user must not need to paste a Google Drive folder URL or id. The app should open Android's system folder picker, persist the selected folder tree read grant, scan only that folder, and continue using the existing finite Agent Inbox review/import contract.

## Requirements

- Settings Agent Inbox disconnected state shows a `Choose folder` action, not a text field for Drive folder URL/id.
- The action launches Android `OpenDocumentTree`, which can surface Google Drive folders on devices with the Drive DocumentsProvider.
- The selected folder persists as an Agent Inbox grant mode distinct from historical Google Picker and `drive.readonly` modes.
- Scanning and importing packages from the selected tree do not require a Google OAuth token.
- The scan remains finite: direct child folders are candidate packages; package files remain bounded by the existing package/file/content/image limits.
- Access loss from a revoked or unavailable folder tree returns to the select-folder state and does not show a false empty scan.
- Remote-safe analytics and Portable Profile must not expose raw folder URIs, Drive ids, file ids, package paths, or content file names.
- Existing `drive.readonly` behavior may remain only for compatibility with already-connected states; it must not be the primary disconnected UX.

## Implementation State

- Added `AGENT_INBOX_DRIVE_GRANT_MODE_DOCUMENT_TREE_FOLDER`.
- Added `AndroidDocumentTreeAgentInboxClient` and `AndroidHybridAgentInboxDriveClient`.
- Production container now routes Agent Inbox scans/downloads to the document-tree client when the saved folder is a `content://tree/...` URI.
- Settings now launches `ActivityResultContracts.OpenDocumentTree()` for disconnected Agent Inbox instead of asking for a pasted folder id.
- Document-tree scan/import paths allow an empty access token because the persisted URI grant is the authorization boundary.
- Disconnect releases the persisted tree read permission for document-tree grants.
- Sprint 29 visual coverage proves the selector UX, Android system folder picker launch, real folder selection callback, connected folder state, access-lost select-folder state, Markdown sidecar image rendering, and dark connected state.
- R2 closes the GPT Pro R1 access-loss finding: document-tree access loss while opening manifest/content/image streams now clears the folder grant instead of marking a single package invalid.
- R2 adds direct Portable Profile evidence that raw `content://tree/...` Agent Inbox folder URIs do not export.

## Validation

- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest --tests com.qualityalternative.app.ui.MainViewModelTest --tests com.qualityalternative.app.data.PreferencesSettingsRepositoryTest --tests com.qualityalternative.app.data.AccountLightProfileExporterTest` - PASS.
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:lintDebug :app:processReleaseManifestForPackage :app:assembleDebug` - PASS.
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.VisualQaScreenshotTest#captureSprint29AgentInboxFolderSelectorStates` - PASS on `qaApi36(AVD) - 16`.
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew connectedDebugAndroidTest` - PASS, 138/138 tests, 0 skipped, 0 failed on `qaApi36(AVD) - 16`.
- `git diff --check` - PASS.

## Evidence

- Visual contact sheet: `evidence/sprint29_agent_inbox_folder_selector/visual_e2e_selector_r2/contact_sheet_selector_r2.png`
- Visual screenshots: `evidence/sprint29_agent_inbox_folder_selector/visual_e2e_selector_r2/sprint29-agent-inbox-folder-selector-1781513593337/`
- Focused visual test XML/logcat: `evidence/sprint29_agent_inbox_folder_selector/visual_e2e_selector_r2/TEST-sprint29-selector-visual-r2.xml`, `evidence/sprint29_agent_inbox_folder_selector/visual_e2e_selector_r2/logcat-sprint29-selector-visual-r2.txt`
- Full connected test log/XML: `evidence/sprint29_agent_inbox_folder_selector/logs/full_connected_debug_android_test_r2.log`, `evidence/sprint29_agent_inbox_folder_selector/logs/TEST-full-connected-debug-android-test-r2.xml`
- Local gate log: `evidence/sprint29_agent_inbox_folder_selector/logs/full_local_gate_r2.log`

## GPT Pro Review

- R1 returned `SCORE 8/10`, `VERDICT REVISE`, `VISUAL REVIEW REVISE`.
- R1 findings fixed for R2: document-tree stream access loss now clears the folder grant; visual copy now says `Folder` and `Choose the folder again`; visual evidence now uses Sprint 29 naming and includes a real Android folder selection callback.
- R2 returned `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`; fresh findings none, bundle gaps none.

## Release Gate

- Release gate passed for `v0.11.17-agent-inbox-folder-selector-alpha`: `versionCode=33`, `versionName=0.11.17-alpha`.
- Final Gradle gate passed: `:app:testDebugUnitTest :app:lintDebug :app:assembleDebug`.
- Final connected Android gate passed: `connectedDebugAndroidTest`, 138 tests, 0 failures, 0 skipped.
- APK badging, signature verification, install, and explicit launch evidence passed.
- Release artifact: `release_artifacts/quality-alternative-v0.11.17-agent-inbox-folder-selector-alpha-debug.apk`
- Release APK SHA-256: `753362b76fdd0110fd15668a1215cbe6e1291b674efca9dc9c94e61c8d9b0fec`
- Release gate summary: `docs/release-gate-logs/2026-06-15-sprint29-agent-inbox-folder-selector/VALIDATION_SUMMARY.md`
- Release notes: `docs/release-gate-logs/2026-06-15-sprint29-agent-inbox-folder-selector/RELEASE_NOTES_v0.11.17-agent-inbox-folder-selector-alpha.md`
