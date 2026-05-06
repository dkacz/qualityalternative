package com.qualityalternative.app.domain.model

data class ReadingAnnotation(
    val id: String,
    val contentId: String,
    val paragraphIndex: Int,
    val quotedText: String,
    val noteText: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val sourceTitle: String = "",
    val sourceLabel: String? = null,
    val sourceType: ContentSourceType? = null,
    val sourceFormat: ContentFormat? = null,
    val selector: ReadingAnnotationSelector = ReadingAnnotationSelector(),
)

data class ReadingAnnotationDraft(
    val id: String? = null,
    val contentId: String,
    val paragraphIndex: Int,
    val quotedText: String,
    val noteText: String,
    val sourceTitle: String = "",
    val sourceLabel: String? = null,
    val sourceType: ContentSourceType? = null,
    val sourceFormat: ContentFormat? = null,
    val selector: ReadingAnnotationSelector = ReadingAnnotationSelector(),
)

data class ReadingAnnotationSelector(
    val sourceHref: String? = null,
    val sourceAnchor: String? = null,
    val sourceBlockIndex: Int = 0,
    val endSourceBlockIndex: Int = sourceBlockIndex,
    val textStartOffset: Int = 0,
    val textEndOffset: Int = 0,
    val prefixText: String = "",
    val suffixText: String = "",
)

fun ReadingAnnotation.fragmentKey(): String {
    return "$contentId#$paragraphIndex"
}
