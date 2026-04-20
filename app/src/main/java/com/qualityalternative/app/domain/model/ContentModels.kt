package com.qualityalternative.app.domain.model

enum class ContentFormat {
    MARKDOWN,
    HTML,
}

enum class ContentSourceType {
    EDITORIAL,
    USER_LINK,
}

enum class ContentAvailability {
    AVAILABLE,
    UNAVAILABLE,
    NEEDS_FALLBACK,
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
    val bodyAssetPath: String? = null,
    val externalUrl: String? = null,
    val sourceType: ContentSourceType = ContentSourceType.EDITORIAL,
    val availability: ContentAvailability = ContentAvailability.AVAILABLE,
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

data class UserLinkDraft(
    val url: String,
    val title: String,
    val description: String = "",
    val durationMinutes: Int,
    val topicTags: Set<TopicTag>,
)

enum class UserLinkValidationError {
    EMPTY_URL,
    UNSUPPORTED_SCHEME,
    MISSING_HOST,
    BLANK_TITLE,
    INVALID_DURATION,
    NO_TOPICS,
}

data class UserLinkValidationResult(
    val normalizedUrl: String? = null,
    val errors: Set<UserLinkValidationError> = emptySet(),
) {
    val isValid: Boolean
        get() = errors.isEmpty()
}
