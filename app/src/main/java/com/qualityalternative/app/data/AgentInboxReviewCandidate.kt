package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.service.AGENT_INBOX_MANIFEST_FILE_NAME
import com.qualityalternative.app.domain.service.AGENT_INBOX_MAX_IMAGE_ATTACHMENT_BYTES
import com.qualityalternative.app.domain.service.AGENT_INBOX_MAX_IMAGE_ATTACHMENTS_PER_PACKAGE
import com.qualityalternative.app.domain.service.AGENT_INBOX_MAX_TOTAL_IMAGE_ATTACHMENT_BYTES
import com.qualityalternative.app.domain.service.AgentInboxDrivePackage
import java.util.Locale

enum class AgentInboxReviewStatus {
    READY,
    INVALID,
    DUPLICATE,
}

enum class AgentInboxPackageValidationError {
    MISSING_MANIFEST,
    MISSING_CONTENT_FILE,
    TOO_MANY_FILES,
    CONTENT_FILE_TOO_LARGE,
    MANIFEST_FILE_TOO_LARGE,
    MULTIPLE_MANIFESTS,
    MULTIPLE_CONTENT_FILES,
    UNSUPPORTED_EXTRA_FILES,
    TOO_MANY_IMAGE_ATTACHMENTS,
    DUPLICATE_IMAGE_ATTACHMENTS,
    IMAGE_ATTACHMENT_TOO_LARGE,
    CONTENT_CHANGED_AFTER_REVIEW,
    DOWNLOAD_UNAVAILABLE,
    LOCAL_IMPORT_REJECTED,
}

data class AgentInboxImageAttachmentFile(
    val fileId: String,
    val fileName: String,
    val sizeBytes: Long?,
)

data class AgentInboxReviewCandidate(
    val packageFolderId: String,
    val packageFolderName: String,
    val status: AgentInboxReviewStatus,
    val manifest: AgentInboxManifest?,
    val manifestFileId: String?,
    val contentFileId: String?,
    val contentFileName: String?,
    val duplicateContentId: String?,
    val reviewedContentSha256: String? = null,
    val reviewedContentSizeBytes: Long? = null,
    val imageAttachmentFiles: List<AgentInboxImageAttachmentFile> = emptyList(),
    val manifestErrors: Set<AgentInboxManifestValidationError> = emptySet(),
    val packageErrors: Set<AgentInboxPackageValidationError> = emptySet(),
) {
    val requestsHighPriority: Boolean
        get() = manifest?.requestsHighPriority == true

    val canImport: Boolean
        get() = status == AgentInboxReviewStatus.READY && manifest != null && contentFileId != null
}

object AgentInboxReviewCandidateFactory {
    fun fromDrivePackage(
        drivePackage: AgentInboxDrivePackage,
        manifestJson: String?,
        existingDocumentIdsBySha256: Map<String, String> = emptyMap(),
        actualContentSha256: String? = null,
        actualContentSizeBytes: Long? = null,
    ): AgentInboxReviewCandidate {
        val manifestFiles = drivePackage.allFiles.filter { file -> file.name == AGENT_INBOX_MANIFEST_FILE_NAME }
        val manifestFile = manifestFiles.firstOrNull()
        if (drivePackage.hasMoreFiles) {
            return drivePackage.invalidCandidate(
                manifestFileId = manifestFile?.id,
                packageErrors = setOf(AgentInboxPackageValidationError.TOO_MANY_FILES),
            )
        }
        if (manifestFiles.size > 1) {
            return drivePackage.invalidCandidate(
                manifestFileId = manifestFile?.id,
                packageErrors = setOf(AgentInboxPackageValidationError.MULTIPLE_MANIFESTS),
            )
        }
        if (drivePackage.contentFiles.size > 1) {
            return drivePackage.invalidCandidate(
                manifestFileId = manifestFile?.id,
                packageErrors = setOf(AgentInboxPackageValidationError.MULTIPLE_CONTENT_FILES),
            )
        }
        if (manifestFile == null || manifestJson == null) {
            return drivePackage.invalidCandidate(
                packageErrors = setOf(AgentInboxPackageValidationError.MISSING_MANIFEST),
            )
        }

        val validation = AgentInboxManifestValidator.validate(
            manifestJson = manifestJson,
            actualContentSha256 = actualContentSha256,
        )
        val manifest = validation.manifest
        if (!validation.isValid || manifest == null) {
            return drivePackage.invalidCandidate(
                manifestFileId = manifestFile.id,
                manifestErrors = validation.errors,
            )
        }

        val contentFile = drivePackage.allFiles.firstOrNull { file -> file.name == manifest.contentFile }
        if (contentFile == null) {
            return drivePackage.invalidCandidate(
                manifestFileId = manifestFile.id,
                manifest = manifest,
                packageErrors = setOf(AgentInboxPackageValidationError.MISSING_CONTENT_FILE),
            )
        }
        val imageAttachments = if (manifest.format == ContentFormat.MARKDOWN) {
            drivePackage.markdownImageAttachmentFiles(contentFileId = contentFile.id)
        } else {
            emptyList()
        }
        when {
            drivePackage.unsupportedExtraFiles(
                contentFileId = contentFile.id,
                imageAttachmentFileIds = imageAttachments.mapTo(mutableSetOf()) { file -> file.fileId },
            ).isNotEmpty() -> {
                return drivePackage.invalidCandidate(
                    manifestFileId = manifestFile.id,
                    manifest = manifest,
                    packageErrors = setOf(AgentInboxPackageValidationError.UNSUPPORTED_EXTRA_FILES),
                )
            }
            imageAttachments.size > AGENT_INBOX_MAX_IMAGE_ATTACHMENTS_PER_PACKAGE -> {
                return drivePackage.invalidCandidate(
                    manifestFileId = manifestFile.id,
                    manifest = manifest,
                    packageErrors = setOf(AgentInboxPackageValidationError.TOO_MANY_IMAGE_ATTACHMENTS),
                )
            }
            imageAttachments.hasDuplicateAddressableNames() -> {
                return drivePackage.invalidCandidate(
                    manifestFileId = manifestFile.id,
                    manifest = manifest,
                    packageErrors = setOf(AgentInboxPackageValidationError.DUPLICATE_IMAGE_ATTACHMENTS),
                )
            }
            imageAttachments.any { file -> (file.sizeBytes ?: 0L) > AGENT_INBOX_MAX_IMAGE_ATTACHMENT_BYTES } ||
                imageAttachments.sumOf { file -> file.sizeBytes ?: 0L } > AGENT_INBOX_MAX_TOTAL_IMAGE_ATTACHMENT_BYTES -> {
                return drivePackage.invalidCandidate(
                    manifestFileId = manifestFile.id,
                    manifest = manifest,
                    packageErrors = setOf(AgentInboxPackageValidationError.IMAGE_ATTACHMENT_TOO_LARGE),
                )
            }
        }

        val duplicateContentId = actualContentSha256?.let(existingDocumentIdsBySha256::get)
        return AgentInboxReviewCandidate(
            packageFolderId = drivePackage.folderId,
            packageFolderName = drivePackage.folderName,
            status = if (duplicateContentId == null) {
                AgentInboxReviewStatus.READY
            } else {
                AgentInboxReviewStatus.DUPLICATE
            },
            manifest = manifest,
            manifestFileId = manifestFile.id,
            contentFileId = contentFile.id,
            contentFileName = contentFile.name,
            duplicateContentId = duplicateContentId,
            reviewedContentSha256 = actualContentSha256,
            reviewedContentSizeBytes = actualContentSizeBytes,
            imageAttachmentFiles = imageAttachments,
        )
    }

    fun invalidPackage(
        drivePackage: AgentInboxDrivePackage,
        packageErrors: Set<AgentInboxPackageValidationError>,
    ): AgentInboxReviewCandidate {
        return drivePackage.invalidCandidate(
            manifestFileId = drivePackage.manifestFile?.id,
            packageErrors = packageErrors,
        )
    }

    private fun AgentInboxDrivePackage.invalidCandidate(
        manifestFileId: String? = manifestFile?.id,
        manifest: AgentInboxManifest? = null,
        manifestErrors: Set<AgentInboxManifestValidationError> = emptySet(),
        packageErrors: Set<AgentInboxPackageValidationError> = emptySet(),
    ): AgentInboxReviewCandidate {
        return AgentInboxReviewCandidate(
            packageFolderId = folderId,
            packageFolderName = folderName,
            status = AgentInboxReviewStatus.INVALID,
            manifest = manifest,
            manifestFileId = manifestFileId,
            contentFileId = null,
            contentFileName = null,
            duplicateContentId = null,
            manifestErrors = manifestErrors,
            packageErrors = packageErrors,
        )
    }
}

private fun AgentInboxDrivePackage.unsupportedExtraFiles(
    contentFileId: String,
    imageAttachmentFileIds: Set<String>,
): List<com.qualityalternative.app.domain.service.AgentInboxDriveFile> {
    return allFiles.filterNot { file ->
        file.name == AGENT_INBOX_MANIFEST_FILE_NAME ||
            file.id == contentFileId ||
            file.id in imageAttachmentFileIds
    }
}

private fun AgentInboxDrivePackage.markdownImageAttachmentFiles(contentFileId: String): List<AgentInboxImageAttachmentFile> {
    return allFiles
        .filter { file -> file.id != contentFileId && file.name != AGENT_INBOX_MANIFEST_FILE_NAME }
        .filter { file -> file.name.isSafeAgentInboxMarkdownImageAttachmentName() }
        .map { file ->
            AgentInboxImageAttachmentFile(
                fileId = file.id,
                fileName = file.name,
                sizeBytes = file.sizeBytes,
            )
        }
}

private fun List<AgentInboxImageAttachmentFile>.hasDuplicateAddressableNames(): Boolean {
    val displayNames = map { file -> file.fileName.trim().lowercase(Locale.US) }
    if (displayNames.distinct().size != displayNames.size) {
        return true
    }
    val storageSegments = map { file -> file.fileName.safeAgentInboxFileSegment().lowercase(Locale.US) }
    return storageSegments.distinct().size != storageSegments.size
}

private fun String.isSafeAgentInboxMarkdownImageAttachmentName(): Boolean {
    val lower = lowercase(Locale.US)
    if (isBlank() || contains('/') || contains('\\') || contains("..")) {
        return false
    }
    return lower.endsWith(".png") ||
        lower.endsWith(".jpg") ||
        lower.endsWith(".jpeg") ||
        lower.endsWith(".webp") ||
        lower.endsWith(".gif") ||
        lower.endsWith(".bmp")
}

fun AgentInboxReviewCandidate.displayTitle(): String {
    return manifest?.title ?: packageFolderName.ifBlank { AGENT_INBOX_MANIFEST_FILE_NAME }
}
