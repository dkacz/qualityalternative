package com.qualityalternative.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reading_annotations",
    indices = [
        Index(value = ["contentId"]),
        Index(value = ["contentId", "paragraphIndex"]),
        Index(value = ["updatedAtMillis"]),
    ],
)
data class ReadingAnnotationEntity(
    @PrimaryKey val id: String,
    val contentId: String,
    val paragraphIndex: Int,
    val quotedText: String,
    val noteText: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val sourceTitle: String,
    val sourceLabel: String?,
    val sourceType: String?,
    val sourceFormat: String?,
    val sourceHref: String?,
    val sourceAnchor: String?,
    val sourceBlockIndex: Int,
    val endSourceBlockIndex: Int,
    val textStartOffset: Int,
    val textEndOffset: Int,
    val prefixText: String,
    val suffixText: String,
)
