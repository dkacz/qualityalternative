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
    INTERVENTION_SHOWN,
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
}

data class AnalyticsEvent(
    val type: AnalyticsEventType,
    val timestampMillis: Long,
    val targetAppPackage: String? = null,
    val contentId: String? = null,
    val metadata: Map<String, String> = emptyMap(),
)
