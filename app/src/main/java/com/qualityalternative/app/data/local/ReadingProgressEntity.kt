package com.qualityalternative.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reading_progress",
    indices = [Index(value = ["updatedAtMillis"])],
)
data class ReadingProgressEntity(
    @PrimaryKey val contentId: String,
    val progressPercent: Int,
    val lastVisibleParagraphIndex: Int,
    val paragraphCount: Int,
    val updatedAtMillis: Long,
    val completedAtMillis: Long?,
)
