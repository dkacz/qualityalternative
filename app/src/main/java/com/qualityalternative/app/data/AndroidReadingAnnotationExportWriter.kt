package com.qualityalternative.app.data

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.qualityalternative.app.domain.service.ReadingAnnotationExportFile
import com.qualityalternative.app.domain.service.ReadingAnnotationExportFormatter
import com.qualityalternative.app.domain.service.ReadingAnnotationExportWriter
import java.io.File
import java.io.OutputStreamWriter

class AndroidReadingAnnotationExportWriter(
    private val context: Context,
) : ReadingAnnotationExportWriter {
    private val formatter = ReadingAnnotationExportFormatter()

    override suspend fun writeMarkdown(uri: String, markdown: String) {
        val parsedUri = Uri.parse(uri)
        if (parsedUri.scheme == "file") {
            val path = parsedUri.path?.takeIf(String::isNotBlank)
                ?: error("Could not open annotation export file")
            File(path).writeText(markdown)
            return
        }

        val outputStream = context.contentResolver.openOutputStream(parsedUri, "wt")
            ?: error("Could not open annotation export file")
        outputStream.use { stream ->
            OutputStreamWriter(stream, Charsets.UTF_8).use { writer ->
                writer.write(markdown)
            }
        }
    }

    override suspend fun writeJsonLdFiles(uri: String, files: List<ReadingAnnotationExportFile>) {
        val parsedUri = Uri.parse(uri)
        if (parsedUri.scheme == "file") {
            val path = parsedUri.path?.takeIf(String::isNotBlank)
                ?: error("Could not open annotation export file")
            val selected = File(path)
            val outputDir = if (selected.isDirectory) selected else selected.parentFile
                ?: error("Could not open annotation export directory")
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }
            // Write the new files first, then prune only the stale ones (old QA annotation files no
            // longer in this export). A crash mid-export then leaves the previous export intact rather
            // than an emptied directory.
            val newFileNames = files.mapTo(mutableSetOf(), ReadingAnnotationExportFile::fileName)
            files.forEach { file ->
                File(outputDir, file.fileName).writeText(file.jsonLd)
            }
            outputDir.listFiles { file ->
                file.isFile &&
                    file.name.startsWith(QUALITY_ALTERNATIVE_ANNOTATION_PREFIX) &&
                    file.name.endsWith(QUALITY_ALTERNATIVE_ANNOTATION_SUFFIX) &&
                    file.name !in newFileNames
            }
                .orEmpty()
                .forEach(File::delete)
            val indexFile = if (selected.isDirectory) {
                File(outputDir, QUALITY_ALTERNATIVE_ANNOTATION_INDEX)
            } else {
                selected
            }
            indexFile.writeText(formatter.formatIndexJson(files))
            return
        }
        if (DocumentsContract.isTreeUri(parsedUri)) {
            writeJsonLdDirectory(treeUri = parsedUri, files = files)
            return
        }

        val payload = files.singleOrNull()?.jsonLd
            ?: error("Choose an annotation export folder so each source can be saved as its own JSON-LD file.")
        writeMarkdown(uri = uri, markdown = payload)
    }

    private fun writeJsonLdDirectory(treeUri: Uri, files: List<ReadingAnnotationExportFile>) {
        val resolver = context.contentResolver
        val parentDocumentId = DocumentsContract.getTreeDocumentId(treeUri)
        val parentDocumentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, parentDocumentId)
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
                if (
                    documentIdIndex >= 0 &&
                    nameIndex >= 0
                ) {
                    val name = cursor.getString(nameIndex).orEmpty()
                    val isQualityAlternativeAnnotationFile =
                        name == QUALITY_ALTERNATIVE_ANNOTATION_INDEX ||
                            (name.startsWith(QUALITY_ALTERNATIVE_ANNOTATION_PREFIX) &&
                                name.endsWith(QUALITY_ALTERNATIVE_ANNOTATION_SUFFIX))
                    if (isQualityAlternativeAnnotationFile) {
                        val documentUri = DocumentsContract.buildDocumentUriUsingTree(
                            treeUri,
                            cursor.getString(documentIdIndex),
                        )
                        DocumentsContract.deleteDocument(resolver, documentUri)
                    }
                }
            }
        }
        files.forEach { file ->
            val documentUri = DocumentsContract.createDocument(
                resolver,
                parentDocumentUri,
                JSON_LD_MIME_TYPE,
                file.fileName,
            ) ?: error("Could not create ${file.fileName}")
            resolver.openOutputStream(documentUri, "wt")
                ?.use { stream ->
                    OutputStreamWriter(stream, Charsets.UTF_8).use { writer ->
                        writer.write(file.jsonLd)
                    }
                }
                ?: error("Could not write ${file.fileName}")
        }
        val indexUri = DocumentsContract.createDocument(
            resolver,
            parentDocumentUri,
            "application/json",
            QUALITY_ALTERNATIVE_ANNOTATION_INDEX,
        ) ?: error("Could not create annotation index")
        resolver.openOutputStream(indexUri, "wt")
            ?.use { stream ->
                OutputStreamWriter(stream, Charsets.UTF_8).use { writer ->
                    writer.write(formatter.formatIndexJson(files))
                }
            }
            ?: error("Could not write annotation index")
    }

    private companion object {
        const val JSON_LD_MIME_TYPE = "application/ld+json"
        const val QUALITY_ALTERNATIVE_ANNOTATION_INDEX = "quality-alternative-annotations.index.json"
        const val QUALITY_ALTERNATIVE_ANNOTATION_PREFIX = "quality-alternative-"
        const val QUALITY_ALTERNATIVE_ANNOTATION_SUFFIX = ".annotations.jsonld"
    }
}
