package com.qualityalternative.app.data

import android.content.ContentValues
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidAccountLightProfileAutosaveWriterTest {
    @Test
    fun writesAndReadsDefaultSharedProfileBackup() = runBlocking {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val writer = AndroidAccountLightProfileAutosaveWriter(targetContext)

        writer.writeProfileJson(
            uri = AndroidAccountLightProfileAutosaveWriter.DEFAULT_PROFILE_BACKUP_URI,
            fileName = ACCOUNT_LIGHT_PROFILE_FILE_NAME,
            json = """{"schemaVersion":1,"source":"default"}""",
        )

        assertEquals(
            """{"schemaVersion":1,"source":"default"}""",
            writer.readProfileJson(
                uri = AndroidAccountLightProfileAutosaveWriter.DEFAULT_PROFILE_BACKUP_URI,
                fileName = ACCOUNT_LIGHT_PROFILE_FILE_NAME,
            ),
        )

        writer.writeProfileJson(
            uri = AndroidAccountLightProfileAutosaveWriter.DEFAULT_PROFILE_BACKUP_URI,
            fileName = ACCOUNT_LIGHT_PROFILE_FILE_NAME,
            json = """{"schemaVersion":2,"source":"default"}""",
        )

        assertEquals(
            """{"schemaVersion":2,"source":"default"}""",
            writer.readProfileJson(
                uri = AndroidAccountLightProfileAutosaveWriter.DEFAULT_PROFILE_BACKUP_URI,
                fileName = ACCOUNT_LIGHT_PROFILE_FILE_NAME,
            ),
        )
    }

    @Test
    fun readsNewestDefaultSharedProfileBackupWhenMediaStoreAddsCollisionSuffix() = runBlocking {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val writer = AndroidAccountLightProfileAutosaveWriter(targetContext)
        val fileName = "quality-alternative-profile-collision-${System.currentTimeMillis()}.json"
        val oldUri = targetContext.insertDownloadProfileJson(
            fileName = fileName,
            json = """{"schemaVersion":1,"source":"old"}""",
        )
        Thread.sleep(1_100L)
        val newUri = targetContext.insertDownloadProfileJson(
            fileName = fileName,
            json = """{"schemaVersion":2,"source":"new"}""",
        )

        try {
            assertEquals(
                """{"schemaVersion":2,"source":"new"}""",
                writer.readProfileJson(
                    uri = AndroidAccountLightProfileAutosaveWriter.DEFAULT_PROFILE_BACKUP_URI,
                    fileName = fileName,
                ),
            )
        } finally {
            targetContext.contentResolver.delete(oldUri, null, null)
            targetContext.contentResolver.delete(newUri, null, null)
        }
    }

    @Test
    fun writesAndRewritesProfileJsonThroughDocumentTreeUri() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val targetContext = instrumentation.targetContext
        val writer = AndroidAccountLightProfileAutosaveWriter(targetContext)
        val treeUri = DocumentsContract.buildTreeDocumentUri(
            TestProfileDocumentsProvider.AUTHORITY,
            TestProfileDocumentsProvider.ROOT_ID,
        )

        writer.writeProfileJson(
            uri = treeUri.toString(),
            fileName = ACCOUNT_LIGHT_PROFILE_FILE_NAME,
            json = """{"schemaVersion":1}""",
        )

        assertEquals(
            """{"schemaVersion":1}""",
            targetContext.readProfileJson(treeUri),
        )

        writer.writeProfileJson(
            uri = treeUri.toString(),
            fileName = ACCOUNT_LIGHT_PROFILE_FILE_NAME,
            json = """{"schemaVersion":2}""",
        )

        assertEquals(listOf(ACCOUNT_LIGHT_PROFILE_FILE_NAME), targetContext.profileDocumentNames(treeUri))
        assertEquals(
            """{"schemaVersion":2}""",
            targetContext.readProfileJson(treeUri),
        )
    }

    private fun android.content.Context.readProfileJson(treeUri: android.net.Uri): String {
        val documentUri = DocumentsContract.buildDocumentUriUsingTree(
            treeUri,
            ACCOUNT_LIGHT_PROFILE_FILE_NAME,
        )
        return contentResolver.openInputStream(documentUri)?.use { stream ->
            InputStreamReader(stream, Charsets.UTF_8).readText()
        } ?: error("Profile document could not be opened for readback.")
    }

    private fun android.content.Context.insertDownloadProfileJson(
        fileName: String,
        json: String,
    ): android.net.Uri {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/Quality Alternative/")
        }
        val uri = contentResolver.insert(MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), values)
            ?: error("Profile document could not be created for collision test.")
        contentResolver.openOutputStream(uri, "wt")?.use { stream ->
            OutputStreamWriter(stream, Charsets.UTF_8).use { writer ->
                writer.write(json)
            }
        } ?: error("Profile document could not be written for collision test.")
        return uri
    }

    private fun android.content.Context.profileDocumentNames(treeUri: android.net.Uri): List<String> {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            treeUri,
            DocumentsContract.getTreeDocumentId(treeUri),
        )
        return contentResolver.query(
            childrenUri,
            arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            buildList {
                while (cursor.moveToNext()) {
                    add(cursor.getString(nameIndex))
                }
            }
        }.orEmpty().sorted()
    }
}
