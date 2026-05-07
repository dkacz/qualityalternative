package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.ReaderDocument
import com.qualityalternative.app.domain.model.ReaderDocumentBlock
import com.qualityalternative.app.domain.model.ReaderTocEntry
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.StringReader
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource

object EpubTextExtractor {
    private const val semanticIndentSentinel = "\uE000"

    fun extract(input: InputStream): String = extractDocument(input).plainText

    fun extractDocument(input: InputStream): ReaderDocument {
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
        val parsedDocuments = readablePaths.mapNotNull { path ->
            entries[path]
                ?.toString(Charsets.UTF_8)
                ?.let { html -> htmlToReaderDocumentBlocks(html = html, sourceHref = path) }
        }.withGlobalSourceBlockIndexes()
        val blocks = parsedDocuments.flatMap(ParsedSpineDocument::blocks)
        if (blocks.isEmpty()) {
            throw IllegalArgumentException("EPUB contains no readable text")
        }
        val sourceFirstBlockIndexes = firstBlockIndexesBySource(blocks)
        val anchorBlockIndexes = anchorBlockIndexesBySource(parsedDocuments)
        return ReaderDocument(
            blocks = blocks,
            tableOfContents = epubTableOfContents(
                entries = entries,
                manifestItems = manifestItems,
                opf = opf,
                opfBaseDir = baseDir,
                sourceFirstBlockIndexes = sourceFirstBlockIndexes,
                anchorBlockIndexes = anchorBlockIndexes,
                blocks = blocks,
            ),
        )
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

    private data class ParsedSpineDocument(
        val sourceHref: String,
        val blocks: List<ReaderDocumentBlock>,
        val anchorBlockIndexes: Map<String, Int>,
    )

    private data class AnchorPosition(
        val anchor: String,
        val blockIndex: Int,
    )

    private data class HrefTarget(
        val href: String,
        val sourceHref: String,
        val anchor: String?,
    )

    private data class RawTocEntry(
        val title: String,
        val href: String,
        val level: Int,
        val basePath: String,
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

    private fun spineTocId(opf: String): String? {
        return Regex("""<spine\b[^>]*\btoc\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE)
            .find(opf)
            ?.groupValues
            ?.getOrNull(1)
    }

    private fun htmlToReaderDocumentBlocks(html: String, sourceHref: String): ParsedSpineDocument {
        val body = bodyFragment(html)
        val blockTexts = htmlFragmentToReadableText(body).toReaderBlockTexts()
        val lastBlockIndex = (blockTexts.size - 1).coerceAtLeast(0)
        val anchors = anchorsInBody(body = body)
            .map { anchor -> anchor.copy(blockIndex = anchor.blockIndex.coerceIn(0, lastBlockIndex)) }
        val anchorsByBlock = anchors.groupBy(AnchorPosition::blockIndex)
        val anchorBlockIndexes = anchors.associate { anchor -> anchor.anchor to anchor.blockIndex }
        var currentAnchor: String? = null
        val blocks = blockTexts.mapIndexed { index, text ->
            anchorsByBlock[index]
                ?.lastOrNull()
                ?.let { anchor -> currentAnchor = anchor.anchor }
            ReaderDocumentBlock(
                text = text,
                sourceHref = sourceHref,
                anchor = currentAnchor,
                sourceBlockIndex = index,
            )
        }
        return ParsedSpineDocument(
            sourceHref = sourceHref,
            blocks = blocks,
            anchorBlockIndexes = anchorBlockIndexes,
        )
    }

    private fun anchorsInBody(body: String): List<AnchorPosition> {
        return Regex("""<[^>]+\b(?:id|name)\s*=\s*["']([^"']+)["'][^>]*>""", RegexOption.IGNORE_CASE)
            .findAll(body)
            .mapNotNull { match ->
                val anchor = match.groupValues.getOrNull(1)?.trim()?.takeIf(String::isNotBlank)
                val blockIndex = htmlFragmentToReadableText(body.substring(0, match.range.first))
                    .toReaderBlockTexts()
                    .size
                anchor?.let { AnchorPosition(anchor = it, blockIndex = blockIndex) }
            }
            .toList()
    }

    private fun firstBlockIndexesBySource(blocks: List<ReaderDocumentBlock>): Map<String, Int> {
        return blocks
            .mapIndexedNotNull { index, block -> block.sourceHref?.let { sourceHref -> sourceHref to index } }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, indexes) -> indexes.minOrNull() ?: 0 }
    }

    private fun List<ParsedSpineDocument>.withGlobalSourceBlockIndexes(): List<ParsedSpineDocument> {
        var globalOffset = 0
        return map { document ->
            val reindexedBlocks = document.blocks.mapIndexed { localIndex, block ->
                block.copy(sourceBlockIndex = globalOffset + localIndex)
            }
            val reindexedAnchors = document.anchorBlockIndexes.mapValues { (_, localIndex) ->
                globalOffset + localIndex
            }
            globalOffset += document.blocks.size
            document.copy(blocks = reindexedBlocks, anchorBlockIndexes = reindexedAnchors)
        }
    }

    private fun anchorBlockIndexesBySource(parsedDocuments: List<ParsedSpineDocument>): Map<String, Int> {
        return parsedDocuments
            .flatMap { document ->
                document.anchorBlockIndexes.map { (anchor, globalIndex) ->
                    "${document.sourceHref}#$anchor" to globalIndex
                }
            }
            .toMap()
    }

    private fun epubTableOfContents(
        entries: Map<String, ByteArray>,
        manifestItems: Map<String, ManifestItem>,
        opf: String,
        opfBaseDir: String,
        sourceFirstBlockIndexes: Map<String, Int>,
        anchorBlockIndexes: Map<String, Int>,
        blocks: List<ReaderDocumentBlock>,
    ): List<ReaderTocEntry> {
        val navItem = manifestItems.values.firstOrNull { item -> "nav" in item.properties }
        val navEntries = navItem
            ?.let { item -> normalizePath(opfBaseDir, item.href) }
            ?.let { navPath ->
                entries[navPath]
                    ?.toString(Charsets.UTF_8)
                    ?.let { navXml -> parseEpub3NavToc(navXml = navXml, navPath = navPath) }
            }
            .orEmpty()
        val rawEntries = navEntries.ifEmpty {
            val tocId = spineTocId(opf)
            val ncxItem = tocId?.let(manifestItems::get)
                ?: manifestItems.values.firstOrNull { item ->
                    item.mediaType?.equals("application/x-dtbncx+xml", ignoreCase = true) == true ||
                        item.href.endsWith(".ncx", ignoreCase = true)
                }
            ncxItem
                ?.let { item -> normalizePath(opfBaseDir, item.href) }
                ?.let { ncxPath ->
                    entries[ncxPath]
                        ?.toString(Charsets.UTF_8)
                        ?.let { ncxXml -> parseEpub2NcxToc(ncxXml = ncxXml, ncxPath = ncxPath) }
                }
                .orEmpty()
        }
        return rawEntries
            .mapNotNull { raw ->
                val target = resolveHref(basePath = raw.basePath, href = raw.href)
                val blockIndex = blockIndexForHref(
                    target = target,
                    sourceFirstBlockIndexes = sourceFirstBlockIndexes,
                    anchorBlockIndexes = anchorBlockIndexes,
                )
                blockIndex?.let {
                    ReaderTocEntry(
                        title = raw.title,
                        href = target.href,
                        sourceHref = target.sourceHref,
                        anchor = target.anchor,
                        blockIndex = it,
                        level = raw.level,
                    )
                }
            }
            .ifEmpty { fallbackTocEntries(blocks = blocks) }
    }

    private fun parseEpub3NavToc(navXml: String, navPath: String): List<RawTocEntry> {
        val document = parseXml(navXml) ?: return emptyList()
        val nav = document
            .elementsByTag("nav")
            .firstOrNull { element ->
                val type = element.attributeValue("epub:type").ifBlank { element.attributeValue("type") }
                type.split(Regex("""\s+""")).any { token -> token.equals("toc", ignoreCase = true) }
            }
            ?: document.elementsByTag("nav").firstOrNull()
            ?: return emptyList()
        return nav.directChildren("ol")
            .flatMap { ol -> ol.navListEntries(level = 0, basePath = navPath) }
    }

    private fun Element.navListEntries(level: Int, basePath: String): List<RawTocEntry> {
        return directChildren("li").flatMap { item ->
            val link = item.directChildren("a").firstOrNull()
            val label = link ?: item.directChildren("span").firstOrNull()
            val current = link?.attributeValue("href")
                ?.takeIf(String::isNotBlank)
                ?.let { href ->
                    RawTocEntry(
                        title = label?.textContent?.cleanTocTitle().orEmpty(),
                        href = href,
                        level = level,
                        basePath = basePath,
                    )
                }
                ?.takeIf { entry -> entry.title.isNotBlank() }
            val nested = item.directChildren("ol")
                .flatMap { ol -> ol.navListEntries(level = level + 1, basePath = basePath) }
            listOfNotNull(current) + nested
        }
    }

    private fun parseEpub2NcxToc(ncxXml: String, ncxPath: String): List<RawTocEntry> {
        val document = parseXml(ncxXml) ?: return emptyList()
        val navMap = document.elementsByTag("navMap").firstOrNull() ?: return emptyList()
        return navMap.directChildren("navPoint")
            .flatMap { point -> point.ncxNavPointEntries(level = 0, basePath = ncxPath) }
    }

    private fun Element.ncxNavPointEntries(level: Int, basePath: String): List<RawTocEntry> {
        val label = descendantsByTag("navLabel")
            .firstOrNull()
            ?.descendantsByTag("text")
            ?.firstOrNull()
            ?.textContent
            ?.cleanTocTitle()
            .orEmpty()
        val href = descendantsByTag("content")
            .firstOrNull()
            ?.attributeValue("src")
        val current = href
            ?.takeIf(String::isNotBlank)
            ?.let { src ->
                RawTocEntry(
                    title = label,
                    href = src,
                    level = level,
                    basePath = basePath,
                )
            }
            ?.takeIf { entry -> entry.title.isNotBlank() }
        val nested = directChildren("navPoint")
            .flatMap { point -> point.ncxNavPointEntries(level = level + 1, basePath = basePath) }
        return listOfNotNull(current) + nested
    }

    private fun fallbackTocEntries(blocks: List<ReaderDocumentBlock>): List<ReaderTocEntry> {
        return blocks
            .mapIndexedNotNull { index, block ->
                val heading = Regex("""^(#{1,6})\s+(.+)$""")
                    .matchEntire(block.text.trim())
                    ?: return@mapIndexedNotNull null
                val sourceHref = block.sourceHref ?: return@mapIndexedNotNull null
                val anchor = block.anchor
                ReaderTocEntry(
                    title = heading.groupValues[2].trim(),
                    href = sourceHref + anchor.orEmptyFragment(),
                    sourceHref = sourceHref,
                    anchor = anchor,
                    blockIndex = index,
                    level = heading.groupValues[1].length - 1,
                )
            }
            .ifEmpty {
                blocks.firstOrNull()?.sourceHref?.let { sourceHref ->
                    listOf(
                        ReaderTocEntry(
                            title = "Start",
                            href = sourceHref,
                            sourceHref = sourceHref,
                            blockIndex = 0,
                        ),
                    )
                }.orEmpty()
            }
    }

    private fun blockIndexForHref(
        target: HrefTarget,
        sourceFirstBlockIndexes: Map<String, Int>,
        anchorBlockIndexes: Map<String, Int>,
    ): Int? {
        return target.anchor
            ?.let { anchor -> anchorBlockIndexes["${target.sourceHref}#$anchor"] }
            ?: sourceFirstBlockIndexes[target.sourceHref]
    }

    private fun resolveHref(basePath: String, href: String): HrefTarget {
        val withoutQuery = href.substringBefore('?')
        val rawPath = withoutQuery.substringBefore('#')
        val anchor = withoutQuery.substringAfter('#', missingDelimiterValue = "")
            .takeIf(String::isNotBlank)
        val sourceHref = if (rawPath.isBlank()) {
            basePath.substringBefore('#')
        } else {
            normalizePath(basePath.substringBeforeLast('/', missingDelimiterValue = ""), rawPath)
        }
        return HrefTarget(
            href = sourceHref + anchor.orEmptyFragment(),
            sourceHref = sourceHref,
            anchor = anchor,
        )
    }

    private fun String?.orEmptyFragment(): String {
        return this?.takeIf(String::isNotBlank)?.let { anchor -> "#$anchor" }.orEmpty()
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

    private fun parseXml(xml: String): org.w3c.dom.Document? {
        return runCatching {
            val factory = DocumentBuilderFactory.newInstance().apply {
                isNamespaceAware = false
                runCatching { setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false) }
                runCatching { setFeature("http://xml.org/sax/features/external-general-entities", false) }
                runCatching { setFeature("http://xml.org/sax/features/external-parameter-entities", false) }
            }
            factory.newDocumentBuilder().parse(InputSource(StringReader(xml)))
        }.getOrNull()
    }

    private fun org.w3c.dom.Document.elementsByTag(name: String): List<Element> {
        return documentElement?.descendantsByTag(name).orEmpty()
    }

    private fun Element.descendantsByTag(name: String): List<Element> {
        val matches = mutableListOf<Element>()
        fun visit(node: Node) {
            if (node is Element) {
                if (node.localTagName().equals(name, ignoreCase = true)) {
                    matches += node
                }
                node.childNodes.asList().forEach(::visit)
            }
        }
        visit(this)
        return matches
    }

    private fun Element.directChildren(name: String): List<Element> {
        return childNodes.asList()
            .filterIsInstance<Element>()
            .filter { element -> element.localTagName().equals(name, ignoreCase = true) }
    }

    private fun Element.attributeValue(name: String): String {
        if (hasAttribute(name)) {
            return getAttribute(name).trim()
        }
        val localName = name.substringAfter(':')
        return attributes.asList()
            .firstOrNull { attribute ->
                attribute.nodeName.equals(name, ignoreCase = true) ||
                    attribute.nodeName.substringAfter(':').equals(localName, ignoreCase = true)
            }
            ?.nodeValue
            ?.trim()
            .orEmpty()
    }

    private fun Element.localTagName(): String = (localName ?: tagName).substringAfter(':')

    private fun org.w3c.dom.NodeList.asList(): List<Node> {
        return (0 until length).mapNotNull(::item)
    }

    private fun org.w3c.dom.NamedNodeMap.asList(): List<Node> {
        return (0 until length).mapNotNull(::item)
    }

    private fun String.cleanTocTitle(): String {
        return replace(Regex("""\s+"""), " ").trim()
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
        return htmlFragmentToReadableText(bodyFragment(html))
    }

    private fun bodyFragment(html: String): String {
        return Regex("""<body\b[^>]*>([\s\S]*?)</body>""", RegexOption.IGNORE_CASE)
            .find(html)
            ?.groupValues
            ?.getOrNull(1)
            ?: html
    }

    private fun htmlFragmentToReadableText(fragment: String): String {
        return fragment
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

    private fun String.toReaderBlockTexts(): List<String> {
        return trim()
            .split(Regex("""\n[ \t\r\f]*\n"""))
            .map { block -> block.trim() }
            .filter(String::isNotBlank)
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
