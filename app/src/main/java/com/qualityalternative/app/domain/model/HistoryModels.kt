package com.qualityalternative.app.domain.model

import java.time.Instant
import java.time.ZoneId

enum class RecommendationSource {
    PRIMARY,
    BACKUP,
}

enum class TimeOfDayBucket {
    MORNING,
    MIDDAY,
    EVENING,
    NIGHT,
    ;

    companion object {
        fun from(timestampMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): TimeOfDayBucket {
            val hour = Instant.ofEpochMilli(timestampMillis).atZone(zoneId).hour
            return when (hour) {
                in 5..10 -> MORNING
                in 11..15 -> MIDDAY
                in 16..21 -> EVENING
                else -> NIGHT
            }
        }
    }
}

data class RecommendationSignals(
    val completedTopics: Set<TopicTag> = emptySet(),
    val skippedTopics: Set<TopicTag> = emptySet(),
    val successfulPackIds: Set<String> = emptySet(),
    val timeOfDay: TimeOfDayBucket,
)

data class ReplacementHistoryEntry(
    val sessionId: String,
    val targetAppPackage: String,
    val targetAppDisplayName: String,
    val contentId: String,
    val contentTitle: String,
    val contentDescription: String,
    val contentTopics: Set<TopicTag>,
    val packId: String,
    val recommendationSource: RecommendationSource,
    val acceptedAtMillis: Long,
    val completedAtMillis: Long? = null,
    val skippedAtMillis: Long? = null,
    val returnedToTargetAtMillis: Long? = null,
    val feedbackGoodFit: Boolean? = null,
    val feedbackHelpedAvoidScrolling: Boolean? = null,
) {
    fun isCompleted(): Boolean = completedAtMillis != null

    fun isSkipped(): Boolean = skippedAtMillis != null

    fun returnedToTarget(): Boolean = returnedToTargetAtMillis != null

    fun lastInteractionAtMillis(): Long {
        return listOfNotNull(
            returnedToTargetAtMillis,
            completedAtMillis,
            skippedAtMillis,
            acceptedAtMillis,
        ).max()
    }
}

data class ReturnToTargetSignal(
    val sessionId: String,
    val targetAppPackage: String,
    val contentId: String,
    val returnedAtMillis: Long,
    val within15Minutes: Boolean,
    val within60Minutes: Boolean,
)

enum class PermissionStatus {
    READY,
    MISSING,
    UNAVAILABLE_IN_BUILD,
}

data class PermissionReadiness(
    val overlayStatus: PermissionStatus,
    val accessibilityStatus: PermissionStatus,
    val interceptionReady: Boolean,
    val summary: String,
)
