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
            .replace(Regex("""<(br|hr)\b[^>]*>""", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("""</(p|div|section|article|blockquote|h[1-6]|li|tr)>""", RegexOption.IGNORE_CASE), "\n\n")
            .replace(Regex("""<li\b[^>]*>""", RegexOption.IGNORE_CASE), "\n- ")
            .replace(Regex("""<[^>]+>"""), " ")
            .decodeXmlEntities()
            .lines()
            .map { line -> line.replace(Regex("""\s+"""), " ").trim() }
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
            .replace(Regex("""&#(\d+);""")) { match ->
                match.groupValues[1].toIntOrNull()?.toChar()?.toString() ?: match.value
            }
    }
}
