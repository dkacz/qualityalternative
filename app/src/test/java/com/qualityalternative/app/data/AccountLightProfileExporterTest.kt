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
        repository.saveThemeMode(AppThemeMode.DARK)
        repository.saveMeditationDurationMinutes(7)
        repository.saveReaderFontScale(1.254)
        repository.saveContentPriority(ContentPriority.MY_FILES)
        repository.savePriorityContentIds(setOf("user-document-11111111-1111-4111-8111-111111111111"))
        repository.saveReactivatedCompletedContentIds(setOf("editorial-deep-work"))
        repository.saveOpenAnywayUnlockMinutes(120)
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
        assertEquals("DARK", profile.settings.themeMode)
        assertEquals(7, profile.settings.meditationDurationMinutes)
        assertEquals(1.25, profile.settings.readerFontScale, 0.0)
        assertEquals("MY_FILES", profile.settings.contentPriority)
        assertEquals(listOf("user-document-11111111-1111-4111-8111-111111111111"), profile.settings.priorityContentIds)
        assertEquals(listOf("editorial-deep-work"), profile.settings.reactivatedCompletedContentIds)
        assertEquals(120, profile.settings.openAnywayUnlockMinutes)

        assertEquals(emptyList<AccountLightUserLink>(), profile.library.userLinks)
        assertEquals(emptyList<AccountLightUserDocument>(), profile.library.userDocuments)
        assertEquals(emptyList<AccountLightReadingProgress>(), profile.reading.progress)
        assertEquals("qa-annotations.jsonld", profile.annotations.export.destinationDisplayName)
        assertEquals(12_000L, profile.annotations.export.lastSuccessfulAtMillis)
        assertEquals(true, profile.annotations.driveSync.wasEnabledOnSourceDevice)
        assertNull(profile.annotations.driveSync.folderDisplayName)
        assertEquals(13_000L, profile.annotations.driveSync.lastSuccessfulAtMillis)
        assertEquals("NONE", profile.sync.profileAutosave.provider)
        assertEquals("REQUIRES_LOCAL_SELECTION", profile.sync.profileAutosave.activationStateOnImport)
        assertEquals(emptyList<AccountLightWarning>(), profile.warnings)

        assertFalse(rawJson.contains("content://"))
        assertFalse(rawJson.contains("raw-drive-folder-id"))
        assertFalse(rawJson.contains("Drive write unavailable"))
        assertFalse(rawJson.contains("token", ignoreCase = true))
        assertFalse(rawJson.contains("oauth", ignoreCase = true))
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
}
