package com.qualityalternative.app.data

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertEquals
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

            - First page
            - Second page - not a feed
            """.trimIndent(),
            text,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun extractThrowsWhenPackageDocumentIsMissing() {
        val epub = epubBytes("OPS/chapter.xhtml" to "<html><body><p>Lost.</p></body></html>")

        EpubTextExtractor.extract(ByteArrayInputStream(epub))
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
