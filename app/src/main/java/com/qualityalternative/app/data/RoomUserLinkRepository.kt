package com.qualityalternative.app.data

import com.qualityalternative.app.data.local.UserLinkDao
import com.qualityalternative.app.data.local.UserLinkEntity
import com.qualityalternative.app.domain.model.ContentAvailability
import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.ContentRightsMetadata
import com.qualityalternative.app.domain.model.ContentSourceType
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.model.UserLinkDraft
import com.qualityalternative.app.domain.service.AddUserLinkResult
import com.qualityalternative.app.domain.service.UserLinkRepository
import java.security.MessageDigest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class RoomUserLinkRepository(
    private val dao: UserLinkDao,
    private val scope: CoroutineScope,
    private val idProvider: (String) -> String = ::stableUserLinkId,
) : UserLinkRepository {
    private val links = MutableStateFlow(emptyList<ContentItem>())
    private val ready = MutableStateFlow(false)

    init {
        scope.launch {
            dao.observeAll()
                .map { rows -> rows.map(UserLinkEntity::toContentItem) }
                .collect { loadedLinks ->
                    links.value = loadedLinks
                    ready.value = true
                }
        }
    }

    override fun userLinks(): List<ContentItem> = links.value

    override fun observeUserLinks(): Flow<List<ContentItem>> = links.asStateFlow()

    override suspend fun addLink(
        draft: UserLinkDraft,
        nowMillis: Long,
    ): AddUserLinkResult {
        val validation = UserLinkValidator.validate(draft)
        val normalizedUrl = validation.normalizedUrl
        if (!validation.isValid || normalizedUrl == null) {
            return AddUserLinkResult.Rejected(validation.errors)
        }

        val existing = dao.findByNormalizedUrl(normalizedUrl)
        val createdAtMillis = existing?.createdAtMillis ?: nowMillis
        val item = ContentItem(
            id = existing?.id ?: idProvider(normalizedUrl),
            packId = USER_LINK_PACK_ID,
            title = draft.title.trim(),
            description = draft.description.trim().ifBlank { normalizedUrl },
            durationMinutes = draft.durationMinutes,
            format = ContentFormat.HTML,
            topicTags = draft.topicTags,
            bodyAssetPath = null,
            externalUrl = normalizedUrl,
            sourceLabel = normalizedUrl.hostLabel().ifBlank { null },
            sourceType = ContentSourceType.USER_LINK,
            availability = ContentAvailability.NEEDS_FALLBACK,
            rights = ContentRightsMetadata.userPrivateExternal(sourceUrl = normalizedUrl),
            addedAtMillis = createdAtMillis,
        )

        dao.insertOrReplace(item.toEntity(createdAtMillis = createdAtMillis, updatedAtMillis = nowMillis))
        links.value = upsertUserLinkForOptimisticState(links.value, item)
        return AddUserLinkResult.Added(item)
    }

    override suspend fun markUnavailable(
        contentId: String,
        nowMillis: Long,
    ) {
        dao.updateAvailability(
            id = contentId,
            availability = ContentAvailability.UNAVAILABLE.name,
            updatedAtMillis = nowMillis,
        )
        links.value = links.value.map { item ->
            if (item.id == contentId) {
                item.copy(availability = ContentAvailability.UNAVAILABLE)
            } else {
                item
            }
        }
    }

    override suspend fun deleteLink(contentId: String) {
        dao.deleteById(contentId)
        links.value = links.value.filterNot { item -> item.id == contentId }
    }

    override fun isReady(): Boolean = ready.value

    override fun observeReady(): Flow<Boolean> = ready.asStateFlow()

    private companion object {
        const val USER_LINK_PACK_ID = "user-links"
    }
}

private fun UserLinkEntity.toContentItem(): ContentItem {
    return ContentItem(
        id = id,
        packId = "user-links",
        title = title,
        description = description,
        durationMinutes = durationMinutes,
        format = ContentFormat.HTML,
        topicTags = topicTagsCsv.toTopicTags(),
        bodyAssetPath = null,
        externalUrl = normalizedUrl,
        sourceLabel = normalizedUrl.hostLabel().ifBlank { null },
        sourceType = ContentSourceType.USER_LINK,
        availability = ContentAvailability.valueOf(availability),
        rights = ContentRightsMetadata.userPrivateExternal(sourceUrl = normalizedUrl),
        addedAtMillis = createdAtMillis,
    )
}

private fun ContentItem.toEntity(
    createdAtMillis: Long,
    updatedAtMillis: Long,
): UserLinkEntity {
    return UserLinkEntity(
        id = id,
        normalizedUrl = requireNotNull(externalUrl) {
            "User link content must provide an external URL."
        },
        title = title,
        description = description,
        durationMinutes = durationMinutes,
        topicTagsCsv = topicTags.joinToString(",") { it.name },
        availability = availability.name,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
    )
}

private fun String.toTopicTags(): Set<TopicTag> {
    if (isBlank()) {
        return emptySet()
    }
    return split(",").mapNotNullTo(mutableSetOf()) { raw ->
        runCatching { TopicTag.valueOf(raw) }.getOrNull()
    }
}

private fun String.hostLabel(): String {
    val normalized = if (startsWith("http://") || startsWith("https://")) this else "https://$this"
    return android.net.Uri.parse(normalized).host.orEmpty().removePrefix("www.")
}

internal fun upsertUserLinkForOptimisticState(
    currentLinks: List<ContentItem>,
    updatedLink: ContentItem,
): List<ContentItem> {
    val existingIndex = currentLinks.indexOfFirst { item ->
        item.id == updatedLink.id || item.externalUrl == updatedLink.externalUrl
    }
    return if (existingIndex >= 0) {
        currentLinks.mapIndexed { index, item ->
            if (index == existingIndex) updatedLink else item
        }
    } else {
        listOf(updatedLink) + currentLinks
    }
}

private fun stableUserLinkId(normalizedUrl: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
        .digest(normalizedUrl.toByteArray(Charsets.UTF_8))
        .joinToString("") { byte -> "%02x".format(byte) }
    return "user-link:$digest"
}
