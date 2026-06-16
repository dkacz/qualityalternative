package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.ContentFormat
import java.io.File
import java.io.IOException
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

class AgentInboxImageAttachmentWriteException(
    cause: Throwable,
) : IOException("Agent Inbox image attachment could not be written.", cause)

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
    private val tempFileFactory: (prefix: String, suffix: String, directory: File) -> File = File::createTempFile,
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
            append(packageFolderId.safeAgentInboxFileSegment())
            append('-')
            append(verifiedContentSha256.safeAgentInboxFileSegment())
            append('.')
            append(format.agentInboxFileExtension(contentFileName))
        }
        val file = File(rootDirectory, safeName)
        if (file.exists()) {
            val existingSha256 = runCatching { AgentInboxManifestValidator.sha256(file.readBytes()) }.getOrNull()
            if (existingSha256 == verifiedContentSha256) {
                val existingAttachments = imageAttachments.writeImageAttachmentsAtomically(contentSafeName = safeName)
                return@withContext file.toStoredAgentInboxDocument(format, existingAttachments)
            }
            check(file.delete()) { "Could not delete stale Agent Inbox document." }
        }

        var attachmentUris: Map<String, String> = emptyMap()
        val tempFile = tempFileFactory("$safeName-", ".tmp", rootDirectory)
        try {
            tempFile.writeBytes(bytes)
            val tempSha256 = AgentInboxManifestValidator.sha256(bytes)
            check(tempSha256 == verifiedContentSha256) {
                "Temporary Agent Inbox document bytes do not match verified SHA-256."
            }
            moveIntoPlace(tempFile = tempFile, targetFile = file)
            attachmentUris = imageAttachments.writeImageAttachmentsAtomically(contentSafeName = safeName)
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

    private fun List<AgentInboxImageAttachmentWrite>.writeImageAttachmentsAtomically(
        contentSafeName: String,
    ): Map<String, String> {
        if (isEmpty()) return emptyMap()
        val plans = mutableListOf<AttachmentWritePlan>()
        val backups = mutableListOf<Pair<File, File>>()
        val promotedTargets = mutableListOf<File>()
        try {
            forEach { attachment ->
                val safeAttachmentName = attachment.fileName.safeAgentInboxFileSegment()
                plans += AttachmentWritePlan(
                    displayName = attachment.fileName.trim(),
                    targetFile = File(rootDirectory, "$contentSafeName-img-$safeAttachmentName"),
                    tempFile = tempFileFactory("$contentSafeName-img-$safeAttachmentName-", ".tmp", rootDirectory),
                    bytes = attachment.bytes,
                )
            }
            check(plans.map { plan -> plan.targetFile.name }.distinct().size == plans.size) {
                "Duplicate Agent Inbox image attachment storage segment."
            }
            plans.forEach { plan -> plan.tempFile.writeBytes(plan.bytes) }
            plans.forEach { plan ->
                if (plan.targetFile.exists()) {
                    check(plan.targetFile.isFile) { "Agent Inbox image attachment target is not a file." }
                    val backup = tempFileFactory("${plan.targetFile.name}-", ".bak", rootDirectory)
                    moveIntoPlace(tempFile = plan.targetFile, targetFile = backup)
                    backups += plan.targetFile to backup
                }
                moveIntoPlace(tempFile = plan.tempFile, targetFile = plan.targetFile)
                promotedTargets += plan.targetFile
            }
            backups.forEach { (_, backup) -> if (backup.exists()) backup.delete() }
        } catch (error: Throwable) {
            plans.forEach { plan -> if (plan.tempFile.exists()) plan.tempFile.delete() }
            promotedTargets.forEach { target -> if (target.exists()) target.delete() }
            backups.asReversed().forEach { (target, backup) ->
                if (backup.exists()) {
                    runCatching { moveIntoPlace(tempFile = backup, targetFile = target) }
                }
            }
            if (error is AgentInboxImageAttachmentWriteException) {
                throw error
            }
            if (error is IOException || error is OutOfMemoryError) {
                throw AgentInboxImageAttachmentWriteException(error)
            }
            throw error
        } finally {
            plans.forEach { plan -> if (plan.tempFile.exists()) plan.tempFile.delete() }
        }
        return plans.flatMap { plan ->
            val uri = plan.targetFile.toURI().toString()
            listOfNotNull(
                plan.displayName.takeIf(String::isNotBlank)?.let { key -> key to uri },
                plan.displayName.lowercase().takeIf { it.isNotBlank() && it != plan.displayName }
                    ?.let { key -> key to uri },
            )
        }.toMap()
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

private data class AttachmentWritePlan(
    val displayName: String,
    val targetFile: File,
    val tempFile: File,
    val bytes: ByteArray,
)

internal fun String.safeAgentInboxFileSegment(): String {
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
        .safeAgentInboxFileSegment()
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
