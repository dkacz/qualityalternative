package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.ContentFormat
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertEquals
import org.junit.Test

class DocumentReadingTimeEstimatorTest {
    @Test
    fun markdownImportPathEstimatesShortNormalAndVeryLongText() {
        assertDocumentEstimate(ContentFormat.MARKDOWN, markdownBytes(100), expectedMinutes = 3, expectedWords = 100)
        assertDocumentEstimate(ContentFormat.MARKDOWN, markdownBytes(1_125), expectedMinutes = 5, expectedWords = 1_125)
        assertDocumentEstimate(ContentFormat.MARKDOWN, markdownBytes(10_000), expectedMinutes = 20, expectedWords = 10_000)
    }

    @Test
    fun epubImportPathEstimatesShortNormalAndVeryLongExtractedText() {
        assertDocumentEstimate(ContentFormat.EPUB, epubBytes(100), expectedMinutes = 3, expectedWords = 100)
        assertDocumentEstimate(ContentFormat.EPUB, epubBytes(1_125), expectedMinutes = 5, expectedWords = 1_125)
        assertDocumentEstimate(ContentFormat.EPUB, epubBytes(10_000), expectedMinutes = 20, expectedWords = 10_000)
    }

    @Test
    fun pdfAndUnsupportedImportPathsUseDefaultsWithoutExtraction() {
        val pdfEstimate = DocumentReadingTimeEstimator.estimate(ContentFormat.PDF) {
            ByteArrayInputStream(markdownBytes(10_000))
        }
        val fallbackEstimate = DocumentReadingTimeEstimator.estimate(null) {
            ByteArrayInputStream(markdownBytes(10_000))
        }

        assertEquals(10, pdfEstimate.minutes)
        assertEquals(null, pdfEstimate.wordCount)
        assertEquals(ReadingTimeEstimateSource.PDF_DEFAULT, pdfEstimate.source)
        assertEquals(10, fallbackEstimate.minutes)
        assertEquals(null, fallbackEstimate.wordCount)
        assertEquals(ReadingTimeEstimateSource.FALLBACK_DEFAULT, fallbackEstimate.source)
    }

    private fun assertDocumentEstimate(
        format: ContentFormat,
        bytes: ByteArray,
        expectedMinutes: Int,
        expectedWords: Int,
    ) {
        val estimate = DocumentReadingTimeEstimator.estimate(format) {
            ByteArrayInputStream(bytes)
        }

        assertEquals(expectedMinutes, estimate.minutes)
        assertEquals(expectedWords, estimate.wordCount)
        assertEquals(ReadingTimeEstimateSource.EXTRACTED_TEXT, estimate.source)
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
