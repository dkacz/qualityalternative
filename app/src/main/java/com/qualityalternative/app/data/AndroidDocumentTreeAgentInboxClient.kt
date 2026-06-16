package com.qualityalternative.app.data

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import com.qualityalternative.app.domain.service.AGENT_INBOX_MANIFEST_FILE_NAME
import com.qualityalternative.app.domain.service.AGENT_INBOX_MAX_FILES_PER_PACKAGE
import com.qualityalternative.app.domain.service.AGENT_INBOX_MAX_PACKAGES_PER_SCAN
import com.qualityalternative.app.domain.service.AgentInboxDriveAccessLostException
import com.qualityalternative.app.domain.service.AgentInboxDriveClient
import com.qualityalternative.app.domain.service.AgentInboxDriveDownloadTooLargeException
import com.qualityalternative.app.domain.service.AgentInboxDriveFile
import com.qualityalternative.app.domain.service.AgentInboxDriveFolderNotSelectedException
import com.qualityalternative.app.domain.service.AgentInboxDrivePackage
import com.qualityalternative.app.domain.service.AgentInboxDriveScanRequest
import com.qualityalternative.app.domain.service.AgentInboxDriveScanResult
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidHybridAgentInboxDriveClient(
    context: Context,
    private val googleDriveClient: AgentInboxDriveClient = AndroidGoogleDriveAgentInboxClient(),
    private val documentTreeClient: AgentInboxDriveClient = AndroidDocumentTreeAgentInboxClient(context),
) : AgentInboxDriveClient {
    override suspend fun scanPackages(request: AgentInboxDriveScanRequest): AgentInboxDriveScanResult {
        return if (request.folderId.isDocumentTreeUri()) {
            documentTreeClient.scanPackages(request)
        } else {
            googleDriveClient.scanPackages(request)
        }
    }

    override suspend fun downloadFile(
        accessToken: String,
        fileId: String,
        maxBytes: Long,
        expectedBytes: Long?,
    ): ByteArray {
        return if (fileId.isContentUri()) {
            documentTreeClient.downloadFile(
                accessToken = accessToken,
                fileId = fileId,
                maxBytes = maxBytes,
                expectedBytes = expectedBytes,
            )
        } else {
            googleDriveClient.downloadFile(
                accessToken = accessToken,
                fileId = fileId,
                maxBytes = maxBytes,
                expectedBytes = expectedBytes,
            )
        }
    }
}

class AndroidDocumentTreeAgentInboxClient(
    private val context: Context,
) : AgentInboxDriveClient {
    override suspend fun scanPackages(
        request: AgentInboxDriveScanRequest,
    ): AgentInboxDriveScanResult = withContext(Dispatchers.IO) {
        val treeUri = request.folderId?.takeIf(String::isNotBlank)?.let(Uri::parse)
            ?: throw AgentInboxDriveFolderNotSelectedException()
        val rootDocumentId = treeUri.safeTreeDocumentId()
        val packageFolders = listChildren(
            treeUri = treeUri,
            parentDocumentId = rootDocumentId,
            limit = AGENT_INBOX_MAX_PACKAGES_PER_SCAN,
            maxRows = MAX_DOCUMENT_TREE_ROOT_ROWS,
            include = TreeDocument::isDirectory,
        )
        val packages = packageFolders.documents.map { folder ->
            val files = listChildren(
                treeUri = treeUri,
                parentDocumentId = folder.documentId,
                limit = AGENT_INBOX_MAX_FILES_PER_PACKAGE,
                maxRows = MAX_DOCUMENT_TREE_PACKAGE_ROWS,
                include = { true },
            )
            AgentInboxDrivePackage(
                folderId = folder.uri.toString(),
                folderName = folder.name,
                manifestFile = files.documents.firstOrNull { file -> file.name == AGENT_INBOX_MANIFEST_FILE_NAME }
                    ?.toAgentInboxDriveFile(),
                contentFiles = files.documents
                    .map(TreeDocument::toAgentInboxDriveFile)
                    .filter(AgentInboxDriveFile::isAgentContentFile),
                allFiles = files.documents.map(TreeDocument::toAgentInboxDriveFile),
                hasMoreFiles = files.hasMore,
            )
        }
        AgentInboxDriveScanResult(
            folderId = treeUri.toString(),
            packages = packages,
            hasMorePackages = packageFolders.hasMore,
        )
    }

    override suspend fun downloadFile(
        accessToken: String,
        fileId: String,
        maxBytes: Long,
        expectedBytes: Long?,
    ): ByteArray = withContext(Dispatchers.IO) {
        val fileUri = Uri.parse(fileId)
        try {
            context.contentResolver.openInputStream(fileUri)?.use { input ->
                input.readBoundedBytes(maxBytes = maxBytes, expectedBytes = expectedBytes)
            } ?: throw AgentInboxDriveAccessLostException()
        } catch (error: SecurityException) {
            throw AgentInboxDriveAccessLostException(cause = error)
        } catch (error: FileNotFoundException) {
            throw AgentInboxDriveAccessLostException(cause = error)
        } catch (error: IllegalArgumentException) {
            throw AgentInboxDriveAccessLostException(cause = error)
        }
    }

    private fun listChildren(
        treeUri: Uri,
        parentDocumentId: String,
        limit: Int,
        maxRows: Int,
        include: (TreeDocument) -> Boolean,
    ): ListedTreeDocuments {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocumentId)
        val documents = mutableListOf<TreeDocument>()
        var hasMore = false
        queryChildren(childrenUri).use { cursor ->
            var inspectedRows = 0
            while (cursor.moveToNext()) {
                inspectedRows += 1
                if (inspectedRows > maxRows) {
                    hasMore = true
                    break
                }
                val document = cursor.toTreeDocument(treeUri) ?: continue
                if (!include(document)) continue
                documents += document
                if (documents.size > limit) {
                    hasMore = true
                    break
                }
            }
        }
        return ListedTreeDocuments(
            documents = documents.take(limit),
            hasMore = hasMore,
        )
    }

    private fun queryChildren(childrenUri: Uri): Cursor {
        return try {
            context.contentResolver.query(
                childrenUri,
                DOCUMENT_TREE_COLUMNS,
                null,
                null,
                null,
            ) ?: throw AgentInboxDriveAccessLostException()
        } catch (error: SecurityException) {
            throw AgentInboxDriveAccessLostException(cause = error)
        } catch (error: IllegalArgumentException) {
            throw AgentInboxDriveAccessLostException(cause = error)
        }
    }

    private fun Cursor.toTreeDocument(treeUri: Uri): TreeDocument? {
        val documentId = stringColumn(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            ?.takeIf(String::isNotBlank)
            ?: return null
        val name = stringColumn(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            ?.takeIf(String::isNotBlank)
            ?: return null
        return TreeDocument(
            documentId = documentId,
            name = name,
            mimeType = stringColumn(DocumentsContract.Document.COLUMN_MIME_TYPE),
            sizeBytes = longColumn(DocumentsContract.Document.COLUMN_SIZE),
            lastModifiedMillis = longColumn(DocumentsContract.Document.COLUMN_LAST_MODIFIED),
            uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId),
        )
    }

    private fun Uri.safeTreeDocumentId(): String {
        return try {
            DocumentsContract.getTreeDocumentId(this)?.takeIf(String::isNotBlank)
                ?: throw AgentInboxDriveAccessLostException()
        } catch (error: IllegalArgumentException) {
            throw AgentInboxDriveAccessLostException(cause = error)
        }
    }

    private data class ListedTreeDocuments(
        val documents: List<TreeDocument>,
        val hasMore: Boolean,
    )

    private data class TreeDocument(
        val documentId: String,
        val name: String,
        val mimeType: String?,
        val sizeBytes: Long?,
        val lastModifiedMillis: Long?,
        val uri: Uri,
    ) {
        val isDirectory: Boolean
            get() = mimeType == DocumentsContract.Document.MIME_TYPE_DIR

        fun toAgentInboxDriveFile(): AgentInboxDriveFile {
            return AgentInboxDriveFile(
                id = uri.toString(),
                name = name,
                mimeType = mimeType,
                sizeBytes = sizeBytes,
                md5Checksum = null,
                modifiedTime = lastModifiedMillis?.toString(),
            )
        }
    }

    private companion object {
        val DOCUMENT_TREE_COLUMNS = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
        )
        const val MAX_DOCUMENT_TREE_ROOT_ROWS = 50
        const val MAX_DOCUMENT_TREE_PACKAGE_ROWS = 50
    }
}

private fun String?.isDocumentTreeUri(): Boolean {
    val uri = this?.takeIf(String::isNotBlank)?.let(Uri::parse) ?: return false
    return uri.scheme == "content" && uri.path.orEmpty().contains("/tree/")
}

private fun String.isContentUri(): Boolean = Uri.parse(this).scheme == "content"

private fun Cursor.stringColumn(columnName: String): String? {
    val index = getColumnIndex(columnName)
    return if (index >= 0 && !isNull(index)) getString(index) else null
}

private fun Cursor.longColumn(columnName: String): Long? {
    val index = getColumnIndex(columnName)
    return if (index >= 0 && !isNull(index)) getLong(index) else null
}

private fun AgentInboxDriveFile.isAgentContentFile(): Boolean {
    val lower = name.lowercase()
    return lower.endsWith(".md") || lower.endsWith(".markdown") || lower.endsWith(".epub")
}

private fun InputStream.readBoundedBytes(maxBytes: Long, expectedBytes: Long? = null): ByteArray {
    val boundedExpectedBytes = expectedBytes
        ?.takeIf { bytes -> bytes >= 0L && bytes <= maxBytes && bytes <= Int.MAX_VALUE }
        ?.toInt()
    if (boundedExpectedBytes != null) {
        val bytes = ByteArray(boundedExpectedBytes)
        var offset = 0
        while (offset < bytes.size) {
            val read = read(bytes, offset, bytes.size - offset)
            if (read == -1) {
                return bytes.copyOf(offset)
            }
            offset += read
        }
        val extra = read()
        if (extra == -1) {
            return bytes
        }
        val output = ByteArrayOutputStream(
            (boundedExpectedBytes.toLong() + DEFAULT_BUFFER_SIZE)
                .coerceAtMost(maxBytes)
                .coerceAtMost(Int.MAX_VALUE.toLong())
                .toInt(),
        )
        output.write(bytes)
        output.write(extra)
        return readRemainingBoundedBytes(
            output = output,
            initialTotal = boundedExpectedBytes + 1L,
            maxBytes = maxBytes,
        )
    }
    val output = ByteArrayOutputStream(DEFAULT_BUFFER_SIZE)
    return readRemainingBoundedBytes(output = output, initialTotal = 0L, maxBytes = maxBytes)
}

private fun InputStream.readRemainingBoundedBytes(
    output: ByteArrayOutputStream,
    initialTotal: Long,
    maxBytes: Long,
): ByteArray {
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var total = initialTotal
    if (total > maxBytes) {
        throw AgentInboxDriveDownloadTooLargeException(maxBytes = maxBytes)
    }
    while (true) {
        val read = read(buffer)
        if (read == -1) break
        total += read.toLong()
        if (total > maxBytes) {
            throw AgentInboxDriveDownloadTooLargeException(maxBytes = maxBytes)
        }
        output.write(buffer, 0, read)
    }
    return output.toByteArray()
}
