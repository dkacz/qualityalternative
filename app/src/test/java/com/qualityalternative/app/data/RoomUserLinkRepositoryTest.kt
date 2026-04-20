package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.ContentAvailability
import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.ContentSourceType
import com.qualityalternative.app.domain.model.TopicTag
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomUserLinkRepositoryTest {
    @Test
    fun optimisticUpsertPrependsNewLinksForNewestFirstUiState() {
        val older = userLink(id = "older", url = "https://example.com/older")
        val newer = userLink(id = "newer", url = "https://example.com/newer")

        val result = upsertUserLinkForOptimisticState(
            currentLinks = listOf(older),
            updatedLink = newer,
        )

        assertEquals(listOf("newer", "older"), result.map(ContentItem::id))
    }

    @Test
    fun optimisticUpsertReplacesExistingLinkWithoutMovingItsPosition() {
        val newest = userLink(id = "newest", url = "https://example.com/newest")
        val existing = userLink(id = "existing", url = "https://example.com/existing")
        val updated = existing.copy(title = "Updated existing")

        val result = upsertUserLinkForOptimisticState(
            currentLinks = listOf(newest, existing),
            updatedLink = updated,
        )

        assertEquals(listOf("newest", "existing"), result.map(ContentItem::id))
        assertEquals("Updated existing", result[1].title)
    }

    private fun userLink(
        id: String,
        url: String,
    ): ContentItem {
        return ContentItem(
            id = id,
            packId = "user-links",
            title = id,
            description = url,
            durationMinutes = 7,
            format = ContentFormat.HTML,
            topicTags = setOf(TopicTag.SCIENCE),
            bodyAssetPath = null,
            externalUrl = url,
            sourceType = ContentSourceType.USER_LINK,
            availability = ContentAvailability.NEEDS_FALLBACK,
        )
    }
}
