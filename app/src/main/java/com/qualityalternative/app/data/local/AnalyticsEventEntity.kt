package com.qualityalternative.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analytics_events")
data class AnalyticsEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val timestampMillis: Long,
    val interventionId: String?,
    val sessionId: String?,
    val targetAppPackage: String?,
    val primaryContentId: String?,
    val backupContentIdsCsv: String,
    val contentId: String?,
    val metadataJson: String,
)
