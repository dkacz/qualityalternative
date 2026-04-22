package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.UserDocumentDraft
import com.qualityalternative.app.domain.model.UserDocumentValidationError
import com.qualityalternative.app.domain.model.UserDocumentValidationResult
import java.util.Locale

object UserDocumentValidator {
    fun validate(draft: UserDocumentDraft): UserDocumentValidationResult {
        val format = detectFormat(displayName = draft.displayName, mimeType = draft.mimeType)
        val errors = mutableSetOf<UserDocumentValidationError>()

        if (draft.uri.isBlank()) {
            errors += UserDocumentValidationError.EMPTY_URI
        }
        if (format == null) {
            errors += UserDocumentValidationError.UNSUPPORTED_FORMAT
        }
        if (draft.title.isBlank()) {
            errors += UserDocumentValidationError.BLANK_TITLE
        }
        if (draft.durationMinutes !in 1..120) {
            errors += UserDocumentValidationError.INVALID_DURATION
        }
        if (draft.topicTags.isEmpty()) {
            errors += UserDocumentValidationError.NO_TOPICS
        }

        return UserDocumentValidationResult(format = format, errors = errors)
    }

    fun detectFormat(displayName: String, mimeType: String?): ContentFormat? {
        val normalizedMime = mimeType?.lowercase(Locale.US).orEmpty()
        val normalizedName = displayName.substringBefore('?').lowercase(Locale.US)
        val hasMarkdownExtension = normalizedName.endsWith(".md") || normalizedName.endsWith(".markdown")
        return when {
            normalizedMime in MARKDOWN_MIME_TYPES || hasMarkdownExtension -> ContentFormat.MARKDOWN

            normalizedMime == "application/pdf" || normalizedName.endsWith(".pdf") -> ContentFormat.PDF

            normalizedMime == "application/epub+zip" || normalizedName.endsWith(".epub") -> ContentFormat.EPUB

            else -> null
        }
    }

    private val MARKDOWN_MIME_TYPES = setOf(
        "text/markdown",
        "text/x-markdown",
    )
}
