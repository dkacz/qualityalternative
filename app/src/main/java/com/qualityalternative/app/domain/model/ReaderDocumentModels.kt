package com.qualityalternative.app.domain.model

data class ReaderDocument(
    val blocks: List<ReaderDocumentBlock>,
    val tableOfContents: List<ReaderTocEntry> = emptyList(),
) {
    val plainText: String
        get() = blocks.joinToString(separator = "\n\n") { block -> block.text }.trim()

    companion object {
        fun fromPlainText(text: String): ReaderDocument {
            val blocks = text
                .split(Regex("""\n[ \t\r\f]*\n"""))
                .mapIndexedNotNull { index, raw ->
                    raw.trim()
                        .takeIf(String::isNotBlank)
                        ?.let { blockText -> ReaderDocumentBlock(text = blockText, sourceBlockIndex = index) }
                }
            return ReaderDocument(blocks = blocks)
        }
    }
}

data class ReaderDocumentBlock(
    val text: String,
    val sourceHref: String? = null,
    val anchor: String? = null,
    val sourceBlockIndex: Int = 0,
    val image: ReaderDocumentImage? = null,
    val table: ReaderDocumentTable? = null,
)

data class ReaderDocumentImage(
    val source: String,
    val altText: String = "",
    val title: String? = null,
)

data class ReaderDocumentTable(
    val headers: List<String>,
    val rows: List<List<String>>,
    val alignments: List<ReaderDocumentTableAlignment> = emptyList(),
)

enum class ReaderDocumentTableAlignment {
    START,
    CENTER,
    END,
}

data class ReaderTocEntry(
    val title: String,
    val href: String,
    val sourceHref: String,
    val anchor: String? = null,
    val blockIndex: Int,
    val level: Int = 0,
)
