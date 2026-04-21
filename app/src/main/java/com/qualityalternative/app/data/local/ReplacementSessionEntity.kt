package com.qualityalternative.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "replacement_sessions")
data class ReplacementSessionEntity(
    @PrimaryKey val sessionId: String,
    val interventionId: String,
    val targetAppPackage: String,
    val targetAppDisplayName: String,
    val interventionShownAtMillis: Long,
    val primaryContentId: String,
    val backupContentIdsCsv: String,
    val contentId: String,
    val contentTitle: String,
    val contentDescription: String,
    val contentTopicsCsv: String,
    val packId: String,
    val recommendationSource: String,
    val acceptedAtMillis: Long,
    val completedAtMillis: Long?,
    val skippedAtMillis: Long?,
    val returnedToTargetAtMillis: Long?,
    val feedbackGoodFit: Boolean?,
    val feedbackHelpedAvoidScrolling: Boolean?,
    val feedbackFitRating: String?,
    val feedbackScrollRating: String?,
)
