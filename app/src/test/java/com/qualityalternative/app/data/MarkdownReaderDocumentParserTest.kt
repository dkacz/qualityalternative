package com.qualityalternative.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MarkdownReaderDocumentParserTest {
    @Test
    fun parseCreatesImageBlocksForStandaloneMarkdownImages() {
        val document = MarkdownReaderDocumentParser.parse(
            markdown = """
                # Intro

                Lead paragraph.
                ![Calm chart](images/chart.png "Evening chart")
                Follow-up paragraph.
            """.trimIndent(),
            baseUri = "file:///tmp/books/session-notes.md",
        )

        assertEquals(4, document.blocks.size)
        assertEquals("# Intro", document.blocks[0].text)
        assertEquals("Lead paragraph.", document.blocks[1].text)
        assertEquals("Calm chart", document.blocks[2].text)
        assertEquals("file:/tmp/books/images/chart.png", document.blocks[2].image?.source)
        assertEquals("Calm chart", document.blocks[2].image?.altText)
        assertEquals("Evening chart", document.blocks[2].image?.title)
        assertEquals("Follow-up paragraph.", document.blocks[3].text)
        assertEquals(listOf(0, 1, 2, 3), document.blocks.map { block -> block.sourceBlockIndex })
    }

    @Test
    fun parseKeepsDataUriImageSourcesForSelfContainedMarkdown() {
        val document = MarkdownReaderDocumentParser.parse(
            markdown = "![Pixel](data:image/png;base64,iVBORw0KGgo=)",
        )

        val image = document.blocks.single().image
        assertNotNull(image)
        assertEquals("data:image/png;base64,iVBORw0KGgo=", image?.source)
        assertEquals("Pixel", document.blocks.single().text)
    }

    @Test
    fun parseKeepsDataUriImageSourcesWhenMarkdownImageHasTitle() {
        val document = MarkdownReaderDocumentParser.parse(
            markdown = """![Pixel](data:image/png;base64,iVBORw0KGgo= "Pixel title")""",
        )

        val image = document.blocks.single().image
        assertNotNull(image)
        assertEquals("data:image/png;base64,iVBORw0KGgo=", image?.source)
        assertEquals("Pixel title", image?.title)
    }

    @Test
    fun parseResolvesRelativeImagesFromPickedAttachmentMap() {
        val document = MarkdownReaderDocumentParser.parse(
            markdown = "![Cover](assets/cover.PNG)",
            baseUri = "content://provider/document/book.md",
            imageAttachmentUris = mapOf("cover.png" to "content://provider/document/cover-image"),
        )

        assertEquals("content://provider/document/cover-image", document.blocks.single().image?.source)
    }

    @Test
    fun parseBlocksUnreviewedLocalImagesWhenFallbackDisabled() {
        val document = MarkdownReaderDocumentParser.parse(
            markdown = """
                ![Outside](../outside.png)

                ![Absolute](/tmp/outside.png)

                ![File](file:///tmp/outside.png)

                ![Cover](cover.png)

                ![Pixel](data:image/png;base64,iVBORw0KGgo=)
            """.trimIndent(),
            baseUri = "file:///tmp/books/session-notes.md",
            imageAttachmentUris = mapOf("cover.png" to "content://provider/document/cover-image"),
            allowLocalImageFallback = false,
        )

        assertEquals("", document.blocks[0].image?.source)
        assertEquals("", document.blocks[1].image?.source)
        assertEquals("", document.blocks[2].image?.source)
        assertEquals("content://provider/document/cover-image", document.blocks[3].image?.source)
        assertEquals("data:image/png;base64,iVBORw0KGgo=", document.blocks[4].image?.source)
    }

    @Test
    fun parseReplacesInlineMarkdownImagesWithAltTextInsideTextBlocks() {
        val document = MarkdownReaderDocumentParser.parse(
            markdown = "See ![diagram](diagram.png) before reading the next section.",
        )

        assertEquals("See diagram before reading the next section.", document.blocks.single().text)
        assertEquals(null, document.blocks.single().image)
    }

    @Test
    fun parseCreatesStructuredBlocksForPipeMarkdownTables() {
        val document = MarkdownReaderDocumentParser.parse(
            markdown = """
                Before table.
                | Habit | Minutes | Notes |
                |:------|--------:|:-----:|
                | Read  | 20      | Calm  |
                | Walk  | 10      | Body  |
                After table.
            """.trimIndent(),
        )

        assertEquals(3, document.blocks.size)
        assertEquals("Before table.", document.blocks[0].text)
        val tableBlock = document.blocks[1]
        assertEquals("Habit\tMinutes\tNotes\nRead\t20\tCalm\nWalk\t10\tBody", tableBlock.text)
        assertEquals(listOf("Habit", "Minutes", "Notes"), tableBlock.table?.headers)
        assertEquals(listOf(listOf("Read", "20", "Calm"), listOf("Walk", "10", "Body")), tableBlock.table?.rows)
        assertEquals("After table.", document.blocks[2].text)
    }
}
