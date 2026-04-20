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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
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

    @Test
    fun isReady_requiresBothRepositoriesToBeReady() {
        val repository = CompositeContentRepository(
            editorialRepository = FakeEditorialRepository(isReady = MutableStateFlow(true)),
            userLinkRepository = FakeUserLinkRepository(
                links = emptyList(),
                isReady = MutableStateFlow(false),
            ),
        )

        assertFalse(repository.isReady())
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeReady_requiresEditorialAndUserLinksToBeReady() = runTest {
        val editorialReady = MutableStateFlow(false)
        val userLinksReady = MutableStateFlow(false)
        val repository = CompositeContentRepository(
            editorialRepository = FakeEditorialRepository(isReady = editorialReady),
            userLinkRepository = FakeUserLinkRepository(
                links = emptyList(),
                isReady = userLinksReady,
            ),
        )
        val emissions = mutableListOf<Boolean>()

        val job = launch {
            repository.observeReady().take(2).toList(emissions)
        }
        advanceUntilIdle()

        assertEquals(listOf(false), emissions)

        userLinksReady.value = true
        advanceUntilIdle()

        assertEquals(listOf(false), emissions)

        editorialReady.value = true
        advanceUntilIdle()

        assertEquals(listOf(false, true), emissions)
        job.cancel()
    }

    private class FakeEditorialRepository(
        private val isReady: MutableStateFlow<Boolean> = MutableStateFlow(true),
    ) : ContentRepository {
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

        override fun isReady(): Boolean = isReady.value

        override fun observeReady(): Flow<Boolean> = isReady
    }

    private class FakeUserLinkRepository(
        private val links: List<ContentItem>,
        private val isReady: MutableStateFlow<Boolean> = MutableStateFlow(true),
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

        override fun isReady(): Boolean = isReady.value

        override fun observeReady(): Flow<Boolean> = isReady
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
