package com.qualityalternative.app.domain.service

const val ANNOTATION_DRIVE_SCOPE = "https://www.googleapis.com/auth/drive.file"
const val AGENT_INBOX_DRIVE_READONLY_SCOPE = "https://www.googleapis.com/auth/drive.readonly"
const val ANNOTATION_DRIVE_FOLDER_NAME = "Quality Alternative annotations"
const val ANNOTATION_DRIVE_INDEX_FILE_NAME = "quality-alternative-annotations.index.json"

data class ReadingAnnotationDriveSyncRequest(
    val accessToken: String,
    val folderId: String?,
    val files: List<ReadingAnnotationExportFile>,
    val indexJson: String,
)

data class ReadingAnnotationDriveSyncResult(
    val folderId: String,
    val syncedFileNames: List<String>,
)

interface ReadingAnnotationDriveSyncClient {
    suspend fun syncJsonLdFiles(request: ReadingAnnotationDriveSyncRequest): ReadingAnnotationDriveSyncResult
}

interface ReadingAnnotationDriveTokenProvider {
    suspend fun driveAccessToken(): String
}
