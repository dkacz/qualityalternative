package com.qualityalternative.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.qualityalternative.app.domain.model.AppSettings
import com.qualityalternative.app.domain.model.AppThemeMode
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.model.DurationBucket
import com.qualityalternative.app.domain.model.OnboardingSelection
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.service.SettingsRepository
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class PreferencesSettingsRepository(
    private val dataStore: DataStore<Preferences>,
    private val supportedApps: List<DistractingApp> = SupportedCatalog.distractingApps,
) : SettingsRepository {
    override fun observeAppSettings(): Flow<AppSettings> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                AppSettings(
                    hasCompletedOnboarding = preferences[HasCompletedOnboarding] ?: false,
                    selectedAppPackages = preferences[SelectedAppPackages].orEmpty(),
                    preferredTopics = preferences[PreferredTopics].orEmpty()
                        .mapNotNullTo(mutableSetOf()) { raw ->
                            runCatching { TopicTag.valueOf(raw) }.getOrNull()
                        },
                    preferredDurationBucket = runCatching {
                        DurationBucket.valueOf(preferences[PreferredDurationBucket] ?: DurationBucket.FOCUS.name)
                    }.getOrDefault(DurationBucket.FOCUS),
                    selectedPackIds = preferences[SelectedPackIds].orEmpty(),
                    themeMode = parseThemeMode(preferences[ThemeMode]),
                )
            }
    }

    override fun supportedDistractingApps(): List<DistractingApp> = supportedApps

    override suspend fun saveOnboardingSelection(selection: OnboardingSelection) {
        dataStore.edit { preferences ->
            preferences[HasCompletedOnboarding] = true
            preferences[SelectedAppPackages] = selection.selectedAppPackages
            preferences[PreferredTopics] = selection.preferredTopics.mapTo(mutableSetOf(), TopicTag::name)
            preferences[PreferredDurationBucket] = selection.preferredDurationBucket.name
            preferences[SelectedPackIds] = selection.selectedPackIds
        }
    }

    override suspend fun saveSelectedAppPackages(packages: Set<String>) {
        dataStore.edit { preferences ->
            preferences[SelectedAppPackages] = packages
        }
    }

    override suspend fun savePreferredDurationBucket(bucket: DurationBucket) {
        dataStore.edit { preferences ->
            preferences[PreferredDurationBucket] = bucket.name
        }
    }

    override suspend fun saveThemeMode(themeMode: AppThemeMode) {
        dataStore.edit { preferences ->
            preferences[ThemeMode] = themeMode.name
        }
    }

    suspend fun clearForTests() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    private companion object {
        fun parseThemeMode(raw: String?): AppThemeMode {
            if (raw == "INK") return AppThemeMode.DARK
            return runCatching {
                AppThemeMode.valueOf(raw ?: AppThemeMode.LIGHT.name)
            }.getOrDefault(AppThemeMode.LIGHT)
        }

        val HasCompletedOnboarding = booleanPreferencesKey("has_completed_onboarding")
        val SelectedAppPackages = stringSetPreferencesKey("selected_app_packages")
        val PreferredTopics = stringSetPreferencesKey("preferred_topics")
        val PreferredDurationBucket = stringPreferencesKey("preferred_duration_bucket")
        val SelectedPackIds = stringSetPreferencesKey("selected_pack_ids")
        val ThemeMode = stringPreferencesKey("theme_mode")
    }
}
