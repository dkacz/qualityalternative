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
import org.junit.Assert.fail
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
    fun extractDocumentKeepsSourceBlockIndexesGlobalAcrossSpineDocuments() {
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
                    <item id="chapter-two" href="chapter2.xhtml" media-type="application/xhtml+xml"/>
                    <item id="chapter-three" href="chapter3.xhtml" media-type="application/xhtml+xml"/>
                  </manifest>
                  <spine>
                    <itemref idref="chapter-one"/>
                    <itemref idref="chapter-two"/>
                    <itemref idref="chapter-three"/>
                  </spine>
                </package>
            """.trimIndent(),
            "OPS/chapter1.xhtml" to "<html><body><h1>One</h1><p>First body.</p></body></html>",
            "OPS/chapter2.xhtml" to "<html><body><h1>Two</h1><p>Second body.</p></body></html>",
            "OPS/chapter3.xhtml" to "<html><body><h1>Three</h1><p>Third body.</p></body></html>",
        )

        val document = EpubTextExtractor.extractDocument(ByteArrayInputStream(epub))

        assertEquals((0..5).toList(), document.blocks.map { block -> block.sourceBlockIndex })
        assertEquals(
            listOf(0, 2, 4),
            document.blocks
                .filter { block -> block.text.startsWith("# ") }
                .map { block -> block.sourceBlockIndex },
        )
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
    fun extractDoesNotFallbackToFilteredSpineOrUnlistedDocuments() {
        val epub = epubBytes(
            "META-INF/container.xml" to """
                <container><rootfiles><rootfile full-path="OPS/package.opf"/></rootfiles></container>
            """.trimIndent(),
            "OPS/package.opf" to """
                <package>
                  <manifest>
                    <item id="guide" href="guide.xhtml" media-type="application/xhtml+xml" properties="nav"/>
                    <item id="appendix" href="appendix.xhtml" media-type="application/xhtml+xml"/>
                    <item id="unlisted" href="chapter.xhtml" media-type="application/xhtml+xml"/>
                  </manifest>
                  <spine>
                    <itemref idref="guide"/>
                    <itemref idref="appendix" linear="no"/>
                  </spine>
                </package>
            """.trimIndent(),
            "OPS/guide.xhtml" to "<html><body><h1>Navigation should not render</h1></body></html>",
            "OPS/appendix.xhtml" to "<html><body><p>Nonlinear appendix should not render.</p></body></html>",
            "OPS/chapter.xhtml" to "<html><body><p>Unlisted fallback should not render when spine exists.</p></body></html>",
        )

        try {
            EpubTextExtractor.extract(ByteArrayInputStream(epub))
            fail("Filtered spine content should not be reintroduced by fallback scanning")
        } catch (expected: IllegalArgumentException) {
            assertTrue(expected.message?.contains("no readable text", ignoreCase = true) == true)
        }
    }

    @Test
    fun extractKeepsLegitimateChapterNamesThatContainCover() {
        val epub = epubBytes(
            "META-INF/container.xml" to """
                <container><rootfiles><rootfile full-path="OPS/package.opf"/></rootfiles></container>
            """.trimIndent(),
            "OPS/package.opf" to """
                <package>
                  <manifest>
                    <item id="discover" href="discover.xhtml" media-type="application/xhtml+xml"/>
                    <item id="recover" href="recover.xhtml" media-type="application/xhtml+xml"/>
                    <item id="cover-story" href="cover-story.xhtml" media-type="application/xhtml+xml"/>
                    <item id="story-cover" href="story-cover.xhtml" media-type="application/xhtml+xml"/>
                  </manifest>
                  <spine>
                    <itemref idref="discover"/>
                    <itemref idref="recover"/>
                    <itemref idref="cover-story"/>
                    <itemref idref="story-cover"/>
                  </spine>
                </package>
            """.trimIndent(),
            "OPS/discover.xhtml" to "<html><body><h1>Discover Slowly</h1></body></html>",
            "OPS/recover.xhtml" to "<html><body><p>Recover attention without opening a feed.</p></body></html>",
            "OPS/cover-story.xhtml" to "<html><body><p>A cover story can still be a chapter.</p></body></html>",
            "OPS/story-cover.xhtml" to "<html><body><p>A story cover chapter can still be readable.</p></body></html>",
        )

        val text = EpubTextExtractor.extract(ByteArrayInputStream(epub))

        assertTrue(text.contains("# Discover Slowly"))
        assertTrue(text.contains("Recover attention without opening a feed."))
        assertTrue(text.contains("A cover story can still be a chapter."))
        assertTrue(text.contains("A story cover chapter can still be readable."))
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

    @Test
    fun extractKeepsNestedListsAttachedToTheirParentItem() {
        val epub = singleChapterEpub(
            """
            <html><body>
              <ol>
                <li>First
                  <ul>
                    <li>Sub A</li>
                    <li>Sub B</li>
                  </ul>
                </li>
                <li>Second</li>
              </ol>
            </body></html>
            """.trimIndent(),
        )

        val text = EpubTextExtractor.extract(ByteArrayInputStream(epub))

        assertEquals(
            """
            1. First
              - Sub A
              - Sub B
            2. Second
            """.trimIndent(),
            text,
        )
        assertFalse(text.contains("2. Sub B"))
        assertFalse(text.contains("3. Second"))
    }

    @Test
    fun extractKeepsSameTypeNestedListsAttachedToTheirParentItem() {
        val ordered = EpubTextExtractor.extract(
            ByteArrayInputStream(
                singleChapterEpub(
                    """
                    <html><body>
                      <ol>
                        <li>Parent
                          <ol>
                            <li>Sub 1</li>
                            <li>Sub 2</li>
                          </ol>
                        </li>
                        <li>Next</li>
                      </ol>
                    </body></html>
                    """.trimIndent(),
                ),
            ),
        )
        val unordered = EpubTextExtractor.extract(
            ByteArrayInputStream(
                singleChapterEpub(
                    """
                    <html><body>
                      <ul>
                        <li>Parent
                          <ul>
                            <li>Sub A</li>
                            <li>Sub B</li>
                          </ul>
                        </li>
                        <li>Next</li>
                      </ul>
                    </body></html>
                    """.trimIndent(),
                ),
            ),
        )

        assertEquals(
            """
            1. Parent
              1. Sub 1
              2. Sub 2
            2. Next
            """.trimIndent(),
            ordered,
        )
        assertEquals(
            """
            - Parent
              - Sub A
              - Sub B
            - Next
            """.trimIndent(),
            unordered,
        )
    }

    @Test
    fun extractPreservesThreeLevelNestedListDepth() {
        val epub = singleChapterEpub(
            """
            <html><body>
              <ol>
                <li>Parent
                  <ol>
                    <li>Child
                      <ol>
                        <li>Grandchild</li>
                      </ol>
                    </li>
                  </ol>
                </li>
              </ol>
            </body></html>
            """.trimIndent(),
        )

        val text = EpubTextExtractor.extract(ByteArrayInputStream(epub))

        assertEquals(
            """
            1. Parent
              1. Child
                1. Grandchild
            """.trimIndent(),
            text,
        )
    }

    @Test
    fun extractPreservesContinuationParagraphsInsideNestedListDepth() {
        val epub = singleChapterEpub(
            """
            <html><body>
              <ol>
                <li>Parent
                  <ol>
                    <li>Child
                      <p>Child note before nested</p>
                      <ol><li>Grandchild</li></ol>
                      <p>Child note after nested</p>
                    </li>
                  </ol>
                </li>
              </ol>
            </body></html>
            """.trimIndent(),
        )

        val text = EpubTextExtractor.extract(ByteArrayInputStream(epub))

        assertEquals(
            """
            1. Parent
              1. Child
                Child note before nested
                1. Grandchild
                Child note after nested
            """.trimIndent(),
            text,
        )
    }

    @Test
    fun extractKeepsParentLevelContinuationAroundNestedChildListAtParentDepth() {
        val epub = singleChapterEpub(
            """
            <html><body>
              <ol>
                <li>Parent
                  <p>Parent note before child list</p>
                  <ol>
                    <li>Child
                      <ol><li>Grandchild</li></ol>
                    </li>
                  </ol>
                  <p>Parent note after child list</p>
                </li>
              </ol>
            </body></html>
            """.trimIndent(),
        )

        val text = EpubTextExtractor.extract(ByteArrayInputStream(epub))

        assertEquals(
            """
            1. Parent
              Parent note before child list
              1. Child
                1. Grandchild
              Parent note after child list
            """.trimIndent(),
            text,
        )
    }

    @Test
    fun extractDocumentParsesEpub3NavTocAndMapsAnchorsToBlocks() {
        val epub = epubBytes(
            "META-INF/container.xml" to """
                <container><rootfiles><rootfile full-path="OPS/package.opf"/></rootfiles></container>
            """.trimIndent(),
            "OPS/package.opf" to """
                <package>
                  <manifest>
                    <item id="nav" href="nav/nav.xhtml" media-type="application/xhtml+xml" properties="nav"/>
                    <item id="chapter-one" href="chapter1.xhtml" media-type="application/xhtml+xml"/>
                    <item id="chapter-two" href="chapter2.xhtml" media-type="application/xhtml+xml"/>
                  </manifest>
                  <spine>
                    <itemref idref="chapter-one"/>
                    <itemref idref="chapter-two"/>
                  </spine>
                </package>
            """.trimIndent(),
            "OPS/nav/nav.xhtml" to """
                <html xmlns:epub="http://www.idpf.org/2007/ops">
                  <body>
                    <nav epub:type="toc">
                      <ol>
                        <li><a href="../chapter1.xhtml#start">Start Here</a></li>
                        <li><a href="../chapter2.xhtml#second">Second Chapter</a></li>
                      </ol>
                    </nav>
                  </body>
                </html>
            """.trimIndent(),
            "OPS/chapter1.xhtml" to """
                <html><body><h1 id="start">Opening</h1><p>Begin deliberately.</p></body></html>
            """.trimIndent(),
            "OPS/chapter2.xhtml" to """
                <html><body><section id="second"><h1>Second</h1><p>Continue deliberately.</p></section></body></html>
            """.trimIndent(),
        )

        val document = EpubTextExtractor.extractDocument(ByteArrayInputStream(epub))

        assertEquals(
            listOf("Start Here", "Second Chapter"),
            document.tableOfContents.map { entry -> entry.title },
        )
        assertEquals("OPS/chapter1.xhtml", document.tableOfContents[0].sourceHref)
        assertEquals("start", document.tableOfContents[0].anchor)
        assertEquals(0, document.tableOfContents[0].blockIndex)
        assertEquals("OPS/chapter2.xhtml", document.tableOfContents[1].sourceHref)
        assertEquals("second", document.tableOfContents[1].anchor)
        assertEquals(2, document.tableOfContents[1].blockIndex)
        assertEquals("OPS/chapter2.xhtml", document.blocks[2].sourceHref)
        assertEquals("second", document.blocks[2].anchor)
    }

    @Test
    fun extractDocumentKeepsEpub3GroupedNavParentOutOfTocWhenItHasNoDirectHref() {
        val epub = epubBytes(
            "META-INF/container.xml" to """
                <container><rootfiles><rootfile full-path="OPS/package.opf"/></rootfiles></container>
            """.trimIndent(),
            "OPS/package.opf" to """
                <package>
                  <manifest>
                    <item id="nav" href="nav.xhtml" media-type="application/xhtml+xml" properties="nav"/>
                    <item id="chapter-one" href="chapter1.xhtml" media-type="application/xhtml+xml"/>
                    <item id="chapter-two" href="chapter2.xhtml" media-type="application/xhtml+xml"/>
                  </manifest>
                  <spine>
                    <itemref idref="chapter-one"/>
                    <itemref idref="chapter-two"/>
                  </spine>
                </package>
            """.trimIndent(),
            "OPS/nav.xhtml" to """
                <html xmlns:epub="http://www.idpf.org/2007/ops">
                  <body>
                    <nav epub:type="toc">
                      <ol>
                        <li>
                          <span>Part I</span>
                          <ol>
                            <li><a href="chapter1.xhtml">Chapter One</a></li>
                            <li><a href="chapter2.xhtml">Chapter Two</a></li>
                          </ol>
                        </li>
                      </ol>
                    </nav>
                  </body>
                </html>
            """.trimIndent(),
            "OPS/chapter1.xhtml" to "<html><body><h1>One</h1><p>First body.</p></body></html>",
            "OPS/chapter2.xhtml" to "<html><body><h1>Two</h1><p>Second body.</p></body></html>",
        )

        val document = EpubTextExtractor.extractDocument(ByteArrayInputStream(epub))

        assertEquals(listOf("Chapter One", "Chapter Two"), document.tableOfContents.map { entry -> entry.title })
        assertEquals(listOf(1, 1), document.tableOfContents.map { entry -> entry.level })
        assertEquals(listOf(0, 2), document.tableOfContents.map { entry -> entry.blockIndex })
    }

    @Test
    fun extractDocumentParsesEpub2NcxToc() {
        val epub = epubBytes(
            "META-INF/container.xml" to """
                <container><rootfiles><rootfile full-path="OPS/package.opf"/></rootfiles></container>
            """.trimIndent(),
            "OPS/package.opf" to """
                <package>
                  <manifest>
                    <item id="ncx" href="toc.ncx" media-type="application/x-dtbncx+xml"/>
                    <item id="chapter-one" href="chapter1.xhtml" media-type="application/xhtml+xml"/>
                    <item id="chapter-two" href="chapter2.xhtml" media-type="application/xhtml+xml"/>
                  </manifest>
                  <spine toc="ncx">
                    <itemref idref="chapter-one"/>
                    <itemref idref="chapter-two"/>
                  </spine>
                </package>
            """.trimIndent(),
            "OPS/toc.ncx" to """
                <ncx>
                  <navMap>
                    <navPoint id="navPoint-1">
                      <navLabel><text>Chapter One</text></navLabel>
                      <content src="chapter1.xhtml#intro"/>
                      <navPoint id="navPoint-1-1">
                        <navLabel><text>First Section</text></navLabel>
                        <content src="chapter1.xhtml#first-section"/>
                      </navPoint>
                    </navPoint>
                    <navPoint id="navPoint-2">
                      <navLabel><text>Chapter Two</text></navLabel>
                      <content src="chapter2.xhtml"/>
                    </navPoint>
                  </navMap>
                </ncx>
            """.trimIndent(),
            "OPS/chapter1.xhtml" to """
                <html><body><h1 id="intro">Intro</h1><p id="first-section">First section body.</p></body></html>
            """.trimIndent(),
            "OPS/chapter2.xhtml" to """
                <html><body><h1>Second</h1><p>Second body.</p></body></html>
            """.trimIndent(),
        )

        val document = EpubTextExtractor.extractDocument(ByteArrayInputStream(epub))

        assertEquals(
            listOf("Chapter One", "First Section", "Chapter Two"),
            document.tableOfContents.map { entry -> entry.title },
        )
        assertEquals(listOf(0, 1, 0), document.tableOfContents.map { entry -> entry.level })
        assertEquals("intro", document.tableOfContents[0].anchor)
        assertEquals(0, document.tableOfContents[0].blockIndex)
        assertEquals("first-section", document.tableOfContents[1].anchor)
        assertEquals(1, document.tableOfContents[1].blockIndex)
        assertEquals("OPS/chapter2.xhtml", document.tableOfContents[2].sourceHref)
        assertEquals(2, document.tableOfContents[2].blockIndex)
    }

    @Test
    fun extractDocumentFallsBackToHeadingTocWhenPackageHasNoToc() {
        val epub = epubBytes(
            "META-INF/container.xml" to """
                <container><rootfiles><rootfile full-path="OPS/package.opf"/></rootfiles></container>
            """.trimIndent(),
            "OPS/package.opf" to """
                <package>
                  <manifest>
                    <item id="chapter-one" href="chapter1.xhtml" media-type="application/xhtml+xml"/>
                    <item id="chapter-two" href="chapter2.xhtml" media-type="application/xhtml+xml"/>
                  </manifest>
                  <spine>
                    <itemref idref="chapter-one"/>
                    <itemref idref="chapter-two"/>
                  </spine>
                </package>
            """.trimIndent(),
            "OPS/chapter1.xhtml" to "<html><body><h1 id=\"one\">One</h1><p>First body.</p></body></html>",
            "OPS/chapter2.xhtml" to "<html><body><h2 id=\"two\">Two</h2><p>Second body.</p></body></html>",
        )

        val document = EpubTextExtractor.extractDocument(ByteArrayInputStream(epub))

        assertEquals(listOf("One", "Two"), document.tableOfContents.map { entry -> entry.title })
        assertEquals(listOf(0, 1), document.tableOfContents.map { entry -> entry.level })
        assertEquals(listOf(0, 2), document.tableOfContents.map { entry -> entry.blockIndex })
        assertEquals("OPS/chapter1.xhtml#one", document.tableOfContents[0].href)
        assertEquals("OPS/chapter2.xhtml#two", document.tableOfContents[1].href)
    }

    @Test
    fun extractDocumentMapsTocHrefToNearestSourceBlockWhenAnchorIsUnavailable() {
        val epub = epubBytes(
            "META-INF/container.xml" to """
                <container><rootfiles><rootfile full-path="OPS/package.opf"/></rootfiles></container>
            """.trimIndent(),
            "OPS/package.opf" to """
                <package>
                  <manifest>
                    <item id="nav" href="nav.xhtml" media-type="application/xhtml+xml" properties="nav"/>
                    <item id="chapter" href="chapter.xhtml" media-type="application/xhtml+xml"/>
                  </manifest>
                  <spine><itemref idref="chapter"/></spine>
                </package>
            """.trimIndent(),
            "OPS/nav.xhtml" to """
                <html xmlns:epub="http://www.idpf.org/2007/ops">
                  <body><nav epub:type="toc"><ol><li><a href="chapter.xhtml#missing">Missing Anchor</a></li></ol></nav></body>
                </html>
            """.trimIndent(),
            "OPS/chapter.xhtml" to "<html><body><h1>Chapter</h1><p>Readable body.</p></body></html>",
        )

        val document = EpubTextExtractor.extractDocument(ByteArrayInputStream(epub))

        assertEquals("Missing Anchor", document.tableOfContents.single().title)
        assertEquals("missing", document.tableOfContents.single().anchor)
        assertEquals(0, document.tableOfContents.single().blockIndex)
        assertEquals("OPS/chapter.xhtml", document.tableOfContents.single().sourceHref)
    }

    @Test
    fun extractDocumentIgnoresLargeBinaryAssetsWhileKeepingReadableSpine() {
        val epub = epubByteEntries(
            "META-INF/container.xml" to """
                <container><rootfiles><rootfile full-path="OPS/package.opf"/></rootfiles></container>
            """.trimIndent().toByteArray(Charsets.UTF_8),
            "OPS/package.opf" to """
                <package>
                  <manifest>
                    <item id="chapter" href="chapter.xhtml" media-type="application/xhtml+xml"/>
                    <item id="image" href="images/plate.jpg" media-type="image/jpeg"/>
                  </manifest>
                  <spine><itemref idref="chapter"/></spine>
                </package>
            """.trimIndent().toByteArray(Charsets.UTF_8),
            "OPS/chapter.xhtml" to "<html><body><h1>Readable</h1><p>Text still loads.</p></body></html>"
                .toByteArray(Charsets.UTF_8),
            "OPS/images/plate.jpg" to ByteArray(3 * 1024 * 1024) { index -> (index % 251).toByte() },
        )

        val document = EpubTextExtractor.extractDocument(ByteArrayInputStream(epub))

        assertEquals(listOf("# Readable", "Text still loads."), document.blocks.map { block -> block.text })
    }

    @Test
    fun extractDocumentMapsManyAnchorsWithoutChangingGlobalBlockTargets() {
        val sections = (1..80).joinToString("\n") { index ->
            """<section id="s$index"><h2>Section $index</h2><p>Body $index.</p></section>"""
        }
        val tocItems = (1..80).joinToString("\n") { index ->
            """<li><a href="chapter.xhtml#s$index">Section $index</a></li>"""
        }
        val epub = epubBytes(
            "META-INF/container.xml" to """
                <container><rootfiles><rootfile full-path="OPS/package.opf"/></rootfiles></container>
            """.trimIndent(),
            "OPS/package.opf" to """
                <package>
                  <manifest>
                    <item id="nav" href="nav.xhtml" media-type="application/xhtml+xml" properties="nav"/>
                    <item id="chapter" href="chapter.xhtml" media-type="application/xhtml+xml"/>
                  </manifest>
                  <spine><itemref idref="chapter"/></spine>
                </package>
            """.trimIndent(),
            "OPS/nav.xhtml" to """
                <html xmlns:epub="http://www.idpf.org/2007/ops">
                  <body><nav epub:type="toc"><ol>$tocItems</ol></nav></body>
                </html>
            """.trimIndent(),
            "OPS/chapter.xhtml" to "<html><body>$sections</body></html>",
        )

        val document = EpubTextExtractor.extractDocument(ByteArrayInputStream(epub))

        assertEquals(80, document.tableOfContents.size)
        assertEquals(0, document.tableOfContents.first().blockIndex)
        assertEquals(158, document.tableOfContents.last().blockIndex)
        assertEquals("s80", document.blocks[158].anchor)
    }

    @Test
    fun extractDocumentMapsLaterTocAnchorAfterInlinePagebreakWithoutDrift() {
        val epub = epubBytes(
            "META-INF/container.xml" to """
                <container><rootfiles><rootfile full-path="OPS/package.opf"/></rootfiles></container>
            """.trimIndent(),
            "OPS/package.opf" to """
                <package>
                  <manifest>
                    <item id="nav" href="nav.xhtml" media-type="application/xhtml+xml" properties="nav"/>
                    <item id="chapter" href="chapter.xhtml" media-type="application/xhtml+xml"/>
                  </manifest>
                  <spine><itemref idref="chapter"/></spine>
                </package>
            """.trimIndent(),
            "OPS/nav.xhtml" to """
                <html xmlns:epub="http://www.idpf.org/2007/ops">
                  <body>
                    <nav epub:type="toc">
                      <ol>
                        <li><a href="chapter.xhtml#target-section">Target Section</a></li>
                      </ol>
                    </nav>
                  </body>
                </html>
            """.trimIndent(),
            "OPS/chapter.xhtml" to """
                <html><body>
                  <p>Opening paragraph <span id="page-2"></span>continues after an inline pagebreak.</p>
                  <h2 id="target-section">Target Section</h2>
                  <p>Target body.</p>
                </body></html>
            """.trimIndent(),
        )

        val document = EpubTextExtractor.extractDocument(ByteArrayInputStream(epub))

        assertEquals(
            listOf(
                "Opening paragraph continues after an inline pagebreak.",
                "## Target Section",
                "Target body.",
            ),
            document.blocks.map { block -> block.text },
        )
        assertEquals("Target Section", document.tableOfContents.single().title)
        assertEquals("target-section", document.tableOfContents.single().anchor)
        assertEquals(1, document.tableOfContents.single().blockIndex)
        assertEquals("target-section", document.blocks[1].anchor)
    }

    @Test
    fun extractThrowsWhenReadableEntryExceedsSafetyLimit() {
        val epub = epubByteEntries(
            "META-INF/container.xml" to """
                <container><rootfiles><rootfile full-path="OPS/package.opf"/></rootfiles></container>
            """.trimIndent().toByteArray(Charsets.UTF_8),
            "OPS/package.opf" to """
                <package>
                  <manifest>
                    <item id="chapter" href="chapter.xhtml" media-type="application/xhtml+xml"/>
                  </manifest>
                  <spine><itemref idref="chapter"/></spine>
                </package>
            """.trimIndent().toByteArray(Charsets.UTF_8),
            "OPS/chapter.xhtml" to ByteArray(24 * 1024 * 1024 + 1) { 'a'.code.toByte() },
        )

        try {
            EpubTextExtractor.extractDocument(ByteArrayInputStream(epub))
            fail("Oversized readable EPUB entry should be rejected")
        } catch (expected: IllegalArgumentException) {
            assertTrue(expected.message?.contains("entry is too large", ignoreCase = true) == true)
        }
    }

    @Test
    fun extractThrowsWhenAggregateReadableTextExceedsSafetyLimit() {
        val manifestItems = (1..5).joinToString("\n") { index ->
            """<item id="chapter-$index" href="chapter$index.xhtml" media-type="application/xhtml+xml"/>"""
        }
        val spineItems = (1..5).joinToString("\n") { index -> """<itemref idref="chapter-$index"/>""" }
        val epub = epubGeneratedEntries(
            "META-INF/container.xml" to """
                <container><rootfiles><rootfile full-path="OPS/package.opf"/></rootfiles></container>
            """.trimIndent().toByteArray(Charsets.UTF_8),
            "OPS/package.opf" to """
                <package>
                  <manifest>$manifestItems</manifest>
                  <spine>$spineItems</spine>
                </package>
            """.trimIndent().toByteArray(Charsets.UTF_8),
            generatedEntries = (1..5).map { index -> "OPS/chapter$index.xhtml" to (20 * 1024 * 1024) },
        )

        try {
            EpubTextExtractor.extractDocument(ByteArrayInputStream(epub))
            fail("Aggregate readable EPUB text should be rejected")
        } catch (expected: IllegalArgumentException) {
            assertTrue(expected.message?.contains("readable text is too large", ignoreCase = true) == true)
        }
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
        return epubByteEntries(
            *entries.map { (name, body) -> name to body.toByteArray(Charsets.UTF_8) }.toTypedArray(),
        )
    }

    private fun epubByteEntries(vararg entries: Pair<String, ByteArray>): ByteArray {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            entries.forEach { (name, body) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(body)
                zip.closeEntry()
            }
        }
        return output.toByteArray()
    }

    private fun epubGeneratedEntries(
        vararg staticEntries: Pair<String, ByteArray>,
        generatedEntries: List<Pair<String, Int>>,
    ): ByteArray {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            staticEntries.forEach { (name, body) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(body)
                zip.closeEntry()
            }
            val chunk = ByteArray(64 * 1024) { 'a'.code.toByte() }
            generatedEntries.forEach { (name, byteCount) ->
                zip.putNextEntry(ZipEntry(name))
                var remaining = byteCount
                while (remaining > 0) {
                    val count = minOf(remaining, chunk.size)
                    zip.write(chunk, 0, count)
                    remaining -= count
                }
                zip.closeEntry()
            }
        }
        return output.toByteArray()
    }
}
