package com.qualityalternative.app.domain.service

import com.qualityalternative.app.domain.model.ContentAvailability
import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.ContentRightsMetadata
import com.qualityalternative.app.domain.model.ContentSourceType
import com.qualityalternative.app.domain.model.ReadingAnnotation
import com.qualityalternative.app.domain.model.TopicTag
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadingAnnotationExportFormatterTest {
    @Test
    fun formatGroupsAnnotationsBySourceAndKeepsMissingSourceContext() {
        val content = ContentItem(
            id = "user-document",
            packId = "user-documents",
            title = "Private Essay",
            description = "Private text",
            durationMinutes = 8,
            format = ContentFormat.MARKDOWN,
            topicTags = setOf(TopicTag.PSYCHOLOGY),
            sourceLabel = "essay.md",
            sourceType = ContentSourceType.USER_DOCUMENT,
            availability = ContentAvailability.AVAILABLE,
            rights = ContentRightsMetadata.userPrivateReader(sourceUrl = "content://quality/essay.md"),
        )
        val markdown = ReadingAnnotationExportFormatter().format(
            annotations = listOf(
                ReadingAnnotation(
                    id = "note-1",
                    contentId = content.id,
                    paragraphIndex = 2,
                    quotedText = "A useful fragment",
                    noteText = "Connect this to the active intervention.",
                    createdAtMillis = 1_000L,
                    updatedAtMillis = 2_000L,
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

        assertTrue(markdown.contains("## Private Essay"))
        assertTrue(markdown.contains("- Source type: User file"))
        assertTrue(markdown.contains("- Source: essay.md"))
        assertTrue(markdown.contains("### Paragraph 3"))
        assertTrue(markdown.contains("> A useful fragment"))
        assertTrue(markdown.contains("Connect this to the active intervention."))
        assertTrue(markdown.contains("## Source no longer in Library"))
        assertTrue(markdown.contains("- Source type: Missing source"))
        assertTrue(markdown.contains("Keep even after deleting the source."))
    }
}
