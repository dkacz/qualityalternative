package com.qualityalternative.app.domain.model

data class ReadingAnnotation(
    val id: String,
    val contentId: String,
    val paragraphIndex: Int,
    val quotedText: String,
    val noteText: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)

data class ReadingAnnotationDraft(
    val id: String? = null,
    val contentId: String,
    val paragraphIndex: Int,
    val quotedText: String,
    val noteText: String,
)

fun ReadingAnnotation.fragmentKey(): String {
    return "$contentId#$paragraphIndex"
}
