package com.qualityalternative.app.domain.service

import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.ContentSourceType
import com.qualityalternative.app.domain.model.ReadingAnnotation
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class ReadingAnnotationExportFormatter {
    fun format(
        annotations: List<ReadingAnnotation>,
        contentById: Map<String, ContentItem>,
    ): String {
        return buildString {
            appendLine("# Quality Alternative Annotations")
            appendLine()
            appendLine("_Autosaved by Quality Alternative._")
            if (annotations.isEmpty()) {
                appendLine()
                appendLine("No annotations saved yet.")
                return@buildString
            }

            annotations
                .sortedWith(
                    compareBy<ReadingAnnotation> { annotation -> contentById[annotation.contentId]?.title ?: "Source no longer in Library" }
                        .thenBy { annotation -> annotation.paragraphIndex }
                        .thenByDescending { annotation -> annotation.updatedAtMillis },
                )
                .groupBy(ReadingAnnotation::contentId)
                .forEach { (contentId, groupedAnnotations) ->
                    val content = contentById[contentId]
                    appendLine()
                    appendLine("## ${markdownLine(content?.title ?: "Source no longer in Library")}")
                    appendLine()
                    appendLine("- Source type: ${content?.sourceType?.exportLabel() ?: "Missing source"}")
                    content?.sourceLabel?.takeIf(String::isNotBlank)?.let { sourceLabel ->
                        appendLine("- Source: ${markdownLine(sourceLabel)}")
                    }
                    appendLine("- Content ID: `${markdownLine(contentId)}`")
                    groupedAnnotations
                        .sortedWith(compareBy<ReadingAnnotation> { it.paragraphIndex }.thenByDescending { it.updatedAtMillis })
                        .forEach { annotation ->
                            appendLine()
                            appendLine("### Paragraph ${annotation.paragraphIndex + 1}")
                            appendLine()
                            appendLine("- Updated: ${exportTimestamp(annotation.updatedAtMillis)}")
                            appendLine()
                            appendBlockQuote(annotation.quotedText)
                            appendLine()
                            appendLine(markdownParagraph(annotation.noteText))
                        }
                }
        }
    }

    private fun StringBuilder.appendBlockQuote(text: String) {
        markdownParagraph(text)
            .lines()
            .filter(String::isNotBlank)
            .forEach { line -> appendLine("> $line") }
    }

    private fun markdownLine(raw: String): String {
        return raw.replace(Regex("\\s+"), " ").trim()
    }

    private fun markdownParagraph(raw: String): String {
        return raw.lines()
            .map(String::trim)
            .joinToString("\n")
            .trim()
    }

    private fun ContentSourceType.exportLabel(): String {
        return when (this) {
            ContentSourceType.USER_LINK -> "User link"
            ContentSourceType.USER_DOCUMENT -> "User file"
            ContentSourceType.MEDITATION -> "Meditation"
            ContentSourceType.EDITORIAL -> "Editorial"
        }
    }

    private fun exportTimestamp(timestampMillis: Long): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.US)
        return formatter.format(Instant.ofEpochMilli(timestampMillis).atZone(ZoneId.systemDefault()))
    }
}
