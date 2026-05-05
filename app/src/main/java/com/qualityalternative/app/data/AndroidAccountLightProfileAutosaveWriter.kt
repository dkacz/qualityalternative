package com.qualityalternative.app.data

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.qualityalternative.app.domain.service.AccountLightProfileAutosaveWriter
import java.io.File
import java.io.OutputStreamWriter

class AndroidAccountLightProfileAutosaveWriter(
    private val context: Context,
) : AccountLightProfileAutosaveWriter {
    override suspend fun writeProfileJson(uri: String, fileName: String, json: String) {
        val parsedUri = Uri.parse(uri)
        if (parsedUri.scheme == "file") {
            val path = parsedUri.path?.takeIf(String::isNotBlank)
                ?: error("Could not open portable profile autosave destination.")
            val selected = File(path)
            val outputFile = if (selected.isDirectory) File(selected, fileName) else selected
            outputFile.parentFile?.mkdirs()
            outputFile.writeText(json, Charsets.UTF_8)
            return
        }
        if (DocumentsContract.isTreeUri(parsedUri)) {
            writeProfileJsonToTree(treeUri = parsedUri, fileName = fileName, json = json)
            return
        }
        context.contentResolver.openOutputStream(parsedUri, "wt")
            ?.use { stream ->
                OutputStreamWriter(stream, Charsets.UTF_8).use { writer ->
                    writer.write(json)
                }
            }
            ?: error("Could not write portable profile.")
    }

    private fun writeProfileJsonToTree(treeUri: Uri, fileName: String, json: String) {
        val resolver = context.contentResolver
        val parentDocumentId = DocumentsContract.getTreeDocumentId(treeUri)
        val parentDocumentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, parentDocumentId)
        val existingDocumentUri = findChildDocumentUri(treeUri = treeUri, fileName = fileName)
        val profileDocumentUri = existingDocumentUri ?: DocumentsContract.createDocument(
            resolver,
            parentDocumentUri,
            "application/json",
            fileName,
        ) ?: error("Could not create portable profile file.")

        resolver.openOutputStream(profileDocumentUri, "wt")
            ?.use { stream ->
                OutputStreamWriter(stream, Charsets.UTF_8).use { writer ->
                    writer.write(json)
                }
            }
            ?: error("Could not write portable profile file.")
    }

    private fun findChildDocumentUri(treeUri: Uri, fileName: String): Uri? {
        val resolver = context.contentResolver
        val parentDocumentId = DocumentsContract.getTreeDocumentId(treeUri)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocumentId)
        resolver.query(
            childrenUri,
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            ),
            null,
            null,
            null,
        )?.use { cursor ->
            val documentIdIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            while (cursor.moveToNext()) {
                if (documentIdIndex >= 0 && nameIndex >= 0 && cursor.getString(nameIndex) == fileName) {
                    return DocumentsContract.buildDocumentUriUsingTree(
                        treeUri,
                        cursor.getString(documentIdIndex),
                    )
                }
            }
        }
        return null
    }
}
