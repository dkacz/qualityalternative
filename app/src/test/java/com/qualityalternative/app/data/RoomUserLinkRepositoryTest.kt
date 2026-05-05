package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.ContentAvailability
import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.ContentSourceType
import com.qualityalternative.app.domain.model.TopicTag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
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

    @Test
    fun portableImportPlanThrowsWhenContentIdAndSecondaryKeyMatchDifferentRows() {
        val localById = userLink(
            id = "user-link-11111111-1111-4111-8111-111111111111",
            url = "https://example.com/local-id",
        )
        val localByUrl = userLink(
            id = "user-link-22222222-2222-4222-8222-222222222222",
            url = "https://example.com/local-url",
        )
        val imported = userLink(
            id = localById.id,
            url = localByUrl.externalUrl.orEmpty(),
        )

        assertThrows(PortableContentImportConflictException::class.java) {
            portableUserContentImportPlan(
                current = listOf(localById, localByUrl),
                imported = listOf(imported),
                replaceExisting = false,
                secondaryKey = ContentItem::externalUrl,
            )
        }
    }

    @Test
    fun portableImportPlanThrowsBeforeReplaceWhenContentIdAndSecondaryKeyMatchDifferentRows() {
        val localById = userLink(
            id = "user-link-11111111-1111-4111-8111-111111111111",
            url = "https://example.com/local-id",
        )
        val localByUrl = userLink(
            id = "user-link-22222222-2222-4222-8222-222222222222",
            url = "https://example.com/local-url",
        )
        val imported = userLink(
            id = localById.id,
            url = localByUrl.externalUrl.orEmpty(),
        )

        assertThrows(PortableContentImportConflictException::class.java) {
            portableUserContentImportPlan(
                current = listOf(localById, localByUrl),
                imported = listOf(imported),
                replaceExisting = true,
                secondaryKey = ContentItem::externalUrl,
            )
        }
    }

    @Test
    fun portableImportPlanReturnsOnlyActuallyAcceptedContentIds() {
        val existing = userLink(
            id = "user-link-11111111-1111-4111-8111-111111111111",
            url = "https://example.com/existing",
        )
        val newImport = userLink(
            id = "user-link-33333333-3333-4333-8333-333333333333",
            url = "https://example.com/new",
        )

        val plan = portableUserContentImportPlan(
            current = listOf(existing),
            imported = listOf(existing, newImport),
            replaceExisting = false,
            secondaryKey = ContentItem::externalUrl,
        )

        assertEquals(listOf(newImport.id), plan.itemsToImport.map(ContentItem::id))
        assertEquals(setOf(existing.id, newImport.id), plan.acceptedContentIds)
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
