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
)

data class AppSettings(
    val hasCompletedOnboarding: Boolean,
    val selectedAppPackages: Set<String>,
    val preferredTopics: Set<TopicTag>,
    val preferredDurationBucket: DurationBucket,
    val selectedPackIds: Set<String>,
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
) {
    fun isActive(nowMillis: Long): Boolean = nowMillis < endsAtMillis
}

data class DelayInspection(
    val activeWindow: DelayWindow? = null,
    val expiredWindow: DelayWindow? = null,
)
