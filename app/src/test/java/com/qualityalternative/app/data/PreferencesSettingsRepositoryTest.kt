package com.qualityalternative.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.qualityalternative.app.domain.model.AppThemeMode
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
            preferredTopics = setOf(TopicTag.PHILOSOPHY, TopicTag.SCIENCE, TopicTag.HISTORY),
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
