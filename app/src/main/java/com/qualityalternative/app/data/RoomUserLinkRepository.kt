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
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class RoomUserLinkRepository(
    private val dao: UserLinkDao,
    private val scope: CoroutineScope,
    private val idProvider: () -> String = ::randomUserLinkId,
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
            id = existing?.id ?: idProvider(),
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

    override suspend fun importPortableLinks(
        links: List<ContentItem>,
        replaceExisting: Boolean,
        nowMillis: Long,
    ): Set<String> {
        val existingLinks = this.links.value
        val importPlan = portableUserContentImportPlan(
            current = existingLinks,
            imported = links,
            replaceExisting = replaceExisting,
            secondaryKey = ContentItem::externalUrl,
        )
        if (replaceExisting) {
            val retainedIds = links.mapTo(mutableSetOf(), ContentItem::id)
            if (retainedIds.isEmpty()) {
                dao.deleteAll()
            } else {
                dao.deleteAllExcept(retainedIds)
            }
        }
        val linksToImport = importPlan.itemsToImport
        linksToImport.forEach { item ->
            dao.insertOrReplace(
                item.toEntity(
                    createdAtMillis = item.addedAtMillis ?: nowMillis,
                    updatedAtMillis = nowMillis,
                ),
            )
        }
        this.links.value = mergeImportedUserContent(
            current = if (replaceExisting) emptyList() else existingLinks,
            imported = linksToImport,
            secondaryKey = ContentItem::externalUrl,
        )
        return importPlan.acceptedContentIds
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

private fun randomUserLinkId(): String = "user-link-${UUID.randomUUID()}"

class PortableContentImportConflictException(
    message: String,
) : IllegalStateException(message)

internal data class PortableUserContentImportPlan(
    val itemsToImport: List<ContentItem>,
    val acceptedContentIds: Set<String>,
    val contentIdMapping: Map<String, String>,
)

internal fun portableUserContentImportPlan(
    current: List<ContentItem>,
    imported: List<ContentItem>,
    replaceExisting: Boolean,
    secondaryKey: (ContentItem) -> String?,
): PortableUserContentImportPlan {
    val distinctImported = imported.distinctPortableUserContentOrThrow(secondaryKey = secondaryKey)
    val currentById = current.associateBy(ContentItem::id)
    val currentBySecondary = current
        .mapNotNull { item -> secondaryKey(item).portableSecondaryKeyOrNull()?.let { key -> key to item } }
        .toMap()

    distinctImported.forEach { item ->
        val importedSecondary = secondaryKey(item).portableSecondaryKeyOrNull()
        val existingById = currentById[item.id]
        val existingBySecondary = importedSecondary?.let(currentBySecondary::get)
        if (existingById != null && existingBySecondary != null && existingById.id != existingBySecondary.id) {
            throw PortableContentImportConflictException(
                "Imported contentId and secondary key match two different local records.",
            )
        }
    }

    if (replaceExisting) {
        return PortableUserContentImportPlan(
            itemsToImport = distinctImported,
            acceptedContentIds = distinctImported.mapTo(mutableSetOf(), ContentItem::id),
            contentIdMapping = distinctImported.associate { item -> item.id to item.id },
        )
    }

    val itemsToImport = mutableListOf<ContentItem>()
    val acceptedContentIds = mutableSetOf<String>()
    val contentIdMapping = mutableMapOf<String, String>()

    distinctImported.forEach { item ->
        val importedSecondary = secondaryKey(item).portableSecondaryKeyOrNull()
        val existingById = currentById[item.id]
        val existingBySecondary = importedSecondary?.let(currentBySecondary::get)
        val existing = existingById ?: existingBySecondary
        if (existing == null) {
            itemsToImport += item
            acceptedContentIds += item.id
            contentIdMapping[item.id] = item.id
        } else {
            acceptedContentIds += existing.id
            contentIdMapping[item.id] = existing.id
        }
    }

    return PortableUserContentImportPlan(
        itemsToImport = itemsToImport,
        acceptedContentIds = acceptedContentIds,
        contentIdMapping = contentIdMapping,
    )
}

private fun List<ContentItem>.distinctPortableUserContentOrThrow(
    secondaryKey: (ContentItem) -> String?,
): List<ContentItem> {
    val seenById = mutableMapOf<String, ContentItem>()
    val seenBySecondary = mutableMapOf<String, ContentItem>()
    val result = mutableListOf<ContentItem>()
    forEach { item ->
        val existingById = seenById[item.id]
        val secondary = secondaryKey(item).portableSecondaryKeyOrNull()
        val existingBySecondary = secondary?.let(seenBySecondary::get)
        if (existingById != null && secondaryKey(existingById).portableSecondaryKeyOrNull() != secondary) {
            throw PortableContentImportConflictException("Imported profile contains duplicate contentId conflict.")
        }
        if (existingBySecondary != null && existingBySecondary.id != item.id) {
            throw PortableContentImportConflictException("Imported profile contains duplicate secondary key conflict.")
        }
        if (existingById == null && existingBySecondary == null) {
            result += item
        }
        seenById[item.id] = item
        if (secondary != null) {
            seenBySecondary[secondary] = item
        }
    }
    return result
}

private fun String?.portableSecondaryKeyOrNull(): String? = this?.takeIf(String::isNotBlank)

internal fun mergeImportedUserContent(
    current: List<ContentItem>,
    imported: List<ContentItem>,
    secondaryKey: (ContentItem) -> String?,
): List<ContentItem> {
    return imported.fold(current) { merged, item ->
        val existingIndex = merged.indexOfFirst { candidate ->
            candidate.id == item.id ||
                secondaryKey(candidate)?.takeIf(String::isNotBlank) == secondaryKey(item)?.takeIf(String::isNotBlank)
        }
        if (existingIndex >= 0) {
            merged.mapIndexed { index, candidate ->
                if (index == existingIndex) item else candidate
            }
        } else {
            listOf(item) + merged
        }
    }
}
