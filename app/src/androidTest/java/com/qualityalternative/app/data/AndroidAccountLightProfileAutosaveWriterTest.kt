package com.qualityalternative.app.data

import android.provider.DocumentsContract
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.InputStreamReader
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidAccountLightProfileAutosaveWriterTest {
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
