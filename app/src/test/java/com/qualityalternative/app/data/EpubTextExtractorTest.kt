package com.qualityalternative.app.data

import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.qualityalternative.app.ui.ReaderMarkdownBlockKind
import com.qualityalternative.app.ui.readerBlocksForDisplay
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EpubTextExtractorTest {
    @Test
    fun extractReadsSpineOrderedXhtmlAsFiniteReaderText() {
        val epub = epubBytes(
            "META-INF/container.xml" to """
                <?xml version="1.0"?>
                <container>
                  <rootfiles>
                    <rootfile full-path="OPS/package.opf" media-type="application/oebps-package+xml"/>
                  </rootfiles>
                </container>
            """.trimIndent(),
            "OPS/package.opf" to """
                <package>
                  <manifest>
                    <item id="chapter-one" href="chapter1.xhtml" media-type="application/xhtml+xml"/>
                    <item id="chapter-two" href="chapters/chapter2.xhtml" media-type="application/xhtml+xml"/>
                  </manifest>
                  <spine>
                    <itemref idref="chapter-one"/>
                    <itemref idref="chapter-two"/>
                  </spine>
                </package>
            """.trimIndent(),
            "OPS/chapter1.xhtml" to """
                <html><body><h1>First</h1><p>Begin &amp; notice.</p></body></html>
            """.trimIndent(),
            "OPS/chapters/chapter2.xhtml" to """
                <html><body><p>Second chapter.</p><ul><li>One breath</li><li>One page</li></ul></body></html>
            """.trimIndent(),
        )

        val text = EpubTextExtractor.extract(ByteArrayInputStream(epub))

        assertTrue(text.contains("# First"))
        assertTrue(text.contains("Begin & notice."))
        assertTrue(text.contains("Second chapter."))
        assertTrue(text.indexOf("Begin & notice.") < text.indexOf("Second chapter."))
        assertTrue(text.contains("- One breath"))
        assertTrue(text.contains("- One page"))
    }

    @Test
    fun extractPreservesEpubStructureAsReaderMarkdown() {
        val epub = epubBytes(
            "META-INF/container.xml" to """
                <?xml version="1.0"?>
                <container>
                  <rootfiles>
                    <rootfile full-path="book/package.opf" media-type="application/oebps-package+xml"/>
                  </rootfiles>
                </container>
            """.trimIndent(),
            "book/package.opf" to """
                <package>
                  <manifest>
                    <item id="chapter" href="chapter.xhtml" media-type="application/xhtml+xml"/>
                  </manifest>
                  <spine>
                    <itemref idref="chapter"/>
                  </spine>
                </package>
            """.trimIndent(),
            "book/chapter.xhtml" to """
                <html><body>
                  <h1>A Better Chapter</h1>
                  <p>This keeps <strong>bold</strong>, <em>italic</em>, and <code>inline code</code> markers.</p>
                  <blockquote><p>Attention returns when the paragraph slows down.</p></blockquote>
                  <ol><li>First page</li><li>Second page &mdash; not a feed</li></ol>
                </body></html>
            """.trimIndent(),
        )

        val text = EpubTextExtractor.extract(ByteArrayInputStream(epub))

        assertEquals(
            """
            # A Better Chapter

            This keeps **bold**, _italic_, and `inline code` markers.

            > Attention returns when the paragraph slows down.

            1. First page
            2. Second page - not a feed
            """.trimIndent(),
            text,
        )
    }

    @Test
    fun extractSkipsNavCoverAndNonLinearSpineItems() {
        val epub = epubBytes(
            "META-INF/container.xml" to """
                <container><rootfiles><rootfile full-path="OPS/package.opf"/></rootfiles></container>
            """.trimIndent(),
            "OPS/package.opf" to """
                <package>
                  <manifest>
                    <item id="nav" href="nav.xhtml" media-type="application/xhtml+xml" properties="nav"/>
                    <item id="cover" href="cover.xhtml" media-type="application/xhtml+xml"/>
                    <item id="notes" href="notes.xhtml" media-type="application/xhtml+xml"/>
                    <item id="chapter" href="chapter.xhtml" media-type="application/xhtml+xml"/>
                  </manifest>
                  <spine>
                    <itemref idref="nav"/>
                    <itemref idref="cover"/>
                    <itemref idref="notes" linear="no"/>
                    <itemref idref="chapter"/>
                  </spine>
                </package>
            """.trimIndent(),
            "OPS/nav.xhtml" to "<html><body><h1>Contents</h1><ol><li>Chapter One</li></ol></body></html>",
            "OPS/cover.xhtml" to "<html><body><h1>Cover Page</h1></body></html>",
            "OPS/notes.xhtml" to "<html><body><p>Skipped auxiliary note.</p></body></html>",
            "OPS/chapter.xhtml" to "<html><body><h1>Chapter One</h1><p>Actual reading starts here.</p></body></html>",
        )

        val text = EpubTextExtractor.extract(ByteArrayInputStream(epub))

        assertFalse(text.contains("Contents"))
        assertFalse(text.contains("Cover Page"))
        assertFalse(text.contains("Skipped auxiliary note"))
        assertTrue(text.contains("# Chapter One"))
        assertTrue(text.contains("Actual reading starts here."))
    }

    @Test
    fun extractPreservesOrderedListsAndContinuationText() {
        val epub = singleChapterEpub(
            """
            <html><body>
              <ol>
                <li><p>First deliberate step</p><p>Keep the second paragraph attached.</p></li>
                <li>Second deliberate step</li>
              </ol>
            </body></html>
            """.trimIndent(),
        )

        val text = EpubTextExtractor.extract(ByteArrayInputStream(epub))

        assertEquals(
            """
            1. First deliberate step
              Keep the second paragraph attached.
            2. Second deliberate step
            """.trimIndent(),
            text,
        )
        val blocks = readerBlocksForDisplay(body = text, fallback = "Fallback.")
        assertEquals(ReaderMarkdownBlockKind.LIST, blocks.single().kind)
        assertEquals(
            """
            1. First deliberate step
              Keep the second paragraph attached.
            2. Second deliberate step
            """.trimIndent(),
            blocks.single().text.text,
        )
    }

    @Test
    fun extractAndReaderHandleNestedEmphasisWithoutRawMarkerLeakage() {
        val epub = singleChapterEpub(
            """
            <html><body>
              <p>Mixed <strong><em>both styles</em></strong> and <em><strong>reverse order</strong></em> should render cleanly.</p>
            </body></html>
            """.trimIndent(),
        )

        val text = EpubTextExtractor.extract(ByteArrayInputStream(epub))
        val block = readerBlocksForDisplay(body = text, fallback = "Fallback.").single()

        assertEquals(
            "Mixed both styles and reverse order should render cleanly.",
            block.text.text,
        )
        assertFalse(block.text.text.contains("**"))
        assertFalse(block.text.text.contains("_"))
        assertTrue(block.text.spanStyles.any { range -> range.item.fontWeight == FontWeight.SemiBold })
        assertTrue(block.text.spanStyles.any { range -> range.item.fontStyle == FontStyle.Italic })
    }

    @Test
    fun extractDecodesNonBmpNumericEntities() {
        val epub = singleChapterEpub("<html><body><p>Smile: &#128512; and hex: &#x1F642;.</p></body></html>")

        val text = EpubTextExtractor.extract(ByteArrayInputStream(epub))

        assertEquals("Smile: 😀 and hex: 🙂.", text)
    }

    @Test(expected = IllegalArgumentException::class)
    fun extractThrowsWhenPackageDocumentIsMissing() {
        val epub = epubBytes("OPS/chapter.xhtml" to "<html><body><p>Lost.</p></body></html>")

        EpubTextExtractor.extract(ByteArrayInputStream(epub))
    }

    private fun singleChapterEpub(chapterBody: String): ByteArray {
        return epubBytes(
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
            "OPS/chapter.xhtml" to chapterBody,
        )
    }

    private fun epubBytes(vararg entries: Pair<String, String>): ByteArray {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            entries.forEach { (name, body) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(body.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
        }
        return output.toByteArray()
    }
}
