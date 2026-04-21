package com.qualityalternative.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentRightsMetadataTest {
    @Test
    fun contentItemDefaultsToLinkOnlyExternalHandoff() {
        val item = ContentItem(
            id = "unknown",
            packId = "starter",
            title = "Unknown source",
            description = "No rights metadata yet",
            durationMinutes = 5,
            format = ContentFormat.MARKDOWN,
            topicTags = setOf(TopicTag.ESSAYS),
        )

        assertEquals(ContentRightsClass.LINK_ONLY, item.rights.rightsClass)
        assertEquals(ContentRenderMode.EXTERNAL_HANDOFF, item.rights.renderMode)
        assertFalse(item.rights.canUseInAppReader)
        assertTrue(item.usesExternalHandoff())
        assertFalse(item.usesRepositoryBody())
    }

    @Test
    fun renderableEditorialCanUseInAppReaderWhenExplicitlyMarked() {
        val rights = ContentRightsMetadata.renderableEditorial(
            licenseName = "Quality Alternative first-party placeholder",
            attribution = "Quality Alternative",
            rightsReviewedAt = "2026-04-21",
        )

        assertEquals(ContentRightsClass.RENDERABLE, rights.rightsClass)
        assertEquals(ContentRenderMode.IN_APP_READER, rights.renderMode)
        assertTrue(rights.canUseInAppReader)
        assertFalse(rights.usesExternalHandoff)
    }

    @Test
    fun userPrivateExternalKeepsUserOwnedLinksOutOfInAppReaderByDefault() {
        val rights = ContentRightsMetadata.userPrivateExternal(sourceUrl = "https://example.com/essay")

        assertEquals(ContentRightsClass.USER_PRIVATE, rights.rightsClass)
        assertEquals(ContentRenderMode.EXTERNAL_HANDOFF, rights.renderMode)
        assertFalse(rights.canUseInAppReader)
        assertTrue(rights.usesExternalHandoff)
    }
}
