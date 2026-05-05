package com.qualityalternative.app.domain.model

data class DistractingApp(
    val packageName: String,
    val displayName: String,
)

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

const val DEFAULT_READER_FONT_SCALE = 1.0
const val MIN_READER_FONT_SCALE = 0.80
const val MAX_READER_FONT_SCALE = 1.60

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
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
    val meditationDurationMinutes: Int = DEFAULT_MEDITATION_MINUTES,
    val contentPriority: ContentPriority = ContentPriority.BALANCED,
    val priorityContentIds: Set<String> = emptySet(),
    val reactivatedCompletedContentIds: Set<String> = emptySet(),
    val openAnywayUnlockMinutes: Int = DEFAULT_OPEN_ANYWAY_UNLOCK_MINUTES,
    val readerFontScale: Double = DEFAULT_READER_FONT_SCALE,
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
