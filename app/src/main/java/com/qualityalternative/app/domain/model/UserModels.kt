package com.qualityalternative.app.domain.model

import java.time.Instant
import java.time.ZoneId

data class DistractingApp(
    val packageName: String,
    val displayName: String,
)

enum class CustomTargetAppEligibility {
    ELIGIBLE,
    EXCLUDED_SELF,
    EXCLUDED_LAUNCHER,
    EXCLUDED_SETTINGS_OR_PERMISSION,
    EXCLUDED_PHONE_OR_EMERGENCY,
    EXCLUDED_INSTALLER,
    EXCLUDED_DOCUMENTS_OR_FILE_PICKER,
    EXCLUDED_SYSTEM_CRITICAL,
    EXCLUDED_NOT_LAUNCHABLE,
}

data class CustomTargetAppCandidate(
    val app: DistractingApp,
    val eligibility: CustomTargetAppEligibility,
    val exclusionReason: String? = null,
) {
    val isEligible: Boolean
        get() = eligibility == CustomTargetAppEligibility.ELIGIBLE
}

enum class WebsiteRuleType {
    EXACT_DOMAIN,
    WILDCARD_SUBDOMAINS,
}

data class WebsiteRule(
    val id: String,
    val type: WebsiteRuleType,
    val host: String,
    val includeApex: Boolean = false,
    val enabled: Boolean = true,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
) {
    val displayPattern: String
        get() = when (type) {
            WebsiteRuleType.EXACT_DOMAIN -> host
            WebsiteRuleType.WILDCARD_SUBDOMAINS -> if (includeApex) "*.$host + $host" else "*.$host"
        }
}

data class UserPreferences(
    val selectedApps: List<DistractingApp>,
    val preferredTopics: Set<TopicTag>,
    val preferredDurationBucket: DurationBucket,
    val selectedPackIds: Set<String>,
    val meditationDurationMinutes: Int = DEFAULT_MEDITATION_MINUTES,
    val contentPriority: ContentPriority = ContentPriority.BALANCED,
    val priorityContentIds: Set<String> = emptySet(),
    val unfinishedContentIds: Set<String> = emptySet(),
)

const val DEFAULT_OPEN_ANYWAY_UNLOCK_MINUTES = 60
const val MIN_OPEN_ANYWAY_UNLOCK_MINUTES = 15
const val MAX_OPEN_ANYWAY_UNLOCK_MINUTES = 240

val OpenAnywayUnlockMinuteOptions = listOf(15, 30, 60, 120)

const val DEFAULT_BEDTIME_ENABLED = false
const val DEFAULT_BEDTIME_START_MINUTES = 22 * 60 + 30
const val DEFAULT_BEDTIME_END_MINUTES = 7 * 60
const val MIN_BEDTIME_MINUTES = 0
const val MAX_BEDTIME_MINUTES = 23 * 60 + 59
const val BEDTIME_OPEN_ANYWAY_UNLOCK_DELAY_MILLIS = 60_000L

val BedtimeStartMinuteOptions = listOf(21 * 60 + 30, 22 * 60, DEFAULT_BEDTIME_START_MINUTES, 23 * 60)
val BedtimeEndMinuteOptions = listOf(6 * 60, 6 * 60 + 30, DEFAULT_BEDTIME_END_MINUTES, 7 * 60 + 30, 8 * 60)

fun bedtimeWindowIsActive(
    enabled: Boolean,
    startMinutes: Int,
    endMinutes: Int,
    nowMillis: Long,
    zoneId: ZoneId = ZoneId.systemDefault(),
): Boolean {
    if (!enabled) return false
    val safeStart = startMinutes.coerceIn(MIN_BEDTIME_MINUTES, MAX_BEDTIME_MINUTES)
    val safeEnd = endMinutes.coerceIn(MIN_BEDTIME_MINUTES, MAX_BEDTIME_MINUTES)
    if (safeStart == safeEnd) return true
    val now = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalTime()
    val currentMinutes = now.hour * 60 + now.minute
    return if (safeStart < safeEnd) {
        currentMinutes in safeStart until safeEnd
    } else {
        currentMinutes >= safeStart || currentMinutes < safeEnd
    }
}

const val DEFAULT_READER_FONT_SCALE = 1.0
const val MIN_READER_FONT_SCALE = 0.80
const val MAX_READER_FONT_SCALE = 1.60
const val DEFAULT_INTERFACE_TEXT_SCALE = 1.0
const val MIN_INTERFACE_TEXT_SCALE = 0.90
const val MAX_INTERFACE_TEXT_SCALE = 1.30

enum class InterventionMode {
    SOFT,
    FIRM,
}

val DEFAULT_INTERVENTION_MODE = InterventionMode.SOFT

enum class AppThemeMode {
    LIGHT,
    DARK,
}

enum class ContentPriority {
    BALANCED,
    READINGS,
    MY_FILES,
    SAVED_LINKS,
    MEDITATION,
}

data class AppSettings(
    val hasCompletedOnboarding: Boolean,
    val selectedAppPackages: Set<String>,
    val preferredTopics: Set<TopicTag>,
    val preferredDurationBucket: DurationBucket,
    val selectedPackIds: Set<String>,
    val interventionMode: InterventionMode = DEFAULT_INTERVENTION_MODE,
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
    val meditationDurationMinutes: Int = DEFAULT_MEDITATION_MINUTES,
    val contentPriority: ContentPriority = ContentPriority.BALANCED,
    val priorityContentIds: Set<String> = emptySet(),
    val reactivatedCompletedContentIds: Set<String> = emptySet(),
    val openAnywayUnlockMinutes: Int = DEFAULT_OPEN_ANYWAY_UNLOCK_MINUTES,
    val bedtimeEnabled: Boolean = DEFAULT_BEDTIME_ENABLED,
    val bedtimeStartMinutes: Int = DEFAULT_BEDTIME_START_MINUTES,
    val bedtimeEndMinutes: Int = DEFAULT_BEDTIME_END_MINUTES,
    val readerFontScale: Double = DEFAULT_READER_FONT_SCALE,
    val interfaceTextScale: Double = DEFAULT_INTERFACE_TEXT_SCALE,
    val annotationExportUri: String? = null,
    val annotationExportDisplayName: String? = null,
    val annotationExportLastSuccessfulAtMillis: Long? = null,
    val annotationExportLastError: String? = null,
    val annotationDriveSyncEnabled: Boolean = false,
    val annotationDriveFolderId: String? = null,
    val annotationDriveLastSuccessfulAtMillis: Long? = null,
    val annotationDriveLastError: String? = null,
    val profileAutosaveUri: String? = null,
    val profileAutosaveDisplayName: String? = null,
    val profileAutosaveLastSuccessfulAtMillis: Long? = null,
    val profileAutosaveLastError: String? = null,
    val websiteRules: List<WebsiteRule> = emptyList(),
)

data class LocalProfileIdentity(
    val profileId: String,
    val createdAtMillis: Long,
)

data class OnboardingSelection(
    val selectedAppPackages: Set<String>,
    val preferredTopics: Set<TopicTag>,
    val preferredDurationBucket: DurationBucket,
    val selectedPackIds: Set<String>,
) {
    fun isValid(minApps: Int = 3, minTopics: Int = 3): Boolean {
        return selectedAppPackages.size >= minApps &&
            preferredTopics.size >= minTopics &&
            selectedPackIds.isNotEmpty()
    }
}

data class DelayWindow(
    val id: String,
    val targetAppPackage: String,
    val startsAtMillis: Long,
    val endsAtMillis: Long,
    val interventionId: String? = null,
    val interventionShownAtMillis: Long? = null,
    val primaryContentId: String? = null,
    val backupContentIds: List<String> = emptyList(),
    val firstReturnAttemptAtMillis: Long? = null,
) {
    fun isActive(nowMillis: Long): Boolean = nowMillis < endsAtMillis
}

data class DelayInspection(
    val activeWindow: DelayWindow? = null,
    val expiredWindow: DelayWindow? = null,
)
