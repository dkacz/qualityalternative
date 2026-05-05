package com.qualityalternative.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_documents",
    indices = [
        Index(value = ["uri"], unique = true),
        Index(value = ["documentFingerprintSha256"]),
    ],
)
data class UserDocumentEntity(
    @PrimaryKey val id: String,
    val uri: String,
    val displayName: String,
    val mimeType: String?,
    val documentFormat: String,
    val title: String,
    val description: String,
    val durationMinutes: Int,
    val topicTagsCsv: String,
    val availability: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val documentFingerprintSha256: String?,
)
