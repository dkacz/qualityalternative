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

class AndroidGoogleDriveAnnotationSyncClient(
    private val endpoints: GoogleDriveAnnotationSyncEndpoints = GoogleDriveAnnotationSyncEndpoints(),
) : ReadingAnnotationDriveSyncClient {
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
                endpoints.filesUrl,
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
                endpoints.filesUrl,
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
        if (existingFileId == null) {
            uploadFile(
                accessToken = accessToken,
                fileId = null,
                folderId = folderId,
                fileName = fileName,
                mimeType = mimeType,
                body = body,
                ifMatch = null,
                throwOnError = true,
            )
            return
        }
        // The file already exists on Drive, so another device may have written to it. Read the remote
        // copy, merge it with the local export (so the other device's annotations survive), and write
        // the merged result back. If-Match guards against a write landing between our read and write;
        // on 412 we re-read, re-merge and write once more so progress is still made.
        val remote = downloadFileText(accessToken = accessToken, fileId = existingFileId)
        val mergedBody = mergeBody(mimeType = mimeType, localBody = body, remoteBody = remote?.body)
        val firstAttempt = uploadFile(
            accessToken = accessToken,
            fileId = existingFileId,
            folderId = folderId,
            fileName = fileName,
            mimeType = mimeType,
            body = mergedBody,
            ifMatch = remote?.etag,
            throwOnError = false,
        )
        if (firstAttempt.code == HTTP_PRECONDITION_FAILED) {
            val freshRemote = downloadFileText(accessToken = accessToken, fileId = existingFileId)
            val reMergedBody = mergeBody(mimeType = mimeType, localBody = body, remoteBody = freshRemote?.body)
            uploadFile(
                accessToken = accessToken,
                fileId = existingFileId,
                folderId = folderId,
                fileName = fileName,
                mimeType = mimeType,
                body = reMergedBody,
                ifMatch = null,
                throwOnError = true,
            )
        } else if (firstAttempt.code !in 200..299) {
            throw IOException("Drive request failed with HTTP ${firstAttempt.code}${firstAttempt.body.toErrorSuffix()}")
        }
    }

    private fun mergeBody(mimeType: String, localBody: String, remoteBody: String?): String {
        if (remoteBody.isNullOrBlank()) {
            return localBody
        }
        return if (mimeType == "application/json") {
            DriveAnnotationSyncMerger.mergeIndexJson(localJson = localBody, remoteJson = remoteBody)
        } else {
            DriveAnnotationSyncMerger.mergeAnnotationCollectionJson(localJson = localBody, remoteJson = remoteBody)
        }
    }

    private fun uploadFile(
        accessToken: String,
        fileId: String?,
        folderId: String,
        fileName: String,
        mimeType: String,
        body: String,
        ifMatch: String?,
        throwOnError: Boolean,
    ): DriveResponse {
        val metadata = JSONObject()
            .put("name", fileName)
            .put("mimeType", mimeType)
        if (fileId == null) {
            metadata.put("parents", JSONArray().put(folderId))
        }
        val method = if (fileId == null) "POST" else "PATCH"
        val endpoint = if (fileId == null) {
            endpoints.uploadFilesUrl
        } else {
            "${endpoints.uploadFilesUrl}/$fileId"
        }
        return request(
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
            ifMatch = ifMatch.takeIf(::isUsableEtag),
            throwOnError = throwOnError,
        )
    }

    private fun downloadFileText(accessToken: String, fileId: String): DriveTextResource? {
        val response = request(
            method = "GET",
            url = driveApiUrl("${endpoints.filesUrl}/$fileId", mapOf("alt" to "media")),
            accessToken = accessToken,
            throwOnError = false,
        )
        if (response.code !in 200..299) {
            return null
        }
        return DriveTextResource(body = response.body, etag = response.etag.takeIf(::isUsableEtag))
    }

    private fun isUsableEtag(etag: String?): Boolean = etag != null && etag.isNotBlank() && etag != "*"

    private fun findFile(accessToken: String, folderId: String, fileName: String): String? {
        val query = listOf(
            "name = '${fileName.driveQueryValue()}'",
            "'${folderId.driveQueryValue()}' in parents",
            "trashed = false",
        ).joinToString(" and ")
        val response = requestJson(
            method = "GET",
            url = driveApiUrl(
                endpoints.filesUrl,
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
        val response = request(
            method = method,
            url = url,
            accessToken = accessToken,
            body = body,
            contentType = contentType,
            throwOnError = true,
        )
        return response.body.takeIf(String::isNotBlank)?.let(::JSONObject) ?: JSONObject()
    }

    private fun request(
        method: String,
        url: String,
        accessToken: String,
        body: String? = null,
        contentType: String? = null,
        ifMatch: String? = null,
        throwOnError: Boolean = true,
    ): DriveResponse {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            // java.net's HttpURLConnection rejects PATCH (only Android's OkHttp-backed stack allows it),
            // so PATCH is tunnelled as POST plus the override header that Google APIs honour. This keeps
            // the update path working on every HTTP stack, including JVM unit tests.
            val usesPatchOverride = method == "PATCH"
            requestMethod = if (usesPatchOverride) "POST" else method
            if (usesPatchOverride) {
                setRequestProperty("X-HTTP-Method-Override", "PATCH")
            }
            // Without explicit timeouts HttpURLConnection defaults to 0 (infinite): a dead/half-open
            // socket would block the sync coroutine forever and wedge the UI in a "syncing" state.
            connectTimeout = CONNECT_TIMEOUT_MILLIS
            readTimeout = READ_TIMEOUT_MILLIS
            setRequestProperty("Authorization", "Bearer $accessToken")
            setRequestProperty("Accept", "application/json")
            if (ifMatch != null) {
                setRequestProperty("If-Match", ifMatch)
            }
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", contentType ?: "application/json; charset=UTF-8")
                outputStream.use { stream -> stream.write(body.toByteArray(Charsets.UTF_8)) }
            }
        }
        try {
            val code = connection.responseCode
            val etag = connection.getHeaderField("ETag")
            val responseBody = if (code in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                if (throwOnError) {
                    throw IOException("Drive request failed with HTTP $code${errorBody.toErrorSuffix()}")
                }
                errorBody
            }
            return DriveResponse(code = code, body = responseBody, etag = etag)
        } finally {
            connection.disconnect()
        }
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

    private data class DriveResponse(val code: Int, val body: String, val etag: String?)

    private data class DriveTextResource(val body: String, val etag: String?)

    private companion object {
        const val CONNECT_TIMEOUT_MILLIS = 15_000
        const val READ_TIMEOUT_MILLIS = 30_000
        const val HTTP_PRECONDITION_FAILED = 412
        const val MULTIPART_BOUNDARY = "quality-alternative-drive-boundary"
    }
}

data class GoogleDriveAnnotationSyncEndpoints(
    val filesUrl: String = "https://www.googleapis.com/drive/v3/files",
    val uploadFilesUrl: String = "https://www.googleapis.com/upload/drive/v3/files",
)

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
