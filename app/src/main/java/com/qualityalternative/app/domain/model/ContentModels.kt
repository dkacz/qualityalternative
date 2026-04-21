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

enum class ContentRightsClass {
    RENDERABLE,
    LINK_ONLY,
    USER_PRIVATE,
}

enum class ContentRenderMode {
    IN_APP_READER,
    EXTERNAL_HANDOFF,
    USER_PRIVATE_READER,
}

data class ContentRightsMetadata(
    val rightsClass: ContentRightsClass,
    val renderMode: ContentRenderMode,
    val licenseName: String? = null,
    val licenseUrl: String? = null,
    val sourceUrl: String? = null,
    val attribution: String? = null,
    val rightsReviewedAt: String? = null,
) {
    val usesInAppReader: Boolean
        get() = renderMode == ContentRenderMode.IN_APP_READER

    val usesUserPrivateReader: Boolean
        get() = renderMode == ContentRenderMode.USER_PRIVATE_READER

    val usesRepositoryBody: Boolean
        get() = usesInAppReader || usesUserPrivateReader

    val usesExternalHandoff: Boolean
        get() = renderMode == ContentRenderMode.EXTERNAL_HANDOFF

    companion object {
        fun safeDefault(): ContentRightsMetadata = ContentRightsMetadata(
            rightsClass = ContentRightsClass.LINK_ONLY,
            renderMode = ContentRenderMode.EXTERNAL_HANDOFF,
        )

        fun renderableEditorial(
            licenseName: String? = null,
            licenseUrl: String? = null,
            sourceUrl: String? = null,
            attribution: String? = null,
            rightsReviewedAt: String? = null,
        ): ContentRightsMetadata = ContentRightsMetadata(
            rightsClass = ContentRightsClass.RENDERABLE,
            renderMode = ContentRenderMode.IN_APP_READER,
            licenseName = licenseName,
            licenseUrl = licenseUrl,
            sourceUrl = sourceUrl,
            attribution = attribution,
            rightsReviewedAt = rightsReviewedAt,
        )

        fun userPrivateExternal(
            sourceUrl: String? = null,
            attribution: String? = null,
            rightsReviewedAt: String? = null,
        ): ContentRightsMetadata = ContentRightsMetadata(
            rightsClass = ContentRightsClass.USER_PRIVATE,
            renderMode = ContentRenderMode.EXTERNAL_HANDOFF,
            sourceUrl = sourceUrl,
            attribution = attribution,
            rightsReviewedAt = rightsReviewedAt,
        )
    }
}

enum class TopicTag {
    ESSAYS,
    PHILOSOPHY,
    SCIENCE,
    DESIGN,
    POETRY,
    HISTORY,
    TECH,
    FICTION,
    CLIMATE,
    ECONOMICS,
    FOOD,
    ARCHITECTURE,
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
    val sourceLabel: String? = null,
    val sourceType: ContentSourceType = ContentSourceType.EDITORIAL,
    val availability: ContentAvailability = ContentAvailability.AVAILABLE,
    val rights: ContentRightsMetadata = ContentRightsMetadata.safeDefault(),
)

fun ContentItem.usesExternalHandoff(): Boolean = rights.usesExternalHandoff

fun ContentItem.usesRepositoryBody(): Boolean = rights.usesRepositoryBody

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
