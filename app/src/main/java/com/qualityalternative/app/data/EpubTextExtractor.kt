package com.qualityalternative.app.data

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

object EpubTextExtractor {
    private const val semanticIndentSentinel = "\uE000"

    fun extract(input: InputStream): String {
        val entries = readZipEntries(input)
        val opfPath = entries["META-INF/container.xml"]
            ?.toString(Charsets.UTF_8)
            ?.let(::rootFilePath)
            ?: entries.keys.firstOrNull { it.endsWith(".opf", ignoreCase = true) }
            ?: throw IllegalArgumentException("EPUB package document not found")
        val opf = entries[opfPath]?.toString(Charsets.UTF_8)
            ?: throw IllegalArgumentException("EPUB package document not readable")
        val baseDir = opfPath.substringBeforeLast('/', missingDelimiterValue = "")
        val manifestItems = manifestItems(opf)
        val spineItems = spineItems(opf)
        val spinePaths = spineItems
            .mapNotNull { spineItem ->
                manifestItems[spineItem.idref]
                    ?.takeIf { manifestItem -> manifestItem.isReadableSpineDocument(spineItem = spineItem) }
            }
            .map { item -> normalizePath(baseDir, item.href) }
            .filter(::isReadableDocumentPath)
            .filter { path -> entries.containsKey(path) }
        val fallbackPaths = {
            manifestItems.values
                .filter { item -> item.isReadableManifestDocument() }
                .map { item -> normalizePath(baseDir, item.href) }
                .filter(::isReadableDocumentPath)
                .filter { path -> entries.containsKey(path) }
                .ifEmpty {
                    entries.keys
                        .filter(::isReadableDocumentPath)
                        .sorted()
                }
        }
        val readablePaths = if (spineItems.isNotEmpty()) {
            spinePaths
        } else {
            fallbackPaths()
        }
        val body = readablePaths
            .mapNotNull { path -> entries[path]?.toString(Charsets.UTF_8) }
            .map(::htmlToReadableText)
            .filter(String::isNotBlank)
            .joinToString(separator = "\n\n")
            .trim()
        if (body.isBlank()) {
            throw IllegalArgumentException("EPUB contains no readable text")
        }
        return body
    }

    private fun readZipEntries(input: InputStream): Map<String, ByteArray> {
        val entries = linkedMapOf<String, ByteArray>()
        ZipInputStream(input.buffered()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (!entry.isDirectory) {
                    val out = ByteArrayOutputStream()
                    zip.copyTo(out)
                    entries[entry.name] = out.toByteArray()
                }
                zip.closeEntry()
            }
        }
        return entries
    }

    private fun rootFilePath(containerXml: String): String? {
        return Regex("""<rootfile\b[^>]*\bfull-path\s*=\s*["']([^"']+)["'][^>]*/?>""", RegexOption.IGNORE_CASE)
            .find(containerXml)
            ?.groupValues
            ?.getOrNull(1)
    }

    private data class ManifestItem(
        val id: String,
        val href: String,
        val mediaType: String?,
        val properties: Set<String>,
    )

    private data class SpineItem(
        val idref: String,
        val linear: Boolean,
    )

    private fun manifestItems(opf: String): Map<String, ManifestItem> {
        return Regex("""<item\b[^>]*>""", RegexOption.IGNORE_CASE)
            .findAll(opf)
            .mapNotNull { match ->
                val tag = match.value
                val id = tag.attribute("id")
                val href = tag.attribute("href")
                if (id != null && href != null) {
                    id to ManifestItem(
                        id = id,
                        href = href,
                        mediaType = tag.attribute("media-type"),
                        properties = tag.attribute("properties").toPropertySet(),
                    )
                } else {
                    null
                }
            }
            .toMap()
    }

    private fun spineItems(opf: String): List<SpineItem> {
        return Regex("""<itemref\b[^>]*>""", RegexOption.IGNORE_CASE)
            .findAll(opf)
            .mapNotNull { match ->
                val tag = match.value
                tag.attribute("idref")?.let { idref ->
                    SpineItem(
                        idref = idref,
                        linear = tag.attribute("linear")?.equals("no", ignoreCase = true) != true,
                    )
                }
            }
            .toList()
    }

    private fun ManifestItem.isReadableSpineDocument(spineItem: SpineItem): Boolean {
        if (!spineItem.linear) {
            return false
        }
        return isReadableManifestDocument()
    }

    private fun ManifestItem.isReadableManifestDocument(): Boolean {
        if ("nav" in properties) {
            return false
        }
        val normalizedMediaType = mediaType?.lowercase()
        return normalizedMediaType == null ||
            normalizedMediaType == "application/xhtml+xml" ||
            normalizedMediaType == "text/html"
    }

    private fun String?.toPropertySet(): Set<String> {
        return this
            ?.split(Regex("""\s+"""))
            ?.mapNotNull { raw -> raw.trim().lowercase().takeIf(String::isNotBlank) }
            ?.toSet()
            .orEmpty()
    }

    private fun isReadableDocumentPath(path: String): Boolean {
        if (!path.endsWith(".xhtml", ignoreCase = true) &&
            !path.endsWith(".html", ignoreCase = true) &&
            !path.endsWith(".htm", ignoreCase = true)
        ) {
            return false
        }
        return !path.isAuxiliaryDocumentPath()
    }

    private fun String.isAuxiliaryDocumentPath(): Boolean {
        val fileName = substringAfterLast('/').substringBeforeLast('.').lowercase()
        val normalized = fileName.replace(Regex("""[_\-\s]+"""), "")
        return normalized in setOf("nav", "toc", "tableofcontents", "cover", "coverpage", "titlepage")
    }

    private fun String.attribute(name: String): String? {
        return Regex("""\b$name\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE)
            .find(this)
            ?.groupValues
            ?.getOrNull(1)
    }

    private fun normalizePath(baseDir: String, href: String): String {
        val raw = if (baseDir.isBlank()) href else "$baseDir/$href"
        val parts = raw.replace('\\', '/')
            .split('/')
            .filter(String::isNotBlank)
        val stack = mutableListOf<String>()
        parts.forEach { part ->
            when (part) {
                "." -> Unit
                ".." -> if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex)
                else -> stack += part
            }
        }
        return stack.joinToString("/")
    }

    private fun htmlToReadableText(html: String): String {
        val body = Regex("""<body\b[^>]*>([\s\S]*?)</body>""", RegexOption.IGNORE_CASE)
            .find(html)
            ?.groupValues
            ?.getOrNull(1)
            ?: html
        return body
            .replace(Regex("""<script\b[\s\S]*?</script>""", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("""<style\b[\s\S]*?</style>""", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("""<svg\b[\s\S]*?</svg>""", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("""<br\b[^>]*\/?>""", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("""<hr\b[^>]*\/?>""", RegexOption.IGNORE_CASE), "\n\n---\n\n")
            .replaceHeadings()
            .replaceBlockquotes()
            .replaceLists()
            .replace(Regex("""</(p|div|section|article|aside|header|footer|main|figure|figcaption|ul|ol|dl|table|tbody|thead|tr)>""", RegexOption.IGNORE_CASE), "\n\n")
            .toInlineReaderMarkdown(preserveLineBreaks = true)
            .normalizeReaderMarkdown()
    }

    private fun String.replaceHeadings(): String {
        return Regex("""<h([1-6])\b[^>]*>([\s\S]*?)</h\1>""", RegexOption.IGNORE_CASE)
            .replace(this) { match ->
                val level = match.groupValues[1].toIntOrNull()?.coerceIn(1, 6) ?: 1
                val text = match.groupValues[2].toInlineReaderMarkdown()
                "\n\n${"#".repeat(level)} $text\n\n"
            }
    }

    private fun String.replaceBlockquotes(): String {
        return Regex("""<blockquote\b[^>]*>([\s\S]*?)</blockquote>""", RegexOption.IGNORE_CASE)
            .replace(this) { match ->
                val quote = match.groupValues[1]
                    .replace(Regex("""</(p|div|li)>""", RegexOption.IGNORE_CASE), "\n")
                    .toInlineReaderMarkdown(preserveLineBreaks = true)
                    .lines()
                    .map { line -> line.trim() }
                    .filter(String::isNotBlank)
                    .joinToString("\n") { line -> "> $line" }
                "\n\n$quote\n\n"
            }
    }

    private fun String.replaceLists(): String {
        var rendered = this
        while (true) {
            val replaced = rendered.replaceOneInnermostList()
            if (replaced == rendered) {
                return rendered
            }
            rendered = replaced
        }
    }

    private fun String.replaceOneInnermostList(): String {
        val listTag = Regex("""</?(ol|ul)\b[^>]*>""", RegexOption.IGNORE_CASE)
        val stack = mutableListOf<ListTag>()
        listTag.findAll(this).forEach { match ->
            val tagName = match.groupValues[1].lowercase()
            val closing = match.value.startsWith("</", ignoreCase = true)
            if (!closing) {
                stack += ListTag(tagName = tagName, start = match.range.first, bodyStart = match.range.last + 1)
            } else {
                val openIndex = stack.indexOfLast { tag -> tag.tagName == tagName }
                if (openIndex >= 0) {
                    val open = stack.removeAt(openIndex)
                    val body = substring(open.bodyStart, match.range.first)
                    val items = body.listItemsToReaderMarkdown(ordered = tagName == "ol")
                    val replacement = if (items.isBlank()) "" else "\n\n$items\n\n"
                    return replaceRange(open.start, match.range.last + 1, replacement)
                }
            }
        }
        return this
    }

    private data class ListTag(
        val tagName: String,
        val start: Int,
        val bodyStart: Int,
    )

    private fun String.listItemsToReaderMarkdown(ordered: Boolean): String {
        return topLevelListItemBodies()
            .mapIndexedNotNull { index, match ->
                val lines = match.listItemLines()
                if (lines.isEmpty()) {
                    null
                } else {
                    val marker = if (ordered) "${index + 1}." else "-"
                    buildString {
                        append(marker)
                        append(' ')
                        append(lines.first())
                        lines.drop(1).forEach { line ->
                            append('\n')
                            append(line.indentContinuationLine())
                        }
                    }
                }
            }
            .joinToString("\n")
    }

    private fun String.topLevelListItemBodies(): List<String> {
        val bodies = mutableListOf<String>()
        var depth = 0
        var bodyStart = -1
        Regex("""</?li\b[^>]*>""", RegexOption.IGNORE_CASE)
            .findAll(this)
            .forEach { match ->
                val tag = match.value
                val closing = tag.startsWith("</", ignoreCase = true)
                if (!closing) {
                    if (depth == 0) {
                        bodyStart = match.range.last + 1
                    }
                    depth += 1
                } else if (depth > 0) {
                    depth -= 1
                    if (depth == 0 && bodyStart >= 0) {
                        bodies += substring(bodyStart, match.range.first)
                        bodyStart = -1
                    }
                }
            }
        return bodies
    }

    private fun String.replaceInlineStyleTags(tagNames: String, marker: String): String {
        val pattern = Regex("""<($tagNames)\b[^>]*>([\s\S]*?)</\1>""", RegexOption.IGNORE_CASE)
        var rendered = this
        while (true) {
            val replaced = pattern.replace(rendered) { match ->
                val text = match.groupValues[2].toInlineReaderMarkdown()
                if (text.isBlank()) "" else "$marker$text$marker"
            }
            if (replaced == rendered) {
                return rendered
            }
            rendered = replaced
        }
    }

    private fun String.listItemLines(): List<String> {
        return replaceLists()
            .replace(Regex("""</(p|div|section|article|blockquote)>""", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("""<br\b[^>]*\/?>""", RegexOption.IGNORE_CASE), "\n")
            .toInlineReaderMarkdown(preserveLineBreaks = true, decodeSemanticIndent = false)
            .lines()
            .map(::normalizeListItemLine)
            .filter(String::isNotBlank)
    }

    private fun normalizeListItemLine(line: String): String {
        line.semanticIndent()?.let { (indent, encodedBody) ->
            val body = encodedBody.trim().replace(Regex("""[ \t\r\f]+"""), " ")
            if (body.isBlank()) {
                return ""
            }
            return if (indent > 0) {
                " ".repeat(indent) + body
            } else {
                body
            }
        }
        val body = line.trim().replace(Regex("""[ \t\r\f]+"""), " ")
        if (body.isBlank()) {
            return ""
        }
        val leadingIndent = line.takeWhile(Char::isWhitespace)
            .sumOf { char -> if (char == '\t') 2 else 1 }
        val nestedListMarker = Regex("""^([-*+]|\d+[.)])\s+.+$""")
        return if (leadingIndent > 0 && nestedListMarker.matches(body)) {
            " ".repeat(leadingIndent) + body
        } else {
            body
        }
    }

    private fun String.indentContinuationLine(): String {
        val existingIndent = takeWhile(Char::isWhitespace)
            .sumOf { char -> if (char == '\t') 2 else 1 }
        return "$semanticIndentSentinel${existingIndent + 2}:${trim()}"
    }

    private fun String.toInlineReaderMarkdown(
        preserveLineBreaks: Boolean = false,
        decodeSemanticIndent: Boolean = true,
    ): String {
        val withoutBlocks = if (preserveLineBreaks) {
            replace(Regex("""<(br|/p|/div|/li)\b[^>]*>""", RegexOption.IGNORE_CASE), "\n")
        } else {
            this
        }
        return withoutBlocks
            .replaceInlineStyleTags("strong|b", "**")
            .replaceInlineStyleTags("em|i|cite", "_")
            .replaceInlineStyleTags("code|kbd|samp", "`")
            .replace(Regex("""<[^>]+>"""), " ")
            .decodeXmlEntities()
            .let { text ->
                if (preserveLineBreaks) {
                    text.lines()
                        .joinToString("\n") { line ->
                            normalizeReaderMarkdownLine(
                                line = line,
                                decodeSemanticIndent = decodeSemanticIndent,
                            )
                        }
                } else {
                    text.replace(Regex("""\s+"""), " ").trim()
                }
            }
            .trim()
    }

    private fun String.normalizeReaderMarkdown(): String {
        return lines()
            .map(::normalizeReaderMarkdownLine)
            .joinToString("\n")
            .replace(Regex("""\n{3,}"""), "\n\n")
            .split(Regex("""\n[ \t\r\f]*\n"""))
            .map { block ->
                block.lines()
                    .map(::normalizeReaderMarkdownLine)
                    .filter(String::isNotBlank)
                    .joinToString("\n")
                    .trim()
            }
            .filter(String::isNotBlank)
            .joinToString(separator = "\n\n")
    }

    private fun normalizeReaderMarkdownLine(line: String, decodeSemanticIndent: Boolean = true): String {
        if (decodeSemanticIndent) {
            line.semanticIndent()?.let { (indent, encodedBody) ->
                val body = encodedBody.trim().replace(Regex("""[ \t\r\f]+"""), " ")
                if (body.isBlank()) {
                    return ""
                }
                return if (indent > 0) {
                    " ".repeat(indent) + body
                } else {
                    body
                }
            }
        }
        val leadingIndent = line.takeWhile(Char::isWhitespace)
            .sumOf { char -> if (char == '\t') 2 else 1 }
        val body = line.trim().replace(Regex("""[ \t\r\f]+"""), " ")
        return if (leadingIndent > 0 && body.isNotBlank()) " ".repeat(leadingIndent) + body else body
    }

    private fun String.semanticIndent(): Pair<Int, String>? {
        if (!startsWith(semanticIndentSentinel)) {
            return null
        }
        val encoded = removePrefix(semanticIndentSentinel)
        val separatorIndex = encoded.indexOf(':')
        if (separatorIndex <= 0) {
            return null
        }
        val indent = encoded.substring(0, separatorIndex).toIntOrNull() ?: return null
        return indent to encoded.substring(separatorIndex + 1)
    }

    private fun String.decodeXmlEntities(): String {
        return replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&rsquo;", "'")
            .replace("&lsquo;", "'")
            .replace("&rdquo;", "\"")
            .replace("&ldquo;", "\"")
            .replace("&mdash;", "-")
            .replace("&ndash;", "-")
            .replace("&hellip;", "...")
            .replace(Regex("""&#(\d+);""")) { match ->
                match.groupValues[1].toIntOrNull()?.toCodePointString() ?: match.value
            }
            .replace(Regex("""&#x([0-9a-fA-F]+);""")) { match ->
                match.groupValues[1].toIntOrNull(radix = 16)?.toCodePointString() ?: match.value
            }
    }

    private fun Int.toCodePointString(): String? {
        return runCatching { String(Character.toChars(this)) }.getOrNull()
    }
}
