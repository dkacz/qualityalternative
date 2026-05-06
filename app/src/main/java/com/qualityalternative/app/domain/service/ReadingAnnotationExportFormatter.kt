package com.qualityalternative.app.domain.service

import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.ContentSourceType
import com.qualityalternative.app.domain.model.ReadingAnnotation
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ReadingAnnotationExportFile(
    val contentId: String,
    val sourceTitle: String,
    val fileName: String,
    val jsonLd: String,
)

class ReadingAnnotationExportFormatter {
    fun formatJsonLdFiles(
        annotations: List<ReadingAnnotation>,
        contentById: Map<String, ContentItem>,
    ): List<ReadingAnnotationExportFile> {
        return annotations
            .sortedWith(
                compareBy<ReadingAnnotation> { annotation -> annotation.sourceTitleOrFallback(contentById) }
                    .thenBy(ReadingAnnotation::paragraphIndex)
                    .thenByDescending(ReadingAnnotation::updatedAtMillis),
            )
            .groupBy(ReadingAnnotation::contentId)
            .map { (contentId, groupedAnnotations) ->
                val content = contentById[contentId]
                val sourceTitle = groupedAnnotations.firstOrNull()?.sourceTitleOrFallback(contentById)
                    ?: content?.title
                    ?: "Source no longer in Library"
                ReadingAnnotationExportFile(
                    contentId = contentId,
                    sourceTitle = sourceTitle,
                    fileName = annotationFileName(sourceTitle = sourceTitle, contentId = contentId),
                    jsonLd = sourceAnnotationCollectionJson(
                        contentId = contentId,
                        content = content,
                        sourceTitle = sourceTitle,
                        annotations = groupedAnnotations.sortedWith(
                            compareBy<ReadingAnnotation> { it.paragraphIndex }.thenByDescending { it.updatedAtMillis },
                        ),
                    ),
                )
            }
    }

    fun formatIndexJson(files: List<ReadingAnnotationExportFile>): String {
        return jsonObject(
            "type" to jsonString("QualityAlternativeAnnotationExportIndex"),
            "generatedBy" to jsonString("Quality Alternative"),
            "files" to jsonArray(
                files.map { file ->
                    jsonObject(
                        "contentId" to jsonString(file.contentId),
                        "sourceTitle" to jsonString(file.sourceTitle),
                        "fileName" to jsonString(file.fileName),
                    )
                },
            ),
        )
    }

    private fun sourceAnnotationCollectionJson(
        contentId: String,
        content: ContentItem?,
        sourceTitle: String,
        annotations: List<ReadingAnnotation>,
    ): String {
        val sourceJson = sourceJson(
            contentId = contentId,
            content = content,
            sourceTitle = sourceTitle,
            fallbackFormat = annotations.firstNotNullOfOrNull { it.sourceFormat },
            fallbackSourceType = annotations.firstNotNullOfOrNull { it.sourceType },
        )
        return jsonObject(
            "@context" to jsonString("http://www.w3.org/ns/anno.jsonld"),
            "id" to jsonString("urn:quality-alternative:annotation-collection:${contentId.urlComponent()}"),
            "type" to jsonString("AnnotationCollection"),
            "label" to jsonString("Annotations for $sourceTitle"),
            "total" to annotations.size.toString(),
            "source" to sourceJson,
            "items" to jsonArray(annotations.map { annotation -> annotation.toW3cAnnotation(sourceJson) }),
        )
    }

    private fun sourceJson(
        contentId: String,
        content: ContentItem?,
        sourceTitle: String,
        fallbackFormat: ContentFormat?,
        fallbackSourceType: ContentSourceType?,
    ): String {
        val fields = mutableListOf(
            "id" to jsonString(content?.rights?.sourceUrl ?: "quality-alternative://content/${contentId.urlComponent()}"),
            "type" to jsonString("Text"),
            "format" to jsonString(content?.format?.w3cFormat() ?: fallbackFormat?.w3cFormat() ?: "text/plain"),
            "title" to jsonString(sourceTitle),
            "contentId" to jsonString(contentId),
            "sourceType" to jsonString(content?.sourceType?.name ?: fallbackSourceType?.name ?: "MISSING_SOURCE"),
        )
        content?.sourceLabel?.takeIf(String::isNotBlank)?.let { label ->
            fields += "label" to jsonString(label)
        }
        return jsonObject(fields)
    }

    private fun ReadingAnnotation.toW3cAnnotation(sourceJson: String): String {
        val selectors = mutableListOf(
            jsonObject(
                "type" to jsonString("TextQuoteSelector"),
                "exact" to jsonString(quotedText),
                "prefix" to jsonString(selector.prefixText),
                "suffix" to jsonString(selector.suffixText),
            ),
            jsonObject(
                "type" to jsonString("TextPositionSelector"),
                "start" to selector.textStartOffset.toString(),
                "end" to selector.textEndOffset.coerceAtLeast(selector.textStartOffset).toString(),
            ),
        )
        selector.sourceHref?.takeIf(String::isNotBlank)?.let { href ->
            selectors += jsonObject(
                "type" to jsonString("FragmentSelector"),
                "value" to jsonString(href + selector.sourceAnchor?.let { anchor -> "#$anchor" }.orEmpty()),
                "refinedBy" to jsonObject(
                    "type" to jsonString("TextPositionSelector"),
                    "start" to selector.textStartOffset.toString(),
                    "end" to selector.textEndOffset.toString(),
                ),
            )
        }

        return jsonObject(
            "id" to jsonString("urn:quality-alternative:annotation:${id.urlComponent()}"),
            "type" to jsonString("Annotation"),
            "motivation" to jsonString("commenting"),
            "created" to jsonString(createdAtMillis.isoTimestamp()),
            "modified" to jsonString(updatedAtMillis.isoTimestamp()),
            "creator" to jsonObject("type" to jsonString("Software"), "name" to jsonString("Quality Alternative")),
            "body" to jsonArray(
                listOf(
                    jsonObject(
                        "type" to jsonString("TextualBody"),
                        "purpose" to jsonString("commenting"),
                        "format" to jsonString("text/plain"),
                        "value" to jsonString(noteText),
                    ),
                ),
            ),
            "target" to jsonObject(
                "source" to sourceJson,
                "selector" to jsonArray(selectors),
            ),
            "qualityAlternative" to jsonObject(
                "contentId" to jsonString(contentId),
                "paragraphIndex" to paragraphIndex.toString(),
                "sourceBlockIndex" to selector.sourceBlockIndex.toString(),
                "endSourceBlockIndex" to selector.endSourceBlockIndex.toString(),
                "sourceTitle" to jsonString(sourceTitle),
                "sourceLabel" to sourceLabel.jsonNullableString(),
                "sourceType" to sourceType?.name.jsonNullableString(),
                "sourceFormat" to sourceFormat?.name.jsonNullableString(),
                "sourceHref" to selector.sourceHref.jsonNullableString(),
                "sourceAnchor" to selector.sourceAnchor.jsonNullableString(),
            ),
        )
    }

    private fun ReadingAnnotation.sourceTitleOrFallback(contentById: Map<String, ContentItem>): String {
        return sourceTitle.takeIf(String::isNotBlank)
            ?: contentById[contentId]?.title
            ?: "Source no longer in Library"
    }

    private fun annotationFileName(sourceTitle: String, contentId: String): String {
        val safeTitle = sourceTitle
            .lowercase(Locale.US)
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .take(64)
            .ifBlank { "source" }
        val safeId = contentId
            .lowercase(Locale.US)
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .take(48)
            .ifBlank { "content" }
        return "quality-alternative-$safeTitle-$safeId.annotations.jsonld"
    }

    private fun ContentFormat.w3cFormat(): String {
        return when (this) {
            ContentFormat.MARKDOWN -> "text/markdown"
            ContentFormat.HTML -> "text/html"
            ContentFormat.PDF -> "application/pdf"
            ContentFormat.EPUB -> "application/epub+zip"
        }
    }

    private fun Long.isoTimestamp(): String {
        return DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(this).atOffset(ZoneOffset.UTC))
    }

    private fun String.urlComponent(): String {
        return replace(Regex("[^A-Za-z0-9._:-]+"), "-").trim('-').ifBlank { "unknown" }
    }

    private fun jsonObject(vararg fields: Pair<String, String>): String = jsonObject(fields.toList())

    private fun jsonObject(fields: List<Pair<String, String>>): String {
        return fields.joinToString(prefix = "{", postfix = "}") { (name, value) ->
            "${jsonString(name)}:$value"
        }
    }

    private fun jsonArray(values: List<String>): String {
        return values.joinToString(prefix = "[", postfix = "]")
    }

    private fun jsonString(raw: String): String {
        return buildString {
            append('"')
            raw.forEach { char ->
                when (char) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\b' -> append("\\b")
                    '\u000C' -> append("\\f")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> {
                        if (char.code < 0x20) {
                            append("\\u")
                            append(char.code.toString(16).padStart(4, '0'))
                        } else {
                            append(char)
                        }
                    }
                }
            }
            append('"')
        }
    }

    private fun String?.jsonNullableString(): String {
        return this?.let(::jsonString) ?: "null"
    }
}
