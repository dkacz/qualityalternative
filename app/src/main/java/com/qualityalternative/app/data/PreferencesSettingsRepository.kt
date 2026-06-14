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
import com.qualityalternative.app.domain.model.CustomTargetAppCandidate
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
import com.qualityalternative.app.domain.model.WebsiteRule
import com.qualityalternative.app.domain.model.WebsiteRuleType
import com.qualityalternative.app.domain.service.AGENT_INBOX_DRIVE_GRANT_MODE_PICKER_FOLDER
import com.qualityalternative.app.domain.service.AGENT_INBOX_DRIVE_GRANT_MODE_READONLY_FOLDER
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
    private val customTargetCandidatesProvider: () -> List<CustomTargetAppCandidate> = { emptyList() },
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
                val agentInboxDriveFolderId = preferences[AgentInboxDriveFolderId]?.takeIf(String::isNotBlank)
                val agentInboxDriveGrantMode = preferences[AgentInboxDriveGrantMode]
                val hasAgentInboxFolderGrant =
                    preferences[AgentInboxDriveEnabled] == true &&
                        agentInboxDriveFolderId != null &&
                        agentInboxDriveGrantMode in AGENT_INBOX_SUPPORTED_GRANT_MODES
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
                    agentInboxDriveEnabled = hasAgentInboxFolderGrant,
                    agentInboxDriveFolderId = agentInboxDriveFolderId.takeIf { hasAgentInboxFolderGrant },
                    agentInboxDriveGrantMode = agentInboxDriveGrantMode.takeIf { hasAgentInboxFolderGrant },
                    agentInboxDriveLastSuccessfulAtMillis =
                        preferences[AgentInboxDriveLastSuccessfulAtMillis].takeIf { hasAgentInboxFolderGrant },
                    agentInboxDriveLastError = preferences[AgentInboxDriveLastError],
                    profileAutosaveUri = preferences[ProfileAutosaveUri],
                    profileAutosaveDisplayName = preferences[ProfileAutosaveDisplayName],
                    profileAutosaveLastSuccessfulAtMillis = preferences[ProfileAutosaveLastSuccessfulAtMillis],
                    profileAutosaveLastError = preferences[ProfileAutosaveLastError],
                    websiteRules = preferences[WebsiteRules].orEmpty().mapNotNull(::decodeWebsiteRule)
                        .sortedWith(compareBy<WebsiteRule> { it.createdAtMillis }.thenBy { it.host }.thenBy { it.id }),
                )
            }
    }

    override fun supportedDistractingApps(): List<DistractingApp> {
        return (supportedApps + customTargetAppCandidates().filter(CustomTargetAppCandidate::isEligible).map { it.app })
            .distinctBy(DistractingApp::packageName)
    }

    override fun customTargetAppCandidates(): List<CustomTargetAppCandidate> {
        return customTargetCandidatesProvider()
            .distinctBy { candidate -> candidate.app.packageName }
            .sortedWith(
                compareBy<CustomTargetAppCandidate> { !it.isEligible }
                    .thenBy { it.app.displayName.lowercase() }
                    .thenBy { it.app.packageName },
            )
    }

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
            preferences[WebsiteRules] = settings.websiteRules.mapTo(mutableSetOf(), ::encodeWebsiteRule)
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

    override suspend fun saveWebsiteRules(rules: List<WebsiteRule>) {
        dataStore.edit { preferences ->
            preferences[WebsiteRules] = rules
                .distinctBy(WebsiteRule::id)
                .mapTo(mutableSetOf(), ::encodeWebsiteRule)
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

    override suspend fun saveAgentInboxDriveConnection(folderId: String?, grantMode: String) {
        dataStore.edit { preferences ->
            if (folderId.isNullOrBlank()) {
                preferences.remove(AgentInboxDriveEnabled)
                preferences.remove(AgentInboxDriveFolderId)
                preferences.remove(AgentInboxDriveGrantMode)
            } else {
                preferences[AgentInboxDriveEnabled] = true
                preferences[AgentInboxDriveFolderId] = folderId
                preferences[AgentInboxDriveGrantMode] = grantMode.takeIf(AGENT_INBOX_SUPPORTED_GRANT_MODES::contains)
                    ?: AGENT_INBOX_DRIVE_GRANT_MODE_PICKER_FOLDER
            }
            preferences.remove(AgentInboxDriveLastError)
        }
    }

    override suspend fun clearAgentInboxDriveConnection() {
        dataStore.edit { preferences ->
            preferences.remove(AgentInboxDriveEnabled)
            preferences.remove(AgentInboxDriveFolderId)
            preferences.remove(AgentInboxDriveGrantMode)
            preferences.remove(AgentInboxDriveLastSuccessfulAtMillis)
            preferences.remove(AgentInboxDriveLastError)
        }
    }

    override suspend fun saveAgentInboxDriveScanSuccess(timestampMillis: Long, folderId: String) {
        dataStore.edit { preferences ->
            val currentFolderId = preferences[AgentInboxDriveFolderId]?.takeIf(String::isNotBlank)
            if (preferences[AgentInboxDriveGrantMode] in AGENT_INBOX_SUPPORTED_GRANT_MODES &&
                currentFolderId != null &&
                folderId == currentFolderId
            ) {
                preferences[AgentInboxDriveEnabled] = true
                preferences[AgentInboxDriveFolderId] = currentFolderId
                preferences[AgentInboxDriveLastSuccessfulAtMillis] = timestampMillis
            } else {
                preferences.remove(AgentInboxDriveEnabled)
                preferences.remove(AgentInboxDriveFolderId)
                preferences.remove(AgentInboxDriveGrantMode)
                preferences.remove(AgentInboxDriveLastSuccessfulAtMillis)
            }
            preferences.remove(AgentInboxDriveLastError)
        }
    }

    override suspend fun saveAgentInboxDriveScanFailure(errorMessage: String) {
        dataStore.edit { preferences ->
            preferences[AgentInboxDriveLastError] = errorMessage
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

        fun encodeWebsiteRule(rule: WebsiteRule): String {
            return listOf(
                rule.id,
                rule.type.name,
                rule.host,
                rule.includeApex.toString(),
                rule.enabled.toString(),
                rule.createdAtMillis.coerceAtLeast(0L).toString(),
                rule.updatedAtMillis.coerceAtLeast(0L).toString(),
            ).joinToString(WebsiteRuleDelimiter)
        }

        fun decodeWebsiteRule(raw: String): WebsiteRule? {
            val parts = raw.split(WebsiteRuleDelimiter)
            if (parts.size != 7) return null
            val type = runCatching { WebsiteRuleType.valueOf(parts[1]) }.getOrNull() ?: return null
            val normalized = WebsiteRuleNormalizer.normalize(
                input = parts[2],
                wildcard = type == WebsiteRuleType.WILDCARD_SUBDOMAINS,
            ) as? WebsiteRuleDraftResult.Valid ?: return null
            if (normalized.type != type || normalized.host != parts[2]) return null
            val createdAtMillis = parts[5].toLongOrNull()?.coerceAtLeast(0L) ?: return null
            val updatedAtMillis = parts[6].toLongOrNull()?.coerceAtLeast(createdAtMillis) ?: return null
            return WebsiteRule(
                id = parts[0].takeIf { it.matches(WebsiteRuleIdRegex) } ?: return null,
                type = type,
                host = normalized.host,
                includeApex = type == WebsiteRuleType.WILDCARD_SUBDOMAINS && (parts[3].toBooleanStrictOrNull() ?: false),
                enabled = parts[4].toBooleanStrictOrNull() ?: true,
                createdAtMillis = createdAtMillis,
                updatedAtMillis = updatedAtMillis,
            )
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
        val AgentInboxDriveEnabled = booleanPreferencesKey("agent_inbox_drive_enabled")
        val AgentInboxDriveFolderId = stringPreferencesKey("agent_inbox_drive_folder_id")
        val AgentInboxDriveGrantMode = stringPreferencesKey("agent_inbox_drive_grant_mode")
        val AgentInboxDriveLastSuccessfulAtMillis = longPreferencesKey("agent_inbox_drive_last_successful_at_millis")
        val AgentInboxDriveLastError = stringPreferencesKey("agent_inbox_drive_last_error")
        val AGENT_INBOX_SUPPORTED_GRANT_MODES = setOf(
            AGENT_INBOX_DRIVE_GRANT_MODE_PICKER_FOLDER,
            AGENT_INBOX_DRIVE_GRANT_MODE_READONLY_FOLDER,
        )
        val ProfileAutosaveUri = stringPreferencesKey("profile_autosave_uri")
        val ProfileAutosaveDisplayName = stringPreferencesKey("profile_autosave_display_name")
        val ProfileAutosaveLastSuccessfulAtMillis = longPreferencesKey("profile_autosave_last_successful_at_millis")
        val ProfileAutosaveLastError = stringPreferencesKey("profile_autosave_last_error")
        val WebsiteRules = stringSetPreferencesKey("website_rules_v1")
        const val WebsiteRuleDelimiter = "\u001F"
        val WebsiteRuleIdRegex = Regex("^website-rule-[0-9a-fA-F-]{36}$")
    }
}
