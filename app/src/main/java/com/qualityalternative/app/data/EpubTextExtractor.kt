package com.qualityalternative.app.data

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

object EpubTextExtractor {
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
        val manifest = manifestItems(opf)
        val spinePaths = spineIdRefs(opf)
            .mapNotNull(manifest::get)
            .map { href -> normalizePath(baseDir, href) }
            .filter { path -> entries.containsKey(path) }
        val readablePaths = spinePaths.ifEmpty {
            entries.keys
                .filter { path ->
                    path.endsWith(".xhtml", ignoreCase = true) ||
                        path.endsWith(".html", ignoreCase = true) ||
                        path.endsWith(".htm", ignoreCase = true)
                }
                .filterNot { path ->
                    path.contains("nav", ignoreCase = true) || path.contains("cover", ignoreCase = true)
                }
                .sorted()
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

    private fun manifestItems(opf: String): Map<String, String> {
        return Regex("""<item\b[^>]*>""", RegexOption.IGNORE_CASE)
            .findAll(opf)
            .mapNotNull { match ->
                val tag = match.value
                val id = tag.attribute("id")
                val href = tag.attribute("href")
                if (id != null && href != null) id to href else null
            }
            .toMap()
    }

    private fun spineIdRefs(opf: String): List<String> {
        return Regex("""<itemref\b[^>]*>""", RegexOption.IGNORE_CASE)
            .findAll(opf)
            .mapNotNull { match -> match.value.attribute("idref") }
            .toList()
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
            .replaceInlineStyleTags("strong|b", "**")
            .replaceInlineStyleTags("em|i|cite", "_")
            .replaceInlineStyleTags("code|kbd|samp", "`")
            .replaceHeadings()
            .replaceBlockquotes()
            .replaceListItems()
            .replace(Regex("""</(p|div|section|article|aside|header|footer|main|figure|figcaption|ul|ol|dl|table|tbody|thead|tr)>""", RegexOption.IGNORE_CASE), "\n\n")
            .replace(Regex("""<[^>]+>"""), " ")
            .decodeXmlEntities()
            .normalizeReaderMarkdown()
    }

    private fun String.replaceHeadings(): String {
        return Regex("""<h([1-6])\b[^>]*>([\s\S]*?)</h\1>""", RegexOption.IGNORE_CASE)
            .replace(this) { match ->
                val level = match.groupValues[1].toIntOrNull()?.coerceIn(1, 6) ?: 1
                val text = match.groupValues[2].stripTagsToInlineText()
                "\n\n${"#".repeat(level)} $text\n\n"
            }
    }

    private fun String.replaceBlockquotes(): String {
        return Regex("""<blockquote\b[^>]*>([\s\S]*?)</blockquote>""", RegexOption.IGNORE_CASE)
            .replace(this) { match ->
                val quote = match.groupValues[1]
                    .replace(Regex("""</(p|div|li)>""", RegexOption.IGNORE_CASE), "\n")
                    .stripTagsToInlineText(preserveLineBreaks = true)
                    .lines()
                    .map { line -> line.trim() }
                    .filter(String::isNotBlank)
                    .joinToString("\n") { line -> "> $line" }
                "\n\n$quote\n\n"
            }
    }

    private fun String.replaceListItems(): String {
        return Regex("""<li\b[^>]*>([\s\S]*?)</li>""", RegexOption.IGNORE_CASE)
            .replace(this) { match ->
                val text = match.groupValues[1].stripTagsToInlineText()
                "\n- $text"
            }
    }

    private fun String.replaceInlineStyleTags(tagNames: String, marker: String): String {
        val pattern = Regex("""<($tagNames)\b[^>]*>([\s\S]*?)</\1>""", RegexOption.IGNORE_CASE)
        var rendered = this
        while (true) {
            val replaced = pattern.replace(rendered) { match ->
                val text = match.groupValues[2].stripTagsToInlineText()
                if (text.isBlank()) "" else "$marker$text$marker"
            }
            if (replaced == rendered) {
                return rendered
            }
            rendered = replaced
        }
    }

    private fun String.stripTagsToInlineText(preserveLineBreaks: Boolean = false): String {
        val withoutBlocks = if (preserveLineBreaks) {
            replace(Regex("""<(br|/p|/div|/li)\b[^>]*>""", RegexOption.IGNORE_CASE), "\n")
        } else {
            this
        }
        return withoutBlocks
            .replace(Regex("""<[^>]+>"""), " ")
            .decodeXmlEntities()
            .let { text ->
                if (preserveLineBreaks) {
                    text.lines()
                        .joinToString("\n") { line -> line.replace(Regex("""[ \t\r\f]+"""), " ").trim() }
                } else {
                    text.replace(Regex("""\s+"""), " ").trim()
                }
            }
            .trim()
    }

    private fun String.normalizeReaderMarkdown(): String {
        return lines()
            .map { line -> line.replace(Regex("""[ \t\r\f]+"""), " ").trim() }
            .joinToString("\n")
            .replace(Regex("""\n{3,}"""), "\n\n")
            .split(Regex("""\n\s*\n"""))
            .map { block ->
                block.lines()
                    .map(String::trim)
                    .filter(String::isNotBlank)
                    .joinToString("\n")
                    .trim()
            }
            .filter(String::isNotBlank)
            .joinToString(separator = "\n\n")
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
                match.groupValues[1].toIntOrNull()?.toChar()?.toString() ?: match.value
            }
            .replace(Regex("""&#x([0-9a-fA-F]+);""")) { match ->
                match.groupValues[1].toIntOrNull(radix = 16)?.toChar()?.toString() ?: match.value
            }
    }
}
