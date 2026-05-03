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
                .mapNotNull { raw ->
                    raw.trim()
                        .takeIf(String::isNotBlank)
                        ?.let { blockText -> ReaderDocumentBlock(text = blockText) }
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
)

data class ReaderTocEntry(
    val title: String,
    val href: String,
    val sourceHref: String,
    val anchor: String? = null,
    val blockIndex: Int,
    val level: Int = 0,
)
