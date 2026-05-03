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
)
