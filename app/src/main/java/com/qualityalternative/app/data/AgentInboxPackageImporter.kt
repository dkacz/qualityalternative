package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.UserDocumentDraft
import com.qualityalternative.app.domain.model.UserDocumentValidationError
import com.qualityalternative.app.domain.service.AddUserDocumentIfFingerprintAbsentResult
import com.qualityalternative.app.domain.service.AGENT_INBOX_MAX_IMAGE_ATTACHMENT_BYTES
import com.qualityalternative.app.domain.service.AGENT_INBOX_MAX_TOTAL_IMAGE_ATTACHMENT_BYTES
import com.qualityalternative.app.domain.service.AGENT_INBOX_MAX_REVIEW_CONTENT_BYTES
import com.qualityalternative.app.domain.service.UserDocumentRepository
import java.io.ByteArrayInputStream

internal const val AGENT_INBOX_DOCUMENT_DISPLAY_NAME = "Agent Inbox document"

enum class AgentInboxImportStatus {
    IMPORTED,
    DUPLICATE,
    INVALID,
    REJECTED,
}

data class AgentInboxImportResult(
    val status: AgentInboxImportStatus,
    val item: ContentItem? = null,
    val duplicateContentId: String? = null,
    val requestedHighPriority: Boolean = false,
    val manifestErrors: Set<AgentInboxManifestValidationError> = emptySet(),
    val packageErrors: Set<AgentInboxPackageValidationError> = emptySet(),
    val documentErrors: Set<UserDocumentValidationError> = emptySet(),
)

class AgentInboxPackageImporter(
    private val userDocumentRepository: UserDocumentRepository,
    private val documentStore: AgentInboxDocumentStore,
) {
    suspend fun importCandidate(
        candidate: AgentInboxReviewCandidate,
        contentBytes: ByteArray,
        imageAttachmentBytes: Map<String, ByteArray> = emptyMap(),
        nowMillis: Long = System.currentTimeMillis(),
    ): AgentInboxImportResult {
        val manifest = candidate.manifest
        if (!candidate.canImport || manifest == null || candidate.contentFileName == null) {
            return AgentInboxImportResult(
                status = AgentInboxImportStatus.INVALID,
                manifestErrors = candidate.manifestErrors,
                packageErrors = candidate.packageErrors,
            )
        }

        if (contentBytes.size.toLong() > AGENT_INBOX_MAX_REVIEW_CONTENT_BYTES) {
            return AgentInboxImportResult(
                status = AgentInboxImportStatus.INVALID,
                requestedHighPriority = manifest.requestsHighPriority,
                packageErrors = setOf(AgentInboxPackageValidationError.CONTENT_FILE_TOO_LARGE),
            )
        }

        val contentSha256 = AgentInboxManifestValidator.sha256(contentBytes)
        val reviewedSha256 = candidate.reviewedContentSha256
        val reviewedSizeBytes = candidate.reviewedContentSizeBytes
        if (
            (reviewedSha256 != null && reviewedSha256 != contentSha256) ||
            (reviewedSizeBytes != null && reviewedSizeBytes != contentBytes.size.toLong())
        ) {
            return AgentInboxImportResult(
                status = AgentInboxImportStatus.INVALID,
                requestedHighPriority = manifest.requestsHighPriority,
                packageErrors = setOf(AgentInboxPackageValidationError.CONTENT_CHANGED_AFTER_REVIEW),
            )
        }

        if (manifest.documentSha256 != null && manifest.documentSha256 != contentSha256) {
            return AgentInboxImportResult(
                status = AgentInboxImportStatus.INVALID,
                requestedHighPriority = manifest.requestsHighPriority,
                manifestErrors = setOf(AgentInboxManifestValidationError.DOCUMENT_SHA256_MISMATCH),
            )
        }
        val safeImageAttachmentBytes = if (manifest.format == ContentFormat.MARKDOWN) {
            imageAttachmentBytes
        } else {
            emptyMap()
        }
        if (
            safeImageAttachmentBytes.values.any { bytes -> bytes.size.toLong() > AGENT_INBOX_MAX_IMAGE_ATTACHMENT_BYTES } ||
            safeImageAttachmentBytes.values.sumOf { bytes -> bytes.size.toLong() } > AGENT_INBOX_MAX_TOTAL_IMAGE_ATTACHMENT_BYTES
        ) {
            return AgentInboxImportResult(
                status = AgentInboxImportStatus.INVALID,
                requestedHighPriority = manifest.requestsHighPriority,
                packageErrors = setOf(AgentInboxPackageValidationError.IMAGE_ATTACHMENT_TOO_LARGE),
            )
        }

        val duplicate = userDocumentRepository.findDocumentByFingerprintSha256(contentSha256)
        if (duplicate != null) {
            return AgentInboxImportResult(
                status = AgentInboxImportStatus.DUPLICATE,
                duplicateContentId = duplicate.id,
                requestedHighPriority = manifest.requestsHighPriority,
            )
        }

        val stored = documentStore.writeDocument(
            packageFolderId = candidate.packageFolderId,
            contentFileName = candidate.contentFileName,
            verifiedContentSha256 = contentSha256,
            format = manifest.format,
            bytes = contentBytes,
            imageAttachments = safeImageAttachmentBytes.map { (fileName, bytes) ->
                AgentInboxImageAttachmentWrite(fileName = fileName, bytes = bytes)
            },
        )
        val estimate = DocumentReadingTimeEstimator.estimate(manifest.format) {
            ByteArrayInputStream(contentBytes)
        }
        val draft = UserDocumentDraft(
            uri = stored.uri,
            displayName = AGENT_INBOX_DOCUMENT_DISPLAY_NAME,
            mimeType = stored.mimeType,
            title = manifest.title,
            description = manifest.description.orEmpty(),
            durationMinutes = estimate.minutes,
            topicTags = manifest.topics,
            imageAttachmentUris = stored.imageAttachmentUris,
            documentFingerprintSha256 = contentSha256,
            documentFingerprintSizeBytes = contentBytes.size.toLong(),
        )

        val addResult = try {
            userDocumentRepository.addDocumentIfFingerprintAbsent(
                draft = draft,
                fingerprintSha256 = contentSha256,
                nowMillis = nowMillis,
            )
        } catch (error: Throwable) {
            documentStore.deleteDocument(stored)
            throw error
        }

        return when (addResult) {
            is AddUserDocumentIfFingerprintAbsentResult.Added -> AgentInboxImportResult(
                status = AgentInboxImportStatus.IMPORTED,
                item = addResult.item,
                requestedHighPriority = manifest.requestsHighPriority,
            )

            is AddUserDocumentIfFingerprintAbsentResult.Duplicate -> {
                documentStore.deleteDocument(stored)
                AgentInboxImportResult(
                    status = AgentInboxImportStatus.DUPLICATE,
                    duplicateContentId = addResult.item.id,
                    requestedHighPriority = manifest.requestsHighPriority,
                )
            }

            is AddUserDocumentIfFingerprintAbsentResult.Rejected -> {
                documentStore.deleteDocument(stored)
                AgentInboxImportResult(
                    status = AgentInboxImportStatus.REJECTED,
                    requestedHighPriority = manifest.requestsHighPriority,
                    documentErrors = addResult.errors,
                )
            }
        }
    }
}
