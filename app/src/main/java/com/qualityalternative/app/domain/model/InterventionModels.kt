package com.qualityalternative.app.domain.model

enum class InterventionAction {
    PRIMARY_ACCEPTED,
    BACKUP_ACCEPTED,
    DELAY_SELECTED,
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
    OPEN_ANYWAY_SELECTED,
    READER_COMPLETED,
    READER_SKIPPED,
    FEEDBACK_SUBMITTED,
    INVENTORY_SHORTAGE,
    RETURN_TO_APP_WITHIN_15_MINUTES,
    RETURN_TO_APP_WITHIN_60_MINUTES,
    RETURN_AFTER_DELAY_ENDED,
    USER_LINK_ADDED,
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
