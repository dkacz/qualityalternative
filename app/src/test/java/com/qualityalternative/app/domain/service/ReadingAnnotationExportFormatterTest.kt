package com.qualityalternative.app.domain.service

import com.qualityalternative.app.domain.model.ContentAvailability
import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.ContentRightsMetadata
import com.qualityalternative.app.domain.model.ContentSourceType
import com.qualityalternative.app.domain.model.ReadingAnnotation
import com.qualityalternative.app.domain.model.ReadingAnnotationSelector
import com.qualityalternative.app.domain.model.TopicTag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadingAnnotationExportFormatterTest {
    @Test
    fun formatJsonLdFilesWritesOneW3cCollectionPerSourceWithSelectorsAndSafeNames() {
        val content = ContentItem(
            id = "user-document:Private Essay",
            packId = "user-documents",
            title = "Private Essay: Habits/Focus?",
            description = "Private text",
            durationMinutes = 8,
            format = ContentFormat.EPUB,
            topicTags = setOf(TopicTag.PSYCHOLOGY),
            sourceLabel = "essay.epub",
            sourceType = ContentSourceType.USER_DOCUMENT,
            availability = ContentAvailability.AVAILABLE,
            rights = ContentRightsMetadata.userPrivateReader(sourceUrl = "content://quality/essay.epub"),
        )

        val files = ReadingAnnotationExportFormatter().formatJsonLdFiles(
            annotations = listOf(
                ReadingAnnotation(
                    id = "note-1",
                    contentId = content.id,
                    paragraphIndex = 2,
                    quotedText = "A useful fragment",
                    noteText = "Connect this to the active intervention.",
                    createdAtMillis = 1_000L,
                    updatedAtMillis = 2_000L,
                    sourceTitle = content.title,
                    sourceLabel = content.sourceLabel,
                    sourceType = content.sourceType,
                    sourceFormat = content.format,
                    selector = ReadingAnnotationSelector(
                        sourceHref = "EPUB/chapter-1.xhtml",
                        sourceAnchor = "habit-loop",
                        sourceBlockIndex = 7,
                        textStartOffset = 42,
                        textEndOffset = 59,
                        prefixText = "Before",
                        suffixText = "After",
                    ),
                ),
                ReadingAnnotation(
                    id = "note-missing",
                    contentId = "deleted-content",
                    paragraphIndex = 0,
                    quotedText = "Old quote",
                    noteText = "Keep even after deleting the source.",
                    createdAtMillis = 1_500L,
                    updatedAtMillis = 2_500L,
                ),
            ),
            contentById = mapOf(content.id to content),
        )

        assertEquals(2, files.size)
        val privateEssay = files.first { it.contentId == content.id }
        assertEquals(
            "quality-alternative-private-essay-habits-focus-user-document-private-essay.annotations.jsonld",
            privateEssay.fileName,
        )
        assertTrue(privateEssay.jsonLd.contains("\"@context\":\"http://www.w3.org/ns/anno.jsonld\""))
        assertTrue(privateEssay.jsonLd.contains("\"type\":\"AnnotationCollection\""))
        assertTrue(privateEssay.jsonLd.contains("\"label\":\"Annotations for Private Essay: Habits/Focus?\""))
        assertTrue(privateEssay.jsonLd.contains("\"format\":\"application/epub+zip\""))
        assertTrue(privateEssay.jsonLd.contains("\"motivation\":\"commenting\""))
        assertTrue(privateEssay.jsonLd.contains("\"value\":\"Connect this to the active intervention.\""))
        assertTrue(privateEssay.jsonLd.contains("\"type\":\"TextQuoteSelector\""))
        assertTrue(privateEssay.jsonLd.contains("\"exact\":\"A useful fragment\""))
        assertTrue(privateEssay.jsonLd.contains("\"prefix\":\"Before\""))
        assertTrue(privateEssay.jsonLd.contains("\"suffix\":\"After\""))
        assertTrue(privateEssay.jsonLd.contains("\"type\":\"TextPositionSelector\""))
        assertTrue(privateEssay.jsonLd.contains("\"start\":42"))
        assertTrue(privateEssay.jsonLd.contains("\"end\":59"))
        assertTrue(privateEssay.jsonLd.contains("\"type\":\"FragmentSelector\""))
        assertTrue(privateEssay.jsonLd.contains("\"value\":\"EPUB/chapter-1.xhtml#habit-loop\""))
        assertTrue(privateEssay.jsonLd.contains("\"sourceBlockIndex\":7"))

        val missingSource = files.first { it.contentId == "deleted-content" }
        assertTrue(missingSource.fileName.startsWith("quality-alternative-source-no-longer-in-library-"))
        assertTrue(missingSource.jsonLd.contains("Keep even after deleting the source."))
    }
}
