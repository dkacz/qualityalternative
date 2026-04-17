package com.qualityalternative.app.domain.model

data class DistractingApp(
    val packageName: String,
    val displayName: String,
)

data class UserPreferences(
    val selectedApps: List<DistractingApp>,
    val preferredTopics: Set<TopicTag>,
    val preferredDurationBucket: DurationBucket,
)

data class DelayWindow(
    val targetAppPackage: String,
    val startsAtMillis: Long,
    val endsAtMillis: Long,
) {
    fun isActive(nowMillis: Long): Boolean = nowMillis < endsAtMillis
}
