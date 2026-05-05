# Sprint 16 Slice 16.3 Test Summary

Slice: Portable Library And Reading State.

Commands run:

- `./gradlew testDebugUnitTest --tests 'com.qualityalternative.app.data.AccountLightProfileExporterTest' --tests 'com.qualityalternative.app.data.AccountLightProfileImporterTest' --tests 'com.qualityalternative.app.ui.MainViewModelTest.previewAccountLightImportShowsValidationErrorWithoutMutatingSettings' --tests 'com.qualityalternative.app.ui.MainViewModelTest.accountLightMergeImportKeepsLocalPortableSettings' --tests 'com.qualityalternative.app.ui.MainViewModelTest.accountLightReplaceImportRequiresConfirmationAndAppliesPortableSettings'`
- `./gradlew testDebugUnitTest --tests 'com.qualityalternative.app.data.AccountLightProfileImporterTest' --tests 'com.qualityalternative.app.ui.MainViewModelTest.missingPortableDocumentDoesNotOpenFromLibrary'`
- `./gradlew testDebugUnitTest --tests 'com.qualityalternative.app.data.AccountLightProfileExporterTest.exportSettingsOnlyProfileJson_includesPortableLibraryAndReadingStateWithoutRawUris' --tests 'com.qualityalternative.app.data.AccountLightProfileImporterTest.validateImportProfileJson_rejectsUnsafeNestedPortableDataBeforeMutation' --tests 'com.qualityalternative.app.data.AccountLightProfileImporterTest.applyMerge_importsLibraryMarksDocumentsMissingAndKeepsMissingDocumentProgressDormant' --tests 'com.qualityalternative.app.ui.MainViewModelTest.missingPortableDocumentDoesNotOpenFromLibrary'`
- `./gradlew testDebugUnitTest --tests 'com.qualityalternative.app.data.AccountLightProfileExporterTest.exportSettingsOnlyProfileJson_includesPortableLibraryAndReadingStateWithoutRawUris' --tests 'com.qualityalternative.app.data.AccountLightProfileImporterTest.validateImportProfileJson_rejectsUnsafeNestedPortableDataBeforeMutation'`
- `./gradlew testDebugUnitTest --tests 'com.qualityalternative.app.data.AccountLightProfileExporterTest.exportSettingsOnlyProfileJson_includesPortableLibraryAndReadingStateWithoutRawUris' --tests 'com.qualityalternative.app.data.AccountLightProfileImporterTest.validateImportProfileJson_rejectsUnsafeNestedPortableDataBeforeMutation'`
- `./gradlew testDebugUnitTest --tests 'com.qualityalternative.app.data.AccountLightProfileExporterTest.exportSettingsOnlyProfileJson_includesPortableLibraryAndReadingStateWithoutRawUris' --tests 'com.qualityalternative.app.data.AccountLightProfileImporterTest.validateImportProfileJson_rejectsUnsafeNestedPortableDataBeforeMutation'`
- `./gradlew testDebugUnitTest --tests 'com.qualityalternative.app.data.AccountLightProfileExporterTest.exportSettingsOnlyProfileJson_includesPortableLibraryAndReadingStateWithoutRawUris' --tests 'com.qualityalternative.app.data.AccountLightProfileImporterTest.validateImportProfileJson_rejectsUnsafeNestedPortableDataBeforeMutation'`
- `./gradlew testDebugUnitTest --tests 'com.qualityalternative.app.data.AccountLightProfileExporterTest.exportSettingsOnlyProfileJson_includesPortableLibraryAndReadingStateWithoutRawUris' --tests 'com.qualityalternative.app.data.AccountLightProfileImporterTest.validateImportProfileJson_rejectsUnsafeNestedPortableDataBeforeMutation'`
- `./gradlew testDebugUnitTest --tests 'com.qualityalternative.app.data.AccountLightProfileExporterTest.exportSettingsOnlyProfileJson_includesPortableLibraryAndReadingStateWithoutRawUris' --tests 'com.qualityalternative.app.data.AccountLightProfileImporterTest.validateImportProfileJson_rejectsUnsafeNestedPortableDataBeforeMutation'`
- `./gradlew testDebugUnitTest --tests 'com.qualityalternative.app.data.AccountLightProfileExporterTest.exportSettingsOnlyProfileJson_includesPortableLibraryAndReadingStateWithoutRawUris' --tests 'com.qualityalternative.app.data.AccountLightProfileImporterTest.validateImportProfileJson_rejectsUnsafeNestedPortableDataBeforeMutation'`
- `./gradlew testDebugUnitTest --tests 'com.qualityalternative.app.data.AccountLightProfileExporterTest.exportSettingsOnlyProfileJson_includesPortableLibraryAndReadingStateWithoutRawUris' --tests 'com.qualityalternative.app.data.AccountLightProfileImporterTest.validateImportProfileJson_rejectsUnsafeNestedPortableDataBeforeMutation'`
- `./gradlew testDebugUnitTest --tests 'com.qualityalternative.app.data.AccountLightProfileExporterTest.exportSettingsOnlyProfileJson_includesPortableLibraryAndReadingStateWithoutRawUris' --tests 'com.qualityalternative.app.data.AccountLightProfileImporterTest.validateImportProfileJson_rejectsUnsafeNestedPortableDataBeforeMutation'`
- `./gradlew testDebugUnitTest --tests 'com.qualityalternative.app.data.AccountLightProfileExporterTest.exportSettingsOnlyProfileJson_includesPortableLibraryAndReadingStateWithoutRawUris' --tests 'com.qualityalternative.app.data.AccountLightProfileImporterTest.validateImportProfileJson_rejectsUnsafeNestedPortableDataBeforeMutation'`
- `./gradlew testDebugUnitTest compileDebugAndroidTestKotlin`
- `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#accountLightImportSettingsShowsPreviewErrorsConfirmationAndSuccess`
- `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.data.RoomUserDocumentRepositoryTest#importPortableDocuments_mergeSkipsExistingAvailableDocumentCollision`
- `git diff --check`

Result:

- Targeted profile/import unit tests passed.
- R2 targeted import/open-guard unit tests passed.
- R3 targeted privacy regression tests passed.
- R4 targeted URL/privacy regression tests passed.
- R5 targeted encoded-URL, MIME, and dangling-settings-reference privacy regression tests passed.
- R6 targeted title-length export/import consistency regression tests passed.
- R7 targeted saved-link source-label export/import consistency regression tests passed.
- R8 targeted nested-encoded saved-link URL privacy regression tests passed.
- R9 targeted decode-depth-cap saved-link URL privacy regression tests passed.
- R10 targeted SAF/provider document-id URL privacy regression tests passed.
- R11 targeted short provider-internal saved-link URL privacy regression tests passed.
- R12 targeted extensionless and unsupported-extension local-storage-path saved-link URL privacy regression tests passed.
- R14 targeted malformed nested URL-decoding local-storage/raw-URI privacy regression tests passed.
- Full debug unit tests and Android test compilation passed.
- Connected visual import flow passed on `qaApi36`.
- Connected Room merge-collision regression passed on `qaApi36`.
- `git diff --check` passed.

Coverage notes:

- Export test verifies saved links, user-document metadata, portable id mapping, reading progress mapping, and absence of raw `content://` data.
- Import test verifies restored links, missing-document state, dormant progress for missing imported documents, and rollback if a progress write fails mid-replace.
- Privacy regression tests verify unsafe link/document descriptions are rejected during import and replaced with neutral metadata during export.
- Privacy regression tests verify unsafe Google Drive URLs, userinfo/OAuth URLs, long opaque ids, raw URI, email, token, and provider-like values cannot leave or enter through Portable Profile library fields.
- Privacy regression tests verify encoded `file://` payloads and provider-like URL paths are rejected even when wrapped in a valid `https://` URL.
- Privacy regression tests verify imported document `mimeType` cannot carry raw URI/provider data and unsafe exported library rows cannot leave settings references behind.
- Export/import consistency tests verify saved user-link and user-document titles over 200 characters are neither exported nor imported as portable library data.
- Export/import consistency tests verify saved-link source labels over 120 characters are omitted from export and rejected on import.
- URL privacy tests verify double-encoded `file://`, `content://`, and account-email payloads inside otherwise valid `https://` saved-link URLs cannot be exported or imported.
- URL privacy tests verify six-deep encoded raw `file://` payloads are rejected because values that are still decoding at the depth cap are treated as non-portable.
- URL privacy tests verify Android SAF/provider IDs such as `primary:Download/book.epub` and storage/download file payloads embedded in saved-link URL query strings are rejected on import and omitted on export.
- URL privacy tests verify no-slash Android provider-internal IDs such as `image:3952` and `msf:29` embedded in saved-link URL query strings are rejected on import and omitted on export.
- URL privacy tests verify local `storage/emulated/0/Download` path payloads without a reader extension or with unsupported `.docx` extensions are rejected on import and omitted on export.
- URL privacy tests verify malformed nested percent-encoding cannot stop URL scanning early; malformed nested local-storage and raw-URI payloads are rejected on import and omitted on export.
- ViewModel test verifies a missing portable document cannot be opened from Library.
- Connected Room test verifies merge mode does not replace an existing available local document with a synthetic missing imported document collision.
- Visual test covers settings import entry, merge preview, replace confirmation, success, invalid JSON, unsupported future schema, and missing-document library state with `File missing` instead of `Open`/`Continue`.
