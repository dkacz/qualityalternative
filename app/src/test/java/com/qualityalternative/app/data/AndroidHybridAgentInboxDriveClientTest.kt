package com.qualityalternative.app.data

import com.qualityalternative.app.domain.service.AgentInboxDriveClient
import com.qualityalternative.app.domain.service.AgentInboxDriveFile
import com.qualityalternative.app.domain.service.AgentInboxDriveFolderListRequest
import com.qualityalternative.app.domain.service.AgentInboxDriveFolderListResult
import com.qualityalternative.app.domain.service.AgentInboxDrivePackage
import com.qualityalternative.app.domain.service.AgentInboxDriveScanRequest
import com.qualityalternative.app.domain.service.AgentInboxDriveScanResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidHybridAgentInboxDriveClientTest {
    @Test
    fun scanPackagesRoutesGoogleDriveDocumentTreeUriThroughDriveApiWhenTokenIsAvailable() = runBlocking {
        val treeUri = "content://com.google.android.apps.docs.storage/tree/acc%3Duser%40example.com%3Bdoc%3Ddrive-folder-id"
        val googleClient = RecordingDriveClient(
            scanResult = AgentInboxDriveScanResult(
                folderId = "drive-folder-id",
                packages = listOf(drivePackage(folderId = "package-folder-id")),
            ),
        )
        val treeClient = RecordingDriveClient()
        val client = AndroidHybridAgentInboxDriveClient(
            googleDriveClient = googleClient,
            documentTreeClient = treeClient,
        )

        val result = client.scanPackages(
            AgentInboxDriveScanRequest(
                accessToken = "drive-token",
                folderId = treeUri,
            ),
        )

        assertEquals(treeUri, result.folderId)
        assertEquals("package-folder-id", result.packages.single().folderId)
        assertEquals(
            AgentInboxDriveScanRequest(accessToken = "drive-token", folderId = "drive-folder-id"),
            googleClient.scanRequests.single(),
        )
        assertTrue(treeClient.scanRequests.isEmpty())
    }

    @Test
    fun scanPackagesKeepsLocalDocumentTreeUriOnDocumentProviderWhenNoDriveTokenExists() = runBlocking {
        val treeUri = "content://com.android.externalstorage.documents/tree/agent-inbox"
        val googleClient = RecordingDriveClient()
        val treeClient = RecordingDriveClient(
            scanResult = AgentInboxDriveScanResult(
                folderId = treeUri,
                packages = listOf(drivePackage(folderId = "content://package")),
            ),
        )
        val client = AndroidHybridAgentInboxDriveClient(
            googleDriveClient = googleClient,
            documentTreeClient = treeClient,
        )

        val result = client.scanPackages(
            AgentInboxDriveScanRequest(
                accessToken = "",
                folderId = treeUri,
            ),
        )

        assertEquals(treeUri, result.folderId)
        assertEquals("content://package", result.packages.single().folderId)
        assertEquals(AgentInboxDriveScanRequest(accessToken = "", folderId = treeUri), treeClient.scanRequests.single())
        assertTrue(googleClient.scanRequests.isEmpty())
    }

    @Test
    fun googleDriveDocumentTreeFolderIdExtractsDocParameterAndRawTreeSegment() {
        assertEquals(
            "drive-folder-id",
            "content://com.google.android.apps.docs.storage/tree/acc%3Duser%40example.com%3Bdoc%3Ddrive-folder-id"
                .googleDriveDocumentTreeFolderId(),
        )
        assertEquals(
            "raw-drive-folder-id",
            "content://com.google.android.apps.docs.storage/tree/raw-drive-folder-id"
                .googleDriveDocumentTreeFolderId(),
        )
        assertEquals(
            null,
            "content://com.android.externalstorage.documents/tree/raw-drive-folder-id"
                .googleDriveDocumentTreeFolderId(),
        )
    }

    @Test
    fun downloadFileRoutesContentUrisToDocumentTreeAndDriveIdsToDriveApi() = runBlocking {
        val googleClient = RecordingDriveClient(files = mapOf("drive-file" to byteArrayOf(1, 2)))
        val treeClient = RecordingDriveClient(files = mapOf("content://file" to byteArrayOf(3, 4)))
        val client = AndroidHybridAgentInboxDriveClient(
            googleDriveClient = googleClient,
            documentTreeClient = treeClient,
        )

        assertArrayEquals(byteArrayOf(1, 2), client.downloadFile("drive-token", "drive-file", 10))
        assertArrayEquals(byteArrayOf(3, 4), client.downloadFile("", "content://file", 10))

        assertEquals(listOf("drive-file"), googleClient.downloadedFileIds)
        assertEquals(listOf("content://file"), treeClient.downloadedFileIds)
    }

    @Test
    fun listFoldersRoutesThroughGoogleDriveClient() = runBlocking {
        val googleClient = RecordingDriveClient()
        val treeClient = RecordingDriveClient()
        val client = AndroidHybridAgentInboxDriveClient(
            googleDriveClient = googleClient,
            documentTreeClient = treeClient,
        )

        client.listFolders(AgentInboxDriveFolderListRequest(accessToken = "drive-token", parentFolderId = "parent"))

        assertEquals(listOf(AgentInboxDriveFolderListRequest("drive-token", "parent")), googleClient.folderListRequests)
        assertTrue(treeClient.folderListRequests.isEmpty())
    }

    private class RecordingDriveClient(
        private val scanResult: AgentInboxDriveScanResult = AgentInboxDriveScanResult(
            folderId = "folder",
            packages = emptyList(),
        ),
        private val files: Map<String, ByteArray> = emptyMap(),
    ) : AgentInboxDriveClient {
        val folderListRequests = mutableListOf<AgentInboxDriveFolderListRequest>()
        val scanRequests = mutableListOf<AgentInboxDriveScanRequest>()
        val downloadedFileIds = mutableListOf<String>()

        override suspend fun listFolders(
            request: AgentInboxDriveFolderListRequest,
        ): AgentInboxDriveFolderListResult {
            folderListRequests += request
            return AgentInboxDriveFolderListResult(parentFolderId = request.parentFolderId, folders = emptyList())
        }

        override suspend fun scanPackages(request: AgentInboxDriveScanRequest): AgentInboxDriveScanResult {
            scanRequests += request
            return scanResult
        }

        override suspend fun downloadFile(
            accessToken: String,
            fileId: String,
            maxBytes: Long,
            expectedBytes: Long?,
        ): ByteArray {
            downloadedFileIds += fileId
            return requireNotNull(files[fileId])
        }
    }

    private fun drivePackage(folderId: String): AgentInboxDrivePackage {
        return AgentInboxDrivePackage(
            folderId = folderId,
            folderName = "package",
            manifestFile = AgentInboxDriveFile(
                id = "manifest",
                name = "manifest.json",
                mimeType = "application/json",
                sizeBytes = null,
                md5Checksum = null,
                modifiedTime = null,
            ),
            contentFiles = emptyList(),
            allFiles = emptyList(),
        )
    }
}
