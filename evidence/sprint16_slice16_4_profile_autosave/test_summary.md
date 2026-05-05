# Test Summary

Slice: Sprint 16 Slice 16.4 Portable Profile Autosave Destination.

Validated commands:

- `./gradlew testDebugUnitTest --tests 'com.qualityalternative.app.data.PreferencesSettingsRepositoryTest.saveProfileAutosaveSettings_persistsStatusAndClearsFailureOnSuccess' --tests 'com.qualityalternative.app.data.AccountLightProfileExporterTest.exportSettingsOnlyProfileJson_emitsVersionedPortableSettingsProfile' --tests 'com.qualityalternative.app.ui.MainViewModelTest.configuredProfileAutosaveWritesPortableProfileAndStatus' --tests 'com.qualityalternative.app.ui.MainViewModelTest.profileAutosaveFailureKeepsDestinationAndDoesNotBlockAppUse' --tests 'com.qualityalternative.app.ui.MainViewModelTest.accountLightMergeImportKeepsLocalPortableSettings'`
- `./gradlew testDebugUnitTest --tests 'com.qualityalternative.app.ui.MainViewModelTest.configuredProfileAutosaveRunsAfterPortableProfileMutations' --tests 'com.qualityalternative.app.ui.MainViewModelTest.configuredProfileAutosaveWritesPortableProfileAndStatus' --tests 'com.qualityalternative.app.ui.MainViewModelTest.profileAutosaveFailureKeepsDestinationAndDoesNotBlockAppUse'`
- `./gradlew testDebugUnitTest --tests 'com.qualityalternative.app.ui.MainViewModelTest.configuredProfileAutosaveRunsAfterThemeModeChange' --tests 'com.qualityalternative.app.ui.MainViewModelTest.selectThemeMode_persistsAndUpdatesUiState'`
- `./gradlew testDebugUnitTest compileDebugAndroidTestKotlin`
- `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#accountLightProfileAutosaveSettingsShowsDestinationSuccessAndRecoverableFailure`
- `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.data.AndroidAccountLightProfileAutosaveWriterTest`

Result:

- All commands passed.
- Unit-test XML results are included under `logs/unit_*`.
- The R3 theme regression test verifies autosave after `selectThemeMode(AppThemeMode.DARK)` and asserts the
  autosaved `quality-alternative-profile.json` contains `"themeMode": "DARK"`.
- The mutation test verifies autosave after saved-link creation, document import, reading progress, priority toggle,
  completion, and library deletion.
- The document-tree connected test verifies create and rewrite of `quality-alternative-profile.json` through
  `DocumentsContract` tree/query/create/open URI paths with provider readback.

Manual visual check:

- Empty Settings state shows no autosave folder and disabled manual save.
- Success Settings state shows the selected destination, last-saved status, manual save, and turn-off action.
- Recoverable failure Settings state shows the selected destination, clear failure copy, retry action, and does not block the Settings screen.
