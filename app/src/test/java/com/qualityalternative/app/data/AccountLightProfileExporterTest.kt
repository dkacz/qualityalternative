package com.qualityalternative.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.qualityalternative.app.domain.model.AppThemeMode
import com.qualityalternative.app.domain.model.ContentAvailability
import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.ContentPriority
import com.qualityalternative.app.domain.model.ContentRightsMetadata
import com.qualityalternative.app.domain.model.ContentSourceType
import com.qualityalternative.app.domain.model.CustomTargetAppCandidate
import com.qualityalternative.app.domain.model.CustomTargetAppEligibility
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.model.DurationBucket
import com.qualityalternative.app.domain.model.InterventionMode
import com.qualityalternative.app.domain.model.OnboardingSelection
import com.qualityalternative.app.domain.model.ReadingProgress
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.model.UserDocumentDraft
import com.qualityalternative.app.domain.model.UserLinkDraft
import com.qualityalternative.app.domain.service.AddUserDocumentResult
import com.qualityalternative.app.domain.service.AddUserLinkResult
import com.qualityalternative.app.domain.service.ReadingProgressRepository
import com.qualityalternative.app.domain.service.UserDocumentRepository
import com.qualityalternative.app.domain.service.UserLinkRepository
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountLightProfileExporterTest {
    @Test
    fun exportSettingsOnlyProfileJson_emitsVersionedPortableSettingsProfile() = runBlocking {
        val repository = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        val selectedPackages = SupportedCatalog.distractingApps.take(2).mapTo(mutableSetOf()) { it.packageName }
        repository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = selectedPackages,
                preferredTopics = setOf(TopicTag.SCIENCE, TopicTag.PHILOSOPHY, TopicTag.HISTORY),
                preferredDurationBucket = DurationBucket.DEEP,
                selectedPackIds = setOf("starter_pack", "attention_reset_v1"),
            ),
        )
        repository.saveInterventionMode(InterventionMode.SOFT)
        repository.saveThemeMode(AppThemeMode.DARK)
        repository.saveMeditationDurationMinutes(7)
        repository.saveReaderFontScale(1.254)
        repository.saveInterfaceTextScale(1.164)
        repository.saveContentPriority(ContentPriority.MY_FILES)
        repository.savePriorityContentIds(setOf("user-document-11111111-1111-4111-8111-111111111111"))
        repository.saveReactivatedCompletedContentIds(setOf("editorial-deep-work"))
        repository.saveOpenAnywayUnlockMinutes(120)
        repository.saveBedtimeSettings(enabled = true, startMinutes = 23 * 60, endMinutes = 6 * 60 + 30)
        repository.saveAnnotationExportDestination(
            uri = "content://drive/raw-provider-id",
            displayName = "qa-annotations.jsonld",
        )
        repository.saveAnnotationExportFailure("Drive write unavailable at content://drive/raw-provider-id")
        repository.saveAnnotationExportSuccess(12_000L)
        repository.saveAnnotationDriveSyncSuccess(
            timestampMillis = 13_000L,
            folderId = "raw-drive-folder-id",
        )
        repository.saveProfileAutosaveDestination(
            uri = "content://tree/raw-profile-folder-id",
            displayName = "QA profile",
        )
        repository.saveProfileAutosaveSuccess(14_000L)

        val exporter = AccountLightProfileExporter(
            settingsRepository = repository,
            appVersionName = "0.8.1-alpha",
            appVersionCode = 13,
        )

        val rawJson = exporter.exportSettingsOnlyProfileJson(nowMillis = 20_000L)
        val profile = AccountLightProfileCodec().decode(rawJson)

        assertEquals(ACCOUNT_LIGHT_PROFILE_FILE_NAME, "quality-alternative-profile.json")
        assertEquals(
            "quality-alternative-profile-19700101-000020.json",
            accountLightTimestampedBackupFileName(nowMillis = 20_000L),
        )
        assertEquals(ACCOUNT_LIGHT_SCHEMA_VERSION, profile.schemaVersion)
        assertEquals(20_000L, profile.exportedAtMillis)
        assertEquals(ACCOUNT_LIGHT_PROFILE_FORMAT, profile.app.profileFormat)
        assertEquals(ACCOUNT_LIGHT_PACKAGE_NAME, profile.app.packageName)
        assertEquals("0.8.1-alpha", profile.app.appVersionName)
        assertEquals(13, profile.app.appVersionCode)
        assertTrue(profile.profile.profileId.matches(Regex("^qa-local-[0-9a-fA-F-]{36}$")))
        assertEquals(20_000L, profile.profile.createdAtMillis)
        assertEquals(20_000L, profile.profile.updatedAtMillis)
        assertNull(profile.profile.displayName)

        assertEquals(true, profile.settings.hasCompletedOnboarding)
        assertEquals(selectedPackages.sorted(), profile.settings.selectedAppPackages)
        assertEquals(listOf("HISTORY", "PHILOSOPHY", "SCIENCE"), profile.settings.preferredTopics)
        assertEquals("DEEP", profile.settings.preferredDurationBucket)
        assertEquals(listOf("attention_reset_v1", "starter_pack"), profile.settings.selectedPackIds)
        assertEquals("SOFT", profile.settings.interventionMode)
        assertEquals("DARK", profile.settings.themeMode)
        assertEquals(7, profile.settings.meditationDurationMinutes)
        assertEquals(1.25, profile.settings.readerFontScale, 0.0)
        assertEquals(1.16, profile.settings.interfaceTextScale, 0.0)
        assertEquals("MY_FILES", profile.settings.contentPriority)
        assertEquals(listOf("user-document-11111111-1111-4111-8111-111111111111"), profile.settings.priorityContentIds)
        assertEquals(listOf("editorial-deep-work"), profile.settings.reactivatedCompletedContentIds)
        assertEquals(120, profile.settings.openAnywayUnlockMinutes)
        assertEquals(true, profile.settings.bedtimeEnabled)
        assertEquals(23 * 60, profile.settings.bedtimeStartMinutes)
        assertEquals(6 * 60 + 30, profile.settings.bedtimeEndMinutes)

        assertEquals(emptyList<AccountLightUserLink>(), profile.library.userLinks)
        assertEquals(emptyList<AccountLightUserDocument>(), profile.library.userDocuments)
        assertEquals(emptyList<AccountLightReadingProgress>(), profile.reading.progress)
        assertEquals("qa-annotations.jsonld", profile.annotations.export.destinationDisplayName)
        assertEquals(12_000L, profile.annotations.export.lastSuccessfulAtMillis)
        assertEquals(true, profile.annotations.driveSync.wasEnabledOnSourceDevice)
        assertNull(profile.annotations.driveSync.folderDisplayName)
        assertEquals(13_000L, profile.annotations.driveSync.lastSuccessfulAtMillis)
        assertEquals("ANDROID_DOCUMENT_TREE", profile.sync.profileAutosave.provider)
        assertEquals("QA profile", profile.sync.profileAutosave.destinationDisplayName)
        assertEquals(14_000L, profile.sync.profileAutosave.lastSuccessfulAtMillis)
        assertEquals("REQUIRES_LOCAL_SELECTION", profile.sync.profileAutosave.activationStateOnImport)
        assertEquals(emptyList<AccountLightWarning>(), profile.warnings)

        assertFalse(rawJson.contains("content://"))
        assertFalse(rawJson.contains("raw-drive-folder-id"))
        assertFalse(rawJson.contains("raw-profile-folder-id"))
        assertFalse(rawJson.contains("Drive write unavailable"))
        assertFalse(rawJson.contains("token", ignoreCase = true))
        assertFalse(rawJson.contains("oauth", ignoreCase = true))
    }

    @Test
    fun exportSettingsOnlyProfileJson_omitsUnsafeSelectedPackIds() = runBlocking {
        val repository = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        repository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = SupportedCatalog.distractingApps.take(2).mapTo(mutableSetOf()) { it.packageName },
                preferredTopics = setOf(TopicTag.SCIENCE, TopicTag.PHILOSOPHY),
                preferredDurationBucket = DurationBucket.FOCUS,
                selectedPackIds = setOf(
                    "starter_pack",
                    "public-domain-expansion-v2",
                    "content://provider/raw-id",
                    "oauth-token-pack",
                    "user@example.com",
                    "com.google.android.apps.docs",
                ),
            ),
        )
        val exporter = AccountLightProfileExporter(
            settingsRepository = repository,
            appVersionName = "0.8.1-alpha",
            appVersionCode = 13,
        )

        val profile = AccountLightProfileCodec().decode(exporter.exportSettingsOnlyProfileJson(nowMillis = 20_000L))

        assertEquals(listOf("public-domain-expansion-v2", "starter_pack"), profile.settings.selectedPackIds)
    }

    @Test
    fun exportSettingsOnlyProfileJson_includesSelectedEligibleCustomAppPackage() = runBlocking {
        val customTarget = DistractingApp(
            packageName = "com.example.deepwork",
            displayName = "Deep Work Trap",
        )
        val repository = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
            customTargetCandidatesProvider = {
                listOf(
                    CustomTargetAppCandidate(
                        app = customTarget,
                        eligibility = CustomTargetAppEligibility.ELIGIBLE,
                    ),
                )
            },
        )
        val selectedPackages = setOf(SupportedCatalog.distractingApps.first().packageName, customTarget.packageName)
        repository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = selectedPackages,
                preferredTopics = setOf(TopicTag.SCIENCE, TopicTag.PHILOSOPHY, TopicTag.HISTORY),
                preferredDurationBucket = DurationBucket.FOCUS,
                selectedPackIds = setOf("starter_pack"),
            ),
        )
        val exporter = AccountLightProfileExporter(
            settingsRepository = repository,
            appVersionName = "0.8.1-alpha",
            appVersionCode = 13,
        )

        val profile = AccountLightProfileCodec().decode(exporter.exportSettingsOnlyProfileJson(nowMillis = 20_000L))

        assertEquals(selectedPackages.sorted(), profile.settings.selectedAppPackages)
    }

    @Test
    fun exportSettingsOnlyProfileJson_filtersNonPortableSettingsReferencesAndDisplayNames() = runBlocking {
        val repository = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        val portablePriorityId = "user-link-22222222-2222-4222-8222-222222222222"
        val portableReactivatedId = "editorial-deep-work"
        repository.savePriorityContentIds(setOf("legacy:id", portablePriorityId, "doc:local"))
        repository.saveReactivatedCompletedContentIds(setOf("p1", portableReactivatedId))
        repository.saveAnnotationExportDestination(
            uri = "content://drive/raw-provider-id",
            displayName = "content://drive/raw-provider-id",
        )
        val exporter = AccountLightProfileExporter(
            settingsRepository = repository,
            appVersionName = "0.8.1-alpha",
            appVersionCode = 13,
        )

        val rawJson = exporter.exportSettingsOnlyProfileJson(nowMillis = 20_000L)
        val profile = AccountLightProfileCodec().decode(rawJson)

        assertEquals(listOf(portablePriorityId), profile.settings.priorityContentIds)
        assertEquals(listOf(portableReactivatedId), profile.settings.reactivatedCompletedContentIds)
        assertNull(profile.annotations.export.destinationDisplayName)
        assertEquals(
            2,
            profile.warnings.count {
                it.code == "CONFLICT_RETAINED_LOCAL_VALUE" && it.section == "settings"
            },
        )
        assertTrue(
            profile.warnings.any {
                it.code == "CONFLICT_RETAINED_LOCAL_VALUE" &&
                    it.section == "annotations"
            },
        )
        assertFalse(rawJson.contains("legacy:id"))
        assertFalse(rawJson.contains("doc:local"))
        assertFalse(rawJson.contains("content://"))
        assertFalse(rawJson.contains("raw-provider-id"))
    }

    @Test
    fun exportSettingsOnlyProfileJson_includesPortableLibraryAndReadingStateWithoutRawUris() = runBlocking {
        val repository = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        val link = ContentItem(
            id = "user-link-11111111-1111-4111-8111-111111111111",
            packId = "user-links",
            title = "Saved essay",
            description = "A saved link from content://com.android.providers.media.documents/document/raw-link.",
            durationMinutes = 11,
            format = ContentFormat.HTML,
            topicTags = setOf(TopicTag.SCIENCE),
            externalUrl = "https://example.com/essay",
            sourceLabel = "example.com",
            sourceType = ContentSourceType.USER_LINK,
            availability = ContentAvailability.AVAILABLE,
            rights = ContentRightsMetadata.userPrivateExternal(sourceUrl = "https://example.com/essay"),
            addedAtMillis = 5_000L,
        )
        val unsafeLink = link.copy(
            id = "user-link-22222222-2222-4222-8222-222222222222",
            title = "Unsafe drive link",
            externalUrl = "https://drive.google.com/file/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/view",
            rights = ContentRightsMetadata.userPrivateExternal(
                sourceUrl = "https://drive.google.com/file/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/view",
            ),
        )
        val longTitleLink = link.copy(
            id = "user-link-55555555-5555-4555-8555-555555555555",
            title = "L".repeat(201),
            externalUrl = "https://example.com/long-title",
            rights = ContentRightsMetadata.userPrivateExternal(sourceUrl = "https://example.com/long-title"),
        )
        val longSourceLabelLink = link.copy(
            id = "user-link-77777777-7777-4777-8777-777777777777",
            title = "Long label link",
            externalUrl = "https://example.com/long-source-label",
            sourceLabel = "s".repeat(121),
            rights = ContentRightsMetadata.userPrivateExternal(sourceUrl = "https://example.com/long-source-label"),
        )
        val doubleEncodedUnsafeLink = link.copy(
            id = "user-link-88888888-8888-4888-8888-888888888888",
            title = "Nested encoded link",
            externalUrl = "https://example.com/read?src=file%253A%252F%252F%252Fsdcard%252Fbook.epub",
            rights = ContentRightsMetadata.userPrivateExternal(
                sourceUrl = "https://example.com/read?src=file%253A%252F%252F%252Fsdcard%252Fbook.epub",
            ),
        )
        val deepEncodedUnsafeLink = link.copy(
            id = "user-link-99999999-9999-4999-8999-999999999999",
            title = "Deep encoded link",
            externalUrl = "https://example.com/read?src=file%25252525253A%25252525252F%25252525252F%25252525252Fsdcard%25252525252Fbook.epub",
            rights = ContentRightsMetadata.userPrivateExternal(
                sourceUrl = "https://example.com/read?src=file%25252525253A%25252525252F%25252525252F%25252525252Fsdcard%25252525252Fbook.epub",
            ),
        )
        val providerDocumentIdLink = link.copy(
            id = "user-link-aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa",
            title = "Provider id link",
            externalUrl = "https://example.com/read?doc=primary%3ADownload%2Fbook.epub",
            rights = ContentRightsMetadata.userPrivateExternal(
                sourceUrl = "https://example.com/read?doc=primary%3ADownload%2Fbook.epub",
            ),
        )
        val shortProviderIdLink = link.copy(
            id = "user-link-bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb",
            title = "Short provider id link",
            externalUrl = "https://example.com/read?image=image%3A3952",
            rights = ContentRightsMetadata.userPrivateExternal(
                sourceUrl = "https://example.com/read?image=image%3A3952",
            ),
        )
        val storagePathNoExtensionLink = link.copy(
            id = "user-link-cccccccc-cccc-4ccc-8ccc-cccccccccccc",
            title = "Storage path link",
            externalUrl = "https://example.com/read?doc=storage%2Femulated%2F0%2FDownload%2Fbook",
            rights = ContentRightsMetadata.userPrivateExternal(
                sourceUrl = "https://example.com/read?doc=storage%2Femulated%2F0%2FDownload%2Fbook",
            ),
        )
        val storagePathUnsupportedExtensionLink = link.copy(
            id = "user-link-dddddddd-dddd-4ddd-8ddd-dddddddddddd",
            title = "Storage docx path link",
            externalUrl = "https://example.com/read?doc=storage%2Femulated%2F0%2FDownload%2Fbook.docx",
            rights = ContentRightsMetadata.userPrivateExternal(
                sourceUrl = "https://example.com/read?doc=storage%2Femulated%2F0%2FDownload%2Fbook.docx",
            ),
        )
        val malformedNestedStorageLink = link.copy(
            id = "user-link-eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee",
            title = "Malformed nested storage link",
            externalUrl = "https://example.com/read?doc=storage%252Femulated%252F0%252FDownload%252Fbook%25ZZ",
            rights = ContentRightsMetadata.userPrivateExternal(
                sourceUrl = "https://example.com/read?doc=storage%252Femulated%252F0%252FDownload%252Fbook%25ZZ",
            ),
        )
        val malformedNestedRawUriLink = link.copy(
            id = "user-link-ffffffff-ffff-4fff-8fff-ffffffffffff",
            title = "Malformed nested raw uri link",
            externalUrl = "https://example.com/read?src=file%253A%252F%252F%252Fsdcard%252Fbook%25ZZ",
            rights = ContentRightsMetadata.userPrivateExternal(
                sourceUrl = "https://example.com/read?src=file%253A%252F%252F%252Fsdcard%252Fbook%25ZZ",
            ),
        )
        val documentFingerprintSha256 = "abababababababababababababababababababababababababababababababab"
        val document = ContentItem(
            id = "user-document-33333333-3333-4333-8333-333333333333",
            packId = "user-documents",
            title = "Imported book",
            description = "A private EPUB from user@example.com with oauth token text.",
            durationMinutes = 45,
            format = ContentFormat.EPUB,
            topicTags = setOf(TopicTag.PHILOSOPHY),
            sourceLabel = "book.epub",
            sourceType = ContentSourceType.USER_DOCUMENT,
            availability = ContentAvailability.AVAILABLE,
            rights = ContentRightsMetadata.userPrivateReader(
                sourceUrl = "content://com.android.providers.media.documents/document/raw-book",
                attribution = "book.epub",
            ),
            addedAtMillis = 6_000L,
            documentFingerprintSha256 = documentFingerprintSha256,
            documentFingerprintSizeBytes = 4_096L,
        )
        val longTitleDocument = document.copy(
            id = "user-document-66666666-6666-4666-8666-666666666666",
            title = "D".repeat(201),
            sourceLabel = "long-title.epub",
        )
        repository.savePriorityContentIds(
            setOf(
                link.id,
                unsafeLink.id,
                longTitleLink.id,
                longSourceLabelLink.id,
                doubleEncodedUnsafeLink.id,
                deepEncodedUnsafeLink.id,
                providerDocumentIdLink.id,
                shortProviderIdLink.id,
                storagePathNoExtensionLink.id,
                storagePathUnsupportedExtensionLink.id,
                malformedNestedStorageLink.id,
                malformedNestedRawUriLink.id,
                longTitleDocument.id,
            ),
        )
        val exporter = AccountLightProfileExporter(
            settingsRepository = repository,
            appVersionName = "0.8.1-alpha",
            appVersionCode = 13,
            userLinkRepository = StaticUserLinkRepository(
                listOf(
                    link,
                    unsafeLink,
                    longTitleLink,
                    longSourceLabelLink,
                    doubleEncodedUnsafeLink,
                    deepEncodedUnsafeLink,
                    providerDocumentIdLink,
                    shortProviderIdLink,
                    storagePathNoExtensionLink,
                    storagePathUnsupportedExtensionLink,
                    malformedNestedStorageLink,
                    malformedNestedRawUriLink,
                ),
            ),
            userDocumentRepository = StaticUserDocumentRepository(listOf(document, longTitleDocument)),
            readingProgressRepository = StaticReadingProgressRepository(
                listOf(
                    ReadingProgress(
                        contentId = link.id,
                        progressPercent = 42,
                        lastVisibleParagraphIndex = 4,
                        lastVisibleTextOffset = 37,
                        paragraphCount = 12,
                        updatedAtMillis = 9_000L,
                    ),
                ),
            ),
        )

        val rawJson = exporter.exportSettingsOnlyProfileJson(nowMillis = 20_000L)
        val profile = AccountLightProfileCodec().decode(rawJson)

        assertEquals(2, profile.library.userLinks.size)
        val exportedLink = profile.library.userLinks.single { it.normalizedUrl == "https://example.com/essay" }
        val exportedLongLabelLink = profile.library.userLinks.single {
            it.normalizedUrl == "https://example.com/long-source-label"
        }
        val exportedDocument = profile.library.userDocuments.single()
        assertTrue(exportedLink.contentId.startsWith("user-link-"))
        assertFalse(exportedLink.contentId.contains(":"))
        assertEquals("https://example.com/essay", exportedLink.normalizedUrl)
        assertEquals("Saved link metadata.", exportedLink.description)
        assertNull(exportedLongLabelLink.sourceLabel)
        assertTrue(exportedDocument.contentId.startsWith("user-document-"))
        assertEquals("Saved document metadata.", exportedDocument.description)
        assertEquals("MISSING_FILE_NEEDS_REATTACH", exportedDocument.documentImportState)
        assertEquals("SHA256_BYTES", exportedDocument.documentFingerprint.strategy)
        assertEquals(documentFingerprintSha256, exportedDocument.documentFingerprint.sha256)
        assertEquals(4_096L, exportedDocument.documentFingerprint.sizeBytes)
        assertFalse(
            profile.warnings.any {
                it.code == "DOCUMENT_FINGERPRINT_UNVERIFIED" &&
                    it.contentId == exportedDocument.contentId
            },
        )
        assertEquals(
            setOf(exportedLink.contentId, exportedLongLabelLink.contentId),
            profile.settings.priorityContentIds.toSet(),
        )
        assertTrue(
            profile.warnings.any {
                it.code == "CONFLICT_RETAINED_LOCAL_VALUE" && it.section == "settings"
            },
        )
        assertEquals(exportedLink.contentId, profile.reading.progress.single().contentId)
        assertEquals(4, profile.reading.progress.single().lastVisibleParagraphIndex)
        assertEquals(37, profile.reading.progress.single().lastVisibleTextOffset)
        assertFalse(rawJson.contains("content://"))
        assertFalse(rawJson.contains("raw-book"))
        assertFalse(rawJson.contains("raw-link"))
        assertFalse(rawJson.contains("user@example.com"))
        assertFalse(rawJson.contains("oauth"))
        assertFalse(rawJson.contains("token"))
        assertFalse(rawJson.contains("1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms"))
        assertFalse(rawJson.contains("drive.google.com"))
        assertFalse(rawJson.contains("legacy-local-id"))
        assertFalse(rawJson.contains("22222222-2222-4222-8222-222222222222"))
        assertFalse(rawJson.contains("55555555-5555-4555-8555-555555555555"))
        assertFalse(rawJson.contains("66666666-6666-4666-8666-666666666666"))
        assertFalse(rawJson.contains("88888888-8888-4888-8888-888888888888"))
        assertFalse(rawJson.contains("99999999-9999-4999-8999-999999999999"))
        assertFalse(rawJson.contains("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa"))
        assertFalse(rawJson.contains("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb"))
        assertFalse(rawJson.contains("cccccccc-cccc-4ccc-8ccc-cccccccccccc"))
        assertFalse(rawJson.contains("dddddddd-dddd-4ddd-8ddd-dddddddddddd"))
        assertFalse(rawJson.contains("eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee"))
        assertFalse(rawJson.contains("ffffffff-ffff-4fff-8fff-ffffffffffff"))
        assertFalse(rawJson.contains("L".repeat(201)))
        assertFalse(rawJson.contains("D".repeat(201)))
        assertFalse(rawJson.contains("s".repeat(121)))
        assertFalse(rawJson.contains("file%253A%252F%252F%252Fsdcard%252Fbook.epub"))
        assertFalse(rawJson.contains("file%25252525253A%25252525252F%25252525252F%25252525252Fsdcard"))
        assertFalse(rawJson.contains("primary%3ADownload%2Fbook.epub"))
        assertFalse(rawJson.contains("image%3A3952"))
        assertFalse(rawJson.contains("storage%2Femulated%2F0%2FDownload%2Fbook"))
        assertFalse(rawJson.contains("book.docx"))
        assertFalse(rawJson.contains("book%25ZZ"))
    }

    @Test
    fun exportSettingsOnlyProfileJson_warnsWhenUserDocumentHasNoVerifiedFingerprint() = runBlocking {
        val repository = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        val document = ContentItem(
            id = "user-document-33333333-3333-4333-8333-333333333333",
            packId = "user-documents",
            title = "Imported book",
            description = "Saved document metadata.",
            durationMinutes = 45,
            format = ContentFormat.EPUB,
            topicTags = setOf(TopicTag.PHILOSOPHY),
            sourceLabel = "book.epub",
            sourceType = ContentSourceType.USER_DOCUMENT,
            availability = ContentAvailability.AVAILABLE,
            rights = ContentRightsMetadata.userPrivateReader(
                sourceUrl = "content://com.android.providers.media.documents/document/raw-book",
                attribution = "book.epub",
            ),
            addedAtMillis = 6_000L,
        )
        val exporter = AccountLightProfileExporter(
            settingsRepository = repository,
            appVersionName = "0.8.1-alpha",
            appVersionCode = 13,
            userDocumentRepository = StaticUserDocumentRepository(listOf(document)),
        )

        val profile = AccountLightProfileCodec().decode(exporter.exportSettingsOnlyProfileJson(nowMillis = 20_000L))
        val exportedDocument = profile.library.userDocuments.single()

        assertEquals("UNVERIFIED_METADATA_ONLY", exportedDocument.documentFingerprint.strategy)
        assertEquals(null, exportedDocument.documentFingerprint.sha256)
        assertEquals(null, exportedDocument.documentFingerprint.sizeBytes)
        assertTrue(
            profile.warnings.any {
                it.code == "DOCUMENT_FINGERPRINT_UNVERIFIED" &&
                    it.section == "library.userDocuments" &&
                    it.contentId == exportedDocument.contentId
            },
        )
    }

    @Test
    fun warningAndAutosaveDtosEnforceApprovedSchemaDomains() {
        AccountLightWarning(
            code = "CONFLICT_RETAINED_LOCAL_VALUE",
            severity = "WARNING",
            section = "settings",
            message = "Portable warning.",
        )
        AccountLightProfileAutosave()

        assertThrows(IllegalArgumentException::class.java) {
            AccountLightWarning(
                code = "INVALID_CONTENT_ID_DROPPED",
                severity = "WARNING",
                section = "settings",
                message = "Portable warning.",
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            AccountLightWarning(
                code = "CONFLICT_RETAINED_LOCAL_VALUE",
                severity = "ERROR",
                section = "settings",
                message = "Portable warning.",
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            AccountLightWarning(
                code = "CONFLICT_RETAINED_LOCAL_VALUE",
                severity = "WARNING",
                section = "settings.priorityContentIds",
                message = "Portable warning.",
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            AccountLightWarning(
                code = "CONFLICT_RETAINED_LOCAL_VALUE",
                severity = "WARNING",
                section = "settings",
                message = "content://raw",
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            AccountLightProfileAutosave(provider = "DROPBOX")
        }
        assertThrows(IllegalArgumentException::class.java) {
            AccountLightProfileAutosave(destinationDisplayName = "file://raw/path")
        }
        assertThrows(IllegalArgumentException::class.java) {
            AccountLightProfileAutosave(lastSuccessfulAtMillis = -1L)
        }
        assertThrows(IllegalArgumentException::class.java) {
            AccountLightProfileAutosave(activationStateOnImport = "ACTIVE")
        }
    }

    @Test
    fun exportSettingsOnlyProfileJson_reusesLocalProfileIdentityAcrossExports() = runBlocking {
        val repository = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        val exporter = AccountLightProfileExporter(
            settingsRepository = repository,
            appVersionName = "0.8.1-alpha",
            appVersionCode = 13,
        )

        val first = AccountLightProfileCodec().decode(exporter.exportSettingsOnlyProfileJson(nowMillis = 10_000L))
        val second = AccountLightProfileCodec().decode(exporter.exportSettingsOnlyProfileJson(nowMillis = 15_000L))

        assertEquals(first.profile.profileId, second.profile.profileId)
        assertEquals(10_000L, second.profile.createdAtMillis)
        assertEquals(15_000L, second.profile.updatedAtMillis)
    }

    @Test
    fun exportSettingsOnlyProfileJson_clampsUpdatedAtWhenDeviceClockMovesBackward() = runBlocking {
        val repository = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        repository.ensureLocalProfileIdentity(nowMillis = 20_000L)
        val exporter = AccountLightProfileExporter(
            settingsRepository = repository,
            appVersionName = "0.8.1-alpha",
            appVersionCode = 13,
        )

        val profile = AccountLightProfileCodec().decode(exporter.exportSettingsOnlyProfileJson(nowMillis = 10_000L))

        assertEquals(10_000L, profile.exportedAtMillis)
        assertEquals(20_000L, profile.profile.createdAtMillis)
        assertEquals(20_000L, profile.profile.updatedAtMillis)
    }

    @Test
    fun decode_rejectsUnsupportedFutureSchemaVersion() {
        val futureJson = """
            {
              "schemaVersion": 2,
              "exportedAtMillis": 1,
              "app": {
                "profileFormat": "quality-alternative-account-light",
                "packageName": "com.qualityalternative.app",
                "appVersionName": "future",
                "appVersionCode": 99
              },
              "profile": {
                "profileId": "qa-local-00000000-0000-4000-8000-000000000000",
                "createdAtMillis": 1,
                "updatedAtMillis": 1,
                "displayName": null
              },
              "settings": {
                "hasCompletedOnboarding": false,
                "selectedAppPackages": [],
                "preferredTopics": [],
                "preferredDurationBucket": "FOCUS",
                "selectedPackIds": [],
                "themeMode": "LIGHT",
                "meditationDurationMinutes": 3,
                "readerFontScale": 1.0,
                "contentPriority": "BALANCED",
                "priorityContentIds": [],
                "reactivatedCompletedContentIds": [],
                "openAnywayUnlockMinutes": 60
              },
              "library": { "userLinks": [], "userDocuments": [] },
              "reading": { "progress": [] },
              "annotations": {
                "export": { "destinationDisplayName": null, "lastSuccessfulAtMillis": null },
                "driveSync": {
                  "wasEnabledOnSourceDevice": false,
                  "folderDisplayName": null,
                  "lastSuccessfulAtMillis": null
                },
                "sidecarIndex": []
              },
              "sync": {
                "profileAutosave": {
                  "provider": "NONE",
                  "destinationDisplayName": null,
                  "lastSuccessfulAtMillis": null,
                  "activationStateOnImport": "REQUIRES_LOCAL_SELECTION"
                }
              },
              "warnings": []
            }
        """.trimIndent()

        assertThrows(IllegalArgumentException::class.java) {
            AccountLightProfileCodec().decode(futureJson)
        }
        assertThrows(IllegalArgumentException::class.java) {
            AccountLightProfileCodec().decode(futureJson.replace("\"schemaVersion\": 2", "\"schemaVersion\": 0"))
        }
    }

    private fun testDataStore(): DataStore<Preferences> {
        val file = File.createTempFile("account-light-profile-test", ".preferences_pb").apply { deleteOnExit() }
        return PreferenceDataStoreFactory.create(produceFile = { file })
    }

    private class StaticUserLinkRepository(
        private val links: List<ContentItem>,
    ) : UserLinkRepository {
        override fun userLinks(): List<ContentItem> = links

        override suspend fun addLink(draft: UserLinkDraft, nowMillis: Long): AddUserLinkResult =
            error("Not used by exporter tests.")

        override suspend fun markUnavailable(contentId: String, nowMillis: Long) = Unit

        override suspend fun deleteLink(contentId: String) = Unit
    }

    private class StaticUserDocumentRepository(
        private val documents: List<ContentItem>,
    ) : UserDocumentRepository {
        override fun userDocuments(): List<ContentItem> = documents

        override suspend fun addDocument(draft: UserDocumentDraft, nowMillis: Long): AddUserDocumentResult =
            error("Not used by exporter tests.")

        override suspend fun markUnavailable(contentId: String, nowMillis: Long) = Unit

        override suspend fun deleteDocument(contentId: String) = Unit
    }

    private class StaticReadingProgressRepository(
        private val progress: List<ReadingProgress>,
    ) : ReadingProgressRepository {
        override fun readingProgress(): List<ReadingProgress> = progress

        override suspend fun saveProgress(progress: ReadingProgress) = Unit

        override suspend fun deleteProgress(contentId: String) = Unit
    }
}
