package com.qualityalternative.app.data

import com.qualityalternative.app.domain.service.ANNOTATION_DRIVE_FOLDER_NAME
import com.qualityalternative.app.domain.service.ANNOTATION_DRIVE_INDEX_FILE_NAME
import com.qualityalternative.app.domain.service.ReadingAnnotationDriveSyncClient
import com.qualityalternative.app.domain.service.ReadingAnnotationDriveSyncRequest
import com.qualityalternative.app.domain.service.ReadingAnnotationDriveSyncResult
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class AndroidGoogleDriveAnnotationSyncClient : ReadingAnnotationDriveSyncClient {
    override suspend fun syncJsonLdFiles(
        request: ReadingAnnotationDriveSyncRequest,
    ): ReadingAnnotationDriveSyncResult = withContext(Dispatchers.IO) {
        val folderId = request.folderId?.takeIf(String::isNotBlank)
            ?: findFolder(accessToken = request.accessToken)
            ?: createFolder(accessToken = request.accessToken)
        val syncedNames = mutableListOf<String>()
        upsertTextFile(
            accessToken = request.accessToken,
            folderId = folderId,
            fileName = ANNOTATION_DRIVE_INDEX_FILE_NAME,
            mimeType = "application/json",
            body = request.indexJson,
        )
        syncedNames += ANNOTATION_DRIVE_INDEX_FILE_NAME
        request.files.forEach { file ->
            upsertTextFile(
                accessToken = request.accessToken,
                folderId = folderId,
                fileName = file.fileName,
                mimeType = "application/ld+json",
                body = file.jsonLd,
            )
            syncedNames += file.fileName
        }
        ReadingAnnotationDriveSyncResult(folderId = folderId, syncedFileNames = syncedNames)
    }

    private fun findFolder(accessToken: String): String? {
        val query = listOf(
            "name = '${ANNOTATION_DRIVE_FOLDER_NAME.driveQueryValue()}'",
            "mimeType = 'application/vnd.google-apps.folder'",
            "trashed = false",
        ).joinToString(" and ")
        val response = requestJson(
            method = "GET",
            url = driveApiUrl(
                "https://www.googleapis.com/drive/v3/files",
                mapOf(
                    "q" to query,
                    "spaces" to "drive",
                    "fields" to "files(id,name)",
                    "pageSize" to "1",
                ),
            ),
            accessToken = accessToken,
        )
        return response.optJSONArray("files")?.firstObjectId()
    }

    private fun createFolder(accessToken: String): String {
        val metadata = JSONObject()
            .put("name", ANNOTATION_DRIVE_FOLDER_NAME)
            .put("mimeType", "application/vnd.google-apps.folder")
        val response = requestJson(
            method = "POST",
            url = driveApiUrl(
                "https://www.googleapis.com/drive/v3/files",
                mapOf("fields" to "id,name"),
            ),
            accessToken = accessToken,
            body = metadata.toString(),
            contentType = "application/json; charset=UTF-8",
        )
        return response.optString("id").takeIf(String::isNotBlank)
            ?: throw IOException("Drive did not return a folder id.")
    }

    private fun upsertTextFile(
        accessToken: String,
        folderId: String,
        fileName: String,
        mimeType: String,
        body: String,
    ) {
        val existingFileId = findFile(accessToken = accessToken, folderId = folderId, fileName = fileName)
        val metadata = JSONObject()
            .put("name", fileName)
            .put("mimeType", mimeType)
        if (existingFileId == null) {
            metadata.put("parents", JSONArray().put(folderId))
        }
        val method = if (existingFileId == null) "POST" else "PATCH"
        val endpoint = if (existingFileId == null) {
            "https://www.googleapis.com/upload/drive/v3/files"
        } else {
            "https://www.googleapis.com/upload/drive/v3/files/$existingFileId"
        }
        requestJson(
            method = method,
            url = driveApiUrl(
                endpoint,
                mapOf(
                    "uploadType" to "multipart",
                    "fields" to "id,name",
                ),
            ),
            accessToken = accessToken,
            body = multipartBody(metadataJson = metadata.toString(), mimeType = mimeType, body = body),
            contentType = "multipart/related; boundary=$MULTIPART_BOUNDARY",
        )
    }

    private fun findFile(accessToken: String, folderId: String, fileName: String): String? {
        val query = listOf(
            "name = '${fileName.driveQueryValue()}'",
            "'${folderId.driveQueryValue()}' in parents",
            "trashed = false",
        ).joinToString(" and ")
        val response = requestJson(
            method = "GET",
            url = driveApiUrl(
                "https://www.googleapis.com/drive/v3/files",
                mapOf(
                    "q" to query,
                    "spaces" to "drive",
                    "fields" to "files(id,name)",
                    "pageSize" to "1",
                ),
            ),
            accessToken = accessToken,
        )
        return response.optJSONArray("files")?.firstObjectId()
    }

    private fun requestJson(
        method: String,
        url: String,
        accessToken: String,
        body: String? = null,
        contentType: String? = null,
    ): JSONObject {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            setRequestProperty("Authorization", "Bearer $accessToken")
            setRequestProperty("Accept", "application/json")
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", contentType ?: "application/json; charset=UTF-8")
                outputStream.use { stream -> stream.write(body.toByteArray(Charsets.UTF_8)) }
            }
        }
        val code = connection.responseCode
        val response = if (code in 200..299) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            throw IOException("Drive request failed with HTTP $code${errorBody.toErrorSuffix()}")
        }
        return response.takeIf(String::isNotBlank)?.let(::JSONObject) ?: JSONObject()
    }

    private fun driveApiUrl(base: String, parameters: Map<String, String>): String {
        if (parameters.isEmpty()) return base
        val query = parameters.entries.joinToString("&") { (key, value) ->
            "${key.urlEncode()}=${value.urlEncode()}"
        }
        return "$base?$query"
    }

    private fun multipartBody(metadataJson: String, mimeType: String, body: String): String {
        return buildString {
            append("--$MULTIPART_BOUNDARY\r\n")
            append("Content-Type: application/json; charset=UTF-8\r\n\r\n")
            append(metadataJson)
            append("\r\n--$MULTIPART_BOUNDARY\r\n")
            append("Content-Type: $mimeType; charset=UTF-8\r\n\r\n")
            append(body)
            append("\r\n--$MULTIPART_BOUNDARY--\r\n")
        }
    }

    private companion object {
        const val MULTIPART_BOUNDARY = "quality-alternative-drive-boundary"
    }
}

private fun JSONArray.firstObjectId(): String? {
    if (length() == 0) return null
    return optJSONObject(0)?.optString("id")?.takeIf(String::isNotBlank)
}

private fun String.driveQueryValue(): String {
    return replace("\\", "\\\\").replace("'", "\\'")
}

private fun String.urlEncode(): String = URLEncoder.encode(this, "UTF-8")

private fun String.toErrorSuffix(): String {
    return takeIf(String::isNotBlank)?.let { ": $it" }.orEmpty()
}
