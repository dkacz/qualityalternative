package com.qualityalternative.app.data

import com.qualityalternative.app.domain.service.AGENT_INBOX_MANIFEST_FILE_NAME
import com.qualityalternative.app.domain.service.AGENT_INBOX_MAX_FILES_PER_PACKAGE
import com.qualityalternative.app.domain.service.AGENT_INBOX_MAX_PACKAGES_PER_SCAN
import com.qualityalternative.app.domain.service.AgentInboxDriveClient
import com.qualityalternative.app.domain.service.AgentInboxDriveDownloadTooLargeException
import com.qualityalternative.app.domain.service.AgentInboxDriveFile
import com.qualityalternative.app.domain.service.AgentInboxDriveFolderNotSelectedException
import com.qualityalternative.app.domain.service.AgentInboxDriveHttpException
import com.qualityalternative.app.domain.service.AgentInboxDrivePackage
import com.qualityalternative.app.domain.service.AgentInboxDriveScanRequest
import com.qualityalternative.app.domain.service.AgentInboxDriveScanResult
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class AndroidGoogleDriveAgentInboxClient(
    private val endpoints: GoogleDriveAgentInboxEndpoints = GoogleDriveAgentInboxEndpoints(),
) : AgentInboxDriveClient {
    override suspend fun scanPackages(
        request: AgentInboxDriveScanRequest,
    ): AgentInboxDriveScanResult = withContext(Dispatchers.IO) {
        val folderId = request.folderId?.takeIf(String::isNotBlank)
            ?: throw AgentInboxDriveFolderNotSelectedException()
        val packageFolders = listChildFolders(accessToken = request.accessToken, parentFolderId = folderId)
        val packages = packageFolders.files.map { folder ->
            val files = listChildFiles(accessToken = request.accessToken, parentFolderId = folder.id)
            AgentInboxDrivePackage(
                folderId = folder.id,
                folderName = folder.name,
                manifestFile = files.files.firstOrNull { file -> file.name == AGENT_INBOX_MANIFEST_FILE_NAME },
                contentFiles = files.files.filter(AgentInboxDriveFile::isAgentContentFile),
                allFiles = files.files,
                hasMoreFiles = files.hasMore,
            )
        }
        AgentInboxDriveScanResult(
            folderId = folderId,
            packages = packages,
            hasMorePackages = packageFolders.hasMore,
        )
    }

    override suspend fun downloadFile(
        accessToken: String,
        fileId: String,
        maxBytes: Long,
    ): ByteArray = withContext(Dispatchers.IO) {
        request(
            method = "GET",
            url = driveApiUrl("${endpoints.filesUrl}/${fileId.urlPathEncode()}", mapOf("alt" to "media")),
            accessToken = accessToken,
            throwOnError = true,
            maxBodyBytes = maxBytes,
        ).body
    }

    private fun listChildFolders(accessToken: String, parentFolderId: String): ListedAgentInboxFiles {
        val query = listOf(
            "'${parentFolderId.driveQueryValue()}' in parents",
            "mimeType = '$DRIVE_FOLDER_MIME_TYPE'",
            "trashed = false",
        ).joinToString(" and ")
        return listFiles(accessToken = accessToken, query = query, limit = AGENT_INBOX_MAX_PACKAGES_PER_SCAN)
    }

    private fun listChildFiles(accessToken: String, parentFolderId: String): ListedAgentInboxFiles {
        val query = listOf(
            "'${parentFolderId.driveQueryValue()}' in parents",
            "trashed = false",
        ).joinToString(" and ")
        return listFiles(accessToken = accessToken, query = query, limit = AGENT_INBOX_MAX_FILES_PER_PACKAGE)
    }

    private fun listFiles(accessToken: String, query: String, limit: Int): ListedAgentInboxFiles {
        val files = mutableListOf<AgentInboxDriveFile>()
        var pageToken: String? = null
        var hasMore = false
        do {
            val remaining = limit - files.size
            if (remaining <= 0) {
                hasMore = true
                break
            }
            val parameters = buildMap {
                put("q", query)
                put("spaces", "drive")
                put("fields", "nextPageToken,files(id,name,mimeType,size,md5Checksum,modifiedTime)")
                put("pageSize", minOf(PAGE_SIZE, remaining).toString())
                pageToken?.let { token -> put("pageToken", token) }
            }
            val response = requestJson(
                method = "GET",
                url = driveApiUrl(endpoints.filesUrl, parameters),
                accessToken = accessToken,
            )
            files += response.optJSONArray("files").orEmpty().mapObjects { file ->
                AgentInboxDriveFile(
                    id = file.optString("id"),
                    name = file.optString("name"),
                    mimeType = file.optString("mimeType").takeIf(String::isNotBlank),
                    sizeBytes = file.optString("size").toLongOrNull(),
                    md5Checksum = file.optString("md5Checksum").takeIf(String::isNotBlank),
                    modifiedTime = file.optString("modifiedTime").takeIf(String::isNotBlank),
                )
            }.filter { file -> file.id.isNotBlank() && file.name.isNotBlank() }
            pageToken = response.optString("nextPageToken").takeIf(String::isNotBlank)
            if (files.size >= limit && pageToken != null) {
                hasMore = true
                break
            }
        } while (pageToken != null)
        return ListedAgentInboxFiles(files = files.take(limit), hasMore = hasMore)
    }

    private fun requestJson(
        method: String,
        url: String,
        accessToken: String,
        body: ByteArray? = null,
        contentType: String? = null,
    ): JSONObject {
        val response = request(
            method = method,
            url = url,
            accessToken = accessToken,
            body = body,
            contentType = contentType,
            throwOnError = true,
            maxBodyBytes = MAX_JSON_RESPONSE_BYTES,
        )
        val text = response.body.toString(Charsets.UTF_8)
        return text.takeIf(String::isNotBlank)?.let(::JSONObject) ?: JSONObject()
    }

    private fun request(
        method: String,
        url: String,
        accessToken: String,
        body: ByteArray? = null,
        contentType: String? = null,
        throwOnError: Boolean = true,
        maxBodyBytes: Long? = null,
    ): DriveByteResponse {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = CONNECT_TIMEOUT_MILLIS
            readTimeout = READ_TIMEOUT_MILLIS
            setRequestProperty("Authorization", "Bearer $accessToken")
            setRequestProperty("Accept", "application/json")
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", contentType ?: "application/json; charset=UTF-8")
                outputStream.use { stream -> stream.write(body) }
            }
        }
        try {
            val code = connection.responseCode
            val responseBody = if (code in 200..299) {
                connection.inputStream.readBoundedBytes(maxBodyBytes)
            } else {
                val errorBody = connection.errorStream?.readBoundedBytes(MAX_JSON_RESPONSE_BYTES) ?: ByteArray(0)
                if (throwOnError) {
                    throw AgentInboxDriveHttpException(
                        statusCode = code,
                        errorBody = String(errorBody, Charsets.UTF_8),
                    )
                }
                errorBody
            }
            return DriveByteResponse(code = code, body = responseBody)
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

    private data class DriveByteResponse(val code: Int, val body: ByteArray)

    private data class ListedAgentInboxFiles(
        val files: List<AgentInboxDriveFile>,
        val hasMore: Boolean,
    )

    private companion object {
        const val CONNECT_TIMEOUT_MILLIS = 15_000
        const val READ_TIMEOUT_MILLIS = 30_000
        const val PAGE_SIZE = 100
        const val MAX_JSON_RESPONSE_BYTES = 256L * 1024L
        const val DRIVE_FOLDER_MIME_TYPE = "application/vnd.google-apps.folder"
    }
}

data class GoogleDriveAgentInboxEndpoints(
    val filesUrl: String = "https://www.googleapis.com/drive/v3/files",
)

private fun AgentInboxDriveFile.isAgentContentFile(): Boolean {
    val lower = name.lowercase()
    return lower.endsWith(".md") || lower.endsWith(".markdown") || lower.endsWith(".epub")
}

private fun JSONArray?.orEmpty(): JSONArray = this ?: JSONArray()

private fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> {
    return buildList {
        for (index in 0 until length()) {
            optJSONObject(index)?.let { item -> add(transform(item)) }
        }
    }
}

private fun String.driveQueryValue(): String {
    return replace("\\", "\\\\").replace("'", "\\'")
}

private fun InputStream.readBoundedBytes(maxBytes: Long?): ByteArray {
    if (maxBytes == null) return readBytes()
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var total = 0L
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

private fun String.urlEncode(): String = URLEncoder.encode(this, "UTF-8")

private fun String.urlPathEncode(): String = urlEncode().replace("+", "%20")
