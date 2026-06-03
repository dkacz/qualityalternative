package com.qualityalternative.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "analytics_events",
    indices = [
        Index(value = ["semanticKey"], unique = true),
        Index(value = ["timestampMillis"]),
    ],
)
data class AnalyticsEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val timestampMillis: Long,
    val semanticKey: String?,
    val interventionId: String?,
    val sessionId: String?,
    val targetAppPackage: String?,
    val primaryContentId: String?,
    val backupContentIdsCsv: String,
    val contentId: String?,
    val metadataJson: String,
)
