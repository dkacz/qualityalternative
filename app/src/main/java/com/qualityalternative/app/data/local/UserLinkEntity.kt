package com.qualityalternative.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_links",
    indices = [
        Index(value = ["normalizedUrl"], unique = true),
    ],
)
data class UserLinkEntity(
    @PrimaryKey val id: String,
    val normalizedUrl: String,
    val title: String,
    val description: String,
    val durationMinutes: Int,
    val topicTagsCsv: String,
    val availability: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
