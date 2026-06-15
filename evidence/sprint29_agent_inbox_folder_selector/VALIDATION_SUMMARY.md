# Sprint 29 Validation Summary

Status: GPT Pro R2 passed

Branch: `codex/sprint29-agent-inbox-folder-selector`

GPT Pro R2: `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`; fresh findings none, bundle gaps none.

## Scope

Sprint 29 replaces the pasted Agent Inbox Drive folder URL/id fallback with Android's normal folder selector. The primary user flow is now:

1. Settings Agent Inbox disconnected state shows `Choose folder`.
2. The app launches Android `OpenDocumentTree`.
3. The selected `content://tree/...` URI is persisted as `document_tree_folder`.
4. Scan/import use the persisted tree grant and do not require a Google OAuth token.
5. Existing review/import safety still applies before private Markdown/EPUB content enters the library.

The legacy `drive.readonly` path remains only as compatibility for already-connected states; it is no longer the disconnected primary UX.

## R2 Fixes After GPT Pro R1

- Stream-level `AgentInboxDriveAccessLostException` from document-tree manifest/content/image downloads now clears the Agent Inbox folder grant instead of marking one package invalid.
- Added focused ViewModel regressions for scan-time and import-time document-tree access loss.
- Settings badge now distinguishes document-tree `Folder` grants from historical Google Drive API grants.
- Access-lost copy now says `Choose the folder again`.
- Visual test is renamed to Sprint 29 and selects the `Documents` folder through Android DocumentsUI before capturing the connected state.
- Portable Profile exporter test now covers a raw `content://.../tree/...` Agent Inbox folder URI and asserts it is omitted from JSON.

## Validation

- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest --tests com.qualityalternative.app.ui.MainViewModelTest --tests com.qualityalternative.app.data.PreferencesSettingsRepositoryTest --tests com.qualityalternative.app.data.AccountLightProfileExporterTest` - PASS.
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:lintDebug :app:processReleaseManifestForPackage :app:assembleDebug` - PASS.
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.VisualQaScreenshotTest#captureSprint29AgentInboxFolderSelectorStates` - PASS on `qaApi36(AVD) - 16`.
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew connectedDebugAndroidTest` - PASS, 138/138 tests, 0 skipped, 0 failed on `qaApi36(AVD) - 16`.
- `git diff --check` - PASS.

## Visual Evidence

- Contact sheet: `evidence/sprint29_agent_inbox_folder_selector/visual_e2e_selector_r2/contact_sheet_selector_r2.png`
- Canonical screenshots: `evidence/sprint29_agent_inbox_folder_selector/visual_e2e_selector_r2/sprint29-agent-inbox-folder-selector-1781513593337/`
- The visual run includes:
  - disconnected Agent Inbox with `Choose folder` and no pasted folder field,
  - Android system folder picker after clicking `Choose folder`,
  - connected `document_tree_folder` state after a real Android folder selection callback,
  - access-lost select-folder state,
  - Markdown sidecar image rendering after Agent Inbox import,
  - dark connected state.

## Notes For Review

- The emulator has both `com.google.android.documentsui` and `com.google.android.apps.docs` installed.
- The visual picker screenshot is the Android DocumentsUI folder picker. It proves the app launches a real system folder selector instead of an in-app paste field.
- The R2 visual test then selects the `Documents` folder through DocumentsUI, confirms the tree grant, returns to the app, scans with no Google access token, and captures the connected state.
- The implementation scans only direct child folders of the selected tree and keeps the existing finite package/file/content/image byte limits.
- Remote-safe analytics expose only `grantMode=documentTreeFolder`, not raw `content://tree/...` URIs or package file ids.
