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
)

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
