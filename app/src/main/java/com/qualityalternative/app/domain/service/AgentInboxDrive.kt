package com.qualityalternative.app.domain.service

const val AGENT_INBOX_DRIVE_FOLDER_NAME = "Quality Alternative Agent Inbox"
const val AGENT_INBOX_MANIFEST_FILE_NAME = "manifest.json"
const val AGENT_INBOX_MAX_PACKAGES_PER_SCAN = 10
const val AGENT_INBOX_MAX_FILES_PER_PACKAGE = 8
const val AGENT_INBOX_MAX_MANIFEST_BYTES = 64L * 1024L
const val AGENT_INBOX_MAX_REVIEW_CONTENT_BYTES = 10L * 1024L * 1024L

data class AgentInboxDriveScanRequest(
    val accessToken: String,
    val folderId: String?,
)

data class AgentInboxDriveScanResult(
    val folderId: String,
    val packages: List<AgentInboxDrivePackage>,
    val hasMorePackages: Boolean = false,
)

data class AgentInboxDrivePackage(
    val folderId: String,
    val folderName: String,
    val manifestFile: AgentInboxDriveFile?,
    val contentFiles: List<AgentInboxDriveFile>,
    val allFiles: List<AgentInboxDriveFile>,
    val hasMoreFiles: Boolean = false,
)

data class AgentInboxDriveFile(
    val id: String,
    val name: String,
    val mimeType: String?,
    val sizeBytes: Long?,
    val md5Checksum: String?,
    val modifiedTime: String?,
)

class AgentInboxDriveDownloadTooLargeException(
    val maxBytes: Long,
) : java.io.IOException("Agent Inbox Drive download exceeded $maxBytes bytes.")

class AgentInboxDriveFolderNotSelectedException :
    java.io.IOException("Select an Agent Inbox folder before scanning.")

class AgentInboxDriveHttpException(
    val statusCode: Int,
    val errorBody: String,
) : java.io.IOException("Drive request failed with HTTP $statusCode${errorBody.toErrorSuffix()}")

interface AgentInboxDriveClient {
    suspend fun scanPackages(request: AgentInboxDriveScanRequest): AgentInboxDriveScanResult

    suspend fun downloadFile(accessToken: String, fileId: String, maxBytes: Long): ByteArray
}

private fun String.toErrorSuffix(): String {
    val trimmed = trim()
    return if (trimmed.isBlank()) "" else ": $trimmed"
}
