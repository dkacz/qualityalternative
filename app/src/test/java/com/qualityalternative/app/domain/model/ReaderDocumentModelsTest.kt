package com.qualityalternative.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ReaderDocumentModelsTest {
    @Test
    fun fromPlainTextKeepsStableSourceBlockIndexes() {
        val document = ReaderDocument.fromPlainText(
            """
                First paragraph.

                Second paragraph.

                Third paragraph.
            """.trimIndent(),
        )

        assertEquals(listOf(0, 1, 2), document.blocks.map { block -> block.sourceBlockIndex })
    }
}
