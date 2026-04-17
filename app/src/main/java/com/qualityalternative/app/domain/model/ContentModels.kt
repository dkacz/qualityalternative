package com.qualityalternative.app.domain.model

enum class ContentFormat {
    MARKDOWN,
    HTML,
}

enum class TopicTag {
    PHILOSOPHY,
    SCIENCE,
    HISTORY,
    ECONOMICS,
    CREATIVITY,
    PSYCHOLOGY,
}

enum class DurationBucket(val minMinutes: Int, val maxMinutes: Int) {
    QUICK(minMinutes = 3, maxMinutes = 5),
    FOCUS(minMinutes = 5, maxMinutes = 10),
    DEEP(minMinutes = 10, maxMinutes = 20),
    ;

    val midpoint: Int
        get() = (minMinutes + maxMinutes) / 2

    fun contains(minutes: Int): Boolean = minutes in minMinutes..maxMinutes
}

data class ContentItem(
    val id: String,
    val packId: String,
    val title: String,
    val description: String,
    val durationMinutes: Int,
    val format: ContentFormat,
    val topicTags: Set<TopicTag>,
    val bodyAssetPath: String,
)

data class EditorialPack(
    val id: String,
    val title: String,
    val description: String,
    val items: List<ContentItem>,
)

data class RecommendationSet(
    val primary: ContentItem,
    val backups: List<ContentItem>,
    val inventoryShortage: Boolean,
    val generatedAtMillis: Long,
)
