package com.qualityalternative.app.ui

import com.qualityalternative.app.data.ReadingTimeEstimateSource
import com.qualityalternative.app.domain.model.ContentFormat
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertEquals
import org.junit.Test

class DocumentImportCandidateFactoryTest {
    @Test
    fun pickedMarkdownCandidateUsesExtractedEstimateForShortNormalAndVeryLongFiles() {
        assertCandidateEstimate("short.md", "text/markdown", markdownBytes(100), ContentFormat.MARKDOWN, 3, 100)
        assertCandidateEstimate("normal.markdown", "text/markdown", markdownBytes(1_125), ContentFormat.MARKDOWN, 5, 1_125)
        assertCandidateEstimate("long.md", "text/markdown", markdownBytes(10_000), ContentFormat.MARKDOWN, 45, 10_000)
        assertCandidateEstimate("huge.md", "text/markdown", markdownBytes(220_000), ContentFormat.MARKDOWN, 720, 220_000)
    }

    @Test
    fun pickedEpubCandidateUsesExtractedEstimateForShortNormalAndVeryLongFiles() {
        assertCandidateEstimate("short.epub", "application/epub+zip", epubBytes(100), ContentFormat.EPUB, 3, 100)
        assertCandidateEstimate("normal.epub", "application/epub+zip", epubBytes(1_125), ContentFormat.EPUB, 5, 1_125)
        assertCandidateEstimate("long.epub", "application/epub+zip", epubBytes(10_000), ContentFormat.EPUB, 45, 10_000)
        assertCandidateEstimate("huge.epub", "application/epub+zip", epubBytes(220_000), ContentFormat.EPUB, 720, 220_000)
    }

    @Test
    fun pickedPdfAndUnsupportedCandidatesUseDefaultsWithoutWordCounts() {
        val pdf = candidate("book.pdf", "application/pdf", markdownBytes(10_000))
        val unsupported = candidate("archive.zip", "application/zip", markdownBytes(10_000))

        assertEquals(ContentFormat.PDF, pdf.format)
        assertEquals("10", pdf.durationMinutes)
        assertEquals(null, pdf.estimatedWordCount)
        assertEquals(ReadingTimeEstimateSource.PDF_DEFAULT, pdf.estimateSource)
        assertEquals(null, unsupported.format)
        assertEquals("10", unsupported.durationMinutes)
        assertEquals(null, unsupported.estimatedWordCount)
        assertEquals(ReadingTimeEstimateSource.FALLBACK_DEFAULT, unsupported.estimateSource)
    }

    @Test
    fun markdownImageAttachmentsAreMappedToMarkdownCandidateAndFilteredFromStandaloneImports() {
        val markdown = candidate("notes.md", "text/markdown", markdownBytes(200))
        val image = candidate("cover.PNG", "image/png", ByteArray(0))
        val epub = candidate("book.epub", "application/epub+zip", epubBytes(200))

        val prepared = listOf(markdown, image, epub).withMarkdownImageAttachments()

        assertEquals(listOf("notes.md", "book.epub"), prepared.map(DocumentImportCandidate::displayName))
        assertEquals(
            mapOf(
                "cover.PNG" to "content://quality/cover.PNG",
                "cover.png" to "content://quality/cover.PNG",
            ),
            prepared.first { it.displayName == "notes.md" }.imageAttachmentUris,
        )
        assertEquals(emptyMap<String, String>(), prepared.first { it.displayName == "book.epub" }.imageAttachmentUris)
    }

    @Test
    fun imageOnlySelectionMergesIntoExistingMarkdownCandidate() {
        val existingMarkdown = candidate("notes.md", "text/markdown", markdownBytes(200))
            .copy(title = "Edited notes")
        val image = candidate("diagram.webp", "image/webp", ByteArray(0))

        val prepared = listOf(image).withMarkdownImageAttachments(baseCandidates = listOf(existingMarkdown))

        assertEquals(listOf("notes.md"), prepared.map(DocumentImportCandidate::displayName))
        assertEquals("Edited notes", prepared.single().title)
        assertEquals(
            mapOf(
                "diagram.webp" to "content://quality/diagram.webp",
            ),
            prepared.single().imageAttachmentUris,
        )
    }

    private fun assertCandidateEstimate(
        displayName: String,
        mimeType: String,
        bytes: ByteArray,
        expectedFormat: ContentFormat,
        expectedMinutes: Int,
        expectedWords: Int,
    ) {
        val candidate = candidate(displayName, mimeType, bytes)

        assertEquals(expectedFormat, candidate.format)
        assertEquals(displayName.substringBefore('.'), candidate.title)
        assertEquals(expectedMinutes.toString(), candidate.durationMinutes)
        assertEquals(expectedWords, candidate.estimatedWordCount)
        assertEquals(ReadingTimeEstimateSource.EXTRACTED_TEXT, candidate.estimateSource)
    }

    private fun candidate(displayName: String, mimeType: String, bytes: ByteArray): DocumentImportCandidate {
        return DocumentImportCandidateFactory.fromPickedDocument(
            uri = "content://quality/$displayName",
            displayName = displayName,
            mimeType = mimeType,
        ) {
            ByteArrayInputStream(bytes)
        }
    }

    private fun markdownBytes(wordCount: Int): ByteArray {
        return words(wordCount).toByteArray(Charsets.UTF_8)
    }

    private fun epubBytes(wordCount: Int): ByteArray {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            listOf(
                "META-INF/container.xml" to """
                    <container><rootfiles><rootfile full-path="OPS/package.opf"/></rootfiles></container>
                """.trimIndent(),
                "OPS/package.opf" to """
                    <package>
                      <manifest>
                        <item id="chapter" href="chapter.xhtml" media-type="application/xhtml+xml"/>
                      </manifest>
                      <spine>
                        <itemref idref="chapter"/>
                      </spine>
                    </package>
                """.trimIndent(),
                "OPS/chapter.xhtml" to "<html><body><p>${words(wordCount)}</p></body></html>",
            ).forEach { (name, body) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(body.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
        }
        return output.toByteArray()
    }

    private fun words(count: Int): String {
        return List(count) { index -> "word$index" }.joinToString(" ")
    }
}
