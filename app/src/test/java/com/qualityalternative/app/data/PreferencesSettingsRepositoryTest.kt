package com.qualityalternative.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.qualityalternative.app.domain.model.AppThemeMode
import com.qualityalternative.app.domain.model.ContentPriority
import com.qualityalternative.app.domain.model.DurationBucket
import com.qualityalternative.app.domain.model.OnboardingSelection
import com.qualityalternative.app.domain.model.TopicTag
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PreferencesSettingsRepositoryTest {
    @Test
    fun saveOnboardingSelection_persistsAndRestoresSettings() = runBlocking {
        val repository = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )

        val initial = repository.observeAppSettings().first()
        assertFalse(initial.hasCompletedOnboarding)

        val selection = OnboardingSelection(
            selectedAppPackages = SupportedCatalog.distractingApps.take(3).mapTo(mutableSetOf()) { it.packageName },
            preferredTopics = setOf(TopicTag.PHILOSOPHY, TopicTag.SCIENCE, TopicTag.HISTORY, TopicTag.OTHER),
            preferredDurationBucket = DurationBucket.DEEP,
            selectedPackIds = setOf("science"),
        )

        repository.saveOnboardingSelection(selection)

        val restored = repository.observeAppSettings().first()
        assertTrue(restored.hasCompletedOnboarding)
        assertEquals(selection.selectedAppPackages, restored.selectedAppPackages)
        assertEquals(selection.preferredTopics, restored.preferredTopics)
        assertEquals(selection.preferredDurationBucket, restored.preferredDurationBucket)
        assertEquals(selection.selectedPackIds, restored.selectedPackIds)
        assertEquals(AppThemeMode.LIGHT, restored.themeMode)
    }

    @Test
    fun saveThemeMode_persistsWithoutResettingOnboarding() = runBlocking {
        val repository = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        val selection = OnboardingSelection(
            selectedAppPackages = SupportedCatalog.distractingApps.take(3).mapTo(mutableSetOf()) { it.packageName },
            preferredTopics = setOf(TopicTag.PHILOSOPHY, TopicTag.SCIENCE, TopicTag.HISTORY),
            preferredDurationBucket = DurationBucket.DEEP,
            selectedPackIds = setOf("science"),
        )

        repository.saveOnboardingSelection(selection)
        repository.saveThemeMode(AppThemeMode.DARK)

        val restored = repository.observeAppSettings().first()
        assertTrue(restored.hasCompletedOnboarding)
        assertEquals(selection.selectedAppPackages, restored.selectedAppPackages)
        assertEquals(AppThemeMode.DARK, restored.themeMode)
    }

    @Test
    fun saveMeditationDuration_persistsWithoutResettingOnboarding() = runBlocking {
        val repository = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        val selection = OnboardingSelection(
            selectedAppPackages = SupportedCatalog.distractingApps.take(3).mapTo(mutableSetOf()) { it.packageName },
            preferredTopics = setOf(TopicTag.PHILOSOPHY, TopicTag.SCIENCE, TopicTag.HISTORY),
            preferredDurationBucket = DurationBucket.FOCUS,
            selectedPackIds = setOf("science"),
        )

        repository.saveOnboardingSelection(selection)
        repository.saveMeditationDurationMinutes(5)

        val restored = repository.observeAppSettings().first()
        assertTrue(restored.hasCompletedOnboarding)
        assertEquals(selection.selectedAppPackages, restored.selectedAppPackages)
        assertEquals(5, restored.meditationDurationMinutes)
    }

    @Test
    fun saveContentPriority_persistsWithoutResettingOnboarding() = runBlocking {
        val repository = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        val selection = OnboardingSelection(
            selectedAppPackages = SupportedCatalog.distractingApps.take(3).mapTo(mutableSetOf()) { it.packageName },
            preferredTopics = setOf(TopicTag.PHILOSOPHY, TopicTag.SCIENCE, TopicTag.HISTORY),
            preferredDurationBucket = DurationBucket.FOCUS,
            selectedPackIds = setOf("science"),
        )

        repository.saveOnboardingSelection(selection)
        repository.saveContentPriority(ContentPriority.MY_FILES)

        val restored = repository.observeAppSettings().first()
        assertTrue(restored.hasCompletedOnboarding)
        assertEquals(selection.selectedAppPackages, restored.selectedAppPackages)
        assertEquals(ContentPriority.MY_FILES, restored.contentPriority)
    }

    @Test
    fun savePriorityContentIds_persistsWithoutResettingOnboarding() = runBlocking {
        val repository = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        val selection = OnboardingSelection(
            selectedAppPackages = SupportedCatalog.distractingApps.take(3).mapTo(mutableSetOf()) { it.packageName },
            preferredTopics = setOf(TopicTag.PHILOSOPHY, TopicTag.SCIENCE, TopicTag.HISTORY),
            preferredDurationBucket = DurationBucket.FOCUS,
            selectedPackIds = setOf("science"),
        )

        repository.saveOnboardingSelection(selection)
        repository.savePriorityContentIds(setOf("p1", "doc:local"))

        val restored = repository.observeAppSettings().first()
        assertTrue(restored.hasCompletedOnboarding)
        assertEquals(selection.selectedAppPackages, restored.selectedAppPackages)
        assertEquals(setOf("p1", "doc:local"), restored.priorityContentIds)
    }

    @Test
    fun saveReactivatedCompletedContentIds_persistsWithoutResettingOnboarding() = runBlocking {
        val repository = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        val selection = OnboardingSelection(
            selectedAppPackages = SupportedCatalog.distractingApps.take(3).mapTo(mutableSetOf()) { it.packageName },
            preferredTopics = setOf(TopicTag.PHILOSOPHY, TopicTag.SCIENCE, TopicTag.HISTORY),
            preferredDurationBucket = DurationBucket.FOCUS,
            selectedPackIds = setOf("science"),
        )

        repository.saveOnboardingSelection(selection)
        repository.saveReactivatedCompletedContentIds(setOf("p1", "doc:local"))

        val restored = repository.observeAppSettings().first()
        assertTrue(restored.hasCompletedOnboarding)
        assertEquals(selection.selectedAppPackages, restored.selectedAppPackages)
        assertEquals(setOf("p1", "doc:local"), restored.reactivatedCompletedContentIds)
    }

    @Test
    fun saveOpenAnywayUnlockMinutes_persistsWithoutResettingOnboarding() = runBlocking {
        val repository = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        val selection = OnboardingSelection(
            selectedAppPackages = SupportedCatalog.distractingApps.take(3).mapTo(mutableSetOf()) { it.packageName },
            preferredTopics = setOf(TopicTag.PHILOSOPHY, TopicTag.SCIENCE, TopicTag.HISTORY),
            preferredDurationBucket = DurationBucket.FOCUS,
            selectedPackIds = setOf("science"),
        )

        repository.saveOnboardingSelection(selection)
        repository.saveOpenAnywayUnlockMinutes(120)

        val restored = repository.observeAppSettings().first()
        assertTrue(restored.hasCompletedOnboarding)
        assertEquals(selection.selectedAppPackages, restored.selectedAppPackages)
        assertEquals(120, restored.openAnywayUnlockMinutes)
    }

    @Test
    fun ensureLocalProfileIdentity_createsAndReusesPortableProfileId() = runBlocking {
        val repository = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )

        val created = repository.ensureLocalProfileIdentity(nowMillis = 10_000L)
        val restored = repository.ensureLocalProfileIdentity(nowMillis = 20_000L)

        assertTrue(created.profileId.matches(Regex("^qa-local-[0-9a-fA-F-]{36}$")))
        assertEquals(10_000L, created.createdAtMillis)
        assertEquals(created, restored)
    }

    @Test
    fun saveReaderFontScale_persistsRoundedPortableValue() = runBlocking {
        val repository = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )

        repository.saveReaderFontScale(1.234)

        val restored = repository.observeAppSettings().first()
        assertEquals(1.23, restored.readerFontScale, 0.0)
    }

    @Test
    fun saveReaderFontScale_clampsToPortableRange() = runBlocking {
        val repository = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )

        repository.saveReaderFontScale(4.0)

        val restored = repository.observeAppSettings().first()
        assertEquals(1.60, restored.readerFontScale, 0.0)
    }

    @Test
    fun saveAnnotationExportSettings_persistsStatusAndClearsFailureOnSuccess() = runBlocking {
        val repository = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )

        repository.saveAnnotationExportDestination(
            uri = "content://drive/qa-annotations.jsonld",
            displayName = "qa-annotations.jsonld",
        )
        repository.saveAnnotationExportSuccess(3_000L)
        repository.saveAnnotationExportDestination(
            uri = "content://drive/new-annotations.jsonld",
            displayName = "new-annotations.jsonld",
        )
        repository.saveAnnotationExportFailure("Drive write unavailable")

        val failed = repository.observeAppSettings().first()
        assertEquals("content://drive/new-annotations.jsonld", failed.annotationExportUri)
        assertEquals("new-annotations.jsonld", failed.annotationExportDisplayName)
        assertEquals(null, failed.annotationExportLastSuccessfulAtMillis)
        assertEquals("Drive write unavailable", failed.annotationExportLastError)

        repository.saveAnnotationExportSuccess(4_000L)

        val restored = repository.observeAppSettings().first()
        assertEquals(4_000L, restored.annotationExportLastSuccessfulAtMillis)
        assertEquals(null, restored.annotationExportLastError)

        repository.clearAnnotationExportDestination()

        val cleared = repository.observeAppSettings().first()
        assertEquals(null, cleared.annotationExportUri)
        assertEquals(null, cleared.annotationExportDisplayName)
        assertEquals(null, cleared.annotationExportLastSuccessfulAtMillis)
        assertEquals(null, cleared.annotationExportLastError)
    }

    @Test
    fun saveAnnotationDriveSyncSettings_persistsConnectionStatusAndFailure() = runBlocking {
        val repository = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )

        repository.saveAnnotationDriveSyncConnection(folderId = null)
        repository.saveAnnotationDriveSyncFailure("Google Drive sync failed. Retry from Settings.")

        val failed = repository.observeAppSettings().first()
        assertEquals(true, failed.annotationDriveSyncEnabled)
        assertEquals(null, failed.annotationDriveFolderId)
        assertEquals(null, failed.annotationDriveLastSuccessfulAtMillis)
        assertEquals("Google Drive sync failed. Retry from Settings.", failed.annotationDriveLastError)

        repository.saveAnnotationDriveSyncSuccess(
            timestampMillis = 6_000L,
            folderId = "drive-folder-annotations",
        )

        val synced = repository.observeAppSettings().first()
        assertEquals(true, synced.annotationDriveSyncEnabled)
        assertEquals("drive-folder-annotations", synced.annotationDriveFolderId)
        assertEquals(6_000L, synced.annotationDriveLastSuccessfulAtMillis)
        assertEquals(null, synced.annotationDriveLastError)

        repository.clearAnnotationDriveSyncConnection()

        val cleared = repository.observeAppSettings().first()
        assertEquals(false, cleared.annotationDriveSyncEnabled)
        assertEquals(null, cleared.annotationDriveFolderId)
        assertEquals(null, cleared.annotationDriveLastSuccessfulAtMillis)
        assertEquals(null, cleared.annotationDriveLastError)
    }

    @Test
    fun saveProfileAutosaveSettings_persistsStatusAndClearsFailureOnSuccess() = runBlocking {
        val repository = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )

        repository.saveProfileAutosaveDestination(
            uri = "content://tree/qa-profile",
            displayName = "QA profile",
        )
        repository.saveProfileAutosaveSuccess(3_000L)
        repository.saveProfileAutosaveDestination(
            uri = "content://tree/new-profile",
            displayName = "New profile folder",
        )
        repository.saveProfileAutosaveFailure("Choose the folder again or retry.")

        val failed = repository.observeAppSettings().first()
        assertEquals("content://tree/new-profile", failed.profileAutosaveUri)
        assertEquals("New profile folder", failed.profileAutosaveDisplayName)
        assertEquals(null, failed.profileAutosaveLastSuccessfulAtMillis)
        assertEquals("Choose the folder again or retry.", failed.profileAutosaveLastError)

        repository.saveProfileAutosaveSuccess(4_000L)

        val restored = repository.observeAppSettings().first()
        assertEquals(4_000L, restored.profileAutosaveLastSuccessfulAtMillis)
        assertEquals(null, restored.profileAutosaveLastError)

        repository.clearProfileAutosaveDestination()

        val cleared = repository.observeAppSettings().first()
        assertEquals(null, cleared.profileAutosaveUri)
        assertEquals(null, cleared.profileAutosaveDisplayName)
        assertEquals(null, cleared.profileAutosaveLastSuccessfulAtMillis)
        assertEquals(null, cleared.profileAutosaveLastError)
    }

    @Test
    fun observeAppSettings_mapsLegacyInkThemeToDark() = runBlocking {
        val dataStore = testDataStore()
        val repository = PreferencesSettingsRepository(
            dataStore = dataStore,
            supportedApps = SupportedCatalog.distractingApps,
        )
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("theme_mode")] = "INK"
        }

        val restored = repository.observeAppSettings().first()

        assertEquals(AppThemeMode.DARK, restored.themeMode)
    }

    @Test
    fun saveSettingsUpdates_persistWithoutResettingOnboarding() = runBlocking {
        val repository = PreferencesSettingsRepository(
            dataStore = testDataStore(),
            supportedApps = SupportedCatalog.distractingApps,
        )
        val selection = OnboardingSelection(
            selectedAppPackages = SupportedCatalog.distractingApps.take(3).mapTo(mutableSetOf()) { it.packageName },
            preferredTopics = setOf(TopicTag.PHILOSOPHY, TopicTag.SCIENCE, TopicTag.HISTORY),
            preferredDurationBucket = DurationBucket.FOCUS,
            selectedPackIds = setOf("science"),
        )
        val updatedPackages = SupportedCatalog.distractingApps.drop(1).take(3).mapTo(mutableSetOf()) { it.packageName }

        repository.saveOnboardingSelection(selection)
        repository.saveSelectedAppPackages(updatedPackages)
        repository.savePreferredDurationBucket(DurationBucket.DEEP)

        val restored = repository.observeAppSettings().first()
        assertTrue(restored.hasCompletedOnboarding)
        assertEquals(updatedPackages, restored.selectedAppPackages)
        assertEquals(DurationBucket.DEEP, restored.preferredDurationBucket)
        assertEquals(selection.preferredTopics, restored.preferredTopics)
    }

    private fun testDataStore(): DataStore<Preferences> {
        val file = File.createTempFile("settings-repository-test", ".preferences_pb").apply { deleteOnExit() }
        return PreferenceDataStoreFactory.create(produceFile = { file })
    }
}
