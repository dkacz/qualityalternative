package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.ContentAvailability
import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.ContentRightsMetadata
import com.qualityalternative.app.domain.model.ContentSourceType
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.model.UserDocumentDraft
import com.qualityalternative.app.domain.model.UserDocumentValidationError
import com.qualityalternative.app.domain.service.AddUserDocumentIfFingerprintAbsentResult
import com.qualityalternative.app.domain.service.AddUserDocumentResult
import com.qualityalternative.app.domain.service.AGENT_INBOX_MAX_IMAGE_ATTACHMENT_BYTES
import com.qualityalternative.app.domain.service.AGENT_INBOX_MAX_REVIEW_CONTENT_BYTES
import com.qualityalternative.app.domain.service.UserDocumentRepository
import java.io.File
import java.io.IOException
import java.net.URI
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class AgentInboxPackageImporterTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun importCandidateWritesDocumentAndAddsUserDocumentDraft() = runBlocking {
        val contentBytes = "# Attention\n\nA useful private note for an impulse.".toByteArray()
        val sha = AgentInboxManifestValidator.sha256(contentBytes)
        val repository = FakeAgentInboxUserDocumentRepository()
        val store = FakeAgentInboxDocumentStore()
        val importer = AgentInboxPackageImporter(userDocumentRepository = repository, documentStore = store)
        val candidate = readyCandidate(documentSha256 = sha, priority = "high")

        val result = importer.importCandidate(
            candidate = candidate,
            contentBytes = contentBytes,
            nowMillis = 2_000L,
        )

        assertEquals(AgentInboxImportStatus.IMPORTED, result.status)
        assertEquals("agent-doc-1", result.item?.id)
        assertTrue(result.requestedHighPriority)
        assertEquals(1, store.writes.size)
        assertEquals("file:/agent-inbox/package-folder/$sha.md", repository.addedDrafts.single().uri)
        assertEquals("Agent Inbox document", repository.addedDrafts.single().displayName)
        assertEquals("text/markdown", repository.addedDrafts.single().mimeType)
        assertEquals("Agent Note", repository.addedDrafts.single().title)
        assertEquals("Private replacement note.", repository.addedDrafts.single().description)
        assertEquals(3, repository.addedDrafts.single().durationMinutes)
        assertEquals(setOf(TopicTag.ATTENTION), repository.addedDrafts.single().topicTags)
        assertEquals(sha, repository.addedDrafts.single().documentFingerprintSha256)
        assertEquals(contentBytes.size.toLong(), repository.addedDrafts.single().documentFingerprintSizeBytes)
    }

    @Test
    fun importCandidateCanSaveAsUncategorizedCompatibleOtherTopic() = runBlocking {
        val contentBytes = "# Attention\n\nA useful private note for an impulse.".toByteArray()
        val sha = AgentInboxManifestValidator.sha256(contentBytes)
        val repository = FakeAgentInboxUserDocumentRepository()
        val store = FakeAgentInboxDocumentStore()
        val importer = AgentInboxPackageImporter(userDocumentRepository = repository, documentStore = store)

        val result = importer.importCandidate(
            candidate = readyCandidate(documentSha256 = sha, priority = "normal"),
            contentBytes = contentBytes,
            categoryMode = AgentInboxImportCategoryMode.UNCATEGORIZED,
            nowMillis = 2_000L,
        )

        assertEquals(AgentInboxImportStatus.IMPORTED, result.status)
        assertEquals(setOf(TopicTag.OTHER), repository.addedDrafts.single().topicTags)
        assertEquals(setOf(TopicTag.OTHER), result.item?.topicTags)
    }

    @Test
    fun importCandidateStoresMarkdownImageAttachmentsInDraft() = runBlocking {
        val contentBytes = "# Attention\n\n![Cover](cover.png)".toByteArray()
        val imageBytes = byteArrayOf(1, 2, 3, 4)
        val sha = AgentInboxManifestValidator.sha256(contentBytes)
        val repository = FakeAgentInboxUserDocumentRepository()
        val storageRoot = temporaryFolder.newFolder("agent-inbox-images")
        val store = FileAgentInboxDocumentStore(storageRoot)
        val importer = AgentInboxPackageImporter(userDocumentRepository = repository, documentStore = store)
        val candidate = readyCandidate(documentSha256 = sha)

        val result = importer.importCandidate(
            candidate = candidate,
            contentBytes = contentBytes,
            imageAttachmentBytes = mapOf("cover.png" to imageBytes),
            nowMillis = 2_000L,
        )

        assertEquals(AgentInboxImportStatus.IMPORTED, result.status)
        val attachmentUris = repository.addedDrafts.single().imageAttachmentUris
        assertEquals(setOf("cover.png"), attachmentUris.keys)
        val storedImage = File(URI.create(requireNotNull(attachmentUris["cover.png"])))
        assertEquals(imageBytes.toList(), storedImage.readBytes().toList())
        assertTrue(storedImage.canonicalFile.toPath().startsWith(storageRoot.canonicalFile.toPath()))
    }

    @Test
    fun importCandidatePersistsMultiMegabyteMarkdownImageAttachment() = runBlocking {
        val contentBytes = "# Attention\n\n![Large](large.png)".toByteArray()
        val imageBytes = ByteArray((3.5 * 1024 * 1024).toInt()) { index -> (index % 251).toByte() }
        assertTrue(imageBytes.size.toLong() < AGENT_INBOX_MAX_IMAGE_ATTACHMENT_BYTES)
        val sha = AgentInboxManifestValidator.sha256(contentBytes)
        val repository = FakeAgentInboxUserDocumentRepository()
        val storageRoot = temporaryFolder.newFolder("agent-inbox-large-images")
        val store = FileAgentInboxDocumentStore(storageRoot)
        val importer = AgentInboxPackageImporter(userDocumentRepository = repository, documentStore = store)
        val candidate = readyCandidate(documentSha256 = sha)

        val result = importer.importCandidate(
            candidate = candidate,
            contentBytes = contentBytes,
            imageAttachmentBytes = mapOf("large.png" to imageBytes),
            nowMillis = 2_000L,
        )

        assertEquals(AgentInboxImportStatus.IMPORTED, result.status)
        val attachmentUri = requireNotNull(repository.addedDrafts.single().imageAttachmentUris["large.png"])
        val storedImage = File(URI.create(attachmentUri))
        assertEquals(imageBytes.size.toLong(), storedImage.length())
        assertEquals(
            AgentInboxManifestValidator.sha256(imageBytes),
            AgentInboxManifestValidator.sha256(storedImage.readBytes()),
        )
        assertTrue(storedImage.canonicalFile.toPath().startsWith(storageRoot.canonicalFile.toPath()))
    }

    @Test
    fun importCandidateMapsSidecarTempCreationFailureToImageWriteFailure() = runBlocking {
        val contentBytes = "# Attention\n\n![Cover](cover.png)".toByteArray()
        val imageBytes = byteArrayOf(1, 2, 3, 4)
        val sha = AgentInboxManifestValidator.sha256(contentBytes)
        val repository = FakeAgentInboxUserDocumentRepository()
        val storageRoot = temporaryFolder.newFolder("agent-inbox-sidecar-temp-failure")
        val store = FileAgentInboxDocumentStore(
            rootDirectory = storageRoot,
            tempFileFactory = { prefix, suffix, directory ->
                if ("-img-" in prefix && suffix == ".tmp") {
                    throw IOException("sidecar temp denied")
                }
                File.createTempFile(prefix, suffix, directory)
            },
        )
        val importer = AgentInboxPackageImporter(userDocumentRepository = repository, documentStore = store)
        val candidate = readyCandidate(documentSha256 = sha)

        var thrown: Throwable? = null
        try {
            importer.importCandidate(
                candidate = candidate,
                contentBytes = contentBytes,
                imageAttachmentBytes = mapOf("cover.png" to imageBytes),
                nowMillis = 2_000L,
            )
        } catch (error: Throwable) {
            thrown = error
        }

        assertTrue(thrown is AgentInboxImageAttachmentWriteException)
        val imageWriteError = thrown as AgentInboxImageAttachmentWriteException
        val rootCause = generateSequence<Throwable>(imageWriteError) { cause -> cause.cause }.last()
        assertEquals("sidecar temp denied", rootCause.message)
        assertEquals(
            AgentInboxPackageValidationError.IMAGE_WRITE_FAILED,
            imageWriteError.toAgentInboxImportPackageError(),
        )
        assertTrue(storageRoot.listFiles().orEmpty().isEmpty())
    }

    @Test
    fun importCandidateWithSamePackageAndNameButDifferentBytesDoesNotOverwritePriorDocument() = runBlocking {
        val firstBytes = "# First\n\nOriginal private note.".toByteArray()
        val secondBytes = "# Second\n\nUpdated but distinct private note.".toByteArray()
        val firstSha = AgentInboxManifestValidator.sha256(firstBytes)
        val secondSha = AgentInboxManifestValidator.sha256(secondBytes)
        val repository = UriUpsertingFingerprintRepository()
        val store = FileAgentInboxDocumentStore(temporaryFolder.newFolder("agent-inbox"))
        val importer = AgentInboxPackageImporter(userDocumentRepository = repository, documentStore = store)

        val first = importer.importCandidate(
            candidate = readyCandidate(
                documentSha256 = firstSha,
                reviewedContentSha256 = firstSha,
                reviewedContentSizeBytes = firstBytes.size.toLong(),
            ),
            contentBytes = firstBytes,
            nowMillis = 1_000L,
        )
        val second = importer.importCandidate(
            candidate = readyCandidate(
                documentSha256 = secondSha,
                reviewedContentSha256 = secondSha,
                reviewedContentSizeBytes = secondBytes.size.toLong(),
            ),
            contentBytes = secondBytes,
            nowMillis = 2_000L,
        )

        assertEquals(AgentInboxImportStatus.IMPORTED, first.status)
        assertEquals(AgentInboxImportStatus.IMPORTED, second.status)
        assertEquals(2, repository.documents.size)
        assertEquals(2, repository.addedDrafts.size)
        assertNotEquals(repository.addedDrafts[0].uri, repository.addedDrafts[1].uri)
        assertTrue(repository.addedDrafts[0].uri.contains(firstSha))
        assertTrue(repository.addedDrafts[1].uri.contains(secondSha))
        assertEquals(firstSha, repository.documents[0].documentFingerprintSha256)
        assertEquals(secondSha, repository.documents[1].documentFingerprintSha256)
    }

    @Test
    fun fileStoreReplacesStaleMismatchingFinalFileWithVerifiedBytes() = runBlocking {
        val storageRoot = temporaryFolder.newFolder("stale-agent-inbox")
        val contentBytes = "# Verified\n\nComplete private note.".toByteArray()
        val sha = AgentInboxManifestValidator.sha256(contentBytes)
        val staleFile = File(storageRoot, "package-folder-$sha.md")
        staleFile.writeText("partial stale bytes")
        val store = FileAgentInboxDocumentStore(storageRoot)

        val stored = store.writeDocument(
            packageFolderId = "package-folder",
            contentFileName = "content.md",
            verifiedContentSha256 = sha,
            format = ContentFormat.MARKDOWN,
            bytes = contentBytes,
            imageAttachments = emptyList(),
        )

        val storedFile = File(URI.create(stored.uri))
        assertEquals(sha, AgentInboxManifestValidator.sha256(storedFile.readBytes()))
        assertEquals(contentBytes.toList(), storedFile.readBytes().toList())
        assertTrue(storageRoot.listFiles().orEmpty().none { file -> file.name.endsWith(".tmp") })
        assertEquals(1, storageRoot.listFiles().orEmpty().count { file -> file.extension == "md" })
    }

    @Test
    fun fileStoreRemovesPromotedSidecarsWhenLaterAttachmentWriteFails() = runBlocking {
        val storageRoot = temporaryFolder.newFolder("failing-sidecar-agent-inbox")
        val contentBytes = "# Verified\n\nComplete private note.".toByteArray()
        val sha = AgentInboxManifestValidator.sha256(contentBytes)
        val safeContentName = "package-folder-$sha.md"
        val firstSidecar = File(storageRoot, "$safeContentName-img-first.png")
        val blockingSecondTarget = File(storageRoot, "$safeContentName-img-second.png")
        assertTrue(blockingSecondTarget.mkdir())
        val store = FileAgentInboxDocumentStore(storageRoot)

        val error = runCatching {
            store.writeDocument(
                packageFolderId = "package-folder",
                contentFileName = "content.md",
                verifiedContentSha256 = sha,
                format = ContentFormat.MARKDOWN,
                bytes = contentBytes,
                imageAttachments = listOf(
                    AgentInboxImageAttachmentWrite(fileName = "first.png", bytes = byteArrayOf(1)),
                    AgentInboxImageAttachmentWrite(fileName = "second.png", bytes = byteArrayOf(2)),
                ),
            )
        }.exceptionOrNull()

        assertTrue(error is IllegalStateException)
        assertFalse(File(storageRoot, safeContentName).exists())
        assertFalse(firstSidecar.exists())
        assertTrue(storageRoot.listFiles().orEmpty().none { file -> file.name.endsWith(".tmp") })
    }

    @Test
    fun importCandidateReturnsDuplicateBeforeWritingWhenContentShaAlreadyExists() = runBlocking {
        val contentBytes = "duplicate".toByteArray()
        val sha = AgentInboxManifestValidator.sha256(contentBytes)
        val repository = FakeAgentInboxUserDocumentRepository(
            initialDocuments = listOf(existingDocument(id = "existing-doc", sha = sha)),
        )
        val store = FakeAgentInboxDocumentStore()
        val importer = AgentInboxPackageImporter(userDocumentRepository = repository, documentStore = store)

        val result = importer.importCandidate(
            candidate = readyCandidate(documentSha256 = sha),
            contentBytes = contentBytes,
        )

        assertEquals(AgentInboxImportStatus.DUPLICATE, result.status)
        assertEquals("existing-doc", result.duplicateContentId)
        assertTrue(store.writes.isEmpty())
        assertTrue(repository.addedDrafts.isEmpty())
    }

    @Test
    fun importCandidateReturnsDuplicateFromAuthoritativeFingerprintLookupWhenSnapshotIsEmpty() = runBlocking {
        val contentBytes = "duplicate from dao".toByteArray()
        val sha = AgentInboxManifestValidator.sha256(contentBytes)
        val repository = FakeAgentInboxUserDocumentRepository(
            fingerprintLookupDocuments = mapOf(sha to existingDocument(id = "existing-doc", sha = sha)),
        )
        val store = FakeAgentInboxDocumentStore()
        val importer = AgentInboxPackageImporter(userDocumentRepository = repository, documentStore = store)

        val result = importer.importCandidate(
            candidate = readyCandidate(documentSha256 = sha),
            contentBytes = contentBytes,
        )

        assertEquals(AgentInboxImportStatus.DUPLICATE, result.status)
        assertEquals("existing-doc", result.duplicateContentId)
        assertTrue(repository.userDocuments().isEmpty())
        assertTrue(store.writes.isEmpty())
        assertTrue(repository.addedDrafts.isEmpty())
    }

    @Test
    fun importCandidateSerializesConcurrentSameShaPackagesThroughAtomicRepositoryAdd() = runBlocking {
        val contentBytes = "# Same\n\nBoth packages contain identical private content.".toByteArray()
        val sha = AgentInboxManifestValidator.sha256(contentBytes)
        val repository = FakeAgentInboxUserDocumentRepository()
        val storageRoot = temporaryFolder.newFolder("concurrent-agent-inbox")
        val store = FileAgentInboxDocumentStore(storageRoot)
        val importer = AgentInboxPackageImporter(userDocumentRepository = repository, documentStore = store)
        val candidates = listOf(
            readyCandidate(packageFolderId = "package-a", documentSha256 = sha),
            readyCandidate(packageFolderId = "package-b", documentSha256 = sha),
        )

        val results = candidates.map { candidate ->
            async {
                importer.importCandidate(
                    candidate = candidate,
                    contentBytes = contentBytes,
                    nowMillis = 2_000L,
                )
            }
        }.awaitAll()

        val imported = results.single { result -> result.status == AgentInboxImportStatus.IMPORTED }
        val duplicate = results.single { result -> result.status == AgentInboxImportStatus.DUPLICATE }
        assertEquals(imported.item?.id, duplicate.duplicateContentId)
        assertEquals(1, repository.userDocuments().size)
        assertEquals(1, repository.addedDrafts.size)
        assertEquals(sha, repository.userDocuments().single().documentFingerprintSha256)
        assertEquals(1, storageRoot.listFiles().orEmpty().size)
    }

    @Test
    fun importCandidateRejectsShaMismatchBeforeWriting() = runBlocking {
        val repository = FakeAgentInboxUserDocumentRepository()
        val store = FakeAgentInboxDocumentStore()
        val importer = AgentInboxPackageImporter(userDocumentRepository = repository, documentStore = store)

        val result = importer.importCandidate(
            candidate = readyCandidate(documentSha256 = "0".repeat(64)),
            contentBytes = "different".toByteArray(),
        )

        assertEquals(AgentInboxImportStatus.INVALID, result.status)
        assertEquals(setOf(AgentInboxManifestValidationError.DOCUMENT_SHA256_MISMATCH), result.manifestErrors)
        assertTrue(store.writes.isEmpty())
        assertTrue(repository.addedDrafts.isEmpty())
    }

    @Test
    fun importCandidateRejectsContentChangedAfterReviewWhenManifestOmitsSha() = runBlocking {
        val reviewedBytes = "reviewed".toByteArray()
        val changedBytes = "changed".toByteArray()
        val repository = FakeAgentInboxUserDocumentRepository()
        val store = FakeAgentInboxDocumentStore()
        val importer = AgentInboxPackageImporter(userDocumentRepository = repository, documentStore = store)

        val result = importer.importCandidate(
            candidate = readyCandidate(
                documentSha256 = null,
                reviewedContentSha256 = AgentInboxManifestValidator.sha256(reviewedBytes),
                reviewedContentSizeBytes = reviewedBytes.size.toLong(),
            ),
            contentBytes = changedBytes,
        )

        assertEquals(AgentInboxImportStatus.INVALID, result.status)
        assertEquals(setOf(AgentInboxPackageValidationError.CONTENT_CHANGED_AFTER_REVIEW), result.packageErrors)
        assertTrue(store.writes.isEmpty())
        assertTrue(repository.addedDrafts.isEmpty())
    }

    @Test
    fun importCandidateDoesNotExposeRawDriveFileNameAsDocumentDisplayName() = runBlocking {
        val contentBytes = "# Private\n\nDetails.".toByteArray()
        val repository = FakeAgentInboxUserDocumentRepository()
        val store = FakeAgentInboxDocumentStore()
        val importer = AgentInboxPackageImporter(userDocumentRepository = repository, documentStore = store)

        val result = importer.importCandidate(
            candidate = readyCandidate(
                documentSha256 = AgentInboxManifestValidator.sha256(contentBytes),
                contentFileName = "private-client-diagnosis.md",
            ),
            contentBytes = contentBytes,
        )

        assertEquals(AgentInboxImportStatus.IMPORTED, result.status)
        assertEquals("Agent Inbox document", repository.addedDrafts.single().displayName)
        assertEquals("private-client-diagnosis.md", store.writes.single().contentFileName)
    }

    @Test
    fun importCandidateRejectsOversizedContentBeforeWriting() = runBlocking {
        val repository = FakeAgentInboxUserDocumentRepository()
        val store = FakeAgentInboxDocumentStore()
        val importer = AgentInboxPackageImporter(userDocumentRepository = repository, documentStore = store)

        val result = importer.importCandidate(
            candidate = readyCandidate(documentSha256 = null),
            contentBytes = ByteArray(AGENT_INBOX_MAX_REVIEW_CONTENT_BYTES.toInt() + 1),
        )

        assertEquals(AgentInboxImportStatus.INVALID, result.status)
        assertEquals(setOf(AgentInboxPackageValidationError.CONTENT_FILE_TOO_LARGE), result.packageErrors)
        assertTrue(store.writes.isEmpty())
        assertTrue(repository.addedDrafts.isEmpty())
    }

    @Test
    fun importCandidateReturnsInvalidForNonImportableCandidate() = runBlocking {
        val importer = AgentInboxPackageImporter(
            userDocumentRepository = FakeAgentInboxUserDocumentRepository(),
            documentStore = FakeAgentInboxDocumentStore(),
        )
        val invalid = AgentInboxReviewCandidate(
            packageFolderId = "package-folder",
            packageFolderName = "Invalid",
            status = AgentInboxReviewStatus.INVALID,
            manifest = null,
            manifestFileId = null,
            contentFileId = null,
            contentFileName = null,
            duplicateContentId = null,
            manifestErrors = setOf(AgentInboxManifestValidationError.BLANK_TITLE),
        )

        val result = importer.importCandidate(candidate = invalid, contentBytes = ByteArray(0))

        assertEquals(AgentInboxImportStatus.INVALID, result.status)
        assertEquals(setOf(AgentInboxManifestValidationError.BLANK_TITLE), result.manifestErrors)
    }

    @Test
    fun importCandidateReturnsRepositoryValidationErrors() = runBlocking {
        val storageRoot = temporaryFolder.newFolder("rejected-agent-inbox")
        val importer = AgentInboxPackageImporter(
            userDocumentRepository = FakeAgentInboxUserDocumentRepository(
                addResult = AddUserDocumentResult.Rejected(setOf(UserDocumentValidationError.NO_TOPICS)),
            ),
            documentStore = FileAgentInboxDocumentStore(storageRoot),
        )

        val result = importer.importCandidate(
            candidate = readyCandidate(documentSha256 = null),
            contentBytes = "body".toByteArray(),
        )

        assertEquals(AgentInboxImportStatus.REJECTED, result.status)
        assertEquals(setOf(UserDocumentValidationError.NO_TOPICS), result.documentErrors)
        assertNull(result.item)
        assertTrue(storageRoot.listFiles().orEmpty().isEmpty())
    }

    @Test
    fun importCandidateDeletesStoredFileWhenAtomicRepositoryAddThrows() = runBlocking {
        val storageRoot = temporaryFolder.newFolder("throwing-agent-inbox")
        val importer = AgentInboxPackageImporter(
            userDocumentRepository = FakeAgentInboxUserDocumentRepository(throwOnAtomicAdd = true),
            documentStore = FileAgentInboxDocumentStore(storageRoot),
        )

        val error = runCatching {
            importer.importCandidate(
                candidate = readyCandidate(documentSha256 = null),
                contentBytes = "body".toByteArray(),
            )
        }.exceptionOrNull()

        assertTrue(error is IllegalStateException)
        assertTrue(storageRoot.listFiles().orEmpty().isEmpty())
    }

    private fun readyCandidate(
        documentSha256: String?,
        priority: String = "normal",
        contentFileName: String = "content.md",
        packageFolderId: String = "package-folder",
        reviewedContentSha256: String? = null,
        reviewedContentSizeBytes: Long? = null,
    ): AgentInboxReviewCandidate {
        val manifest = requireNotNull(
            AgentInboxManifestValidator.validate(
                manifestJson = """
                    {
                      "schemaVersion": 1,
                      "title": "Agent Note",
                      "topics": ["ATTENTION"],
                      "contentFile": "$contentFileName",
                      "format": "MARKDOWN",
                      "rightsClass": "USER_PRIVATE",
                      "description": "Private replacement note.",
                      "priority": "$priority"
                      ${documentSha256?.let { ""","documentSha256":"$it"""" }.orEmpty()}
                    }
                """.trimIndent(),
            ).manifest,
        )
        return AgentInboxReviewCandidate(
            packageFolderId = packageFolderId,
            packageFolderName = "Codex package",
            status = AgentInboxReviewStatus.READY,
            manifest = manifest,
            manifestFileId = "manifest-file",
            contentFileId = "content-file",
            contentFileName = contentFileName,
            duplicateContentId = null,
            reviewedContentSha256 = reviewedContentSha256,
            reviewedContentSizeBytes = reviewedContentSizeBytes,
        )
    }

    private fun existingDocument(id: String, sha: String): ContentItem {
        return ContentItem(
            id = id,
            packId = "user-documents",
            title = "Existing",
            description = "Existing document",
            durationMinutes = 5,
            format = ContentFormat.MARKDOWN,
            topicTags = setOf(TopicTag.ATTENTION),
            sourceType = ContentSourceType.USER_DOCUMENT,
            availability = ContentAvailability.AVAILABLE,
            rights = ContentRightsMetadata.userPrivateReader(sourceUrl = "file:/existing.md"),
            documentFingerprintSha256 = sha,
            documentFingerprintSizeBytes = 9,
        )
    }

    private class FakeAgentInboxDocumentStore : AgentInboxDocumentStore {
        val writes = mutableListOf<Write>()
        val deletedUris = mutableListOf<String>()

        override suspend fun writeDocument(
            packageFolderId: String,
            contentFileName: String,
            verifiedContentSha256: String,
            format: ContentFormat,
            bytes: ByteArray,
            imageAttachments: List<AgentInboxImageAttachmentWrite>,
        ): StoredAgentInboxDocument {
            writes += Write(packageFolderId, contentFileName, verifiedContentSha256, format, bytes, imageAttachments)
            return StoredAgentInboxDocument(
                uri = "file:/agent-inbox/$packageFolderId/$verifiedContentSha256.md",
                displayName = "Agent Inbox document",
                mimeType = when (format) {
                    ContentFormat.MARKDOWN -> "text/markdown"
                    ContentFormat.EPUB -> "application/epub+zip"
                    ContentFormat.PDF -> "application/pdf"
                    ContentFormat.HTML -> "text/html"
                },
                imageAttachmentUris = imageAttachments.associate { attachment ->
                    attachment.fileName to "file:/agent-inbox/$packageFolderId/${attachment.fileName}"
                },
            )
        }

        override suspend fun deleteDocument(stored: StoredAgentInboxDocument) {
            deletedUris += stored.uri
        }
    }

    private data class Write(
        val packageFolderId: String,
        val contentFileName: String,
        val verifiedContentSha256: String,
        val format: ContentFormat,
        val bytes: ByteArray,
        val imageAttachments: List<AgentInboxImageAttachmentWrite>,
    )

    private class UriUpsertingFingerprintRepository : UserDocumentRepository {
        val documents = mutableListOf<ContentItem>()
        val addedDrafts = mutableListOf<UserDocumentDraft>()

        override fun userDocuments(): List<ContentItem> = documents

        override fun observeUserDocuments(): Flow<List<ContentItem>> = flowOf(documents)

        override suspend fun addDocument(
            draft: UserDocumentDraft,
            nowMillis: Long,
        ): AddUserDocumentResult {
            addedDrafts += draft
            val existingIndex = documents.indexOfFirst { item -> item.rights.sourceUrl == draft.uri }
            val fingerprint = File(URI.create(draft.uri)).readBytes().let { bytes ->
                AgentInboxManifestValidator.sha256(bytes) to bytes.size.toLong()
            }
            val existing = existingIndex.takeIf { it >= 0 }?.let(documents::get)
            val item = ContentItem(
                id = existing?.id ?: "agent-doc-${documents.size + 1}",
                packId = "user-documents",
                title = draft.title,
                description = draft.description,
                durationMinutes = draft.durationMinutes,
                format = draft.mimeType.toContentFormat(),
                topicTags = draft.topicTags,
                sourceType = ContentSourceType.USER_DOCUMENT,
                availability = ContentAvailability.AVAILABLE,
                rights = ContentRightsMetadata.userPrivateReader(sourceUrl = draft.uri),
                documentFingerprintSha256 = existing?.documentFingerprintSha256 ?: fingerprint.first,
                documentFingerprintSizeBytes = existing?.documentFingerprintSizeBytes ?: fingerprint.second,
            )
            if (existingIndex >= 0) {
                documents[existingIndex] = item
            } else {
                documents += item
            }
            return AddUserDocumentResult.Added(item)
        }

        override suspend fun markUnavailable(contentId: String, nowMillis: Long) = Unit

        override suspend fun deleteDocument(contentId: String) = Unit

        private fun String?.toContentFormat(): ContentFormat {
            return when (this) {
                "application/epub+zip" -> ContentFormat.EPUB
                "application/pdf" -> ContentFormat.PDF
                "text/html" -> ContentFormat.HTML
                else -> ContentFormat.MARKDOWN
            }
        }
    }

    private class FakeAgentInboxUserDocumentRepository(
        initialDocuments: List<ContentItem> = emptyList(),
        private val addResult: AddUserDocumentResult? = null,
        private val fingerprintLookupDocuments: Map<String, ContentItem> = emptyMap(),
        private val throwOnAtomicAdd: Boolean = false,
    ) : UserDocumentRepository {
        private val documents = initialDocuments.toMutableList()
        private val addMutex = Mutex()
        val addedDrafts = mutableListOf<UserDocumentDraft>()

        override fun userDocuments(): List<ContentItem> = documents

        override fun observeUserDocuments(): Flow<List<ContentItem>> = flowOf(documents)

        override suspend fun findDocumentByFingerprintSha256(sha256: String): ContentItem? {
            return fingerprintLookupDocuments[sha256]
                ?: documents.firstOrNull { item -> item.documentFingerprintSha256 == sha256 }
        }

        override suspend fun addDocument(
            draft: UserDocumentDraft,
            nowMillis: Long,
        ): AddUserDocumentResult {
            addedDrafts += draft
            addResult?.let { return it }
            val item = ContentItem(
                id = "agent-doc-${addedDrafts.size}",
                packId = "user-documents",
                title = draft.title,
                description = draft.description,
                durationMinutes = draft.durationMinutes,
                format = ContentFormat.MARKDOWN,
                topicTags = draft.topicTags,
                sourceType = ContentSourceType.USER_DOCUMENT,
                availability = ContentAvailability.AVAILABLE,
                rights = ContentRightsMetadata.userPrivateReader(sourceUrl = draft.uri),
                documentFingerprintSha256 = draft.documentFingerprintSha256,
                documentFingerprintSizeBytes = draft.documentFingerprintSizeBytes,
            )
            documents += item
            return AddUserDocumentResult.Added(item)
        }

        override suspend fun addDocumentIfFingerprintAbsent(
            draft: UserDocumentDraft,
            fingerprintSha256: String,
            nowMillis: Long,
        ): AddUserDocumentIfFingerprintAbsentResult = addMutex.withLock {
            if (throwOnAtomicAdd) {
                error("Simulated atomic add failure")
            }
            findDocumentByFingerprintSha256(fingerprintSha256)?.let { existing ->
                return@withLock AddUserDocumentIfFingerprintAbsentResult.Duplicate(existing)
            }
            when (
                val result = addDocument(
                    draft = draft.copy(documentFingerprintSha256 = fingerprintSha256),
                    nowMillis = nowMillis,
                )
            ) {
                is AddUserDocumentResult.Added -> AddUserDocumentIfFingerprintAbsentResult.Added(result.item)
                is AddUserDocumentResult.Rejected -> AddUserDocumentIfFingerprintAbsentResult.Rejected(result.errors)
            }
        }

        override suspend fun markUnavailable(contentId: String, nowMillis: Long) = Unit

        override suspend fun deleteDocument(contentId: String) = Unit
    }
}
