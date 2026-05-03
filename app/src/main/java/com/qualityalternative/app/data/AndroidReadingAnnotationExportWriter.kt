package com.qualityalternative.app.data

import android.content.Context
import android.net.Uri
import com.qualityalternative.app.domain.service.ReadingAnnotationExportWriter
import java.io.File
import java.io.OutputStreamWriter

class AndroidReadingAnnotationExportWriter(
    private val context: Context,
) : ReadingAnnotationExportWriter {
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
}
