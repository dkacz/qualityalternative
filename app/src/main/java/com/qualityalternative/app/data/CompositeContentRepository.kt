package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.ContentAvailability
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.ContentSourceType
import com.qualityalternative.app.domain.model.EditorialPack
import com.qualityalternative.app.domain.service.ContentRepository
import com.qualityalternative.app.domain.service.UserDocumentRepository
import com.qualityalternative.app.domain.service.UserLinkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

class CompositeContentRepository(
    private val editorialRepository: ContentRepository,
    private val userLinkRepository: UserLinkRepository,
    private val userDocumentRepository: UserDocumentRepository,
) : ContentRepository {
    override fun starterPacks(): List<EditorialPack> = editorialRepository.starterPacks()

    override fun inventory(): List<ContentItem> {
        return editorialRepository.inventory() +
            userLinkRepository.userLinks().filterNot { item ->
                item.availability == ContentAvailability.UNAVAILABLE
            } +
            userDocumentRepository.userDocuments().filterNot { item ->
                item.availability == ContentAvailability.UNAVAILABLE
            }
    }

    override fun contentBody(item: ContentItem): String {
        return when (item.sourceType) {
            ContentSourceType.EDITORIAL -> editorialRepository.contentBody(item)
            ContentSourceType.MEDITATION -> item.description
            ContentSourceType.USER_LINK -> item.description
            ContentSourceType.USER_DOCUMENT -> userDocumentRepository.contentBody(item)
        }
    }

    override fun isReady(): Boolean = editorialRepository.isReady() && userLinkRepository.isReady() && userDocumentRepository.isReady()

    override fun observeReady(): Flow<Boolean> {
        return combine(
            editorialRepository.observeReady(),
            userLinkRepository.observeReady(),
            userDocumentRepository.observeReady(),
        ) { editorialReady, userLinksReady, userDocumentsReady ->
            editorialReady && userLinksReady && userDocumentsReady
        }.distinctUntilChanged()
    }
}
