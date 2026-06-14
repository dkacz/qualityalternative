package com.qualityalternative.app.data

import com.qualityalternative.app.domain.service.AGENT_INBOX_MAX_FILES_PER_PACKAGE
import com.qualityalternative.app.domain.service.AGENT_INBOX_MAX_PACKAGES_PER_SCAN
import com.qualityalternative.app.domain.service.AGENT_INBOX_MANIFEST_FILE_NAME
import com.qualityalternative.app.domain.service.AgentInboxDriveDownloadTooLargeException
import com.qualityalternative.app.domain.service.AgentInboxDriveFolderNotSelectedException
import com.qualityalternative.app.domain.service.AgentInboxDriveHttpException
import com.qualityalternative.app.domain.service.AgentInboxDriveScanRequest
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.net.URLDecoder
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidGoogleDriveAgentInboxClientTest {
    private var server: HttpServer? = null
    private val capturedRequests = mutableListOf<CapturedAgentInboxDriveRequest>()

    @After
    fun tearDown() {
        server?.stop(0)
    }

    @Test
    fun scanPackagesRejectsMissingFolderIdWithoutDriveRequest() {
        val baseUrl = startDriveServer { request ->
            404 to """{"error":"unexpected ${request.method} ${request.path} ${request.decodedQuery}"}"""
        }
        val client = AndroidGoogleDriveAgentInboxClient(testEndpoints(baseUrl))

        assertThrows(AgentInboxDriveFolderNotSelectedException::class.java) {
            runBlocking {
                client.scanPackages(
                    AgentInboxDriveScanRequest(accessToken = "drive-token", folderId = null),
                )
            }
        }

        assertTrue(capturedRequests.isEmpty())
    }

    @Test
    fun scanPackagesListsOnlyPackageFoldersAndPackageFilesInsideSelectedFolder() = runBlocking {
        val baseUrl = startDriveServer { request ->
            when {
                request.method == "GET" &&
                    request.path == "/drive/v3/files" &&
                    request.decodedQuery.contains("'agent-inbox-folder' in parents") ->
                    200 to """
                        {
                          "files": [
                            {
                              "id": "package-folder-1",
                              "name": "codex-package",
                              "mimeType": "application/vnd.google-apps.folder",
                              "modifiedTime": "2026-06-12T10:00:00Z"
                            }
                          ]
                        }
                    """.trimIndent()

                request.method == "GET" &&
                    request.path == "/drive/v3/files" &&
                    request.decodedQuery.contains("'package-folder-1' in parents") ->
                    200 to """
                        {
                          "files": [
                            {
                              "id": "manifest-file",
                              "name": "$AGENT_INBOX_MANIFEST_FILE_NAME",
                              "mimeType": "application/json",
                              "size": "512",
                              "md5Checksum": "manifest-md5"
                            },
                            {
                              "id": "content-file",
                              "name": "content.md",
                              "mimeType": "text/markdown",
                              "size": "2048",
                              "md5Checksum": "content-md5"
                            },
                            {
                              "id": "ignored-image",
                              "name": "cover.png",
                              "mimeType": "image/png"
                            }
                          ]
                        }
                    """.trimIndent()

                else -> 404 to """{"error":"unexpected ${request.method} ${request.path} ${request.decodedQuery}"}"""
            }
        }
        val client = AndroidGoogleDriveAgentInboxClient(testEndpoints(baseUrl))

        val result = client.scanPackages(
            AgentInboxDriveScanRequest(accessToken = "drive-token", folderId = "agent-inbox-folder"),
        )

        assertEquals("agent-inbox-folder", result.folderId)
        assertEquals(1, result.packages.size)
        val pack = result.packages.single()
        assertEquals("package-folder-1", pack.folderId)
        assertEquals("codex-package", pack.folderName)
        assertEquals("manifest-file", pack.manifestFile?.id)
        assertEquals(listOf("content.md"), pack.contentFiles.map { file -> file.name })
        assertEquals(3, pack.allFiles.size)
        assertEquals(2048L, pack.contentFiles.single().sizeBytes)
        assertTrue(capturedRequests.all { request -> request.authorization == "Bearer drive-token" })
        assertFalse(capturedRequests.any { request -> request.decodedQuery.contains("name =") })
        assertTrue(
            capturedRequests.any { request ->
                request.decodedQuery.contains("'agent-inbox-folder' in parents") &&
                    request.decodedQuery.contains("mimeType = 'application/vnd.google-apps.folder'")
            },
        )
        assertTrue(
            capturedRequests.any { request ->
                request.decodedQuery.contains("'package-folder-1' in parents") &&
                    request.decodedQuery.contains("trashed = false")
            },
        )
    }

    @Test
    fun scanPackagesUsesProvidedFolderIdWithoutSearchingOrCreatingFolder() = runBlocking {
        val baseUrl = startDriveServer { request ->
            when {
                request.method == "GET" &&
                    request.path == "/drive/v3/files" &&
                    request.decodedQuery.contains("'known-inbox' in parents") ->
                    200 to """{"files":[]}"""
                else -> 404 to """{"error":"unexpected ${request.method} ${request.path} ${request.decodedQuery}"}"""
            }
        }
        val client = AndroidGoogleDriveAgentInboxClient(testEndpoints(baseUrl))

        val result = client.scanPackages(
            AgentInboxDriveScanRequest(accessToken = "drive-token", folderId = "known-inbox"),
        )

        assertEquals("known-inbox", result.folderId)
        assertTrue(result.packages.isEmpty())
        assertFalse(capturedRequests.any { request -> request.decodedQuery.contains("name =") })
        assertFalse(capturedRequests.any { request -> request.method == "POST" })
    }

    @Test
    fun scanPackagesFollowsDrivePaginationForPackageFolders() = runBlocking {
        val baseUrl = startDriveServer { request ->
            when {
                request.method == "GET" &&
                    request.decodedQuery.contains("'known-inbox' in parents") &&
                    !request.decodedQuery.contains("pageToken=") ->
                    200 to """
                        {
                          "nextPageToken": "next-folder-page",
                          "files": [
                            {"id": "package-folder-1", "name": "first", "mimeType": "application/vnd.google-apps.folder"}
                          ]
                        }
                    """.trimIndent()

                request.method == "GET" &&
                    request.decodedQuery.contains("pageToken=next-folder-page") ->
                    200 to """
                        {
                          "files": [
                            {"id": "package-folder-2", "name": "second", "mimeType": "application/vnd.google-apps.folder"}
                          ]
                        }
                    """.trimIndent()

                request.method == "GET" && request.decodedQuery.contains("'package-folder-1' in parents") ->
                    200 to """{"files":[]}"""

                request.method == "GET" && request.decodedQuery.contains("'package-folder-2' in parents") ->
                    200 to """{"files":[]}"""

                else -> 404 to """{"error":"unexpected ${request.method} ${request.path} ${request.decodedQuery}"}"""
            }
        }
        val client = AndroidGoogleDriveAgentInboxClient(testEndpoints(baseUrl))

        val result = client.scanPackages(
            AgentInboxDriveScanRequest(accessToken = "drive-token", folderId = "known-inbox"),
        )

        assertEquals(listOf("first", "second"), result.packages.map { pack -> pack.folderName })
    }

    @Test
    fun scanPackagesCapsPackageFoldersAndMarksHasMorePackages() = runBlocking {
        val packageFoldersJson = (1..AGENT_INBOX_MAX_PACKAGES_PER_SCAN).joinToString(",") { index ->
            """{"id": "package-folder-$index", "name": "package-$index", "mimeType": "application/vnd.google-apps.folder"}"""
        }
        val baseUrl = startDriveServer { request ->
            when {
                request.method == "GET" &&
                    request.decodedQuery.contains("'known-inbox' in parents") &&
                    !request.decodedQuery.contains("pageToken=") ->
                    200 to """
                        {
                          "nextPageToken": "hidden-folder-page",
                          "files": [$packageFoldersJson]
                        }
                    """.trimIndent()

                request.method == "GET" &&
                    Regex("'package-folder-\\d+' in parents").containsMatchIn(request.decodedQuery) ->
                    200 to """{"files":[]}"""

                else -> 404 to """{"error":"unexpected ${request.method} ${request.path} ${request.decodedQuery}"}"""
            }
        }
        val client = AndroidGoogleDriveAgentInboxClient(testEndpoints(baseUrl))

        val result = client.scanPackages(
            AgentInboxDriveScanRequest(accessToken = "drive-token", folderId = "known-inbox"),
        )

        assertEquals(AGENT_INBOX_MAX_PACKAGES_PER_SCAN, result.packages.size)
        assertTrue(result.hasMorePackages)
        assertFalse(capturedRequests.any { request -> request.decodedQuery.contains("pageToken=hidden-folder-page") })
    }

    @Test
    fun scanPackagesCapsPackageFilesAndMarksPackageHasMoreFiles() = runBlocking {
        val packageFilesJson = (1..AGENT_INBOX_MAX_FILES_PER_PACKAGE).joinToString(",") { index ->
            if (index == 1) {
                """{"id": "manifest-file", "name": "$AGENT_INBOX_MANIFEST_FILE_NAME", "mimeType": "application/json"}"""
            } else {
                """{"id": "content-file-$index", "name": "content-$index.md", "mimeType": "text/markdown"}"""
            }
        }
        val baseUrl = startDriveServer { request ->
            when {
                request.method == "GET" &&
                    request.decodedQuery.contains("'known-inbox' in parents") ->
                    200 to """
                        {
                          "files": [
                            {"id": "package-folder-1", "name": "package-1", "mimeType": "application/vnd.google-apps.folder"}
                          ]
                        }
                    """.trimIndent()

                request.method == "GET" &&
                    request.decodedQuery.contains("'package-folder-1' in parents") &&
                    !request.decodedQuery.contains("pageToken=") ->
                    200 to """
                        {
                          "nextPageToken": "hidden-file-page",
                          "files": [$packageFilesJson]
                        }
                    """.trimIndent()

                else -> 404 to """{"error":"unexpected ${request.method} ${request.path} ${request.decodedQuery}"}"""
            }
        }
        val client = AndroidGoogleDriveAgentInboxClient(testEndpoints(baseUrl))

        val result = client.scanPackages(
            AgentInboxDriveScanRequest(accessToken = "drive-token", folderId = "known-inbox"),
        )

        val pack = result.packages.single()
        assertTrue(pack.hasMoreFiles)
        assertEquals(AGENT_INBOX_MAX_FILES_PER_PACKAGE, pack.allFiles.size)
        assertEquals("manifest-file", pack.manifestFile?.id)
        assertFalse(capturedRequests.any { request -> request.decodedQuery.contains("pageToken=hidden-file-page") })
    }

    @Test
    fun downloadFileUsesAltMediaAndReturnsRawBytes() = runBlocking {
        val expected = "epub-bytes".toByteArray()
        val baseUrl = startDriveServer { request ->
            when {
                request.method == "GET" &&
                    request.path == "/drive/v3/files/content-file" &&
                    request.decodedQuery == "alt=media" ->
                    200 to expected.decodeToString()
                else -> 404 to """{"error":"unexpected ${request.method} ${request.path} ${request.decodedQuery}"}"""
            }
        }
        val client = AndroidGoogleDriveAgentInboxClient(testEndpoints(baseUrl))

        val bytes = client.downloadFile(accessToken = "drive-token", fileId = "content-file", maxBytes = 32)

        assertArrayEquals(expected, bytes)
        assertEquals("Bearer drive-token", capturedRequests.single().authorization)
    }

    @Test
    fun downloadFileAbortsWhenResponseBodyExceedsLimit() {
        val baseUrl = startDriveServer { request ->
            when {
                request.method == "GET" &&
                    request.path == "/drive/v3/files/content-file" &&
                    request.decodedQuery == "alt=media" ->
                    200 to "x".repeat(33)
                else -> 404 to """{"error":"unexpected ${request.method} ${request.path} ${request.decodedQuery}"}"""
            }
        }
        val client = AndroidGoogleDriveAgentInboxClient(testEndpoints(baseUrl))

        val error = assertThrows(AgentInboxDriveDownloadTooLargeException::class.java) {
            runBlocking {
                client.downloadFile(accessToken = "drive-token", fileId = "content-file", maxBytes = 32)
            }
        }

        assertEquals(32L, error.maxBytes)
    }

    @Test
    fun driveHttpFailureIncludesStatusAndErrorBody() {
        val baseUrl = startDriveServer { 403 to """{"error":"forbidden"}""" }
        val client = AndroidGoogleDriveAgentInboxClient(testEndpoints(baseUrl))

        val error = assertThrows(AgentInboxDriveHttpException::class.java) {
            runBlocking {
                client.scanPackages(
                    AgentInboxDriveScanRequest(accessToken = "drive-token", folderId = "known-inbox"),
                )
            }
        }

        assertEquals(403, error.statusCode)
        assertEquals("""{"error":"forbidden"}""", error.errorBody)
        assertTrue(error.message.orEmpty().contains("HTTP 403"))
        assertTrue(error.message.orEmpty().contains("forbidden"))
    }

    private fun startDriveServer(responseFor: (CapturedAgentInboxDriveRequest) -> Pair<Int, String>): String {
        val localServer = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server = localServer
        localServer.createContext("/") { exchange ->
            val request = exchange.toCapturedRequest()
            capturedRequests += request
            val (status, body) = responseFor(request)
            exchange.sendBody(status, body)
        }
        localServer.start()
        return "http://127.0.0.1:${localServer.address.port}"
    }

    private fun testEndpoints(baseUrl: String) = GoogleDriveAgentInboxEndpoints(
        filesUrl = "$baseUrl/drive/v3/files",
    )
}

private data class CapturedAgentInboxDriveRequest(
    val method: String,
    val path: String,
    val query: String,
    val authorization: String?,
    val body: String,
) {
    val decodedQuery: String
        get() = URLDecoder.decode(query, "UTF-8")
}

private fun HttpExchange.toCapturedRequest(): CapturedAgentInboxDriveRequest {
    return CapturedAgentInboxDriveRequest(
        method = requestMethod,
        path = requestURI.path,
        query = requestURI.rawQuery.orEmpty(),
        authorization = requestHeaders.getFirst("Authorization"),
        body = requestBody.bufferedReader().use { it.readText() },
    )
}

private fun HttpExchange.sendBody(status: Int, body: String) {
    val bytes = body.toByteArray(Charsets.UTF_8)
    responseHeaders.set("Content-Type", "application/json")
    sendResponseHeaders(status, bytes.size.toLong())
    responseBody.use { stream -> stream.write(bytes) }
}
