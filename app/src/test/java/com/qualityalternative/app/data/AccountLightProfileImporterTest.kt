package com.qualityalternative.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.qualityalternative.app.domain.model.AppThemeMode
import com.qualityalternative.app.domain.model.ContentPriority
import com.qualityalternative.app.domain.model.DurationBucket
import com.qualityalternative.app.domain.model.OnboardingSelection
import com.qualityalternative.app.domain.model.TopicTag
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
            importer.validateImportProfileJson(validProfileJson().withUserLinks(listOf(validUserLink(title = "content://com.android.providers.media.documents/document/1"))))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withUserLinks(listOf(validUserLink(title = "com.dropbox.android.FileProvider"))))
        }
        assertThrows(AccountLightImportException::class.java) {
            importer.validateImportProfileJson(validProfileJson().withUserDocuments(listOf(validUserDocument(fingerprintStrategy = "SHA256_BYTES", sha256 = null, sizeBytes = JsonPrimitive(1234L)))))
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
        paragraphCount: Int,
        completedAtMillis: JsonElement,
    ): String {
        val root = Json.parseToJsonElement(this).jsonObject
        val reading = root.getValue("reading").jsonObject
        return JsonObject(
            root + (
                "reading" to JsonObject(
                    reading + (
                        "progress" to JsonArray(
                            listOf(
                                JsonObject(
                                    mapOf(
                                        "contentId" to JsonPrimitive(contentId),
                                        "progressPercent" to progressPercent,
                                        "lastVisibleParagraphIndex" to JsonPrimitive(lastVisibleParagraphIndex),
                                        "paragraphCount" to JsonPrimitive(paragraphCount),
                                        "updatedAtMillis" to JsonPrimitive(20_000L),
                                        "completedAtMillis" to completedAtMillis,
                                    ),
                                ),
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
        durationMinutes: JsonElement = JsonPrimitive(12),
    ): JsonObject {
        return JsonObject(
            mapOf(
                "contentId" to JsonPrimitive(contentId),
                "normalizedUrl" to JsonPrimitive(normalizedUrl),
                "title" to JsonPrimitive(title),
                "description" to JsonPrimitive("Saved link from another device."),
                "durationMinutes" to durationMinutes,
                "topicTags" to JsonArray(listOf(JsonPrimitive("SCIENCE"))),
                "availability" to JsonPrimitive("AVAILABLE"),
                "createdAtMillis" to JsonPrimitive(10_000L),
                "updatedAtMillis" to JsonPrimitive(20_000L),
                "sourceLabel" to JsonNull,
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
        fingerprintStrategy: String = "SHA256_BYTES",
        sha256: String? = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
        sizeBytes: JsonElement = JsonPrimitive(1234L),
        normalizedTitle: String = "imported book",
        documentImportState: String = "MISSING_FILE_NEEDS_REATTACH",
        sourceDisplayName: String? = "book.epub",
        providerLabel: String? = null,
        createdAtMillis: JsonElement = JsonPrimitive(10_000L),
    ): JsonObject {
        return JsonObject(
            mapOf(
                "contentId" to JsonPrimitive(contentId),
                "displayName" to JsonPrimitive(displayName),
                "mimeType" to JsonPrimitive("application/epub+zip"),
                "documentFormat" to JsonPrimitive("EPUB"),
                "title" to JsonPrimitive(title),
                "description" to JsonPrimitive("Saved document metadata."),
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

    private fun testDataStore(): DataStore<Preferences> {
        val file = File.createTempFile("account-light-importer-test", ".preferences_pb").apply { deleteOnExit() }
        return PreferenceDataStoreFactory.create(produceFile = { file })
    }
}
