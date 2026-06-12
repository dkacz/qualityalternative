package com.qualityalternative.app.data

import com.qualityalternative.app.domain.service.AGENT_INBOX_MANIFEST_FILE_NAME
import com.qualityalternative.app.domain.service.AgentInboxDriveFile
import com.qualityalternative.app.domain.service.AgentInboxDrivePackage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentInboxReviewCandidateFactoryTest {
    @Test
    fun fromDrivePackage_buildsReadyCandidateWithPriorityIntent() {
        val candidate = AgentInboxReviewCandidateFactory.fromDrivePackage(
            drivePackage = drivePackage(
                files = listOf(
                    driveFile("manifest-id", AGENT_INBOX_MANIFEST_FILE_NAME),
                    driveFile("content-id", "content.md"),
                ),
            ),
            manifestJson = manifestJson(priority = "high"),
        )

        assertEquals(AgentInboxReviewStatus.READY, candidate.status)
        assertTrue(candidate.canImport)
        assertEquals("package-folder", candidate.packageFolderId)
        assertEquals("manifest-id", candidate.manifestFileId)
        assertEquals("content-id", candidate.contentFileId)
        assertEquals("content.md", candidate.contentFileName)
        assertEquals("Agent Note", candidate.displayTitle())
        assertTrue(candidate.requestsHighPriority)
    }

    @Test
    fun fromDrivePackage_marksMissingManifestInvalid() {
        val candidate = AgentInboxReviewCandidateFactory.fromDrivePackage(
            drivePackage = drivePackage(files = listOf(driveFile("content-id", "content.md"))),
            manifestJson = null,
        )

        assertEquals(AgentInboxReviewStatus.INVALID, candidate.status)
        assertFalse(candidate.canImport)
        assertEquals(setOf(AgentInboxPackageValidationError.MISSING_MANIFEST), candidate.packageErrors)
    }

    @Test
    fun fromDrivePackage_marksManifestValidationErrorsInvalid() {
        val candidate = AgentInboxReviewCandidateFactory.fromDrivePackage(
            drivePackage = drivePackage(
                files = listOf(
                    driveFile("manifest-id", AGENT_INBOX_MANIFEST_FILE_NAME),
                    driveFile("content-id", "content.md"),
                ),
            ),
            manifestJson = manifestJson(rightsClass = "RENDERABLE"),
        )

        assertEquals(AgentInboxReviewStatus.INVALID, candidate.status)
        assertEquals("manifest-id", candidate.manifestFileId)
        assertEquals(
            setOf(AgentInboxManifestValidationError.RIGHTS_CLASS_MUST_BE_USER_PRIVATE),
            candidate.manifestErrors,
        )
    }

    @Test
    fun fromDrivePackage_marksMissingContentFileInvalid() {
        val candidate = AgentInboxReviewCandidateFactory.fromDrivePackage(
            drivePackage = drivePackage(files = listOf(driveFile("manifest-id", AGENT_INBOX_MANIFEST_FILE_NAME))),
            manifestJson = manifestJson(contentFile = "content.md"),
        )

        assertEquals(AgentInboxReviewStatus.INVALID, candidate.status)
        assertEquals(setOf(AgentInboxPackageValidationError.MISSING_CONTENT_FILE), candidate.packageErrors)
        assertEquals("Agent Note", candidate.displayTitle())
    }

    @Test
    fun fromDrivePackage_doesNotMarkDuplicateFromUnverifiedManifestSha() {
        val sha = "1".repeat(64)
        val candidate = AgentInboxReviewCandidateFactory.fromDrivePackage(
            drivePackage = drivePackage(
                files = listOf(
                    driveFile("manifest-id", AGENT_INBOX_MANIFEST_FILE_NAME),
                    driveFile("content-id", "content.md"),
                ),
            ),
            manifestJson = manifestJson(documentSha256 = sha),
            existingDocumentIdsBySha256 = mapOf(sha to "existing-doc"),
        )

        assertEquals(AgentInboxReviewStatus.READY, candidate.status)
        assertTrue(candidate.canImport)
        assertEquals(null, candidate.duplicateContentId)
    }

    @Test
    fun fromDrivePackage_marksActualContentShaAsDuplicateWhenManifestIncludesSameSha() {
        val sha = "1".repeat(64)
        val candidate = AgentInboxReviewCandidateFactory.fromDrivePackage(
            drivePackage = drivePackage(
                files = listOf(
                    driveFile("manifest-id", AGENT_INBOX_MANIFEST_FILE_NAME),
                    driveFile("content-id", "content.md"),
                ),
            ),
            manifestJson = manifestJson(documentSha256 = sha),
            existingDocumentIdsBySha256 = mapOf(sha to "existing-doc"),
            actualContentSha256 = sha,
        )

        assertEquals(AgentInboxReviewStatus.DUPLICATE, candidate.status)
        assertFalse(candidate.canImport)
        assertEquals("existing-doc", candidate.duplicateContentId)
    }

    @Test
    fun fromDrivePackage_marksActualContentShaAsDuplicateWhenManifestOmitsSha() {
        val sha = "2".repeat(64)
        val candidate = AgentInboxReviewCandidateFactory.fromDrivePackage(
            drivePackage = drivePackage(
                files = listOf(
                    driveFile("manifest-id", AGENT_INBOX_MANIFEST_FILE_NAME),
                    driveFile("content-id", "content.md"),
                ),
            ),
            manifestJson = manifestJson(documentSha256 = null),
            existingDocumentIdsBySha256 = mapOf(sha to "existing-doc"),
            actualContentSha256 = sha,
        )

        assertEquals(AgentInboxReviewStatus.DUPLICATE, candidate.status)
        assertFalse(candidate.canImport)
        assertEquals("existing-doc", candidate.duplicateContentId)
    }

    @Test
    fun fromDrivePackage_marksTruncatedPackageFileListingInvalid() {
        val candidate = AgentInboxReviewCandidateFactory.fromDrivePackage(
            drivePackage = drivePackage(
                files = listOf(
                    driveFile("manifest-id", AGENT_INBOX_MANIFEST_FILE_NAME),
                    driveFile("content-id", "content.md"),
                ),
                hasMoreFiles = true,
            ),
            manifestJson = manifestJson(),
        )

        assertEquals(AgentInboxReviewStatus.INVALID, candidate.status)
        assertFalse(candidate.canImport)
        assertEquals(setOf(AgentInboxPackageValidationError.TOO_MANY_FILES), candidate.packageErrors)
    }

    @Test
    fun fromDrivePackage_marksDuplicateManifestsInvalid() {
        val candidate = AgentInboxReviewCandidateFactory.fromDrivePackage(
            drivePackage = drivePackage(
                files = listOf(
                    driveFile("manifest-id-1", AGENT_INBOX_MANIFEST_FILE_NAME),
                    driveFile("manifest-id-2", AGENT_INBOX_MANIFEST_FILE_NAME),
                    driveFile("content-id", "content.md"),
                ),
            ),
            manifestJson = manifestJson(),
        )

        assertEquals(AgentInboxReviewStatus.INVALID, candidate.status)
        assertFalse(candidate.canImport)
        assertEquals(setOf(AgentInboxPackageValidationError.MULTIPLE_MANIFESTS), candidate.packageErrors)
    }

    @Test
    fun fromDrivePackage_marksMultipleContentFilesInvalid() {
        val candidate = AgentInboxReviewCandidateFactory.fromDrivePackage(
            drivePackage = drivePackage(
                files = listOf(
                    driveFile("manifest-id", AGENT_INBOX_MANIFEST_FILE_NAME),
                    driveFile("content-id", "content.md"),
                    driveFile("second-content-id", "other.epub"),
                ),
            ),
            manifestJson = manifestJson(),
        )

        assertEquals(AgentInboxReviewStatus.INVALID, candidate.status)
        assertFalse(candidate.canImport)
        assertEquals(setOf(AgentInboxPackageValidationError.MULTIPLE_CONTENT_FILES), candidate.packageErrors)
    }

    @Test
    fun fromDrivePackage_marksUnsupportedExtraFilesInvalid() {
        val candidate = AgentInboxReviewCandidateFactory.fromDrivePackage(
            drivePackage = drivePackage(
                files = listOf(
                    driveFile("manifest-id", AGENT_INBOX_MANIFEST_FILE_NAME),
                    driveFile("content-id", "content.md"),
                    driveFile("cover-id", "cover.png"),
                ),
            ),
            manifestJson = manifestJson(),
        )

        assertEquals(AgentInboxReviewStatus.INVALID, candidate.status)
        assertFalse(candidate.canImport)
        assertEquals(setOf(AgentInboxPackageValidationError.UNSUPPORTED_EXTRA_FILES), candidate.packageErrors)
    }

    private fun drivePackage(
        files: List<AgentInboxDriveFile>,
        hasMoreFiles: Boolean = false,
    ): AgentInboxDrivePackage {
        return AgentInboxDrivePackage(
            folderId = "package-folder",
            folderName = "Codex package",
            manifestFile = files.firstOrNull { file -> file.name == AGENT_INBOX_MANIFEST_FILE_NAME },
            contentFiles = files.filter { file -> file.name.endsWith(".md") || file.name.endsWith(".epub") },
            allFiles = files,
            hasMoreFiles = hasMoreFiles,
        )
    }

    private fun driveFile(id: String, name: String): AgentInboxDriveFile {
        return AgentInboxDriveFile(
            id = id,
            name = name,
            mimeType = null,
            sizeBytes = null,
            md5Checksum = null,
            modifiedTime = null,
        )
    }

    private fun manifestJson(
        contentFile: String = "content.md",
        rightsClass: String = "USER_PRIVATE",
        priority: String = "normal",
        documentSha256: String? = null,
    ): String {
        val shaField = documentSha256?.let { ""","documentSha256":"$it"""" }.orEmpty()
        return """
            {
              "schemaVersion": 1,
              "title": "Agent Note",
              "topics": ["ATTENTION"],
              "contentFile": "$contentFile",
              "format": "MARKDOWN",
              "rightsClass": "$rightsClass",
              "priority": "$priority"
              $shaField
            }
        """.trimIndent()
    }
}
