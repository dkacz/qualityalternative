package com.qualityalternative.app.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.qualityalternative.app.data.local.UserDocumentDao
import com.qualityalternative.app.data.local.UserDocumentEntity
import com.qualityalternative.app.domain.model.ContentAvailability
import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.ContentRightsMetadata
import com.qualityalternative.app.domain.model.ContentSourceType
import com.qualityalternative.app.domain.model.ReaderDocument
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.model.UserDocumentDraft
import com.qualityalternative.app.domain.service.AddUserDocumentResult
import com.qualityalternative.app.domain.service.UserDocumentRepository
import java.io.InputStream
import java.security.MessageDigest
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class RoomUserDocumentRepository(
    private val dao: UserDocumentDao,
    private val scope: CoroutineScope,
    private val bodyLoader: UserDocumentBodyLoader,
    private val idProvider: () -> String = ::randomUserDocumentId,
) : UserDocumentRepository {
    private val documents = MutableStateFlow(emptyList<ContentItem>())
    private val ready = MutableStateFlow(false)

    init {
        scope.launch {
            dao.observeAll()
                .map { rows -> rows.map(UserDocumentEntity::toContentItem) }
                .collect { loadedDocuments ->
                    documents.value = loadedDocuments
                    ready.value = true
                }
        }
    }

    override fun userDocuments(): List<ContentItem> = documents.value

    override fun observeUserDocuments(): Flow<List<ContentItem>> = documents.asStateFlow()

    override suspend fun addDocument(
        draft: UserDocumentDraft,
        nowMillis: Long,
    ): AddUserDocumentResult {
        val validation = UserDocumentValidator.validate(draft)
        val format = validation.format
        if (!validation.isValid || format == null) {
            return AddUserDocumentResult.Rejected(validation.errors)
        }

        val normalizedUri = draft.uri.trim()
        val existing = dao.findByUri(normalizedUri)
        val createdAtMillis = existing?.createdAtMillis ?: nowMillis
        val displayName = draft.displayName.trim().ifBlank { draft.title.trim() }
        val loadedFingerprint = if (
            existing?.documentFingerprintSha256 != null &&
            existing.documentFingerprintSizeBytes != null
        ) {
            null
        } else {
            (bodyLoader as? UserDocumentFingerprintProvider)?.documentFingerprint(normalizedUri)
        }
        val fingerprintSha256 = existing?.documentFingerprintSha256 ?: loadedFingerprint?.sha256
        val fingerprintSizeBytes = existing?.documentFingerprintSizeBytes ?: loadedFingerprint?.sizeBytes
        val item = ContentItem(
            id = existing?.id ?: idProvider(),
            packId = USER_DOCUMENT_PACK_ID,
            title = draft.title.trim(),
            description = draft.description.trim().ifBlank {
                defaultDescription(format = format, displayName = displayName)
            },
            durationMinutes = draft.durationMinutes,
            format = format,
            topicTags = draft.topicTags,
            bodyAssetPath = null,
            externalUrl = if (format.usesPrivateReader()) null else normalizedUri,
            whyThisNow = "You chose this file as a better answer to an impulse.",
            sourceLabel = displayName,
            sourceType = ContentSourceType.USER_DOCUMENT,
            availability = if (format.usesPrivateReader()) {
                ContentAvailability.AVAILABLE
            } else {
                ContentAvailability.NEEDS_FALLBACK
            },
            rights = if (format.usesPrivateReader()) {
                ContentRightsMetadata.userPrivateReader(
                    sourceUrl = normalizedUri,
                    attribution = displayName,
                )
            } else {
                ContentRightsMetadata.userPrivateExternal(
                    sourceUrl = normalizedUri,
                    attribution = displayName,
                )
            },
            addedAtMillis = createdAtMillis,
            documentFingerprintSha256 = fingerprintSha256,
            documentFingerprintSizeBytes = fingerprintSizeBytes,
        )

        dao.insertOrReplace(
            item.toEntity(
                uri = normalizedUri,
                displayName = displayName,
                mimeType = draft.mimeType,
                createdAtMillis = createdAtMillis,
                updatedAtMillis = nowMillis,
                documentFingerprintSha256 = fingerprintSha256,
                documentFingerprintSizeBytes = fingerprintSizeBytes,
            ),
        )
        documents.value = upsertUserDocumentForOptimisticState(documents.value, item)
        return AddUserDocumentResult.Added(item)
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
        documents.value = documents.value.map { item ->
            if (item.id == contentId) {
                item.copy(availability = ContentAvailability.UNAVAILABLE)
            } else {
                item
            }
        }
    }

    override suspend fun deleteDocument(contentId: String) {
        dao.deleteById(contentId)
        documents.value = documents.value.filterNot { item -> item.id == contentId }
    }

    override suspend fun importPortableDocuments(
        documents: List<ContentItem>,
        replaceExisting: Boolean,
        nowMillis: Long,
    ): Set<String> {
        val existingDocuments = this.documents.value
        val importPlan = portableUserContentImportPlan(
            current = existingDocuments,
            imported = documents,
            replaceExisting = replaceExisting,
            secondaryKey = ContentItem::verifiedDocumentFingerprintSha256,
        )
        if (replaceExisting) {
            val retainedIds = documents.mapTo(mutableSetOf(), ContentItem::id)
            if (retainedIds.isEmpty()) {
                dao.deleteAll()
            } else {
                dao.deleteAllExcept(retainedIds)
            }
        }
        val documentsToImport = importPlan.itemsToImport
        documentsToImport.forEach { item ->
            dao.insertOrReplace(
                item.toEntity(
                    uri = requireNotNull(item.rights.sourceUrl) {
                        "Imported document content must provide a synthetic source reference."
                    },
                    displayName = item.sourceLabel.orEmpty().removeSuffix(" (missing)").ifBlank { item.title },
                    mimeType = null,
                    createdAtMillis = item.addedAtMillis ?: nowMillis,
                    updatedAtMillis = nowMillis,
                    documentFingerprintSha256 = item.verifiedDocumentFingerprintSha256(),
                    documentFingerprintSizeBytes = item.verifiedDocumentFingerprintSizeBytes(),
                ),
            )
        }
        this.documents.value = mergeImportedUserContent(
            current = if (replaceExisting) emptyList() else existingDocuments,
            imported = documentsToImport,
            secondaryKey = ContentItem::verifiedDocumentFingerprintSha256,
        )
        return importPlan.acceptedContentIds
    }

    override fun contentBody(item: ContentItem): String {
        return if (item.sourceType == ContentSourceType.USER_DOCUMENT && item.format.usesPrivateReader()) {
            bodyLoader.loadBody(uri = item.rights.sourceUrl.orEmpty(), format = item.format)
        } else {
            item.description
        }
    }

    override fun readerDocument(item: ContentItem): ReaderDocument {
        return if (item.sourceType == ContentSourceType.USER_DOCUMENT && item.format.usesPrivateReader()) {
            val structuredLoader = bodyLoader as? UserDocumentReaderDocumentLoader
            structuredLoader?.loadReaderDocument(uri = item.rights.sourceUrl.orEmpty(), format = item.format)
                ?: ReaderDocument.fromPlainText(contentBody(item))
        } else {
            ReaderDocument.fromPlainText(item.description)
        }
    }

    override fun isReady(): Boolean = ready.value

    override fun observeReady(): Flow<Boolean> = ready.asStateFlow()

    private companion object {
        const val USER_DOCUMENT_PACK_ID = "user-documents"
    }
}

fun interface UserDocumentBodyLoader {
    fun loadBody(uri: String, format: ContentFormat): String
}

interface UserDocumentFingerprintProvider {
    fun documentFingerprint(uri: String): UserDocumentFingerprint?
}

data class UserDocumentFingerprint(
    val sha256: String,
    val sizeBytes: Long,
)

interface UserDocumentReaderDocumentLoader : UserDocumentBodyLoader {
    fun loadReaderDocument(uri: String, format: ContentFormat): ReaderDocument
}

class AndroidUserDocumentBodyLoader(
    context: Context,
) : UserDocumentReaderDocumentLoader, UserDocumentFingerprintProvider {
    private val contentResolver: ContentResolver = context.contentResolver

    override fun loadBody(uri: String, format: ContentFormat): String {
        return loadReaderDocument(uri = uri, format = format).plainText
    }

    override fun loadReaderDocument(uri: String, format: ContentFormat): ReaderDocument {
        if (!format.usesPrivateReader()) {
            return ReaderDocument.fromPlainText("")
        }
        return runCatching {
            val parsedUri = Uri.parse(uri)
            contentResolver.openInputStream(parsedUri)?.use { input ->
                when (format) {
                    ContentFormat.MARKDOWN -> ReaderDocument.fromPlainText(input.bufferedReader(Charsets.UTF_8).readText())
                    ContentFormat.EPUB -> EpubTextExtractor.extractDocument(input)
                    else -> ReaderDocument.fromPlainText("")
                }
            } ?: throw UserDocumentBodyLoadException(uri)
        }.getOrElse { error ->
            throw UserDocumentBodyLoadException(uri = uri, cause = error)
        }
    }

    override fun documentFingerprint(uri: String): UserDocumentFingerprint? {
        return runCatching {
            contentResolver.openInputStream(Uri.parse(uri))?.use { input ->
                input.toUserDocumentFingerprint()
            }
        }.getOrNull()
    }
}

private fun InputStream.toUserDocumentFingerprint(): UserDocumentFingerprint {
    val digest = MessageDigest.getInstance("SHA-256")
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var sizeBytes = 0L
    while (true) {
        val read = read(buffer)
        if (read <= 0) break
        digest.update(buffer, 0, read)
        sizeBytes += read.toLong()
    }
    return UserDocumentFingerprint(
        sha256 = digest.digest().joinToString("") { byte -> "%02x".format(byte) },
        sizeBytes = sizeBytes,
    )
}

class UserDocumentBodyLoadException(
    uri: String,
    cause: Throwable? = null,
) : IllegalStateException("Unable to load private document body for $uri", cause)

private fun UserDocumentEntity.toContentItem(): ContentItem {
    val format = runCatching { ContentFormat.valueOf(documentFormat) }.getOrDefault(ContentFormat.PDF)
    return ContentItem(
        id = id,
        packId = "user-documents",
        title = title,
        description = description,
        durationMinutes = durationMinutes,
        format = format,
        topicTags = topicTagsCsv.toTopicTags(),
        bodyAssetPath = null,
        externalUrl = if (format.usesPrivateReader()) null else uri,
        whyThisNow = "You chose this file as a better answer to an impulse.",
        sourceLabel = displayName,
        sourceType = ContentSourceType.USER_DOCUMENT,
        availability = ContentAvailability.valueOf(availability),
        rights = if (format.usesPrivateReader()) {
            ContentRightsMetadata.userPrivateReader(
                sourceUrl = uri,
                attribution = displayName,
            )
        } else {
            ContentRightsMetadata.userPrivateExternal(
                sourceUrl = uri,
                attribution = displayName,
            )
        },
        addedAtMillis = createdAtMillis,
        documentFingerprintSha256 = documentFingerprintSha256,
        documentFingerprintSizeBytes = documentFingerprintSizeBytes,
    )
}

private fun ContentItem.toEntity(
    uri: String,
    displayName: String,
    mimeType: String?,
    createdAtMillis: Long,
    updatedAtMillis: Long,
    documentFingerprintSha256: String?,
    documentFingerprintSizeBytes: Long?,
): UserDocumentEntity {
    return UserDocumentEntity(
        id = id,
        uri = uri,
        displayName = displayName,
        mimeType = mimeType,
        documentFormat = format.name,
        title = title,
        description = description,
        durationMinutes = durationMinutes,
        topicTagsCsv = topicTags.joinToString(",") { it.name },
        availability = availability.name,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
        documentFingerprintSha256 = documentFingerprintSha256,
        documentFingerprintSizeBytes = documentFingerprintSizeBytes,
    )
}

internal fun ContentItem.verifiedDocumentFingerprintSha256(): String? =
    documentFingerprintSha256?.takeIf { fingerprint -> fingerprint.matches(VerifiedDocumentFingerprintRegex) }

internal fun ContentItem.verifiedDocumentFingerprintSizeBytes(): Long? =
    documentFingerprintSizeBytes?.takeIf { size -> size >= 0L && verifiedDocumentFingerprintSha256() != null }

private fun ContentFormat.usesPrivateReader(): Boolean = this == ContentFormat.MARKDOWN || this == ContentFormat.EPUB

private fun defaultDescription(format: ContentFormat, displayName: String): String {
    return when (format) {
        ContentFormat.MARKDOWN -> "A private Markdown file from your library: $displayName."
        ContentFormat.PDF -> "A private PDF from your library. Opens through Android's document viewer."
        ContentFormat.EPUB -> "A private EPUB from your library: $displayName."
        ContentFormat.HTML -> "A private document from your library."
    }
}

private fun String.toTopicTags(): Set<TopicTag> {
    if (isBlank()) {
        return emptySet()
    }
    return split(",").mapNotNullTo(mutableSetOf()) { raw ->
        runCatching { TopicTag.valueOf(raw) }.getOrNull()
    }
}

internal fun upsertUserDocumentForOptimisticState(
    currentDocuments: List<ContentItem>,
    updatedDocument: ContentItem,
): List<ContentItem> {
    val existingIndex = currentDocuments.indexOfFirst { item ->
        item.id == updatedDocument.id || item.rights.sourceUrl == updatedDocument.rights.sourceUrl
    }
    return if (existingIndex >= 0) {
        currentDocuments.mapIndexed { index, item ->
            if (index == existingIndex) updatedDocument else item
        }
    } else {
        listOf(updatedDocument) + currentDocuments
    }
}

private fun randomUserDocumentId(): String = "user-document-${UUID.randomUUID()}"

private val VerifiedDocumentFingerprintRegex = Regex("^[0-9a-f]{64}$")
