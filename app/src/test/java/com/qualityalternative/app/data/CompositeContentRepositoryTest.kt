package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.ContentAvailability
import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.ContentSourceType
import com.qualityalternative.app.domain.model.EditorialPack
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.model.UserLinkDraft
import com.qualityalternative.app.domain.service.AddUserLinkResult
import com.qualityalternative.app.domain.service.ContentRepository
import com.qualityalternative.app.domain.service.UserLinkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class CompositeContentRepositoryTest {
    @Test
    fun inventory_mergesEditorialAndAvailableUserLinksOnly() {
        val repository = CompositeContentRepository(
            editorialRepository = FakeEditorialRepository(),
            userLinkRepository = FakeUserLinkRepository(
                links = listOf(
                    userLink(id = "link-available", availability = ContentAvailability.NEEDS_FALLBACK),
                    userLink(id = "link-unavailable", availability = ContentAvailability.UNAVAILABLE),
                ),
            ),
        )

        val inventory = repository.inventory()

        assertEquals(listOf("editorial-1", "link-available"), inventory.map(ContentItem::id))
        assertFalse(inventory.any { it.id == "link-unavailable" })
    }

    @Test
    fun contentBody_returnsEditorialBodyAndUserLinkFallbackDescription() {
        val editorial = FakeEditorialRepository()
        val userLink = userLink(id = "link-available", description = "Fallback summary")
        val repository = CompositeContentRepository(
            editorialRepository = editorial,
            userLinkRepository = FakeUserLinkRepository(links = listOf(userLink)),
        )

        assertEquals("Editorial body", repository.contentBody(editorial.inventory().single()))
        assertEquals("Fallback summary", repository.contentBody(userLink))
    }

    private class FakeEditorialRepository : ContentRepository {
        private val item = ContentItem(
            id = "editorial-1",
            packId = "starter",
            title = "Editorial",
            description = "Editorial item",
            durationMinutes = 7,
            format = ContentFormat.MARKDOWN,
            topicTags = setOf(TopicTag.SCIENCE),
            bodyAssetPath = "editorial.md",
        )

        override fun starterPacks(): List<EditorialPack> = listOf(
            EditorialPack(
                id = "starter",
                title = "Starter",
                description = "Starter pack",
                items = listOf(item),
            ),
        )

        override fun inventory(): List<ContentItem> = listOf(item)

        override fun contentBody(item: ContentItem): String = "Editorial body"
    }

    private class FakeUserLinkRepository(
        private val links: List<ContentItem>,
    ) : UserLinkRepository {
        override fun userLinks(): List<ContentItem> = links

        override fun observeUserLinks(): Flow<List<ContentItem>> = flowOf(links)

        override suspend fun addLink(
            draft: UserLinkDraft,
            nowMillis: Long,
        ): AddUserLinkResult = error("Not needed")

        override suspend fun markUnavailable(
            contentId: String,
            nowMillis: Long,
        ) = Unit
    }

    private fun userLink(
        id: String,
        description: String = "Saved link",
        availability: ContentAvailability = ContentAvailability.NEEDS_FALLBACK,
    ): ContentItem = ContentItem(
        id = id,
        packId = "user-links",
        title = id,
        description = description,
        durationMinutes = 6,
        format = ContentFormat.HTML,
        topicTags = setOf(TopicTag.PSYCHOLOGY),
        externalUrl = "https://example.com/$id",
        sourceType = ContentSourceType.USER_LINK,
        availability = availability,
    )
}
