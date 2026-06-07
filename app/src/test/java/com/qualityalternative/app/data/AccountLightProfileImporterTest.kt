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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountLightProfileImporterTest {
    @Test
    fun validateImportProfileJson_rejectsMalformedMissingAndUnsupportedProfiles() {
        val repository = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        val importer = AccountLightProfileImporter(settingsRepository = repository)

        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson("{not-json")
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson("""{"schemaVersion":1}""")
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().replace("\"schemaVersion\": 1", "\"schemaVersion\": 99"))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withoutNestedField("app", "profileFormat"))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withoutNestedField("annotations", "export"))
        }
    }

    @Test
    fun validateImportProfileJson_rejectsUnsafeSelectedPackIds() {
        val repository = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        val importer = AccountLightProfileImporter(settingsRepository = repository)

        listOf(
            "content://provider/raw-id",
            "oauth-token-pack",
            "user@example.com",
            "com.google.android.apps.docs",
            "externalstorage-documents",
        ).forEach { unsafePackId ->
            assertThrows(AccountLightImportException::class.java) {
                importer.validateImportProfileJson(validProfileJson().withSelectedPackIds(listOf(unsafePackId)))
            }
        }

        val plan = importer.validateImportProfileJson(
            validProfileJson().withSelectedPackIds(
                listOf("starter_pack", "public-domain-expansion-v2", "attention_practical_agency_v1"),
            ),
        )
        assertEquals(3, plan.preview.importedPackCount)
    }

    @Test
    fun applyMerge_validatesButKeepsLocalPortableSettings() = runBlocking {
        val target = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        target.saveThemeMode(AppThemeMode.LIGHT)
        target.saveContentPriority(ContentPriority.BALANCED)
        val importer = AccountLightProfileImporter(settingsRepository = target)
        val plan = importer.validateImportProfileJson(validProfileJson())

        val result = importer.applyMerge(plan)
        val settings = target.observeAppSettings().first()

        assertEquals(AccountLightImportMode.MERGE, result.mode)
        assertEquals(false, result.settingsApplied)
        assertEquals(AppThemeMode.LIGHT, settings.themeMode)
        assertEquals(ContentPriority.BALANCED, settings.contentPriority)
    }

    @Test
    fun applyReplace_replacesPortableSettingsAndPreservesLocalAnnotationDestinations() = runBlocking {
        val target = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        target.saveAnnotationExportDestination(
            uri = "content://local/export",
            displayName = "local-folder",
        )
        val importer = AccountLightProfileImporter(settingsRepository = target)
        val plan = importer.validateImportProfileJson(validProfileJson())

        val result = importer.applyReplace(plan)
        val settings = target.observeAppSettings().first()
        val identity = target.ensureLocalProfileIdentity(nowMillis = 99_000L)

        assertEquals(AccountLightImportMode.REPLACE, result.mode)
        assertEquals(true, result.settingsApplied)
        assertEquals(true, settings.hasCompletedOnboarding)
        assertEquals(AppThemeMode.DARK, settings.themeMode)
        assertEquals(ContentPriority.MY_FILES, settings.contentPriority)
        assertEquals(1.35, settings.readerFontScale, 0.0)
        assertEquals(1.12, settings.interfaceTextScale, 0.0)
        assertEquals(setOf("starter_pack"), settings.selectedPackIds)
        assertEquals("content://local/export", settings.annotationExportUri)
        assertEquals("local-folder", settings.annotationExportDisplayName)
        assertTrue(identity.profileId.startsWith("qa-local-11111111"))
        assertEquals(10_000L, identity.createdAtMillis)
    }

    @Test
    fun validateImportProfileJson_warnsAndKeepsUnsupportedAppsInactiveOnReplace() = runBlocking {
        val target = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        val importer = AccountLightProfileImporter(settingsRepository = target)
        val plan = importer.validateImportProfileJson(
            validProfileJson().withSelectedAppPackages(
                listOf("com.instagram.android", "com.future.reader", "com.future.reader"),
            ),
        )

        assertEquals(1, plan.preview.unsupportedAppCount)
        assertEquals(2, plan.preview.warningCount)
        assertEquals(
            setOf("DUPLICATE_SCALAR_DEDUPED", "UNSUPPORTED_LOCAL_APP_PACKAGE"),
            plan.allWarnings.mapTo(mutableSetOf()) { it.code },
        )

        importer.applyReplace(plan)
        val settings = target.observeAppSettings().first()

        assertEquals(setOf("com.instagram.android"), settings.selectedAppPackages)
    }

    @Test
    fun validateImportProfileJson_keepsAllMissingAppTargetsInactiveOnReplace() = runBlocking {
        val target = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        val importer = AccountLightProfileImporter(settingsRepository = target)
        val plan = importer.validateImportProfileJson(
            validProfileJson().withSelectedAppPackages(
                listOf("com.future.reader", "com.future.social"),
            ),
        )

        assertEquals(2, plan.preview.unsupportedAppCount)
        assertTrue(plan.allWarnings.any { it.code == "UNSUPPORTED_LOCAL_APP_PACKAGE" })

        importer.applyReplace(plan)
        val settings = target.observeAppSettings().first()

        assertTrue(settings.hasCompletedOnboarding)
        assertEquals(emptySet<String>(), settings.selectedAppPackages)
    }

    @Test
    fun validateImportProfileJson_activatesEligibleCustomAppAndKeepsMissingPackageInactiveOnReplace() = runBlocking {
        val customTarget = DistractingApp(
            packageName = "com.example.deepwork",
            displayName = "Deep Work Trap",
        )
        val target = PreferencesSettingsRepository(
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
        val importer = AccountLightProfileImporter(settingsRepository = target)
        val importedStandardPackage = SupportedCatalog.distractingApps.first().packageName
        val plan = importer.validateImportProfileJson(
            validProfileJson().withSelectedAppPackages(
                listOf(importedStandardPackage, customTarget.packageName, "com.future.reader"),
            ),
        )

        assertEquals(1, plan.preview.unsupportedAppCount)
        assertTrue(plan.allWarnings.any { it.code == "UNSUPPORTED_LOCAL_APP_PACKAGE" })

        importer.applyReplace(plan)
        val settings = target.observeAppSettings().first()

        assertEquals(setOf(importedStandardPackage, customTarget.packageName), settings.selectedAppPackages)
    }

    @Test
    fun validateImportProfileJson_rejectsContentIdTypeMismatchesBeforeMutation() = runBlocking {
        val target = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        target.saveThemeMode(AppThemeMode.DARK)
        val importer = AccountLightProfileImporter(settingsRepository = target)
        val invalidJson = validProfileJson().withUserLinkContentId("user-document-11111111-1111-4111-8111-111111111111")

        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(invalidJson)
        }

        assertEquals(AppThemeMode.DARK, target.observeAppSettings().first().themeMode)
    }

    @Test
    fun validateImportProfileJson_rejectsMalformedLibraryAndReadingEntriesBeforeMutation() = runBlocking {
        val target = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        target.saveContentPriority(ContentPriority.MY_FILES)
        val importer = AccountLightProfileImporter(settingsRepository = target)

        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withMinimalMalformedUserLink())
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withInvalidReadingProgress())
        }

        assertEquals(ContentPriority.MY_FILES, target.observeAppSettings().first().contentPriority)
    }

    @Test
    fun validateImportProfileJson_rejectsUnsafeNestedPortableDataBeforeMutation() = runBlocking {
        val target = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        target.saveThemeMode(AppThemeMode.DARK)
        val importer = AccountLightProfileImporter(settingsRepository = target)

        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withUserLinks(listOf(validUserLink(normalizedUrl = "https://EXAMPLE.com/Essay"))))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(
                validProfileJson().withUserLinks(
                    listOf(
                        validUserLink(
                            normalizedUrl = "https://drive.google.com/file/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/view",
                        ),
                    ),
                ),
            )
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(
                validProfileJson().withUserLinks(
                    listOf(
                        validUserLink(
                            normalizedUrl = "https://user@example.com@example.org/read?oauth_token=abc123",
                        ),
                    ),
                ),
            )
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(
                validProfileJson().withUserLinks(
                    listOf(
                        validUserLink(
                            normalizedUrl = "https://example.com/read?src=file%3A%2F%2F%2Fsdcard%2Fbook.epub",
                        ),
                    ),
                ),
            )
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(
                validProfileJson().withUserLinks(
                    listOf(
                        validUserLink(
                            normalizedUrl = "https://example.com/read?src=file%253A%252F%252F%252Fsdcard%252Fbook.epub",
                        ),
                    ),
                ),
            )
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(
                validProfileJson().withUserLinks(
                    listOf(
                        validUserLink(
                            normalizedUrl = "https://example.com/read?src=file%25252525253A%25252525252F%25252525252F%25252525252Fsdcard%25252525252Fbook.epub",
                        ),
                    ),
                ),
            )
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(
                validProfileJson().withUserLinks(
                    listOf(
                        validUserLink(
                            normalizedUrl = "https://example.com/read?src=content%253A%252F%252Fprovider%252Fraw-id",
                        ),
                    ),
                ),
            )
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(
                validProfileJson().withUserLinks(
                    listOf(
                        validUserLink(
                            normalizedUrl = "https://example.com/read?acct=user%2540example.com",
                        ),
                    ),
                ),
            )
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(
                validProfileJson().withUserLinks(
                    listOf(
                        validUserLink(
                            normalizedUrl = "https://example.com/read?doc=primary%3ADownload%2Fbook.epub",
                        ),
                    ),
                ),
            )
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(
                validProfileJson().withUserLinks(
                    listOf(
                        validUserLink(
                            normalizedUrl = "https://example.com/read?doc=storage%2Femulated%2F0%2FDownload%2Fbook.epub",
                        ),
                    ),
                ),
            )
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(
                validProfileJson().withUserLinks(
                    listOf(
                        validUserLink(
                            normalizedUrl = "https://example.com/read?doc=storage%2Femulated%2F0%2FDownload%2Fbook",
                        ),
                    ),
                ),
            )
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(
                validProfileJson().withUserLinks(
                    listOf(
                        validUserLink(
                            normalizedUrl = "https://example.com/read?doc=storage%2Femulated%2F0%2FDownload%2Fbook.docx",
                        ),
                    ),
                ),
            )
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(
                validProfileJson().withUserLinks(
                    listOf(
                        validUserLink(
                            normalizedUrl = "https://example.com/read?doc=storage%252Femulated%252F0%252FDownload%252Fbook%25ZZ",
                        ),
                    ),
                ),
            )
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(
                validProfileJson().withUserLinks(
                    listOf(
                        validUserLink(
                            normalizedUrl = "https://example.com/read?src=file%253A%252F%252F%252Fsdcard%252Fbook%25ZZ",
                        ),
                    ),
                ),
            )
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(
                validProfileJson().withUserLinks(
                    listOf(
                        validUserLink(
                            normalizedUrl = "https://example.com/read?image=image%3A3952",
                        ),
                    ),
                ),
            )
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(
                validProfileJson().withUserLinks(
                    listOf(
                        validUserLink(
                            normalizedUrl = "https://example.com/read?doc=msf%3A29",
                        ),
                    ),
                ),
            )
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(
                validProfileJson().withUserLinks(
                    listOf(
                        validUserLink(
                            normalizedUrl = "https://example.com/com.dropbox.android.FileProvider/document/raw",
                        ),
                    ),
                ),
            )
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withUserLinks(listOf(validUserLink(title = "content://com.android.providers.media.documents/document/1"))))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withUserLinks(listOf(validUserLink(title = "com.dropbox.android.FileProvider"))))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withUserLinks(listOf(validUserLink(title = "L".repeat(201)))))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withUserLinks(listOf(validUserLink(sourceLabel = "s".repeat(121)))))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withUserLinks(listOf(validUserLink(description = "Stored from content://provider/raw-id"))))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withUserLinks(listOf(validUserLink(description = "User email user@example.com was present"))))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(
                validProfileJson().withUserLinks(
                    listOf(
                        validUserLink(
                            description = "Drive file id 1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms",
                        ),
                    ),
                ),
            )
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withUserDocuments(listOf(validUserDocument(fingerprintStrategy = "SHA256_BYTES", sha256 = null, sizeBytes = JsonPrimitive(1234L)))))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withUserDocuments(listOf(validUserDocument(mimeType = "text/plain; src=content://provider/raw-id"))))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withUserDocuments(listOf(validUserDocument(mimeType = "application/vnd.com.dropbox.android.FileProvider"))))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withUserDocuments(listOf(validUserDocument(sourceDisplayName = "content://provider/raw-id"))))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withUserDocuments(listOf(validUserDocument(displayName = "file:///tmp/book.epub"))))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withUserDocuments(listOf(validUserDocument(title = "/Users/me/book.epub"))))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withUserDocuments(listOf(validUserDocument(title = "mailto:user@example.com"))))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withUserDocuments(listOf(validUserDocument(title = "content:com.android.providers.media.documents"))))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withUserDocuments(listOf(validUserDocument(title = "D".repeat(201)))))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withUserDocuments(listOf(validUserDocument(description = "OAuth token copied from device"))))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withUserDocuments(listOf(validUserDocument(description = "Drive file id com.google.android.apps.docs.storage"))))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(
                validProfileJson().withUserDocuments(
                    listOf(
                        validUserDocument(
                            description = "Opaque source id 1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms",
                        ),
                    ),
                ),
            )
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withUserDocuments(listOf(validUserDocument(providerLabel = "com.google.android.apps.docs.storage"))))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withUserDocuments(listOf(validUserDocument(normalizedTitle = "Imported  Book.epub"))))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withUserDocuments(listOf(validUserDocument(normalizedTitle = " imported book "))))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withUserDocuments(listOf(validUserDocument(normalizedTitle = "imported book.html"))))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(
                validProfileJson().withUserDocuments(
                    listOf(
                        validUserDocument(
                            fingerprintStrategy = "UNVERIFIED_METADATA_ONLY",
                            sha256 = null,
                            sizeBytes = JsonNull,
                            documentImportState = "AVAILABLE_ON_THIS_DEVICE",
                        ),
                    ),
                ),
            )
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withReadingProgressFor(contentId = "user-link-33333333-3333-4333-8333-333333333333"))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withPriorityContentIds(listOf("editorial-not-in-this-build")))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withUserLinks(listOf(validUserLink(durationMinutes = JsonPrimitive("12")))))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withUserDocuments(listOf(validUserDocument(createdAtMillis = JsonPrimitive("10000")))))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withUserDocuments(listOf(validUserDocument(sizeBytes = JsonPrimitive("1234")))))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withReadingProgressPercent(JsonPrimitive("40")))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withSchemaVersion(JsonPrimitive("1")))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withWarningMessage("com.android.providers.media.documents was omitted"))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withWarningMessage("com.dropbox.android.FileProvider was omitted"))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withWarningMessage("java.lang.IllegalStateException at MainViewModel.kt line 42"))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withWarningMessage("IllegalStateException"))
        }

        assertEquals(AppThemeMode.DARK, target.observeAppSettings().first().themeMode)
    }

    @Test
    fun validateImportProfileJson_acceptsPortableAnnotationSidecarFileName() = runBlocking {
        val target = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        val importer = AccountLightProfileImporter(settingsRepository = target)

        val plan = importer.validateImportProfileJson(
            validProfileJson().withAnnotationSidecarFileName("book.annotations.jsonld"),
        )

        assertEquals(0, plan.preview.warningCount)
    }

    @Test
    fun validateImportProfileJson_acceptsReadingProgressForImportedLibraryContent() = runBlocking {
        val target = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        val importer = AccountLightProfileImporter(settingsRepository = target)
        val contentId = "user-link-33333333-3333-4333-8333-333333333333"

        val plan = importer.validateImportProfileJson(
            validProfileJson()
                .withUserLinks(listOf(validUserLink(contentId = contentId)))
                .withReadingProgressFor(contentId = contentId),
        )

        assertEquals(0, plan.preview.warningCount)
    }

    @Test
    fun applyMerge_importsLibraryMarksDocumentsMissingAndKeepsMissingDocumentProgressDormant() = runBlocking {
        val target = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        val linkRepository = RecordingUserLinkRepository()
        val documentRepository = RecordingUserDocumentRepository()
        val readingProgressRepository = RecordingReadingProgressRepository()
        val linkContentId = "user-link-33333333-3333-4333-8333-333333333333"
        val documentContentId = "user-document-44444444-4444-4444-8444-444444444444"
        val importer = AccountLightProfileImporter(
            settingsRepository = target,
            userLinkRepository = linkRepository,
            userDocumentRepository = documentRepository,
            readingProgressRepository = readingProgressRepository,
        )
        val plan = importer.validateImportProfileJson(
            validProfileJson()
                .withUserLinks(listOf(validUserLink(contentId = linkContentId)))
                .withUserDocuments(listOf(validUserDocument(contentId = documentContentId)))
                .withReadingProgressEntries(
                    listOf(
                        readingProgressJson(contentId = linkContentId),
                        readingProgressJson(contentId = documentContentId, progressPercent = 64),
                    ),
                ),
        )

        assertEquals(1, plan.preview.importedLinkCount)
        assertEquals(1, plan.preview.importedDocumentCount)
        assertEquals(2, plan.preview.importedProgressCount)
        assertEquals(1, plan.preview.missingDocumentCount)

        importer.applyMerge(plan)

        assertEquals(linkContentId, linkRepository.links.single().id)
        val importedDocument = documentRepository.documents.single()
        assertEquals(documentContentId, importedDocument.id)
        assertEquals(ContentAvailability.UNAVAILABLE, importedDocument.availability)
        assertTrue(importedDocument.sourceLabel.orEmpty().contains("(missing)"))
        assertEquals(
            setOf(linkContentId, documentContentId),
            readingProgressRepository.progress.mapTo(mutableSetOf(), ReadingProgress::contentId),
        )
        assertEquals(
            64,
            readingProgressRepository.progress.single { progress -> progress.contentId == documentContentId }
                .progressPercent,
        )
    }

    @Test
    fun applyMergeStopsBeforeProgressWhenPortableContentIdsConflict() = runBlocking {
        val target = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        val readingProgressRepository = RecordingReadingProgressRepository()
        val linkContentId = "user-link-33333333-3333-4333-8333-333333333333"
        val importer = AccountLightProfileImporter(
            settingsRepository = target,
            userLinkRepository = ConflictingUserLinkRepository(),
            readingProgressRepository = readingProgressRepository,
        )
        val plan = importer.validateImportProfileJson(
            validProfileJson()
                .withUserLinks(listOf(validUserLink(contentId = linkContentId)))
                .withReadingProgressFor(contentId = linkContentId),
        )

        val error = assertThrows(AccountLightImportException::class.java) {
            runBlocking { importer.applyMerge(plan) }
        }

        assertEquals(AccountLightImportErrorCode.CONTENT_ID_SECONDARY_KEY_CONFLICT, error.code)
        assertTrue(readingProgressRepository.progress.isEmpty())
    }

    @Test
    fun applyMergeStoresProgressOnlyForAcceptedImportedContentIds() = runBlocking {
        val target = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        val linkRepository = RejectingImportedUserLinkRepository()
        val readingProgressRepository = RecordingReadingProgressRepository()
        val linkContentId = "user-link-33333333-3333-4333-8333-333333333333"
        val importer = AccountLightProfileImporter(
            settingsRepository = target,
            userLinkRepository = linkRepository,
            readingProgressRepository = readingProgressRepository,
        )
        val plan = importer.validateImportProfileJson(
            validProfileJson()
                .withUserLinks(listOf(validUserLink(contentId = linkContentId)))
                .withReadingProgressFor(contentId = linkContentId),
        )

        importer.applyMerge(plan)

        assertEquals(listOf(linkContentId), linkRepository.seenContentIds)
        assertTrue(readingProgressRepository.progress.isEmpty())
    }

    @Test
    fun applyMergeMapsSecondaryOnlyLinkProgressToLocalContentId() = runBlocking {
        val target = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        val localContentId = "user-link-11111111-1111-4111-8111-111111111111"
        val importedContentId = "user-link-33333333-3333-4333-8333-333333333333"
        val sharedUrl = "https://example.com/essay"
        val linkRepository = RecordingUserLinkRepository().apply {
            links = listOf(userLinkContent(id = localContentId, url = sharedUrl))
        }
        val readingProgressRepository = RecordingReadingProgressRepository()
        val importer = AccountLightProfileImporter(
            settingsRepository = target,
            userLinkRepository = linkRepository,
            readingProgressRepository = readingProgressRepository,
        )
        val plan = importer.validateImportProfileJson(
            validProfileJson()
                .withUserLinks(
                    listOf(
                        validUserLink(
                            contentId = importedContentId,
                            normalizedUrl = sharedUrl,
                        ),
                    ),
                )
                .withReadingProgressFor(contentId = importedContentId),
        )

        assertTrue(plan.generatedWarnings.map(AccountLightWarning::code).contains("CONFLICT_RETAINED_LOCAL_VALUE"))

        importer.applyMerge(plan)

        assertEquals(listOf(localContentId), linkRepository.links.map(ContentItem::id))
        assertEquals(listOf(localContentId), readingProgressRepository.progress.map(ReadingProgress::contentId))
    }

    @Test
    fun applyMergeMapsFingerprintOnlyDocumentProgressToLocalContentId() = runBlocking {
        val target = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        val localContentId = "user-document-11111111-1111-4111-8111-111111111111"
        val importedContentId = "user-document-44444444-4444-4444-8444-444444444444"
        val sharedFingerprint = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        val documentRepository = RecordingUserDocumentRepository().apply {
            documents = listOf(userDocumentContent(id = localContentId, fingerprintSha256 = sharedFingerprint))
        }
        val readingProgressRepository = RecordingReadingProgressRepository()
        val importer = AccountLightProfileImporter(
            settingsRepository = target,
            userDocumentRepository = documentRepository,
            readingProgressRepository = readingProgressRepository,
        )
        val plan = importer.validateImportProfileJson(
            validProfileJson()
                .withUserDocuments(
                    listOf(
                        validUserDocument(
                            contentId = importedContentId,
                            sha256 = sharedFingerprint,
                        ),
                    ),
                )
                .withReadingProgressFor(contentId = importedContentId),
        )

        assertTrue(plan.generatedWarnings.map(AccountLightWarning::code).contains("CONFLICT_RETAINED_LOCAL_VALUE"))

        importer.applyMerge(plan)

        assertEquals(listOf(localContentId), documentRepository.documents.map(ContentItem::id))
        assertEquals(listOf(localContentId), readingProgressRepository.progress.map(ReadingProgress::contentId))
    }

    @Test
    fun validateImportProfileJsonPreflightsWholeLibraryBeforeAnyMutation() = runBlocking {
        val target = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        val linkRepository = RecordingUserLinkRepository()
        val localDocumentById = userDocumentContent(
            id = "user-document-11111111-1111-4111-8111-111111111111",
            fingerprintSha256 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
        )
        val localDocumentByFingerprint = userDocumentContent(
            id = "user-document-22222222-2222-4222-8222-222222222222",
            fingerprintSha256 = "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
        )
        val documentRepository = RecordingUserDocumentRepository().apply {
            documents = listOf(localDocumentById, localDocumentByFingerprint)
        }
        val importer = AccountLightProfileImporter(
            settingsRepository = target,
            userLinkRepository = linkRepository,
            userDocumentRepository = documentRepository,
        )

        val error = assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(
                validProfileJson()
                    .withUserLinks(listOf(validUserLink()))
                    .withUserDocuments(
                        listOf(
                            validUserDocument(
                                contentId = localDocumentById.id,
                                sha256 = localDocumentByFingerprint.documentFingerprintSha256,
                            ),
                        ),
                    ),
            )
        }

        assertEquals(AccountLightImportErrorCode.CONTENT_ID_SECONDARY_KEY_CONFLICT, error.code)
        assertEquals(0, linkRepository.importCallCount)
        assertTrue(linkRepository.links.isEmpty())
    }

    @Test
    fun exportedVerifiedDocumentFingerprintReconcilesCrossDeviceImport() = runBlocking {
        val sharedFingerprint = "cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc"
        val exportedContentId = "user-document-33333333-3333-4333-8333-333333333333"
        val localContentId = "user-document-44444444-4444-4444-8444-444444444444"
        val sourceSettings = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        val exportedDocument = userDocumentContent(
            id = exportedContentId,
            fingerprintSha256 = sharedFingerprint,
        )
        val exportedJson = AccountLightProfileExporter(
            settingsRepository = sourceSettings,
            appVersionName = "0.8.1-alpha",
            appVersionCode = 13,
            userDocumentRepository = StaticUserDocumentRepository(listOf(exportedDocument)),
            readingProgressRepository = StaticReadingProgressRepository(
                listOf(
                    ReadingProgress(
                        contentId = exportedContentId,
                        progressPercent = 55,
                        lastVisibleParagraphIndex = 5,
                        paragraphCount = 12,
                        updatedAtMillis = 30_000L,
                    ),
                ),
            ),
        ).exportSettingsOnlyProfileJson(nowMillis = 40_000L)
        val exportedProfile = AccountLightProfileCodec().decode(exportedJson)
        val portableDocument = exportedProfile.library.userDocuments.single()
        assertEquals("SHA256_BYTES", portableDocument.documentFingerprint.strategy)
        assertEquals(sharedFingerprint, portableDocument.documentFingerprint.sha256)
        assertEquals(1_024L, portableDocument.documentFingerprint.sizeBytes)

        val targetSettings = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        val documentRepository = RecordingUserDocumentRepository().apply {
            documents = listOf(userDocumentContent(id = localContentId, fingerprintSha256 = sharedFingerprint))
        }
        val readingProgressRepository = RecordingReadingProgressRepository()
        val importer = AccountLightProfileImporter(
            settingsRepository = targetSettings,
            userDocumentRepository = documentRepository,
            readingProgressRepository = readingProgressRepository,
        )

        val plan = importer.validateImportProfileJson(exportedJson)
        assertTrue(plan.generatedWarnings.map(AccountLightWarning::code).contains("CONFLICT_RETAINED_LOCAL_VALUE"))

        importer.applyMerge(plan)

        assertEquals(listOf(localContentId), documentRepository.documents.map(ContentItem::id))
        assertEquals(listOf(localContentId), readingProgressRepository.progress.map(ReadingProgress::contentId))
    }

    @Test
    fun applyReplace_restoresLibraryWhenProgressImportFails() = runBlocking {
        val target = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        val linkRepository = RecordingUserLinkRepository()
        val documentRepository = RecordingUserDocumentRepository()
        val readingProgressRepository = FailingOnceReadingProgressRepository()
        val importer = AccountLightProfileImporter(
            settingsRepository = target,
            userLinkRepository = linkRepository,
            userDocumentRepository = documentRepository,
            readingProgressRepository = readingProgressRepository,
        )
        val linkContentId = "user-link-33333333-3333-4333-8333-333333333333"
        val documentContentId = "user-document-44444444-4444-4444-8444-444444444444"
        val plan = importer.validateImportProfileJson(
            validProfileJson()
                .withUserLinks(listOf(validUserLink(contentId = linkContentId)))
                .withUserDocuments(listOf(validUserDocument(contentId = documentContentId)))
                .withReadingProgressFor(contentId = documentContentId),
        )

        assertThrows(IllegalStateException::class.java) {
            runBlocking { importer.applyReplace(plan) }
        }

        assertTrue(linkRepository.links.isEmpty())
        assertTrue(documentRepository.documents.isEmpty())
        assertTrue(readingProgressRepository.progress.isEmpty())
    }

    @Test
    fun validateImportProfileJson_reportsUnknownNestedFieldsAsWarnings() = runBlocking {
        val target = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        val importer = AccountLightProfileImporter(settingsRepository = target)

        val plan = importer.validateImportProfileJson(
            validProfileJson()
                .withUnknownSettingsField()
                .withUnknownSyncAutosaveField()
                .withUnknownAnnotationExportField(),
        )

        val warningMessages = plan.allWarnings
            .filter { it.code == "UNKNOWN_FIELD_IGNORED" }
            .mapNotNull { it.message }
        assertTrue(warningMessages.any { it == "An unknown settings field was ignored" })
        assertTrue(warningMessages.any { it == "An unknown sync field was ignored" })
        assertTrue(warningMessages.any { it == "An unknown annotations field was ignored" })
        assertTrue(warningMessages.none { it.contains(".") })
    }

    @Test
    fun validateImportProfileJson_sanitizesDottedUnknownFieldSections() = runBlocking {
        val target = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        val importer = AccountLightProfileImporter(settingsRepository = target)
        val linkContentId = "user-link-33333333-3333-4333-8333-333333333333"

        val plan = importer.validateImportProfileJson(
            validProfileJson()
                .withUserLinks(listOf(validUserLink(contentId = linkContentId).withUnknownField()))
                .withUserDocuments(listOf(validUserDocument().withUnknownField()))
                .withReadingProgressFor(contentId = linkContentId)
                .withUnknownReadingProgressField(),
        )

        val warningMessages = plan.allWarnings
            .filter { it.code == "UNKNOWN_FIELD_IGNORED" }
            .mapNotNull { it.message }
        assertTrue(warningMessages.any { it == "An unknown saved link field was ignored" })
        assertTrue(warningMessages.any { it == "An unknown document field was ignored" })
        assertTrue(warningMessages.any { it == "An unknown reading progress field was ignored" })
        assertTrue(warningMessages.none { it.contains(".") })
    }

    @Test
    fun validateImportProfileJson_acceptsReadingProgressTextOffsetWithoutUnknownWarning() = runBlocking {
        val target = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        val importer = AccountLightProfileImporter(settingsRepository = target)
        val linkContentId = "user-link-33333333-3333-4333-8333-333333333333"

        val plan = importer.validateImportProfileJson(
            validProfileJson()
                .withUserLinks(listOf(validUserLink(contentId = linkContentId)))
                .withReadingProgress(
                    contentId = linkContentId,
                    progressPercent = JsonPrimitive(40),
                    lastVisibleParagraphIndex = 4,
                    lastVisibleTextOffset = 77,
                    paragraphCount = 12,
                    completedAtMillis = JsonNull,
                ),
        )

        val progress = plan.profile.reading.progress.single()
        val warningMessages = plan.allWarnings
            .filter { it.code == "UNKNOWN_FIELD_IGNORED" }
            .mapNotNull { it.message }
        assertEquals(77, progress.lastVisibleTextOffset)
        assertTrue(warningMessages.none { it == "An unknown reading progress field was ignored" })
    }

    private fun validProfileJson(): String = runBlocking {
        val repository = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        repository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = SupportedCatalog.distractingApps.take(3).mapTo(mutableSetOf()) { it.packageName },
                preferredTopics = setOf(TopicTag.SCIENCE, TopicTag.PHILOSOPHY, TopicTag.HISTORY),
                preferredDurationBucket = DurationBucket.DEEP,
                selectedPackIds = setOf("starter_pack"),
            ),
        )
        repository.saveThemeMode(AppThemeMode.DARK)
        repository.saveContentPriority(ContentPriority.MY_FILES)
        repository.saveReaderFontScale(1.35)
        repository.saveInterfaceTextScale(1.12)
        repository.replacePortableSettings(
            settings = repository.observeAppSettings().first(),
            profileIdentity = com.qualityalternative.app.domain.model.LocalProfileIdentity(
                profileId = "qa-local-11111111-1111-4111-8111-111111111111",
                createdAtMillis = 10_000L,
            ),
        )
        AccountLightProfileExporter(
            settingsRepository = repository,
            appVersionName = "0.8.1-alpha",
            appVersionCode = 13,
        ).exportSettingsOnlyProfileJson(nowMillis = 20_000L)
    }

    private fun String.withSelectedAppPackages(packages: List<String>): String {
        val root = Json.parseToJsonElement(this).jsonObject
        val settings = root.getValue("settings").jsonObject
        return JsonObject(
            root + (
                "settings" to JsonObject(
                    settings + ("selectedAppPackages" to JsonArray(packages.map(::JsonPrimitive))),
                )
                ),
        ).toString()
    }

    private fun String.withSelectedPackIds(packIds: List<String>): String {
        val root = Json.parseToJsonElement(this).jsonObject
        val settings = root.getValue("settings").jsonObject
        return JsonObject(
            root + (
                "settings" to JsonObject(
                    settings + ("selectedPackIds" to JsonArray(packIds.map(::JsonPrimitive))),
                )
                ),
        ).toString()
    }

    private fun String.withSchemaVersion(schemaVersion: JsonElement): String {
        val root = Json.parseToJsonElement(this).jsonObject
        return JsonObject(root + ("schemaVersion" to schemaVersion)).toString()
    }

    private fun String.withUserLinkContentId(contentId: String): String {
        val root = Json.parseToJsonElement(this).jsonObject
        val library = root.getValue("library").jsonObject
        return JsonObject(
            root + (
                "library" to JsonObject(
                    library + (
                        "userLinks" to JsonArray(
                            listOf(JsonObject(mapOf("contentId" to JsonPrimitive(contentId)))),
                        )
                        ),
                )
                ),
        ).toString()
    }

    private fun String.withoutNestedField(section: String, field: String): String {
        val root = Json.parseToJsonElement(this).jsonObject
        val nested = root.getValue(section).jsonObject
        return JsonObject(root + (section to JsonObject(nested - field))).toString()
    }

    private fun String.withUnknownSettingsField(): String {
        val root = Json.parseToJsonElement(this).jsonObject
        val settings = root.getValue("settings").jsonObject
        return JsonObject(
            root + (
                "settings" to JsonObject(
                    settings + ("unexpectedReaderThing" to JsonPrimitive("ignored")),
                )
                ),
        ).toString()
    }

    private fun String.withUnknownSyncAutosaveField(): String {
        val root = Json.parseToJsonElement(this).jsonObject
        val sync = root.getValue("sync").jsonObject
        val profileAutosave = sync.getValue("profileAutosave").jsonObject
        return JsonObject(
            root + (
                "sync" to JsonObject(
                    sync + (
                        "profileAutosave" to JsonObject(
                            profileAutosave + ("someFutureField" to JsonPrimitive("ignored")),
                        )
                        ),
                )
                ),
        ).toString()
    }

    private fun String.withUnknownAnnotationExportField(): String {
        val root = Json.parseToJsonElement(this).jsonObject
        val annotations = root.getValue("annotations").jsonObject
        val export = annotations.getValue("export").jsonObject
        return JsonObject(
            root + (
                "annotations" to JsonObject(
                    annotations + (
                        "export" to JsonObject(
                            export + ("someFutureField" to JsonPrimitive("ignored")),
                        )
                        ),
                )
                ),
        ).toString()
    }

    private fun String.withMinimalMalformedUserLink(): String {
        val root = Json.parseToJsonElement(this).jsonObject
        val library = root.getValue("library").jsonObject
        return JsonObject(
            root + (
                "library" to JsonObject(
                    library + (
                        "userLinks" to JsonArray(
                            listOf(
                                JsonObject(
                                    mapOf(
                                        "contentId" to JsonPrimitive("user-link-11111111-1111-4111-8111-111111111111"),
                                    ),
                                ),
                            ),
                        )
                        ),
                )
                ),
        ).toString()
    }

    private fun String.withUserLinks(links: List<JsonObject>): String {
        val root = Json.parseToJsonElement(this).jsonObject
        val library = root.getValue("library").jsonObject
        return JsonObject(
            root + (
                "library" to JsonObject(
                    library + ("userLinks" to JsonArray(links)),
                )
                ),
        ).toString()
    }

    private fun String.withUserDocuments(documents: List<JsonObject>): String {
        val root = Json.parseToJsonElement(this).jsonObject
        val library = root.getValue("library").jsonObject
        return JsonObject(
            root + (
                "library" to JsonObject(
                    library + ("userDocuments" to JsonArray(documents)),
                )
                ),
        ).toString()
    }

    private fun String.withPriorityContentIds(contentIds: List<String>): String {
        val root = Json.parseToJsonElement(this).jsonObject
        val settings = root.getValue("settings").jsonObject
        return JsonObject(
            root + (
                "settings" to JsonObject(
                    settings + ("priorityContentIds" to JsonArray(contentIds.map(::JsonPrimitive))),
                )
                ),
        ).toString()
    }

    private fun String.withWarningMessage(message: String): String {
        val root = Json.parseToJsonElement(this).jsonObject
        return JsonObject(
            root + (
                "warnings" to JsonArray(
                    listOf(
                        JsonObject(
                            mapOf(
                                "code" to JsonPrimitive("UNKNOWN_FIELD_IGNORED"),
                                "severity" to JsonPrimitive("WARNING"),
                                "section" to JsonPrimitive("unknown"),
                                "contentId" to JsonNull,
                                "message" to JsonPrimitive(message),
                            ),
                        ),
                    ),
                )
                ),
        ).toString()
    }

    private fun String.withAnnotationSidecarFileName(fileName: String): String {
        val root = Json.parseToJsonElement(this).jsonObject
        val annotations = root.getValue("annotations").jsonObject
        return JsonObject(
            root + (
                "annotations" to JsonObject(
                    annotations + (
                        "sidecarIndex" to JsonArray(
                            listOf(
                                JsonObject(
                                    mapOf(
                                        "contentId" to JsonPrimitive("user-document-44444444-4444-4444-8444-444444444444"),
                                        "sourceTitle" to JsonPrimitive("Imported book"),
                                        "jsonLdFileName" to JsonPrimitive(fileName),
                                        "sha256" to JsonNull,
                                        "updatedAtMillis" to JsonPrimitive(20_000L),
                                    ),
                                ),
                            ),
                        )
                        ),
                )
                ),
        ).toString()
    }

    private fun String.withInvalidReadingProgress(): String {
        return withReadingProgress(
            contentId = "editorial-attention-reset",
            progressPercent = JsonPrimitive(40),
            lastVisibleParagraphIndex = 5,
            paragraphCount = 5,
            completedAtMillis = JsonNull,
        )
    }

    private fun String.withReadingProgressFor(contentId: String): String {
        return withReadingProgress(
            contentId = contentId,
            progressPercent = JsonPrimitive(40),
            lastVisibleParagraphIndex = 4,
            paragraphCount = 12,
            completedAtMillis = JsonNull,
        )
    }

    private fun String.withReadingProgressEntries(progressEntries: List<JsonObject>): String {
        val root = Json.parseToJsonElement(this).jsonObject
        val reading = root.getValue("reading").jsonObject
        return JsonObject(
            root + (
                "reading" to JsonObject(
                    reading + ("progress" to JsonArray(progressEntries)),
                )
                ),
        ).toString()
    }

    private fun readingProgressJson(
        contentId: String,
        progressPercent: Int = 40,
    ): JsonObject {
        return JsonObject(
            mapOf(
                "contentId" to JsonPrimitive(contentId),
                "progressPercent" to JsonPrimitive(progressPercent),
                "lastVisibleParagraphIndex" to JsonPrimitive(4),
                "paragraphCount" to JsonPrimitive(12),
                "updatedAtMillis" to JsonPrimitive(20_000L),
                "completedAtMillis" to JsonNull,
            ),
        )
    }

    private fun String.withReadingProgressPercent(progressPercent: JsonElement): String {
        return withReadingProgress(
            contentId = "editorial-attention-reset",
            progressPercent = progressPercent,
            lastVisibleParagraphIndex = 4,
            paragraphCount = 12,
            completedAtMillis = JsonNull,
        )
    }

    private fun String.withUnknownReadingProgressField(): String {
        val root = Json.parseToJsonElement(this).jsonObject
        val reading = root.getValue("reading").jsonObject
        val progress = reading.getValue("progress").jsonArray
        val firstProgress = progress.first().jsonObject
        return JsonObject(
            root + (
                "reading" to JsonObject(
                    reading + (
                        "progress" to JsonArray(
                            listOf(
                                JsonObject(firstProgress + ("someFutureField" to JsonPrimitive("ignored"))),
                            ),
                        )
                        ),
                )
                ),
        ).toString()
    }

    private fun String.withReadingProgress(
        contentId: String,
        progressPercent: JsonElement,
        lastVisibleParagraphIndex: Int,
        lastVisibleTextOffset: Int? = null,
        paragraphCount: Int,
        completedAtMillis: JsonElement,
    ): String {
        val root = Json.parseToJsonElement(this).jsonObject
        val reading = root.getValue("reading").jsonObject
        val fields = mutableMapOf<String, JsonElement>(
            "contentId" to JsonPrimitive(contentId),
            "progressPercent" to progressPercent,
            "lastVisibleParagraphIndex" to JsonPrimitive(lastVisibleParagraphIndex),
            "paragraphCount" to JsonPrimitive(paragraphCount),
            "updatedAtMillis" to JsonPrimitive(20_000L),
            "completedAtMillis" to completedAtMillis,
        )
        lastVisibleTextOffset?.let { fields["lastVisibleTextOffset"] = JsonPrimitive(it) }
        return JsonObject(
            root + (
                "reading" to JsonObject(
                    reading + (
                        "progress" to JsonArray(
                            listOf(
                                JsonObject(fields),
                            ),
                        )
                        ),
                )
                ),
        ).toString()
    }

    private fun validUserLink(
        contentId: String = "user-link-33333333-3333-4333-8333-333333333333",
        normalizedUrl: String = "https://example.com/essay",
        title: String = "Imported essay",
        description: String = "Saved link from another device.",
        durationMinutes: JsonElement = JsonPrimitive(12),
        sourceLabel: String? = null,
    ): JsonObject {
        return JsonObject(
            mapOf(
                "contentId" to JsonPrimitive(contentId),
                "normalizedUrl" to JsonPrimitive(normalizedUrl),
                "title" to JsonPrimitive(title),
                "description" to JsonPrimitive(description),
                "durationMinutes" to durationMinutes,
                "topicTags" to JsonArray(listOf(JsonPrimitive("SCIENCE"))),
                "availability" to JsonPrimitive("AVAILABLE"),
                "createdAtMillis" to JsonPrimitive(10_000L),
                "updatedAtMillis" to JsonPrimitive(20_000L),
                "sourceLabel" to (sourceLabel?.let(::JsonPrimitive) ?: JsonNull),
            ),
        )
    }

    private fun JsonObject.withUnknownField(): JsonObject {
        return JsonObject(this + ("someFutureField" to JsonPrimitive("ignored")))
    }

    private fun validUserDocument(
        contentId: String = "user-document-44444444-4444-4444-8444-444444444444",
        displayName: String = "book.epub",
        title: String = "Imported book",
        description: String = "Saved document metadata.",
        fingerprintStrategy: String = "SHA256_BYTES",
        sha256: String? = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
        sizeBytes: JsonElement = JsonPrimitive(1234L),
        normalizedTitle: String = "imported book",
        documentImportState: String = "MISSING_FILE_NEEDS_REATTACH",
        sourceDisplayName: String? = "book.epub",
        providerLabel: String? = null,
        createdAtMillis: JsonElement = JsonPrimitive(10_000L),
        mimeType: String = "application/epub+zip",
    ): JsonObject {
        return JsonObject(
            mapOf(
                "contentId" to JsonPrimitive(contentId),
                "displayName" to JsonPrimitive(displayName),
                "mimeType" to JsonPrimitive(mimeType),
                "documentFormat" to JsonPrimitive("EPUB"),
                "title" to JsonPrimitive(title),
                "description" to JsonPrimitive(description),
                "durationMinutes" to JsonPrimitive(45),
                "topicTags" to JsonArray(listOf(JsonPrimitive("PHILOSOPHY"))),
                "availability" to JsonPrimitive("UNAVAILABLE"),
                "documentImportState" to JsonPrimitive(documentImportState),
                "documentFingerprint" to JsonObject(
                    mapOf(
                        "strategy" to JsonPrimitive(fingerprintStrategy),
                        "sha256" to (sha256?.let(::JsonPrimitive) ?: JsonNull),
                        "sizeBytes" to sizeBytes,
                        "normalizedTitle" to JsonPrimitive(normalizedTitle),
                        "format" to JsonPrimitive("EPUB"),
                    ),
                ),
                "createdAtMillis" to createdAtMillis,
                "updatedAtMillis" to JsonPrimitive(20_000L),
                "sourceHint" to JsonObject(
                    mapOf(
                        "lastKnownDisplayName" to (sourceDisplayName?.let(::JsonPrimitive) ?: JsonNull),
                        "providerLabel" to (providerLabel?.let(::JsonPrimitive) ?: JsonNull),
                    ),
                ),
            ),
        )
    }

    private fun userLinkContent(
        id: String,
        url: String,
    ): ContentItem {
        return ContentItem(
            id = id,
            packId = "user-links",
            title = "Local link",
            description = "Local link metadata.",
            durationMinutes = 12,
            format = ContentFormat.HTML,
            topicTags = setOf(TopicTag.SCIENCE),
            externalUrl = url,
            sourceType = ContentSourceType.USER_LINK,
            availability = ContentAvailability.NEEDS_FALLBACK,
            rights = ContentRightsMetadata.userPrivateExternal(sourceUrl = url),
            addedAtMillis = 10_000L,
        )
    }

    private fun userDocumentContent(
        id: String,
        fingerprintSha256: String,
    ): ContentItem {
        val sourceReference = "portable-missing:$id"
        return ContentItem(
            id = id,
            packId = "user-documents",
            title = "Local document",
            description = "Local document metadata.",
            durationMinutes = 45,
            format = ContentFormat.EPUB,
            topicTags = setOf(TopicTag.PHILOSOPHY),
            sourceLabel = "book.epub",
            sourceType = ContentSourceType.USER_DOCUMENT,
            availability = ContentAvailability.AVAILABLE,
            rights = ContentRightsMetadata.userPrivateReader(
                sourceUrl = sourceReference,
                attribution = "book.epub",
            ),
            addedAtMillis = 10_000L,
            documentFingerprintSha256 = fingerprintSha256,
            documentFingerprintSizeBytes = 1_024L,
        )
    }

    private fun testDataStore(): DataStore<Preferences> {
        val file = File.createTempFile("account-light-importer-test", ".preferences_pb").apply { deleteOnExit() }
        return PreferenceDataStoreFactory.create(produceFile = { file })
    }

    private class ConflictingUserLinkRepository : UserLinkRepository {
        override fun userLinks(): List<ContentItem> = emptyList()

        override suspend fun addLink(draft: UserLinkDraft, nowMillis: Long): AddUserLinkResult =
            error("Not used by portable import tests.")

        override suspend fun markUnavailable(contentId: String, nowMillis: Long) = Unit

        override suspend fun deleteLink(contentId: String) = Unit

        override suspend fun importPortableLinks(
            links: List<ContentItem>,
            replaceExisting: Boolean,
            nowMillis: Long,
        ): Set<String> {
            throw PortableContentImportConflictException("Simulated contentId secondary key conflict.")
        }
    }

    private class RejectingImportedUserLinkRepository : UserLinkRepository {
        var seenContentIds: List<String> = emptyList()

        override fun userLinks(): List<ContentItem> = emptyList()

        override suspend fun addLink(draft: UserLinkDraft, nowMillis: Long): AddUserLinkResult =
            error("Not used by portable import tests.")

        override suspend fun markUnavailable(contentId: String, nowMillis: Long) = Unit

        override suspend fun deleteLink(contentId: String) = Unit

        override suspend fun importPortableLinks(
            links: List<ContentItem>,
            replaceExisting: Boolean,
            nowMillis: Long,
        ): Set<String> {
            seenContentIds = links.map(ContentItem::id)
            return emptySet()
        }
    }

    private class StaticUserDocumentRepository(
        private val documents: List<ContentItem>,
    ) : UserDocumentRepository {
        override fun userDocuments(): List<ContentItem> = documents

        override suspend fun addDocument(draft: UserDocumentDraft, nowMillis: Long): AddUserDocumentResult =
            error("Not used by portable import tests.")

        override suspend fun markUnavailable(contentId: String, nowMillis: Long) = Unit

        override suspend fun deleteDocument(contentId: String) = Unit
    }

    private class StaticReadingProgressRepository(
        private val progress: List<ReadingProgress>,
    ) : ReadingProgressRepository {
        override fun readingProgress(): List<ReadingProgress> = progress

        override suspend fun saveProgress(progress: ReadingProgress) =
            error("Not used by portable import tests.")

        override suspend fun deleteProgress(contentId: String) =
            error("Not used by portable import tests.")
    }

    private class RecordingUserLinkRepository : UserLinkRepository {
        var links: List<ContentItem> = emptyList()
        var importCallCount: Int = 0

        override fun userLinks(): List<ContentItem> = links

        override suspend fun addLink(draft: UserLinkDraft, nowMillis: Long): AddUserLinkResult =
            error("Not used by portable import tests.")

        override suspend fun markUnavailable(contentId: String, nowMillis: Long) = Unit

        override suspend fun deleteLink(contentId: String) = Unit

        override suspend fun importPortableLinks(
            links: List<ContentItem>,
            replaceExisting: Boolean,
            nowMillis: Long,
        ): Set<String> {
            importCallCount += 1
            val plan = portableUserContentImportPlan(
                current = this.links,
                imported = links,
                replaceExisting = replaceExisting,
                secondaryKey = ContentItem::externalUrl,
            )
            this.links = mergeImportedUserContent(
                current = if (replaceExisting) emptyList() else this.links,
                imported = plan.itemsToImport,
                secondaryKey = ContentItem::externalUrl,
            )
            return plan.acceptedContentIds
        }
    }

    private class RecordingUserDocumentRepository : UserDocumentRepository {
        var documents: List<ContentItem> = emptyList()

        override fun userDocuments(): List<ContentItem> = documents

        override suspend fun addDocument(draft: UserDocumentDraft, nowMillis: Long): AddUserDocumentResult =
            error("Not used by portable import tests.")

        override suspend fun markUnavailable(contentId: String, nowMillis: Long) = Unit

        override suspend fun deleteDocument(contentId: String) = Unit

        override suspend fun importPortableDocuments(
            documents: List<ContentItem>,
            replaceExisting: Boolean,
            nowMillis: Long,
        ): Set<String> {
            val plan = portableUserContentImportPlan(
                current = this.documents,
                imported = documents,
                replaceExisting = replaceExisting,
                secondaryKey = ContentItem::verifiedDocumentFingerprintSha256,
            )
            this.documents = mergeImportedUserContent(
                current = if (replaceExisting) emptyList() else this.documents,
                imported = plan.itemsToImport,
                secondaryKey = ContentItem::verifiedDocumentFingerprintSha256,
            )
            return plan.acceptedContentIds
        }
    }

    private class RecordingReadingProgressRepository : ReadingProgressRepository {
        var progress: List<ReadingProgress> = emptyList()

        override fun readingProgress(): List<ReadingProgress> = progress

        override fun observeReadingProgress(): Flow<List<ReadingProgress>> = flowOf(progress)

        override suspend fun saveProgress(progress: ReadingProgress) {
            this.progress = this.progress.filterNot { item -> item.contentId == progress.contentId } + progress
        }

        override suspend fun deleteProgress(contentId: String) {
            progress = progress.filterNot { item -> item.contentId == contentId }
        }

        override suspend fun replaceReadingProgress(progress: List<ReadingProgress>) {
            this.progress = progress
        }
    }

    private class FailingOnceReadingProgressRepository : ReadingProgressRepository {
        var progress: List<ReadingProgress> = emptyList()
        private var shouldFailReplace = true

        override fun readingProgress(): List<ReadingProgress> = progress

        override fun observeReadingProgress(): Flow<List<ReadingProgress>> = flowOf(progress)

        override suspend fun saveProgress(progress: ReadingProgress) {
            this.progress = this.progress.filterNot { item -> item.contentId == progress.contentId } + progress
        }

        override suspend fun deleteProgress(contentId: String) {
            progress = progress.filterNot { item -> item.contentId == contentId }
        }

        override suspend fun replaceReadingProgress(progress: List<ReadingProgress>) {
            if (shouldFailReplace) {
                shouldFailReplace = false
                throw IllegalStateException("Simulated progress write failure")
            }
            this.progress = progress
        }
    }
}
