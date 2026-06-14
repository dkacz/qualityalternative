package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.ContentAvailability
import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.ContentRightsMetadata
import com.qualityalternative.app.domain.model.ContentSourceType
import com.qualityalternative.app.domain.model.TopicTag
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomUserDocumentRepositoryTest {
    @Test
    fun allowsLocalMarkdownImageFallback_blocksAgentInboxStoredDocumentsOnly() {
        val agentInboxDocument = userDocument(
            sourceLabel = AGENT_INBOX_DOCUMENT_DISPLAY_NAME,
            sourceUrl = "file:///data/user/0/com.qualityalternative.app/files/agent-inbox-imports/pkg.md",
        )
        val sameLabelManualDocument = userDocument(
            sourceLabel = AGENT_INBOX_DOCUMENT_DISPLAY_NAME,
            sourceUrl = "content://provider/documents/manual-md",
        )
        val regularManualDocument = userDocument(
            sourceLabel = "notes.md",
            sourceUrl = "content://provider/documents/notes-md",
        )

        assertFalse(agentInboxDocument.allowsLocalMarkdownImageFallback())
        assertTrue(sameLabelManualDocument.allowsLocalMarkdownImageFallback())
        assertTrue(regularManualDocument.allowsLocalMarkdownImageFallback())
    }

    private fun userDocument(
        sourceLabel: String,
        sourceUrl: String,
    ): ContentItem {
        return ContentItem(
            id = "doc-$sourceLabel-$sourceUrl",
            packId = "user-documents",
            title = "Document",
            description = "Private document",
            durationMinutes = 5,
            format = ContentFormat.MARKDOWN,
            topicTags = setOf(TopicTag.ESSAYS),
            sourceLabel = sourceLabel,
            sourceType = ContentSourceType.USER_DOCUMENT,
            availability = ContentAvailability.AVAILABLE,
            rights = ContentRightsMetadata.userPrivateReader(
                sourceUrl = sourceUrl,
                attribution = sourceLabel,
            ),
        )
    }
}
