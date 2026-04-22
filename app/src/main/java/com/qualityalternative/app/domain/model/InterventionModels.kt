package com.qualityalternative.app.domain.model

enum class InterventionAction {
    PRIMARY_ACCEPTED,
    BACKUP_ACCEPTED,
    DELAY_SELECTED,
    DELAY_ALTERNATIVE_STARTED,
    OPEN_ANYWAY_SELECTED,
}

data class InterventionDecision(
    val action: InterventionAction,
    val targetAppPackage: String,
    val contentId: String? = null,
    val occurredAtMillis: Long,
)

data class SessionFeedback(
    val wasGoodFit: Boolean,
    val helpedAvoidScrolling: Boolean,
    val fitRating: String? = null,
    val scrollRating: String? = null,
    val submittedAtMillis: Long,
)

enum class AnalyticsEventType {
    TARGET_APP_FOREGROUND_DETECTED,
    INTERVENTION_SHOWN,
    INTERVENTION_DEGRADED_PERFORMANCE,
    NO_RECOMMENDATION_AVAILABLE,
    PRIMARY_ACCEPTED,
    BACKUP_ACCEPTED,
    DELAY_SELECTED,
    DELAY_ALTERNATIVE_STARTED,
    OPEN_ANYWAY_SELECTED,
    READER_COMPLETED,
    READER_SKIPPED,
    FEEDBACK_SUBMITTED,
    INVENTORY_SHORTAGE,
    RETURN_TO_APP_WITHIN_15_MINUTES,
    RETURN_TO_APP_WITHIN_60_MINUTES,
    RETURN_AFTER_DELAY_ENDED,
    MEDITATION_TIMER_COMPLETED,
    MEDITATION_TIMER_SKIPPED,
    USER_LINK_ADDED,
    USER_LINK_FALLBACK_OPENED,
    USER_LINK_HANDOFF_FAILED,
}

data class AnalyticsEvent(
    val type: AnalyticsEventType,
    val timestampMillis: Long,
    val semanticKey: String? = null,
    val interventionId: String? = null,
    val sessionId: String? = null,
    val targetAppPackage: String? = null,
    val primaryContentId: String? = null,
    val backupContentIds: List<String> = emptyList(),
    val contentId: String? = null,
    val metadata: Map<String, String> = emptyMap(),
)
