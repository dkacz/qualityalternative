package com.qualityalternative.app.data

import com.qualityalternative.app.domain.service.AGENT_INBOX_MANIFEST_FILE_NAME
import com.qualityalternative.app.domain.service.AgentInboxDrivePackage

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
    CONTENT_CHANGED_AFTER_REVIEW,
    DOWNLOAD_UNAVAILABLE,
    LOCAL_IMPORT_REJECTED,
}

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
        if (drivePackage.unsupportedExtraFiles().isNotEmpty()) {
            return drivePackage.invalidCandidate(
                manifestFileId = manifestFile?.id,
                packageErrors = setOf(AgentInboxPackageValidationError.UNSUPPORTED_EXTRA_FILES),
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

private fun AgentInboxDrivePackage.unsupportedExtraFiles(): List<com.qualityalternative.app.domain.service.AgentInboxDriveFile> {
    return allFiles.filterNot { file ->
        file.name == AGENT_INBOX_MANIFEST_FILE_NAME ||
            contentFiles.any { contentFile -> contentFile.id == file.id }
    }
}

fun AgentInboxReviewCandidate.displayTitle(): String {
    return manifest?.title ?: packageFolderName.ifBlank { AGENT_INBOX_MANIFEST_FILE_NAME }
}
