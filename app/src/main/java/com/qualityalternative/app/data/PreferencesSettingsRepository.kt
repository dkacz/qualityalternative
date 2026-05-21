package com.qualityalternative.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.qualityalternative.app.domain.model.AppSettings
import com.qualityalternative.app.domain.model.AppThemeMode
import com.qualityalternative.app.domain.model.ContentPriority
import com.qualityalternative.app.domain.model.DEFAULT_BEDTIME_ENABLED
import com.qualityalternative.app.domain.model.DEFAULT_BEDTIME_END_MINUTES
import com.qualityalternative.app.domain.model.DEFAULT_BEDTIME_START_MINUTES
import com.qualityalternative.app.domain.model.DEFAULT_MEDITATION_MINUTES
import com.qualityalternative.app.domain.model.DEFAULT_INTERFACE_TEXT_SCALE
import com.qualityalternative.app.domain.model.DEFAULT_INTERVENTION_MODE
import com.qualityalternative.app.domain.model.DEFAULT_OPEN_ANYWAY_UNLOCK_MINUTES
import com.qualityalternative.app.domain.model.DEFAULT_READER_FONT_SCALE
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.model.DurationBucket
import com.qualityalternative.app.domain.model.InterventionMode
import com.qualityalternative.app.domain.model.LocalProfileIdentity
import com.qualityalternative.app.domain.model.MAX_BEDTIME_MINUTES
import com.qualityalternative.app.domain.model.MAX_MEDITATION_MINUTES
import com.qualityalternative.app.domain.model.MAX_INTERFACE_TEXT_SCALE
import com.qualityalternative.app.domain.model.MAX_OPEN_ANYWAY_UNLOCK_MINUTES
import com.qualityalternative.app.domain.model.MAX_READER_FONT_SCALE
import com.qualityalternative.app.domain.model.MIN_BEDTIME_MINUTES
import com.qualityalternative.app.domain.model.MIN_MEDITATION_MINUTES
import com.qualityalternative.app.domain.model.MIN_INTERFACE_TEXT_SCALE
import com.qualityalternative.app.domain.model.MIN_OPEN_ANYWAY_UNLOCK_MINUTES
import com.qualityalternative.app.domain.model.MIN_READER_FONT_SCALE
import com.qualityalternative.app.domain.model.OnboardingSelection
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.service.SettingsRepository
import java.io.IOException
import java.util.UUID
import kotlin.math.roundToInt
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
                    interventionMode = parseInterventionMode(preferences[InterventionModePreference]),
                    themeMode = parseThemeMode(preferences[ThemeMode]),
                    meditationDurationMinutes = (preferences[MeditationDurationMinutes] ?: DEFAULT_MEDITATION_MINUTES)
                        .coerceIn(MIN_MEDITATION_MINUTES, MAX_MEDITATION_MINUTES),
                    contentPriority = parseContentPriority(preferences[ContentPriorityPreference]),
                    priorityContentIds = preferences[PriorityContentIds].orEmpty(),
                    reactivatedCompletedContentIds = preferences[ReactivatedCompletedContentIds].orEmpty(),
                    openAnywayUnlockMinutes = (preferences[OpenAnywayUnlockMinutes] ?: DEFAULT_OPEN_ANYWAY_UNLOCK_MINUTES)
                        .coerceIn(MIN_OPEN_ANYWAY_UNLOCK_MINUTES, MAX_OPEN_ANYWAY_UNLOCK_MINUTES),
                    bedtimeEnabled = preferences[BedtimeEnabled] ?: DEFAULT_BEDTIME_ENABLED,
                    bedtimeStartMinutes = portableBedtimeMinutes(
                        preferences[BedtimeStartMinutes] ?: DEFAULT_BEDTIME_START_MINUTES,
                    ),
                    bedtimeEndMinutes = portableBedtimeMinutes(
                        preferences[BedtimeEndMinutes] ?: DEFAULT_BEDTIME_END_MINUTES,
                    ),
                    readerFontScale = portableReaderFontScale(
                        preferences[ReaderFontScale] ?: DEFAULT_READER_FONT_SCALE,
                    ),
                    interfaceTextScale = portableInterfaceTextScale(
                        preferences[InterfaceTextScale] ?: DEFAULT_INTERFACE_TEXT_SCALE,
                    ),
                    annotationExportUri = preferences[AnnotationExportUri],
                    annotationExportDisplayName = preferences[AnnotationExportDisplayName],
                    annotationExportLastSuccessfulAtMillis = preferences[AnnotationExportLastSuccessfulAtMillis],
                    annotationExportLastError = preferences[AnnotationExportLastError],
                    annotationDriveSyncEnabled = preferences[AnnotationDriveSyncEnabled] ?: false,
                    annotationDriveFolderId = preferences[AnnotationDriveFolderId],
                    annotationDriveLastSuccessfulAtMillis = preferences[AnnotationDriveLastSuccessfulAtMillis],
                    annotationDriveLastError = preferences[AnnotationDriveLastError],
                    profileAutosaveUri = preferences[ProfileAutosaveUri],
                    profileAutosaveDisplayName = preferences[ProfileAutosaveDisplayName],
                    profileAutosaveLastSuccessfulAtMillis = preferences[ProfileAutosaveLastSuccessfulAtMillis],
                    profileAutosaveLastError = preferences[ProfileAutosaveLastError],
                )
            }
    }

    override fun supportedDistractingApps(): List<DistractingApp> = supportedApps

    override suspend fun ensureLocalProfileIdentity(nowMillis: Long): LocalProfileIdentity {
        var identity: LocalProfileIdentity? = null
        dataStore.edit { preferences ->
            val profileId = preferences[LocalProfileId]
                ?.takeIf(::isValidLocalProfileId)
                ?: "qa-local-${UUID.randomUUID()}"
            val createdAtMillis = preferences[LocalProfileCreatedAtMillis]
                ?.takeIf { it >= 0L }
                ?: nowMillis.coerceAtLeast(0L)
            preferences[LocalProfileId] = profileId
            preferences[LocalProfileCreatedAtMillis] = createdAtMillis
            identity = LocalProfileIdentity(
                profileId = profileId,
                createdAtMillis = createdAtMillis,
            )
        }
        return requireNotNull(identity)
    }

    override suspend fun replacePortableSettings(settings: AppSettings, profileIdentity: LocalProfileIdentity?) {
        dataStore.edit { preferences ->
            if (profileIdentity != null) {
                preferences[LocalProfileId] = profileIdentity.profileId
                preferences[LocalProfileCreatedAtMillis] = profileIdentity.createdAtMillis.coerceAtLeast(0L)
            }
            preferences[HasCompletedOnboarding] = settings.hasCompletedOnboarding
            preferences[SelectedAppPackages] = settings.selectedAppPackages
            preferences[PreferredTopics] = settings.preferredTopics.mapTo(mutableSetOf(), TopicTag::name)
            preferences[PreferredDurationBucket] = settings.preferredDurationBucket.name
            preferences[SelectedPackIds] = settings.selectedPackIds
            preferences[InterventionModePreference] = settings.interventionMode.name
            preferences[ThemeMode] = settings.themeMode.name
            preferences[MeditationDurationMinutes] = settings.meditationDurationMinutes
                .coerceIn(MIN_MEDITATION_MINUTES, MAX_MEDITATION_MINUTES)
            preferences[ReaderFontScale] = portableReaderFontScale(settings.readerFontScale)
            preferences[InterfaceTextScale] = portableInterfaceTextScale(settings.interfaceTextScale)
            preferences[ContentPriorityPreference] = settings.contentPriority.name
            preferences[PriorityContentIds] = settings.priorityContentIds
            preferences[ReactivatedCompletedContentIds] = settings.reactivatedCompletedContentIds
            preferences[OpenAnywayUnlockMinutes] = settings.openAnywayUnlockMinutes
                .coerceIn(MIN_OPEN_ANYWAY_UNLOCK_MINUTES, MAX_OPEN_ANYWAY_UNLOCK_MINUTES)
            preferences[BedtimeEnabled] = settings.bedtimeEnabled
            preferences[BedtimeStartMinutes] = portableBedtimeMinutes(settings.bedtimeStartMinutes)
            preferences[BedtimeEndMinutes] = portableBedtimeMinutes(settings.bedtimeEndMinutes)
        }
    }

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

    override suspend fun saveInterventionMode(mode: InterventionMode) {
        dataStore.edit { preferences ->
            preferences[InterventionModePreference] = mode.name
        }
    }

    override suspend fun saveThemeMode(themeMode: AppThemeMode) {
        dataStore.edit { preferences ->
            preferences[ThemeMode] = themeMode.name
        }
    }

    override suspend fun saveMeditationDurationMinutes(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[MeditationDurationMinutes] = minutes.coerceIn(MIN_MEDITATION_MINUTES, MAX_MEDITATION_MINUTES)
        }
    }

    override suspend fun saveReaderFontScale(scale: Double) {
        dataStore.edit { preferences ->
            preferences[ReaderFontScale] = portableReaderFontScale(scale)
        }
    }

    override suspend fun saveInterfaceTextScale(scale: Double) {
        dataStore.edit { preferences ->
            preferences[InterfaceTextScale] = portableInterfaceTextScale(scale)
        }
    }

    override suspend fun saveContentPriority(priority: ContentPriority) {
        dataStore.edit { preferences ->
            preferences[ContentPriorityPreference] = priority.name
        }
    }

    override suspend fun savePriorityContentIds(contentIds: Set<String>) {
        dataStore.edit { preferences ->
            preferences[PriorityContentIds] = contentIds
        }
    }

    override suspend fun saveReactivatedCompletedContentIds(contentIds: Set<String>) {
        dataStore.edit { preferences ->
            preferences[ReactivatedCompletedContentIds] = contentIds
        }
    }

    override suspend fun saveOpenAnywayUnlockMinutes(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[OpenAnywayUnlockMinutes] =
                minutes.coerceIn(MIN_OPEN_ANYWAY_UNLOCK_MINUTES, MAX_OPEN_ANYWAY_UNLOCK_MINUTES)
        }
    }

    override suspend fun saveBedtimeSettings(enabled: Boolean, startMinutes: Int, endMinutes: Int) {
        dataStore.edit { preferences ->
            preferences[BedtimeEnabled] = enabled
            preferences[BedtimeStartMinutes] = portableBedtimeMinutes(startMinutes)
            preferences[BedtimeEndMinutes] = portableBedtimeMinutes(endMinutes)
        }
    }

    override suspend fun saveAnnotationExportDestination(uri: String, displayName: String) {
        dataStore.edit { preferences ->
            preferences[AnnotationExportUri] = uri
            preferences[AnnotationExportDisplayName] = displayName
            preferences.remove(AnnotationExportLastSuccessfulAtMillis)
            preferences.remove(AnnotationExportLastError)
        }
    }

    override suspend fun clearAnnotationExportDestination() {
        dataStore.edit { preferences ->
            preferences.remove(AnnotationExportUri)
            preferences.remove(AnnotationExportDisplayName)
            preferences.remove(AnnotationExportLastSuccessfulAtMillis)
            preferences.remove(AnnotationExportLastError)
        }
    }

    override suspend fun saveAnnotationExportSuccess(timestampMillis: Long) {
        dataStore.edit { preferences ->
            preferences[AnnotationExportLastSuccessfulAtMillis] = timestampMillis
            preferences.remove(AnnotationExportLastError)
        }
    }

    override suspend fun saveAnnotationExportFailure(errorMessage: String) {
        dataStore.edit { preferences ->
            preferences[AnnotationExportLastError] = errorMessage
        }
    }

    override suspend fun saveAnnotationDriveSyncConnection(folderId: String?) {
        dataStore.edit { preferences ->
            preferences[AnnotationDriveSyncEnabled] = true
            if (folderId.isNullOrBlank()) {
                preferences.remove(AnnotationDriveFolderId)
            } else {
                preferences[AnnotationDriveFolderId] = folderId
            }
            preferences.remove(AnnotationDriveLastError)
        }
    }

    override suspend fun clearAnnotationDriveSyncConnection() {
        dataStore.edit { preferences ->
            preferences.remove(AnnotationDriveSyncEnabled)
            preferences.remove(AnnotationDriveFolderId)
            preferences.remove(AnnotationDriveLastSuccessfulAtMillis)
            preferences.remove(AnnotationDriveLastError)
        }
    }

    override suspend fun saveAnnotationDriveSyncSuccess(timestampMillis: Long, folderId: String) {
        dataStore.edit { preferences ->
            preferences[AnnotationDriveSyncEnabled] = true
            preferences[AnnotationDriveFolderId] = folderId
            preferences[AnnotationDriveLastSuccessfulAtMillis] = timestampMillis
            preferences.remove(AnnotationDriveLastError)
        }
    }

    override suspend fun saveAnnotationDriveSyncFailure(errorMessage: String) {
        dataStore.edit { preferences ->
            preferences[AnnotationDriveLastError] = errorMessage
        }
    }

    override suspend fun saveProfileAutosaveDestination(uri: String, displayName: String) {
        dataStore.edit { preferences ->
            preferences[ProfileAutosaveUri] = uri
            preferences[ProfileAutosaveDisplayName] = displayName
            preferences.remove(ProfileAutosaveLastSuccessfulAtMillis)
            preferences.remove(ProfileAutosaveLastError)
        }
    }

    override suspend fun clearProfileAutosaveDestination() {
        dataStore.edit { preferences ->
            preferences.remove(ProfileAutosaveUri)
            preferences.remove(ProfileAutosaveDisplayName)
            preferences.remove(ProfileAutosaveLastSuccessfulAtMillis)
            preferences.remove(ProfileAutosaveLastError)
        }
    }

    override suspend fun saveProfileAutosaveSuccess(timestampMillis: Long) {
        dataStore.edit { preferences ->
            preferences[ProfileAutosaveLastSuccessfulAtMillis] = timestampMillis
            preferences.remove(ProfileAutosaveLastError)
        }
    }

    override suspend fun saveProfileAutosaveFailure(errorMessage: String) {
        dataStore.edit { preferences ->
            preferences[ProfileAutosaveLastError] = errorMessage
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

        fun parseInterventionMode(raw: String?): InterventionMode {
            return runCatching {
                InterventionMode.valueOf(raw ?: DEFAULT_INTERVENTION_MODE.name)
            }.getOrDefault(DEFAULT_INTERVENTION_MODE)
        }

        fun parseContentPriority(raw: String?): ContentPriority {
            return runCatching {
                ContentPriority.valueOf(raw ?: ContentPriority.BALANCED.name)
            }.getOrDefault(ContentPriority.BALANCED)
        }

        fun portableReaderFontScale(raw: Double): Double {
            return (raw.coerceIn(MIN_READER_FONT_SCALE, MAX_READER_FONT_SCALE) * 100.0).roundToInt() / 100.0
        }

        fun portableInterfaceTextScale(raw: Double): Double {
            return (raw.coerceIn(MIN_INTERFACE_TEXT_SCALE, MAX_INTERFACE_TEXT_SCALE) * 100.0).roundToInt() / 100.0
        }

        fun portableBedtimeMinutes(raw: Int): Int {
            return raw.coerceIn(MIN_BEDTIME_MINUTES, MAX_BEDTIME_MINUTES)
        }

        fun isValidLocalProfileId(raw: String): Boolean {
            return Regex("^qa-local-[0-9a-fA-F-]{36}$").matches(raw)
        }

        val LocalProfileId = stringPreferencesKey("local_profile_id")
        val LocalProfileCreatedAtMillis = longPreferencesKey("local_profile_created_at_millis")
        val HasCompletedOnboarding = booleanPreferencesKey("has_completed_onboarding")
        val SelectedAppPackages = stringSetPreferencesKey("selected_app_packages")
        val PreferredTopics = stringSetPreferencesKey("preferred_topics")
        val PreferredDurationBucket = stringPreferencesKey("preferred_duration_bucket")
        val SelectedPackIds = stringSetPreferencesKey("selected_pack_ids")
        val InterventionModePreference = stringPreferencesKey("intervention_mode")
        val ThemeMode = stringPreferencesKey("theme_mode")
        val MeditationDurationMinutes = intPreferencesKey("meditation_duration_minutes")
        val ReaderFontScale = doublePreferencesKey("reader_font_scale")
        val InterfaceTextScale = doublePreferencesKey("interface_text_scale")
        val ContentPriorityPreference = stringPreferencesKey("content_priority")
        val PriorityContentIds = stringSetPreferencesKey("priority_content_ids")
        val ReactivatedCompletedContentIds = stringSetPreferencesKey("reactivated_completed_content_ids")
        val OpenAnywayUnlockMinutes = intPreferencesKey("open_anyway_unlock_minutes")
        val BedtimeEnabled = booleanPreferencesKey("bedtime_enabled")
        val BedtimeStartMinutes = intPreferencesKey("bedtime_start_minutes")
        val BedtimeEndMinutes = intPreferencesKey("bedtime_end_minutes")
        val AnnotationExportUri = stringPreferencesKey("annotation_export_uri")
        val AnnotationExportDisplayName = stringPreferencesKey("annotation_export_display_name")
        val AnnotationExportLastSuccessfulAtMillis = longPreferencesKey("annotation_export_last_successful_at_millis")
        val AnnotationExportLastError = stringPreferencesKey("annotation_export_last_error")
        val AnnotationDriveSyncEnabled = booleanPreferencesKey("annotation_drive_sync_enabled")
        val AnnotationDriveFolderId = stringPreferencesKey("annotation_drive_folder_id")
        val AnnotationDriveLastSuccessfulAtMillis = longPreferencesKey("annotation_drive_last_successful_at_millis")
        val AnnotationDriveLastError = stringPreferencesKey("annotation_drive_last_error")
        val ProfileAutosaveUri = stringPreferencesKey("profile_autosave_uri")
        val ProfileAutosaveDisplayName = stringPreferencesKey("profile_autosave_display_name")
        val ProfileAutosaveLastSuccessfulAtMillis = longPreferencesKey("profile_autosave_last_successful_at_millis")
        val ProfileAutosaveLastError = stringPreferencesKey("profile_autosave_last_error")
    }
}
