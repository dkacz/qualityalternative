package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.ReaderDocument
import com.qualityalternative.app.domain.model.ReaderDocumentBlock
import com.qualityalternative.app.domain.model.ReaderDocumentImage
import com.qualityalternative.app.domain.model.ReaderDocumentTable
import com.qualityalternative.app.domain.model.ReaderDocumentTableAlignment
import java.io.File
import java.net.URI

object MarkdownReaderDocumentParser {
    fun parse(
        markdown: String,
        baseUri: String? = null,
        imageAttachmentUris: Map<String, String> = emptyMap(),
    ): ReaderDocument {
        val blocks = mutableListOf<ReaderDocumentBlock>()
        markdown
            .split(Regex("""\n[ \t\r\f]*\n"""))
            .forEach { rawBlock ->
                val block = rawBlock.trim()
                if (block.isBlank()) return@forEach
                appendMarkdownBlock(
                    output = blocks,
                    rawBlock = block,
                    baseUri = baseUri,
                    imageAttachmentUris = imageAttachmentUris,
                )
            }
        return ReaderDocument(blocks = blocks)
    }

    private fun appendMarkdownBlock(
        output: MutableList<ReaderDocumentBlock>,
        rawBlock: String,
        baseUri: String?,
        imageAttachmentUris: Map<String, String>,
    ) {
        val pendingTextLines = mutableListOf<String>()

        fun flushTextLines() {
            val text = pendingTextLines
                .joinToString("\n")
                .replaceInlineMarkdownImagesWithAltText()
                .trim()
            pendingTextLines.clear()
            if (text.isNotBlank()) {
                output += ReaderDocumentBlock(
                    text = text,
                    sourceBlockIndex = output.size,
                )
            }
        }

        val lines = rawBlock.lines()
        var lineIndex = 0
        while (lineIndex < lines.size) {
            val table = parseMarkdownTable(lines = lines, startIndex = lineIndex)
            if (table != null) {
                flushTextLines()
                output += ReaderDocumentBlock(
                    text = table.readerText,
                    sourceBlockIndex = output.size,
                    table = table.documentTable,
                )
                lineIndex += table.consumedLines
            } else {
                val line = lines[lineIndex]
                val image = parseStandaloneMarkdownImage(
                    line = line,
                    baseUri = baseUri,
                    imageAttachmentUris = imageAttachmentUris,
                )
                if (image == null) {
                    pendingTextLines += line
                } else {
                    flushTextLines()
                    output += ReaderDocumentBlock(
                        text = image.readerText,
                        sourceBlockIndex = output.size,
                        image = image.documentImage,
                    )
                }
                lineIndex += 1
            }
        }
        flushTextLines()
    }

    private fun parseMarkdownTable(lines: List<String>, startIndex: Int): ParsedMarkdownTable? {
        if (startIndex + 1 >= lines.size) {
            return null
        }
        val header = splitMarkdownTableRow(lines[startIndex])
        val delimiter = splitMarkdownTableRow(lines[startIndex + 1])
        if (header.size < 2 || delimiter.size < 2 || !delimiter.all(::isMarkdownTableDelimiterCell)) {
            return null
        }
        val columnCount = maxOf(header.size, delimiter.size)
        val alignments = delimiter.map(::markdownTableAlignment)
        val rows = mutableListOf<List<String>>()
        var cursor = startIndex + 2
        while (cursor < lines.size) {
            val cells = splitMarkdownTableRow(lines[cursor])
            if (cells.isEmpty()) {
                break
            }
            rows += cells.normalizedTableRow(columnCount)
            cursor += 1
        }
        if (rows.isEmpty()) {
            return null
        }
        val normalizedHeader = header.normalizedTableRow(columnCount)
        val table = ReaderDocumentTable(
            headers = normalizedHeader,
            rows = rows,
            alignments = alignments.normalizedAlignments(columnCount),
        )
        return ParsedMarkdownTable(
            documentTable = table,
            readerText = table.toReaderText(),
            consumedLines = cursor - startIndex,
        )
    }

    private fun parseStandaloneMarkdownImage(
        line: String,
        baseUri: String?,
        imageAttachmentUris: Map<String, String>,
    ): ParsedMarkdownImage? {
        val trimmed = line.trim()
        if (!trimmed.startsWith("![") || !trimmed.endsWith(")")) {
            return null
        }
        val closeAlt = findClosingAltBracket(trimmed)
        if (closeAlt <= 1 || closeAlt + 1 >= trimmed.length || trimmed[closeAlt + 1] != '(') {
            return null
        }
        val alt = trimmed.substring(2, closeAlt).decodeMarkdownImageText().trim()
        val destination = trimmed.substring(closeAlt + 2, trimmed.lastIndex).trim()
        val parsedDestination = parseMarkdownImageDestination(destination) ?: return null
        val resolvedSource = resolveMarkdownImageSource(
            target = parsedDestination.target,
            baseUri = baseUri,
            imageAttachmentUris = imageAttachmentUris,
        )
        val title = parsedDestination.title?.decodeMarkdownImageText()?.trim()?.takeIf(String::isNotBlank)
        val readerText = listOf(alt, title)
            .firstOrNull { value -> !value.isNullOrBlank() }
            ?: parsedDestination.target.substringAfterLast('/').ifBlank { "Markdown image" }
        return ParsedMarkdownImage(
            readerText = readerText,
            documentImage = ReaderDocumentImage(
                source = resolvedSource,
                altText = alt,
                title = title,
            ),
        )
    }

    private fun findClosingAltBracket(markdownImage: String): Int {
        var index = 2
        var escaped = false
        while (index < markdownImage.length) {
            val char = markdownImage[index]
            when {
                escaped -> escaped = false
                char == '\\' -> escaped = true
                char == ']' -> return index
            }
            index += 1
        }
        return -1
    }

    private fun parseMarkdownImageDestination(destination: String): MarkdownImageDestination? {
        if (destination.isBlank()) {
            return null
        }
        if (destination.startsWith("<")) {
            val close = destination.indexOf('>')
            if (close <= 1) {
                return null
            }
            return MarkdownImageDestination(
                target = destination.substring(1, close).trim(),
                title = destination.substring(close + 1).trim().trimImageTitle(),
            )
        }
        val split = firstWhitespaceOutsideQuotes(destination)
        return if (split < 0) {
            MarkdownImageDestination(target = destination.trim(), title = null)
        } else {
            MarkdownImageDestination(
                target = destination.substring(0, split).trim(),
                title = destination.substring(split + 1).trim().trimImageTitle(),
            )
        }
    }

    private fun firstWhitespaceOutsideQuotes(value: String): Int {
        var quote: Char? = null
        value.forEachIndexed { index, char ->
            when {
                quote != null && char == quote -> quote = null
                quote == null && (char == '"' || char == '\'') -> quote = char
                quote == null && char.isWhitespace() -> return index
            }
        }
        return -1
    }

    private fun String.trimImageTitle(): String? {
        val trimmed = trim()
        if (trimmed.isBlank()) {
            return null
        }
        return when {
            trimmed.length >= 2 && trimmed.first() == '"' && trimmed.last() == '"' -> trimmed.substring(1, trimmed.lastIndex)
            trimmed.length >= 2 && trimmed.first() == '\'' && trimmed.last() == '\'' -> trimmed.substring(1, trimmed.lastIndex)
            trimmed.length >= 2 && trimmed.first() == '(' && trimmed.last() == ')' -> trimmed.substring(1, trimmed.lastIndex)
            else -> trimmed
        }
    }

    private fun String.decodeMarkdownImageText(): String {
        return replace("\\]", "]")
            .replace("\\[", "[")
            .replace("\\)", ")")
            .replace("\\(", "(")
            .replace("\\\\", "\\")
    }

    private fun String.replaceInlineMarkdownImagesWithAltText(): String {
        return InlineMarkdownImageRegex.replace(this) { match ->
            match.groupValues[1].decodeMarkdownImageText().ifBlank { "Markdown image" }
        }
    }

    private fun resolveMarkdownImageSource(
        target: String,
        baseUri: String?,
        imageAttachmentUris: Map<String, String>,
    ): String {
        val cleanedTarget = target.trim()
        if (cleanedTarget.isBlank() || cleanedTarget.startsWith("data:image/", ignoreCase = true)) {
            return cleanedTarget
        }
        imageAttachmentUris.attachmentUriForMarkdownTarget(cleanedTarget)?.let { attachmentUri ->
            return attachmentUri
        }
        val targetUri = runCatching { URI(cleanedTarget) }.getOrNull()
        if (targetUri?.scheme != null) {
            return cleanedTarget
        }
        if (File(cleanedTarget).isAbsolute) {
            return File(cleanedTarget).toURI().toString()
        }
        val base = baseUri
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?.let { value -> runCatching { URI(value) }.getOrNull() }
        return if (base?.scheme == "file") {
            base.resolve(cleanedTarget).toString()
        } else {
            cleanedTarget
        }
    }

    private fun Map<String, String>.attachmentUriForMarkdownTarget(target: String): String? {
        val targetKeys = listOf(
            target,
            target.substringAfterLast('/'),
            target.substringAfterLast('\\'),
            target.lowercase(),
            target.substringAfterLast('/').lowercase(),
            target.substringAfterLast('\\').lowercase(),
        )
        return targetKeys.firstNotNullOfOrNull { key -> this[key]?.takeIf(String::isNotBlank) }
    }

    private data class MarkdownImageDestination(
        val target: String,
        val title: String?,
    )

    private data class ParsedMarkdownImage(
        val readerText: String,
        val documentImage: ReaderDocumentImage,
    )

    private data class ParsedMarkdownTable(
        val documentTable: ReaderDocumentTable,
        val readerText: String,
        val consumedLines: Int,
    )

    private val InlineMarkdownImageRegex = Regex("""!\[([^\]]*)]\(([^)]+)\)""")
}

private fun splitMarkdownTableRow(line: String): List<String> {
    val trimmed = line.trim()
    if (!trimmed.contains('|')) {
        return emptyList()
    }
    val body = trimmed
        .removePrefix("|")
        .removeSuffix("|")
    val cells = mutableListOf<String>()
    val current = StringBuilder()
    var escaped = false
    body.forEach { char ->
        when {
            escaped -> {
                current.append(char)
                escaped = false
            }

            char == '\\' -> escaped = true
            char == '|' -> {
                cells += current.toString().trim()
                current.clear()
            }

            else -> current.append(char)
        }
    }
    cells += current.toString().trim()
    return cells
}

private fun isMarkdownTableDelimiterCell(cell: String): Boolean {
    val trimmed = cell.trim()
    if (trimmed.isBlank()) {
        return false
    }
    val core = trimmed.trim(':')
    return core.length >= 3 && core.all { char -> char == '-' }
}

private fun markdownTableAlignment(cell: String): ReaderDocumentTableAlignment {
    val trimmed = cell.trim()
    return when {
        trimmed.startsWith(":") && trimmed.endsWith(":") -> ReaderDocumentTableAlignment.CENTER
        trimmed.endsWith(":") -> ReaderDocumentTableAlignment.END
        else -> ReaderDocumentTableAlignment.START
    }
}

private fun List<String>.normalizedTableRow(columnCount: Int): List<String> {
    return (0 until columnCount).map { index -> getOrNull(index).orEmpty() }
}

private fun List<ReaderDocumentTableAlignment>.normalizedAlignments(columnCount: Int): List<ReaderDocumentTableAlignment> {
    return (0 until columnCount).map { index -> getOrNull(index) ?: ReaderDocumentTableAlignment.START }
}

private fun ReaderDocumentTable.toReaderText(): String {
    return (listOf(headers) + rows)
        .joinToString("\n") { cells -> cells.joinToString("\t") }
        .trim()
}
