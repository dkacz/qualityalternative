package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.model.UserDocumentDraft
import com.qualityalternative.app.domain.model.UserDocumentValidationError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserDocumentValidatorTest {
    @Test
    fun detectFormat_acceptsPdfMarkdownAndEpubFromMimeOrExtension() {
        assertEquals(ContentFormat.PDF, UserDocumentValidator.detectFormat("essay.bin", "application/pdf"))
        assertEquals(ContentFormat.MARKDOWN, UserDocumentValidator.detectFormat("essay.bin", "text/markdown"))
        assertEquals(ContentFormat.MARKDOWN, UserDocumentValidator.detectFormat("notes.md", null))
        assertEquals(ContentFormat.MARKDOWN, UserDocumentValidator.detectFormat("notes.md", "text/plain"))
        assertEquals(ContentFormat.MARKDOWN, UserDocumentValidator.detectFormat("notes.markdown", "application/octet-stream"))
        assertEquals(ContentFormat.EPUB, UserDocumentValidator.detectFormat("book.epub", null))
    }

    @Test
    fun detectFormat_doesNotTreatPlainTextAsMarkdownWithoutMarkdownExtension() {
        assertEquals(null, UserDocumentValidator.detectFormat("notes.txt", "text/plain"))
    }

    @Test
    fun validate_rejectsUnsupportedDocumentOrMissingMetadata() {
        val result = UserDocumentValidator.validate(
            UserDocumentDraft(
                uri = "",
                displayName = "archive.zip",
                mimeType = "application/zip",
                title = "",
                durationMinutes = 0,
                topicTags = emptySet(),
            ),
        )

        assertEquals(
            setOf(
                UserDocumentValidationError.EMPTY_URI,
                UserDocumentValidationError.UNSUPPORTED_FORMAT,
                UserDocumentValidationError.BLANK_TITLE,
                UserDocumentValidationError.INVALID_DURATION,
                UserDocumentValidationError.NO_TOPICS,
            ),
            result.errors,
        )
    }

    @Test
    fun validate_acceptsPrivateMarkdownDocument() {
        val result = UserDocumentValidator.validate(
            UserDocumentDraft(
                uri = "content://docs/notes",
                displayName = "notes.md",
                mimeType = "text/plain",
                title = "Notes",
                durationMinutes = 8,
                topicTags = setOf(TopicTag.PSYCHOLOGY),
            ),
        )

        assertTrue(result.isValid)
        assertEquals(ContentFormat.MARKDOWN, result.format)
    }

    @Test
    fun validate_rejectsDocumentSessionEstimateOutsideThreeToTwentyMinutes() {
        val tooLong = UserDocumentValidator.validate(
            UserDocumentDraft(
                uri = "content://docs/book",
                displayName = "book.epub",
                mimeType = "application/epub+zip",
                title = "Book",
                durationMinutes = 60,
                topicTags = setOf(TopicTag.HISTORY),
            ),
        )

        assertEquals(setOf(UserDocumentValidationError.INVALID_DURATION), tooLong.errors)
    }
}
