package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.ContentFormat
import java.io.File
import java.net.URI
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class StoredAgentInboxDocument(
    val uri: String,
    val displayName: String,
    val mimeType: String,
    val imageAttachmentUris: Map<String, String> = emptyMap(),
)

data class AgentInboxImageAttachmentWrite(
    val fileName: String,
    val bytes: ByteArray,
)

interface AgentInboxDocumentStore {
    suspend fun writeDocument(
        packageFolderId: String,
        contentFileName: String,
        verifiedContentSha256: String,
        format: ContentFormat,
        bytes: ByteArray,
        imageAttachments: List<AgentInboxImageAttachmentWrite> = emptyList(),
    ): StoredAgentInboxDocument

    suspend fun deleteDocument(stored: StoredAgentInboxDocument)
}

class FileAgentInboxDocumentStore(
    private val rootDirectory: File,
) : AgentInboxDocumentStore {
    override suspend fun writeDocument(
        packageFolderId: String,
        contentFileName: String,
        verifiedContentSha256: String,
        format: ContentFormat,
        bytes: ByteArray,
        imageAttachments: List<AgentInboxImageAttachmentWrite>,
    ): StoredAgentInboxDocument = withContext(Dispatchers.IO) {
        rootDirectory.mkdirs()
        val safeName = buildString {
            append(packageFolderId.safeFileSegment())
            append('-')
            append(verifiedContentSha256.safeFileSegment())
            append('.')
            append(format.agentInboxFileExtension(contentFileName))
        }
        val file = File(rootDirectory, safeName)
        if (file.exists()) {
            val existingSha256 = runCatching { AgentInboxManifestValidator.sha256(file.readBytes()) }.getOrNull()
            if (existingSha256 == verifiedContentSha256) {
                val existingAttachments = imageAttachments.writeImageAttachments(
                    contentSafeName = safeName,
                    replaceExisting = true,
                )
                return@withContext file.toStoredAgentInboxDocument(format, existingAttachments)
            }
            check(file.delete()) { "Could not delete stale Agent Inbox document." }
        }

        var attachmentUris: Map<String, String> = emptyMap()
        val tempFile = File.createTempFile("$safeName-", ".tmp", rootDirectory)
        try {
            tempFile.writeBytes(bytes)
            val tempSha256 = AgentInboxManifestValidator.sha256(tempFile.readBytes())
            check(tempSha256 == verifiedContentSha256) {
                "Temporary Agent Inbox document bytes do not match verified SHA-256."
            }
            moveIntoPlace(tempFile = tempFile, targetFile = file)
            attachmentUris = imageAttachments.writeImageAttachments(
                contentSafeName = safeName,
                replaceExisting = true,
            )
        } catch (error: Throwable) {
            if (file.exists()) {
                file.delete()
            }
            attachmentUris.values.forEach { uri ->
                runCatching { File(URI(uri)).delete() }
            }
            throw error
        } finally {
            if (tempFile.exists()) {
                tempFile.delete()
            }
        }
        file.toStoredAgentInboxDocument(format, attachmentUris)
    }

    private fun List<AgentInboxImageAttachmentWrite>.writeImageAttachments(
        contentSafeName: String,
        replaceExisting: Boolean,
    ): Map<String, String> {
        return flatMap { attachment ->
            val safeAttachmentName = attachment.fileName.safeFileSegment()
            val attachmentFile = File(rootDirectory, "$contentSafeName-img-$safeAttachmentName")
            if (replaceExisting || !attachmentFile.exists()) {
                attachmentFile.writeBytes(attachment.bytes)
            }
            val uri = attachmentFile.toURI().toString()
            val displayName = attachment.fileName.trim()
            listOfNotNull(
                displayName.takeIf(String::isNotBlank)?.let { key -> key to uri },
                displayName.lowercase().takeIf { it.isNotBlank() && it != displayName }?.let { key -> key to uri },
            )
        }.distinctBy { (key, _) -> key }.toMap()
    }

    private fun moveIntoPlace(tempFile: File, targetFile: File) {
        try {
            Files.move(
                tempFile.toPath(),
                targetFile.toPath(),
                StandardCopyOption.ATOMIC_MOVE,
            )
        } catch (atomicMoveNotSupported: AtomicMoveNotSupportedException) {
            Files.move(
                tempFile.toPath(),
                targetFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
            )
        }
    }

    private fun File.toStoredAgentInboxDocument(
        format: ContentFormat,
        imageAttachmentUris: Map<String, String>,
    ): StoredAgentInboxDocument {
        return StoredAgentInboxDocument(
            uri = toURI().toString(),
            displayName = "Agent Inbox document",
            mimeType = format.agentInboxMimeType(),
            imageAttachmentUris = imageAttachmentUris,
        )
    }

    override suspend fun deleteDocument(stored: StoredAgentInboxDocument) {
        withContext(Dispatchers.IO) {
            val rootPath = rootDirectory.canonicalFile.toPath()
            val file = runCatching { File(URI(stored.uri)).canonicalFile }.getOrNull() ?: return@withContext
            if (!file.toPath().startsWith(rootPath)) return@withContext
            if (file.exists()) {
                check(file.delete()) { "Could not delete uncommitted Agent Inbox document." }
            }
            stored.imageAttachmentUris.values.toSet().forEach { attachmentUri ->
                val attachmentFile = runCatching { File(URI(attachmentUri)).canonicalFile }.getOrNull()
                    ?: return@forEach
                if (!attachmentFile.toPath().startsWith(rootPath)) return@forEach
                if (attachmentFile.exists()) {
                    check(attachmentFile.delete()) { "Could not delete uncommitted Agent Inbox image attachment." }
                }
            }
        }
    }
}

private fun String.safeFileSegment(): String {
    return trim()
        .ifBlank { "document" }
        .replace(Regex("""[^A-Za-z0-9._-]+"""), "-")
        .trim('-', '.')
        .ifBlank { "document" }
}

private fun ContentFormat.agentInboxMimeType(): String {
    return when (this) {
        ContentFormat.MARKDOWN -> "text/markdown"
        ContentFormat.EPUB -> "application/epub+zip"
        ContentFormat.PDF -> "application/pdf"
        ContentFormat.HTML -> "text/html"
    }
}

private fun ContentFormat.agentInboxFileExtension(contentFileName: String): String {
    val sourceExtension = contentFileName
        .substringAfterLast('.', missingDelimiterValue = "")
        .safeFileSegment()
        .takeIf { it != "document" }
    return when (this) {
        ContentFormat.MARKDOWN -> when (sourceExtension) {
            "md",
            "markdown",
            -> sourceExtension
            else -> "md"
        }
        ContentFormat.EPUB -> "epub"
        ContentFormat.PDF -> "pdf"
        ContentFormat.HTML -> when (sourceExtension) {
            "html",
            "htm",
            -> sourceExtension
            else -> "html"
        }
    }
}
