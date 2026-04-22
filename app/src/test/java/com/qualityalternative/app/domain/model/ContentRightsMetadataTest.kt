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
        assertFalse(item.rights.usesInAppReader)
        assertTrue(item.usesExternalHandoff())
        assertFalse(item.usesRepositoryBody())
    }

    @Test
    fun renderModeDrivesInAppReaderWithoutRuntimeRightsBlocking() {
        val item = ContentItem(
            id = "malformed-link-only",
            packId = "starter",
            title = "Malformed",
            description = "Inventory audit should catch this, runtime should not act as a copyright gate",
            durationMinutes = 5,
            format = ContentFormat.MARKDOWN,
            topicTags = setOf(TopicTag.ESSAYS),
            bodyAssetPath = "editorial/items/example.md",
            rights = ContentRightsMetadata(
                rightsClass = ContentRightsClass.LINK_ONLY,
                renderMode = ContentRenderMode.IN_APP_READER,
            ),
        )

        assertTrue(item.rights.usesInAppReader)
        assertTrue(item.usesRepositoryBody())
        assertFalse(item.usesExternalHandoff())
    }

    @Test
    fun userPrivateItemFollowsConfiguredRenderMode() {
        val item = ContentItem(
            id = "malformed-private",
            packId = "user-links",
            title = "Malformed private",
            description = "Runtime follows render mode; triage owns copyright validation",
            durationMinutes = 5,
            format = ContentFormat.HTML,
            topicTags = setOf(TopicTag.ESSAYS),
            rights = ContentRightsMetadata(
                rightsClass = ContentRightsClass.USER_PRIVATE,
                renderMode = ContentRenderMode.IN_APP_READER,
            ),
        )

        assertTrue(item.usesRepositoryBody())
        assertFalse(item.usesExternalHandoff())
    }

    @Test
    fun userPrivateReaderRequiresExplicitPrivateReaderMode() {
        val item = ContentItem(
            id = "private-pdf-future",
            packId = "user-private",
            title = "Private item",
            description = "Future private reader path",
            durationMinutes = 8,
            format = ContentFormat.MARKDOWN,
            topicTags = setOf(TopicTag.ESSAYS),
            rights = ContentRightsMetadata(
                rightsClass = ContentRightsClass.USER_PRIVATE,
                renderMode = ContentRenderMode.USER_PRIVATE_READER,
            ),
        )

        assertTrue(item.rights.usesUserPrivateReader)
        assertTrue(item.usesRepositoryBody())
        assertFalse(item.usesExternalHandoff())
    }

    @Test
    fun renderableEditorialCanUseInAppReaderWhenExplicitlyMarked() {
        val rights = ContentRightsMetadata.renderableEditorial(
            licenseName = "Public domain text; Project Gutenberg source policy linked",
            attribution = "Example Author, Example Work",
            rightsReviewedAt = "2026-04-22",
        )

        assertEquals(ContentRightsClass.RENDERABLE, rights.rightsClass)
        assertEquals(ContentRenderMode.IN_APP_READER, rights.renderMode)
        assertTrue(rights.usesInAppReader)
        assertFalse(rights.usesExternalHandoff)
    }

    @Test
    fun userPrivateExternalKeepsUserOwnedLinksOutOfInAppReaderByDefault() {
        val rights = ContentRightsMetadata.userPrivateExternal(sourceUrl = "https://example.com/essay")

        assertEquals(ContentRightsClass.USER_PRIVATE, rights.rightsClass)
        assertEquals(ContentRenderMode.EXTERNAL_HANDOFF, rights.renderMode)
        assertFalse(rights.usesInAppReader)
        assertTrue(rights.usesExternalHandoff)
    }
}
