# Sprint 35 R2 Fix Summary

GPT Pro R2 returned `SCORE: 9/10`, `VERDICT: REVISE`, `VISUAL REVIEW: PASS`.

## R2 Finding

R2 found that legacy `picker_folder` grants were repaired by the visible Compose click path, but were not hard-blocked below the UI. A direct ViewModel caller could still treat `picker_folder` as an operational grant, and repository scan-success persistence accepted it.

## Fix Applied

- `MainUiState.hasAgentInboxDriveFolderGrant` now includes only operational grants:
  - `readonly_folder`
  - `document_tree_folder`
- `picker_folder` remains visible through `hasAgentInboxPickerFolderGrant` so the UI can show reconnect/repair state.
- `scanAgentInboxDrive()` now explicitly blocks `picker_folder`, copies the legacy folder id into the draft, and reports: `Drive file Picker access is not enough for Agent Inbox. Use Drive link access.`
- `importAgentInboxCandidate()` now explicitly blocks `picker_folder`, copies the legacy folder id into the draft, and reports: `Drive file Picker access is not enough for Agent Inbox import. Use Drive link access.`
- `PreferencesSettingsRepository.saveAgentInboxDriveScanSuccess()` now accepts only `readonly_folder` and `document_tree_folder`; `picker_folder` remains in supported grant modes only for legacy restore/repair state.
- The settings UI still renders `Disconnect` for picker repair state so users can clear stale legacy grants.

## Tests Added/Updated

- `MainViewModelTest.legacyPickerFolderGrantIsRepairOnlyAndCannotScanBelowUi`
- `MainViewModelTest.legacyPickerFolderGrantIsRepairOnlyAndCannotImportBelowUi`
- `PreferencesSettingsRepositoryTest.saveAgentInboxDriveSettings_persistsConnectionStatusAndFailure` now asserts picker scan-success is rejected.

## Current Validation

- Targeted ViewModel/preferences/authorization tests passed.
- Full gate passed after the R2 fix: `testDebugUnitTest`, `lintDebug`, `assembleRelease`, `assembleDebug` with `--rerun-tasks`.
- `git diff --check` passed.
- Debug APK was rebuilt, hashed, installed, package-dumped, and launched.
- Connected visual E2E was rerun and passed: `VisualQaScreenshotTest#captureSprint35AgentInboxFolderSelectorRepairStates`.
