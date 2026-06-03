package com.qualityalternative.app.data

import com.qualityalternative.app.domain.service.ANNOTATION_DRIVE_INDEX_FILE_NAME
import com.qualityalternative.app.domain.service.ReadingAnnotationDriveSyncRequest
import com.qualityalternative.app.domain.service.ReadingAnnotationExportFile
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.io.IOException
import java.net.InetSocketAddress
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidGoogleDriveAnnotationSyncClientTest {
    private var server: HttpServer? = null
    private val capturedRequests = mutableListOf<CapturedDriveRequest>()

    @After
    fun tearDown() {
        server?.stop(0)
    }

    @Test
    fun createsFolderAndUploadsIndexAndAnnotationJsonLdWithBearerToken() = runBlocking {
        val baseUrl = startDriveServer { request ->
            when {
                request.method == "GET" && request.path == "/drive/v3/files" && request.query.contains("mimeType") ->
                    200 to """{"files":[]}"""
                request.method == "POST" && request.path == "/drive/v3/files" ->
                    200 to """{"id":"drive-folder","name":"Quality Alternative annotations"}"""
                request.method == "GET" && request.path == "/drive/v3/files" ->
                    200 to """{"files":[]}"""
                request.method == "POST" && request.path == "/upload/drive/v3/files" ->
                    200 to """{"id":"uploaded","name":"ok"}"""
                else -> 404 to """{"error":"unexpected ${request.method} ${request.path}"}"""
            }
        }
        val client = AndroidGoogleDriveAnnotationSyncClient(testEndpoints(baseUrl))

        val result = client.syncJsonLdFiles(
            ReadingAnnotationDriveSyncRequest(
                accessToken = "drive-token",
                folderId = null,
                indexJson = """{"files":["source-1.jsonld"]}""",
                files = listOf(
                    ReadingAnnotationExportFile(
                        contentId = "source-1",
                        sourceTitle = "Source One",
                        fileName = "source-1.jsonld",
                        jsonLd = """{"@context":"https://www.w3.org/ns/anno.jsonld","body":"note"}""",
                    ),
                ),
            ),
        )

        assertEquals("drive-folder", result.folderId)
        assertEquals(listOf(ANNOTATION_DRIVE_INDEX_FILE_NAME, "source-1.jsonld"), result.syncedFileNames)
        assertTrue(capturedRequests.all { it.authorization == "Bearer drive-token" })
        assertTrue(capturedRequests.any { it.method == "POST" && it.path == "/drive/v3/files" && it.body.contains("Quality Alternative annotations") })

        val uploads = capturedRequests.filter { it.path == "/upload/drive/v3/files" }
        assertEquals(2, uploads.size)
        assertTrue(uploads.all { it.query.contains("uploadType=multipart") })
        assertTrue(uploads.first().body.contains(ANNOTATION_DRIVE_INDEX_FILE_NAME))
        assertTrue(uploads.first().body.contains("drive-folder"))
        assertTrue(uploads.first().body.contains("""{"files":["source-1.jsonld"]}"""))
        assertTrue(uploads.last().body.contains("source-1.jsonld"))
        assertTrue(uploads.last().body.contains("application/ld+json"))
        assertTrue(uploads.last().body.contains(""""body":"note""""))
    }

    @Test
    fun usesExistingFolderWhenFolderAlreadyExists() = runBlocking {
        val baseUrl = startDriveServer { request ->
            when {
                request.method == "GET" && request.path == "/drive/v3/files" && request.query.contains("mimeType") ->
                    200 to """{"files":[{"id":"drive-folder"}]}"""
                request.method == "GET" && request.path == "/drive/v3/files" ->
                    200 to """{"files":[]}"""
                request.method == "POST" && request.path == "/upload/drive/v3/files" ->
                    200 to """{"id":"index-created","name":"$ANNOTATION_DRIVE_INDEX_FILE_NAME"}"""
                else -> 404 to """{"error":"unexpected ${request.method} ${request.path}"}"""
            }
        }
        val client = AndroidGoogleDriveAnnotationSyncClient(testEndpoints(baseUrl))

        val result = client.syncJsonLdFiles(
            ReadingAnnotationDriveSyncRequest(
                accessToken = "drive-token",
                folderId = null,
                indexJson = """{"files":[]}""",
                files = emptyList(),
            ),
        )

        assertEquals("drive-folder", result.folderId)
        assertEquals(listOf(ANNOTATION_DRIVE_INDEX_FILE_NAME), result.syncedFileNames)
        assertTrue(capturedRequests.none { it.method == "POST" && it.path == "/drive/v3/files" })
        assertTrue(capturedRequests.any { it.method == "POST" && it.path == "/upload/drive/v3/files" })
    }

    @Test
    fun driveHttpFailureIncludesStatusAndErrorBody() {
        val baseUrl = startDriveServer { 500 to """{"error":"quota"}""" }
        val client = AndroidGoogleDriveAnnotationSyncClient(testEndpoints(baseUrl))

        val error = assertThrows(IOException::class.java) {
            runBlocking {
                client.syncJsonLdFiles(
                    ReadingAnnotationDriveSyncRequest(
                        accessToken = "drive-token",
                        folderId = null,
                        indexJson = """{"files":[]}""",
                        files = emptyList(),
                    ),
                )
            }
        }

        assertTrue(error.message.orEmpty().contains("HTTP 500"))
        assertTrue(error.message.orEmpty().contains("quota"))
    }

    @Test
    fun mergesRemoteIndexIntoExistingFileAndSendsIfMatchBeforeOverwriting() = runBlocking {
        val remoteIndex = """{"type":"QualityAlternativeAnnotationExportIndex","files":[{"contentId":"remote","sourceTitle":"Remote","fileName":"remote.jsonld"}]}"""
        val baseUrl = startDriveServer { request ->
            when {
                request.method == "GET" && request.path == "/drive/v3/files" && request.query.contains("mimeType") ->
                    200 to """{"files":[{"id":"drive-folder"}]}"""
                request.method == "GET" && request.path == "/drive/v3/files/index-file" && request.query.contains("alt=media") ->
                    200 to remoteIndex
                request.method == "GET" && request.path == "/drive/v3/files" ->
                    200 to """{"files":[{"id":"index-file"}]}"""
                request.effectiveMethod == "PATCH" && request.path == "/upload/drive/v3/files/index-file" ->
                    200 to """{"id":"index-file","name":"ok"}"""
                else -> 404 to """{"error":"unexpected ${request.method} ${request.path} ${request.query}"}"""
            }
        }
        val client = AndroidGoogleDriveAnnotationSyncClient(testEndpoints(baseUrl))

        val result = client.syncJsonLdFiles(
            ReadingAnnotationDriveSyncRequest(
                accessToken = "drive-token",
                folderId = "drive-folder",
                indexJson = """{"type":"QualityAlternativeAnnotationExportIndex","files":[{"contentId":"local","sourceTitle":"Local","fileName":"local.jsonld"}]}""",
                files = emptyList(),
            ),
        )

        assertEquals("drive-folder", result.folderId)
        val mediaReads = capturedRequests.filter { it.path == "/drive/v3/files/index-file" && it.query.contains("alt=media") }
        assertEquals(1, mediaReads.size)
        val patch = capturedRequests.single { it.effectiveMethod == "PATCH" && it.path == "/upload/drive/v3/files/index-file" }
        // The merged upload keeps both the remote-only and the local index entries: no silent overwrite.
        assertTrue(patch.body.contains("remote.jsonld"))
        assertTrue(patch.body.contains("local.jsonld"))
        // The captured ETag from the read is echoed back as an optimistic-concurrency guard.
        assertTrue(patch.ifMatch.orEmpty().startsWith("etag-"))
    }

    @Test
    fun retriesMergeWithoutIfMatchWhenDriveReportsPreconditionFailed() = runBlocking {
        val remoteIndex = """{"type":"QualityAlternativeAnnotationExportIndex","files":[{"contentId":"remote","sourceTitle":"Remote","fileName":"remote.jsonld"}]}"""
        val baseUrl = startDriveServer { request ->
            when {
                request.method == "GET" && request.path == "/drive/v3/files" && request.query.contains("mimeType") ->
                    200 to """{"files":[{"id":"drive-folder"}]}"""
                request.method == "GET" && request.path == "/drive/v3/files/index-file" && request.query.contains("alt=media") ->
                    200 to remoteIndex
                request.method == "GET" && request.path == "/drive/v3/files" ->
                    200 to """{"files":[{"id":"index-file"}]}"""
                request.effectiveMethod == "PATCH" && request.path == "/upload/drive/v3/files/index-file" -> {
                    val priorPatches = capturedRequests.count { it.effectiveMethod == "PATCH" && it.path == "/upload/drive/v3/files/index-file" }
                    // The first PATCH is the one currently being captured (count includes it), so prior==1.
                    if (priorPatches <= 1) 412 to """{"error":"precondition"}""" else 200 to """{"id":"index-file","name":"ok"}"""
                }
                else -> 404 to """{"error":"unexpected ${request.method} ${request.path} ${request.query}"}"""
            }
        }
        val client = AndroidGoogleDriveAnnotationSyncClient(testEndpoints(baseUrl))

        client.syncJsonLdFiles(
            ReadingAnnotationDriveSyncRequest(
                accessToken = "drive-token",
                folderId = "drive-folder",
                indexJson = """{"type":"QualityAlternativeAnnotationExportIndex","files":[{"contentId":"local","sourceTitle":"Local","fileName":"local.jsonld"}]}""",
                files = emptyList(),
            ),
        )

        val patches = capturedRequests.filter { it.effectiveMethod == "PATCH" && it.path == "/upload/drive/v3/files/index-file" }
        assertEquals(2, patches.size)
        assertTrue(patches.first().ifMatch.orEmpty().startsWith("etag-"))
        assertEquals(null, patches.last().ifMatch)
        val mediaReads = capturedRequests.filter { it.path == "/drive/v3/files/index-file" && it.query.contains("alt=media") }
        assertEquals(2, mediaReads.size)
    }

    private fun startDriveServer(responseFor: (CapturedDriveRequest) -> Pair<Int, String>): String {
        val localServer = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server = localServer
        localServer.createContext("/") { exchange ->
            val request = exchange.toCapturedRequest()
            capturedRequests += request
            val (status, body) = responseFor(request)
            exchange.sendJson(status, body)
        }
        localServer.start()
        return "http://127.0.0.1:${localServer.address.port}"
    }

    private fun testEndpoints(baseUrl: String) = GoogleDriveAnnotationSyncEndpoints(
        filesUrl = "$baseUrl/drive/v3/files",
        uploadFilesUrl = "$baseUrl/upload/drive/v3/files",
    )
}

private data class CapturedDriveRequest(
    val method: String,
    val path: String,
    val query: String,
    val authorization: String?,
    val contentType: String?,
    val ifMatch: String?,
    val methodOverride: String?,
    val body: String,
) {
    // PATCH is tunnelled as POST + X-HTTP-Method-Override, so tests reason about the logical method.
    val effectiveMethod: String get() = methodOverride ?: method
}

private fun HttpExchange.toCapturedRequest(): CapturedDriveRequest {
    return CapturedDriveRequest(
        method = requestMethod,
        path = requestURI.path,
        query = requestURI.rawQuery.orEmpty(),
        authorization = requestHeaders.getFirst("Authorization"),
        contentType = requestHeaders.getFirst("Content-Type"),
        ifMatch = requestHeaders.getFirst("If-Match"),
        methodOverride = requestHeaders.getFirst("X-HTTP-Method-Override"),
        body = requestBody.bufferedReader().use { it.readText() },
    )
}

private fun HttpExchange.sendJson(status: Int, body: String) {
    val bytes = body.toByteArray(Charsets.UTF_8)
    responseHeaders.set("Content-Type", "application/json")
    responseHeaders.set("ETag", "etag-${requestURI.path.hashCode()}")
    sendResponseHeaders(status, bytes.size.toLong())
    responseBody.use { stream -> stream.write(bytes) }
}
