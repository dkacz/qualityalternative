package com.qualityalternative.app.ui

import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.qualityalternative.app.domain.model.AnalyticsEvent
import com.qualityalternative.app.domain.model.AnalyticsEventType
import com.qualityalternative.app.domain.model.MEDITATION_TIMER_CONTENT_ID
import com.qualityalternative.app.domain.model.RecommendationSource
import com.qualityalternative.app.domain.model.ReadingAnnotation
import com.qualityalternative.app.domain.model.ReadingAnnotationSelector
import com.qualityalternative.app.domain.model.ReplacementHistoryEntry
import com.qualityalternative.app.domain.model.TopicTag
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressSnapshotTest {
    @Test
    fun progressSnapshotCountsConstructiveOutcomesWithoutDuplicateDelayEvents() {
        val entries = listOf(
            replacementEntry(
                sessionId = "session-1",
                acceptedAtMillis = 1_000L,
                completedAtMillis = 2_000L,
            ),
            replacementEntry(
                sessionId = "session-2",
                acceptedAtMillis = 3_000L,
            ),
        )
        val events = listOf(
            event(AnalyticsEventType.INTERVENTION_SHOWN, interventionId = "intervention-1"),
            event(AnalyticsEventType.INTERVENTION_SHOWN, interventionId = "intervention-2"),
            event(
                AnalyticsEventType.DELAY_SELECTED,
                interventionId = "intervention-1",
                metadata = mapOf("delayId" to "delay-1"),
            ),
            event(
                AnalyticsEventType.DELAY_SELECTED,
                interventionId = "intervention-1",
                metadata = mapOf("delayId" to "delay-1"),
            ),
            event(AnalyticsEventType.OPEN_ANYWAY_SELECTED, interventionId = "intervention-3"),
        )

        val snapshot = progressSnapshot(
            entries = entries,
            events = events,
            zoneId = ZoneOffset.UTC,
            nowMillis = 3_000L,
        )

        assertEquals(1, snapshot.daysConverted)
        assertEquals(1, snapshot.currentStreakDays)
        assertEquals(21, snapshot.dayBars.size)
        assertEquals(2, snapshot.interventionsShown)
        assertEquals(2, snapshot.alternativesChosen)
        assertEquals(1, snapshot.delayedOpens)
        assertEquals(1, snapshot.consciousOverrides)
        assertEquals(1, snapshot.completedReads)
        assertEquals(listOf("session-1", "session-2"), snapshot.recentReplacements.map { it.sessionId })
    }

    @Test
    fun progressSnapshotDoesNotCountMeditationAsCompletedReadOrReadingStreak() {
        val today = LocalDate.of(2026, 4, 21)
        val entries = listOf(
            replacementEntry(
                sessionId = "meditation",
                acceptedAtMillis = today.toMillis(),
                completedAtMillis = today.toMillis() + 1_000L,
                contentId = MEDITATION_TIMER_CONTENT_ID,
            ),
        )

        val snapshot = progressSnapshot(
            entries = entries,
            events = emptyList(),
            zoneId = ZoneOffset.UTC,
            nowMillis = today.toMillis(),
        )

        assertEquals(0, snapshot.completedReads)
        assertEquals(0, snapshot.currentStreakDays)
        assertEquals(1, snapshot.alternativesChosen)
    }

    @Test
    fun progressSnapshotBuildsDynamicCalendarStripFromReplacementsAndDelays() {
        val today = LocalDate.of(2026, 4, 21)
        val convertedDate = today.minusDays(2)
        val partialDate = today.minusDays(1)
        val entries = listOf(
            replacementEntry(
                sessionId = "converted",
                acceptedAtMillis = convertedDate.toMillis(),
            ),
        )
        val events = listOf(
            event(
                AnalyticsEventType.DELAY_SELECTED,
                interventionId = "delay-intervention",
                timestampMillis = partialDate.toMillis(),
                metadata = mapOf("delayId" to "delay-1"),
            ),
        )

        val snapshot = progressSnapshot(
            entries = entries,
            events = events,
            zoneId = ZoneOffset.UTC,
            nowMillis = today.toMillis(),
        )

        assertEquals(today.minusDays(20), snapshot.dayBars.first().date)
        assertEquals(today, snapshot.dayBars.last().date)
        assertEquals(ProgressDayState.CONVERTED, snapshot.dayBars.first { it.date == convertedDate }.state)
        assertEquals(ProgressDayState.PARTIAL, snapshot.dayBars.first { it.date == partialDate }.state)
        assertEquals(ProgressDayState.EMPTY, snapshot.dayBars.first { it.date == today.minusDays(3) }.state)
    }

    @Test
    fun progressSnapshotLetsConvertedDayWinOverPartialDelayDay() {
        val today = LocalDate.of(2026, 4, 21)
        val entries = listOf(
            replacementEntry(
                sessionId = "converted",
                acceptedAtMillis = today.toMillis(),
            ),
        )
        val events = listOf(
            event(
                AnalyticsEventType.DELAY_SELECTED,
                interventionId = "delay-intervention",
                timestampMillis = today.toMillis(),
                metadata = mapOf("delayId" to "delay-1"),
            ),
        )

        val snapshot = progressSnapshot(
            entries = entries,
            events = events,
            zoneId = ZoneOffset.UTC,
            nowMillis = today.toMillis(),
        )

        assertEquals(1, snapshot.daysConverted)
        assertEquals(ProgressDayState.CONVERTED, snapshot.dayBars.last().state)
    }

    @Test
    fun progressSnapshotKeepsDaysConvertedAlignedToTwentyOneDayStrip() {
        val today = LocalDate.of(2026, 4, 21)
        val entries = listOf(
            replacementEntry(
                sessionId = "older-than-strip",
                acceptedAtMillis = today.minusDays(22).toMillis(),
            ),
        )

        val snapshot = progressSnapshot(
            entries = entries,
            events = emptyList(),
            zoneId = ZoneOffset.UTC,
            nowMillis = today.toMillis(),
        )

        assertEquals(0, snapshot.daysConverted)
        assertEquals(false, snapshot.dayBars.any { it.state == ProgressDayState.CONVERTED })
    }

    @Test
    fun progressSnapshotShowsOnlyRecentSevenDayReplacements() {
        val today = LocalDate.of(2026, 4, 21)
        val entries = listOf(
            replacementEntry(
                sessionId = "today",
                acceptedAtMillis = today.toMillis(),
            ),
            replacementEntry(
                sessionId = "last-week",
                acceptedAtMillis = today.minusDays(6).toMillis(),
            ),
            replacementEntry(
                sessionId = "too-old",
                acceptedAtMillis = today.minusDays(7).toMillis(),
            ),
        )

        val snapshot = progressSnapshot(
            entries = entries,
            events = emptyList(),
            zoneId = ZoneOffset.UTC,
            nowMillis = today.toMillis(),
        )

        assertEquals(listOf("today", "last-week"), snapshot.recentReplacements.map { it.sessionId })
    }

    @Test
    fun recentReplacementDurationLabelUsesPersistedDuration() {
        val entry = replacementEntry(
            sessionId = "five-minute-reset",
            acceptedAtMillis = 1_000L,
            contentDurationMinutes = 5,
        )

        assertEquals("5 min", recentReplacementDurationLabel(entry))
    }

    @Test
    fun progressSnapshotCountsCurrentReadingStreakFromCompletedSessions() {
        val today = LocalDate.of(2026, 4, 21)
        val entries = listOf(
            replacementEntry(
                sessionId = "today",
                acceptedAtMillis = today.toMillis(),
                completedAtMillis = today.toMillis() + 5_000L,
            ),
            replacementEntry(
                sessionId = "yesterday",
                acceptedAtMillis = today.minusDays(1).toMillis(),
                completedAtMillis = today.minusDays(1).toMillis() + 5_000L,
            ),
            replacementEntry(
                sessionId = "gap",
                acceptedAtMillis = today.minusDays(3).toMillis(),
                completedAtMillis = today.minusDays(3).toMillis() + 5_000L,
            ),
        )

        val snapshot = progressSnapshot(
            entries = entries,
            events = emptyList(),
            zoneId = ZoneOffset.UTC,
            nowMillis = today.toMillis(),
        )

        assertEquals(2, snapshot.currentStreakDays)
    }

    @Test
    fun finiteReaderParagraphsReturnsOnlyRealParagraphs() {
        assertEquals(
            listOf("First paragraph.", "Second paragraph.", "Third paragraph."),
            finiteReaderParagraphs(" First paragraph. \n\n\n Second paragraph. \n \n Third paragraph. "),
        )
    }

    @Test
    fun readerParagraphsForDisplayDoesNotCapLongAssets() {
        val body = (1..7).joinToString(separator = "\n\n") { index -> "Paragraph $index." }

        assertEquals(
            (1..7).map { index -> "Paragraph $index." },
            readerParagraphsForDisplay(body = body, fallback = "Fallback."),
        )
    }

    @Test
    fun readerParagraphsForDisplayUsesFallbackWhenBodyIsBlank() {
        assertEquals(
            listOf("Fallback."),
            readerParagraphsForDisplay(body = " \n\n ", fallback = "Fallback."),
        )
    }

    @Test
    fun readerBlocksForDisplayPreservesBasicMarkdownFormatting() {
        val blocks = readerBlocksForDisplay(
            body = """
                # Saved **Heading**

                - First **bold** item
                - Second _italic_ item

                > A quoted line

                Use `inline code` before scrolling.
            """.trimIndent(),
            fallback = "Fallback.",
        )

        assertEquals(4, blocks.size)
        assertEquals(ReaderMarkdownBlockKind.HEADING, blocks[0].kind)
        assertEquals("Saved Heading", blocks[0].text.text)
        assertTrue(blocks[0].text.hasSpan { span -> span.fontWeight == FontWeight.SemiBold })
        assertEquals(ReaderMarkdownBlockKind.LIST, blocks[1].kind)
        assertEquals("• First bold item\n• Second italic item", blocks[1].text.text)
        assertTrue(blocks[1].text.hasSpan { span -> span.fontWeight == FontWeight.SemiBold })
        assertTrue(blocks[1].text.hasSpan { span -> span.fontStyle == FontStyle.Italic })
        assertEquals(ReaderMarkdownBlockKind.QUOTE, blocks[2].kind)
        assertEquals("A quoted line", blocks[2].text.text)
        assertEquals(ReaderMarkdownBlockKind.BODY, blocks[3].kind)
        assertEquals("Use inline code before scrolling.", blocks[3].text.text)
    }

    @Test
    fun readerBlocksForDisplayPreservesIndentedNestedMarkdownListLines() {
        val blocks = readerBlocksForDisplay(
            body = """
                1. Parent
                  1. Sub one
                  2. Sub two
                2. Next
            """.trimIndent(),
            fallback = "Fallback.",
        )

        assertEquals(1, blocks.size)
        assertEquals(ReaderMarkdownBlockKind.LIST, blocks.single().kind)
        assertEquals(
            """
            1. Parent
              1. Sub one
              2. Sub two
            2. Next
            """.trimIndent(),
            blocks.single().text.text,
        )
    }

    @Test
    fun readerBlocksForDisplayPreservesThreeLevelMarkdownListDepth() {
        val blocks = readerBlocksForDisplay(
            body = """
                1. Parent
                  1. Child
                    1. Grandchild
                2. Next
            """.trimIndent(),
            fallback = "Fallback.",
        )

        assertEquals(1, blocks.size)
        assertEquals(ReaderMarkdownBlockKind.LIST, blocks.single().kind)
        assertEquals(
            """
            1. Parent
              1. Child
                1. Grandchild
            2. Next
            """.trimIndent(),
            blocks.single().text.text,
        )
    }

    @Test
    fun readerPagesSplitLongContentAndRestoreContainingParagraph() {
        val blocks = (1..9).map { index ->
            readerMarkdownBlock("Paragraph $index with enough text to act like ordinary reader prose.")
        }

        val pages = readerPagesForBlocks(blocks = blocks, maxPageWeight = 9)

        assertEquals(
            listOf(
                ReaderPage(start = 0, endInclusive = 2),
                ReaderPage(start = 3, endInclusive = 5),
                ReaderPage(start = 6, endInclusive = 8),
            ),
            pages,
        )
        assertEquals(1, readerPageIndexForParagraph(pages = pages, paragraphIndex = 4))
        assertEquals(66, readerProgressPercentForParagraphIndex(paragraphIndex = 5, paragraphCount = 9))
        assertEquals(66, readerProgressPercentForPageIndex(pageIndex = 1, pageCount = 3))
    }

    @Test
    fun readerProgressPercentUsesSourcePositionInsteadOfRepaginatedPageCount() {
        val blocks = listOf(
            readerMarkdownBlock(rawBlock = "AAAAAA", sourceBlockIndex = 0),
            readerMarkdownBlock(rawBlock = "BBBBBB", sourceBlockIndex = 1),
            readerMarkdownBlock(rawBlock = "CCCCCC", sourceBlockIndex = 2),
        )

        assertEquals(
            50,
            readerProgressPercentForSourcePosition(
                sourceBlockIndex = 1,
                textOffset = 3,
                sourceBlocks = blocks,
            ),
        )
        assertEquals(66, readerProgressPercentForPageIndex(pageIndex = 1, pageCount = 3))
        assertEquals(57, readerProgressPercentForPageIndex(pageIndex = 3, pageCount = 7))
    }

    @Test
    fun readerProgressFractionMatchesDisplayedPercentForFooterBar() {
        assertEquals(0f, readerProgressFraction(-4), 0.0001f)
        assertEquals(0.46f, readerProgressFraction(46), 0.0001f)
        assertEquals(1f, readerProgressFraction(140), 0.0001f)
    }

    @Test
    fun readerProgressPercentUsesSourceBlockIdentityInsteadOfAssumingDenseIndexes() {
        val blocks = listOf(
            readerMarkdownBlock(rawBlock = "AAAAAA", sourceBlockIndex = 10),
            readerMarkdownBlock(rawBlock = "BBBBBB", sourceBlockIndex = 20),
            readerMarkdownBlock(rawBlock = "CCCCCC", sourceBlockIndex = 30),
        )

        assertEquals(
            50,
            readerProgressPercentForSourcePosition(
                sourceBlockIndex = 20,
                textOffset = 3,
                sourceBlocks = blocks,
            ),
        )
    }

    @Test
    fun readerPageBoundarySignatureChangesWhenSamePageCountBoundariesShift() {
        val fallbackPages = listOf(
            ReaderPage(start = 0, endInclusive = 9),
            ReaderPage(start = 10, endInclusive = 18),
            ReaderPage(start = 19, endInclusive = 27),
            ReaderPage(start = 28, endInclusive = 33),
        )
        val measuredPages = listOf(
            ReaderPage(start = 0, endInclusive = 8),
            ReaderPage(start = 9, endInclusive = 17),
            ReaderPage(start = 18, endInclusive = 26),
            ReaderPage(start = 27, endInclusive = 33),
        )

        assertEquals(fallbackPages.size, measuredPages.size)
        assertTrue(readerPageBoundarySignature(fallbackPages) != readerPageBoundarySignature(measuredPages))
        assertEquals(0, readerPageIndexForParagraph(pages = fallbackPages, paragraphIndex = 9))
        assertEquals(1, readerPageIndexForParagraph(pages = measuredPages, paragraphIndex = 9))
    }

    @Test
    fun readerLayoutSplitsLongSingleBlockBeforePagination() {
        val longParagraph = (1..20)
            .joinToString(separator = " ") { index ->
                "Sentence $index has enough calm reader prose to wrap across the fixed page viewport."
            }
        val blocks = listOf(
            readerMarkdownBlock("Short first paragraph."),
            readerMarkdownBlock(longParagraph),
            readerMarkdownBlock("Short final paragraph."),
        )

        val layout = splitOversizedReaderBlocks(blocks = blocks, maxBlockWeight = 8)
        val pages = readerPagesForBlocks(blocks = layout.blocks, maxPageWeight = 8)

        assertTrue(layout.blocks.size > blocks.size)
        assertEquals(1, layout.displayBlockIndexFor(1))
        assertTrue(layout.blocks.drop(1).dropLast(1).all { block -> block.text.text.length < longParagraph.length })
        assertTrue(pages.size >= 4)
        assertEquals(pages.lastIndex, readerPageIndexForParagraph(pages = pages, paragraphIndex = 99))
    }

    @Test
    fun adaptiveReaderPageWeightUsesViewportAndReaderFontScale() {
        val compactPhone = adaptiveReaderPageWeight(
            viewportWidthDp = 360f,
            viewportHeightDp = 420f,
            readerFontScale = 1.0,
        )
        val tallPhone = adaptiveReaderPageWeight(
            viewportWidthDp = 360f,
            viewportHeightDp = 780f,
            readerFontScale = 1.0,
        )
        val tallSmallText = adaptiveReaderPageWeight(
            viewportWidthDp = 360f,
            viewportHeightDp = 780f,
            readerFontScale = 0.9,
        )
        val tallLargeText = adaptiveReaderPageWeight(
            viewportWidthDp = 360f,
            viewportHeightDp = 780f,
            readerFontScale = 1.3,
        )

        assertTrue("Taller reader viewport should fit more content.", tallPhone > compactPhone)
        assertTrue("Tall phone pages should use most of the measured reader height.", tallPhone >= compactPhone + 12)
        assertTrue("Tall phone pages should keep avoidable blank space low.", tallPhone >= 25)
        assertTrue("Larger reader text should fit less content.", tallLargeText < tallPhone)
        assertTrue("Smaller reader text should fit more content.", tallSmallText > tallPhone)
        assertTrue("Smaller reader text should fit more content.", tallSmallText > tallLargeText)
        assertTrue("Small reader text should materially increase tall-phone page capacity.", tallSmallText >= 29)
        assertTrue("Page budget should include only a bounded tall-phone fill allowance.", tallPhone <= 32)
        assertTrue(
            "Reader text width should adapt to the app-level font size.",
            adaptiveReaderCharsPerLine(viewportWidthDp = 360f, readerFontScale = 0.9) >
                adaptiveReaderCharsPerLine(viewportWidthDp = 360f, readerFontScale = 1.3),
        )
        assertTrue("Reader block chunks should stay smaller than the page budget.", adaptiveReaderBlockChunkWeight(tallLargeText) < tallLargeText)
        assertTrue("Large reader text should stay within the measured line capacity after reserve.", tallLargeText <= 21)
    }

    @Test
    fun adaptiveReaderPageFitReturnsConsistentViewportBudget() {
        val compactFit = adaptiveReaderPageFit(
            viewportWidthDp = 360f,
            viewportHeightDp = 420f,
            readerFontScale = 1.0,
        )
        val tallFit = adaptiveReaderPageFit(
            viewportWidthDp = 360f,
            viewportHeightDp = 780f,
            readerFontScale = 1.0,
        )
        val largeTextFit = adaptiveReaderPageFit(
            viewportWidthDp = 360f,
            viewportHeightDp = 780f,
            readerFontScale = 1.3,
        )

        assertEquals(tallFit.maxPageWeight, adaptiveReaderPageWeight(360f, 780f, 1.0))
        assertEquals(tallFit.charsPerLine, adaptiveReaderCharsPerLine(360f, 1.0))
        assertEquals(tallFit.maxBlockWeight, adaptiveReaderBlockChunkWeight(tallFit.maxPageWeight))
        assertTrue("Compact screens should keep a larger per-block gap reserve.", compactFit.blockGapLineCost > tallFit.blockGapLineCost)
        assertTrue("Large reader text should not inherit compact screen spacing.", largeTextFit.blockGapLineCost < compactFit.blockGapLineCost)
        assertTrue("Measured tall reader viewport should admit more rendered blocks.", tallFit.maxBlocksPerPage > compactFit.maxBlocksPerPage)
        assertTrue("Large configured reader text should reduce rendered-block capacity.", largeTextFit.maxBlocksPerPage < tallFit.maxBlocksPerPage)
        assertTrue("Measured tall reader viewport should receive a larger page budget.", tallFit.maxPageWeight > compactFit.maxPageWeight)
        assertTrue("Large configured reader text should reduce line and page capacity.", largeTextFit.maxPageWeight < tallFit.maxPageWeight)
        assertTrue("Large configured reader text should reduce line width.", largeTextFit.charsPerLine < tallFit.charsPerLine)
    }

    @Test
    fun adaptiveReaderPaginationPacksMoreBlocksOnTallViewport() {
        val blocks = (1..24).map { index ->
            readerMarkdownBlock("Paragraph $index keeps enough words for reader wrapping while still being short enough to show packing differences.")
        }
        val compactFit = adaptiveReaderPageFit(
            viewportWidthDp = 360f,
            viewportHeightDp = 420f,
            readerFontScale = 1.0,
        )
        val tallFit = adaptiveReaderPageFit(
            viewportWidthDp = 360f,
            viewportHeightDp = 780f,
            readerFontScale = 1.0,
        )
        val compactPages = readerPagesForBlocks(
            blocks = blocks,
            maxPageWeight = compactFit.maxPageWeight,
            charsPerLine = compactFit.charsPerLine,
            blockGapLineCost = compactFit.blockGapLineCost,
            maxBlocksPerPage = compactFit.maxBlocksPerPage,
        )
        val tallPages = readerPagesForBlocks(
            blocks = blocks,
            maxPageWeight = tallFit.maxPageWeight,
            charsPerLine = tallFit.charsPerLine,
            blockGapLineCost = tallFit.blockGapLineCost,
            maxBlocksPerPage = tallFit.maxBlocksPerPage,
        )

        assertTrue("Tall viewport should put more reader blocks on the first page.", tallPages.first().endInclusive > compactPages.first().endInclusive)
        assertTrue("Tall viewport should need fewer reader pages for the same text.", tallPages.size < compactPages.size)
    }

    @Test
    fun measuredReaderPaginationUsesRenderedPixelBudgetAndHeadingBreaks() {
        val blocks = listOf(
            readerMarkdownBlock("Measured body paragraph 1."),
            readerMarkdownBlock("Measured body paragraph 2."),
            readerMarkdownBlock(rawBlock = "## Measured section"),
            readerMarkdownBlock("Measured body paragraph 3."),
        )

        val pages = readerPagesForMeasuredBlocks(
            blocks = blocks,
            blockHeightsPx = listOf(90, 90, 30, 90),
            maxPageHeightPx = 190,
        )

        assertEquals(listOf(ReaderPage(start = 0, endInclusive = 1), ReaderPage(start = 2, endInclusive = 3)), pages)
    }

    @Test
    fun adaptiveReaderPaginationCapsShortParagraphsByRenderedBlockHeight() {
        val shortBlocks = (1..32).map { index -> readerMarkdownBlock("Short paragraph $index.") }
        val tallFit = adaptiveReaderPageFit(
            viewportWidthDp = 411f,
            viewportHeightDp = 815f,
            readerFontScale = 1.0,
        )
        val tallLargeTextFit = adaptiveReaderPageFit(
            viewportWidthDp = 411f,
            viewportHeightDp = 815f,
            readerFontScale = 1.3,
        )

        val tallPages = readerPagesForBlocks(
            blocks = shortBlocks,
            maxPageWeight = tallFit.maxPageWeight,
            charsPerLine = tallFit.charsPerLine,
            blockGapLineCost = tallFit.blockGapLineCost,
            maxBlocksPerPage = tallFit.maxBlocksPerPage,
        )
        val tallLargeTextPages = readerPagesForBlocks(
            blocks = shortBlocks,
            maxPageWeight = tallLargeTextFit.maxPageWeight,
            charsPerLine = tallLargeTextFit.charsPerLine,
            blockGapLineCost = tallLargeTextFit.blockGapLineCost,
            maxBlocksPerPage = tallLargeTextFit.maxBlocksPerPage,
            readerFontScale = tallLargeTextFit.readerFontScale,
        )

        val tallBlockCount = tallPages.first().endInclusive - tallPages.first().start + 1
        val tallLargeTextBlockCount = tallLargeTextPages.first().endInclusive - tallLargeTextPages.first().start + 1
        assertTrue("Tall one-line pages must obey the rendered block-height cap.", tallBlockCount <= tallFit.maxBlocksPerPage)
        assertTrue("Tall default one-line pages must not admit the 20-block clipping case.", tallBlockCount < 20)
        assertTrue("Tall large-text one-line pages must obey the rendered block-height cap.", tallLargeTextBlockCount <= tallLargeTextFit.maxBlocksPerPage)
        assertTrue("Tall large-text one-line pages must not admit the 18-block clipping case.", tallLargeTextBlockCount < 18)
    }

    @Test
    fun adaptiveReaderPaginationChargesBodyLikeBlocksForRenderedPadding() {
        val twoLineBodyBlocks = (1..24).map { index ->
            readerMarkdownBlock("Large text line $index.\nSecond rendered line $index.")
        }
        val tallLargeTextFit = adaptiveReaderPageFit(
            viewportWidthDp = 411f,
            viewportHeightDp = 815f,
            readerFontScale = 1.3,
        )

        val tallLargeTextPages = readerPagesForBlocks(
            blocks = twoLineBodyBlocks,
            maxPageWeight = tallLargeTextFit.maxPageWeight,
            charsPerLine = tallLargeTextFit.charsPerLine,
            blockGapLineCost = tallLargeTextFit.blockGapLineCost,
            maxBlocksPerPage = tallLargeTextFit.maxBlocksPerPage,
            readerFontScale = tallLargeTextFit.readerFontScale,
        )

        val tallLargeTextBodyBlockCount = tallLargeTextPages.first().endInclusive - tallLargeTextPages.first().start + 1
        assertTrue("Tall large-text two-line body pages must not admit the 10-block clipping case.", tallLargeTextBodyBlockCount < 10)
        assertTrue("Tall large-text two-line body pages should account for body bottom padding.", tallLargeTextBodyBlockCount <= 9)
    }

    @Test
    fun adaptiveReaderPaginationCostsCodeBlocksByRenderedPadding() {
        val codeBlocks = (1..32).map { index ->
            readerMarkdownBlock(
                rawBlock = """
                    ```kotlin
                    val sprint17CodeBlock$index = $index
                    ```
                """.trimIndent(),
            )
        }
        val tallFit = adaptiveReaderPageFit(
            viewportWidthDp = 411f,
            viewportHeightDp = 815f,
            readerFontScale = 1.0,
        )
        val tallLargeTextFit = adaptiveReaderPageFit(
            viewportWidthDp = 411f,
            viewportHeightDp = 815f,
            readerFontScale = 1.3,
        )
        val tallPages = readerPagesForBlocks(
            blocks = codeBlocks,
            maxPageWeight = tallFit.maxPageWeight,
            charsPerLine = tallFit.charsPerLine,
            blockGapLineCost = tallFit.blockGapLineCost,
            maxBlocksPerPage = tallFit.maxBlocksPerPage,
            readerFontScale = tallFit.readerFontScale,
        )
        val tallLargeTextPages = readerPagesForBlocks(
            blocks = codeBlocks,
            maxPageWeight = tallLargeTextFit.maxPageWeight,
            charsPerLine = tallLargeTextFit.charsPerLine,
            blockGapLineCost = tallLargeTextFit.blockGapLineCost,
            maxBlocksPerPage = tallLargeTextFit.maxBlocksPerPage,
            readerFontScale = tallLargeTextFit.readerFontScale,
        )

        val tallCodeBlockCount = tallPages.first().endInclusive - tallPages.first().start + 1
        val tallLargeCodeBlockCount = tallLargeTextPages.first().endInclusive - tallLargeTextPages.first().start + 1
        assertTrue("Tall default one-line code pages should still use the visible, footer-safe viewport.", tallCodeBlockCount >= 20)
        assertTrue("Tall default one-line code pages should fit within rendered code padding.", tallCodeBlockCount <= 20)
        assertTrue(
            "Tall large-text one-line code pages should still use the visible, footer-safe viewport. " +
                "count=$tallLargeCodeBlockCount fit=$tallLargeTextFit",
            tallLargeCodeBlockCount >= 18,
        )
        assertTrue("Tall large-text one-line code pages should fit within rendered code padding.", tallLargeCodeBlockCount <= 18)
    }

    @Test
    fun adaptiveReaderPaginationCostsMultiLineCodeByFullRenderedLineHeight() {
        val codeBlocks = (1..10).map { index ->
            readerMarkdownBlock(
                rawBlock = """
                    ```kotlin
                    val sprint17CodeBlock${index}_line1 = 1
                    val sprint17CodeBlock${index}_line2 = 2
                    val sprint17CodeBlock${index}_line3 = 3
                    val sprint17CodeBlock${index}_line4 = 4
                    val sprint17CodeBlock${index}_line5 = 5
                    val sprint17CodeBlock${index}_line6 = 6
                    val sprint17CodeBlock${index}_line7 = 7
                    val sprint17CodeBlock${index}_line8 = 8
                    ```
                """.trimIndent(),
            )
        }
        val tallFit = adaptiveReaderPageFit(
            viewportWidthDp = 411f,
            viewportHeightDp = 815f,
            readerFontScale = 1.0,
        )
        val tallLargeTextFit = adaptiveReaderPageFit(
            viewportWidthDp = 411f,
            viewportHeightDp = 815f,
            readerFontScale = 1.3,
        )
        val tallLargeTextLayout = splitOversizedReaderBlocks(
            blocks = codeBlocks,
            maxBlockWeight = tallLargeTextFit.maxBlockWeight,
            charsPerLine = tallLargeTextFit.charsPerLine,
        )

        val tallPages = readerPagesForBlocks(
            blocks = codeBlocks,
            maxPageWeight = tallFit.maxPageWeight,
            charsPerLine = tallFit.charsPerLine,
            blockGapLineCost = tallFit.blockGapLineCost,
            maxBlocksPerPage = tallFit.maxBlocksPerPage,
            readerFontScale = tallFit.readerFontScale,
        )
        val tallLargeTextPages = readerPagesForBlocks(
            blocks = codeBlocks,
            maxPageWeight = tallLargeTextFit.maxPageWeight,
            charsPerLine = tallLargeTextFit.charsPerLine,
            blockGapLineCost = tallLargeTextFit.blockGapLineCost,
            maxBlocksPerPage = tallLargeTextFit.maxBlocksPerPage,
            readerFontScale = tallLargeTextFit.readerFontScale,
        )

        val tallCodeBlockCount = tallPages.first().endInclusive - tallPages.first().start + 1
        val tallLargeCodeBlockCount = tallLargeTextPages.first().endInclusive - tallLargeTextPages.first().start + 1
        assertEquals(
            "Large-text eight-line code blocks should stay whole because the rendered code cost still fits one page.",
            codeBlocks.size,
            tallLargeTextLayout.blocks.size,
        )
        assertEquals("Tall default eight-line code pages should admit four footer-safe rendered blocks.", 4, tallCodeBlockCount)
        assertEquals("Tall large-text eight-line code pages should admit one rendered-safe block.", 1, tallLargeCodeBlockCount)
    }

    @Test
    fun adaptiveReaderPaginationPacksShortMultiLineCodeWithoutUnderfill() {
        fun repeatedCodeBlocks(lineCount: Int): List<ReaderMarkdownBlock> {
            return (1..24).map { index ->
                val codeLines = (1..lineCount).joinToString(separator = "\n") { line ->
                    "val code${lineCount}Line${index}_$line = ${index + line}"
                }
                readerMarkdownBlock(rawBlock = "```kotlin\n$codeLines\n```")
            }
        }
        val tallFit = adaptiveReaderPageFit(
            viewportWidthDp = 411f,
            viewportHeightDp = 815f,
            readerFontScale = 1.0,
        )
        val expectedFirstPageBlocksByLineCount = mapOf(
            2 to 13,
            3 to 9,
            4 to 7,
            5 to 6,
            6 to 5,
            7 to 4,
            8 to 4,
            9 to 3,
            10 to 3,
            11 to 3,
        )

        expectedFirstPageBlocksByLineCount.forEach { (lineCount, expectedFirstPageBlocks) ->
            val tallPages = readerPagesForBlocks(
                blocks = repeatedCodeBlocks(lineCount),
                maxPageWeight = tallFit.maxPageWeight,
                charsPerLine = tallFit.charsPerLine,
                blockGapLineCost = tallFit.blockGapLineCost,
                maxBlocksPerPage = tallFit.maxBlocksPerPage,
                readerFontScale = tallFit.readerFontScale,
            )
            val tallCodeBlockCount = tallPages.first().endInclusive - tallPages.first().start + 1
            assertEquals(
                "Tall default $lineCount-line CODE pages should admit only rendered-safe blocks.",
                expectedFirstPageBlocks,
                tallCodeBlockCount,
            )
            assertTrue("Tall default $lineCount-line CODE should still leave a real next page.", tallPages.size > 1)
        }
    }

    @Test
    fun adaptiveReaderPaginationSplitsOversizedShortLineCodeBlocksBeforePaging() {
        fun shortLineCodeBlock(lineCount: Int, sourceBlockIndex: Int = 0): ReaderMarkdownBlock {
            return readerMarkdownBlock(
                rawBlock = "```kotlin\n" +
                    (1..lineCount).joinToString(separator = "\n") { line -> "x$line" } +
                    "\n```",
                sourceBlockIndex = sourceBlockIndex,
            )
        }
        val oversizedCodeBlock = shortLineCodeBlock(lineCount = 40)
        val splitTailCodeBlock = shortLineCodeBlock(lineCount = 36)
        val adjacentWholeCodeBlocks = listOf(
            shortLineCodeBlock(lineCount = 19, sourceBlockIndex = 0),
            shortLineCodeBlock(lineCount = 17, sourceBlockIndex = 1),
        )
        val mixedCodeAndBodyBlocks = listOf(
            shortLineCodeBlock(lineCount = 34, sourceBlockIndex = 0),
            readerMarkdownBlock(
                rawBlock = "A short body tail must not squeeze below two full code chunks.",
                sourceBlockIndex = 1,
            ),
        )
        val tallFit = adaptiveReaderPageFit(
            viewportWidthDp = 411f,
            viewportHeightDp = 815f,
            readerFontScale = 1.0,
        )
        val tallLargeTextFit = adaptiveReaderPageFit(
            viewportWidthDp = 411f,
            viewportHeightDp = 815f,
            readerFontScale = 1.3,
        )

        val tallLayout = splitOversizedReaderBlocks(
            blocks = listOf(oversizedCodeBlock),
            maxBlockWeight = tallFit.maxBlockWeight,
            charsPerLine = tallFit.charsPerLine,
        )
        val tallMixedCodeAndBodyLayout = splitOversizedReaderBlocks(
            blocks = mixedCodeAndBodyBlocks,
            maxBlockWeight = tallFit.maxBlockWeight,
            charsPerLine = tallFit.charsPerLine,
        )
        val tallLargeTextLayout = splitOversizedReaderBlocks(
            blocks = listOf(oversizedCodeBlock),
            maxBlockWeight = tallLargeTextFit.maxBlockWeight,
            charsPerLine = tallLargeTextFit.charsPerLine,
        )
        val tallSplitTailLayout = splitOversizedReaderBlocks(
            blocks = listOf(splitTailCodeBlock),
            maxBlockWeight = tallFit.maxBlockWeight,
            charsPerLine = tallFit.charsPerLine,
        )
        val tallAdjacentWholeLayout = splitOversizedReaderBlocks(
            blocks = adjacentWholeCodeBlocks,
            maxBlockWeight = tallFit.maxBlockWeight,
            charsPerLine = tallFit.charsPerLine,
        )
        val tallPages = readerPagesForBlocks(
            blocks = tallLayout.blocks,
            maxPageWeight = tallFit.maxPageWeight,
            charsPerLine = tallFit.charsPerLine,
            blockGapLineCost = tallFit.blockGapLineCost,
            maxBlocksPerPage = tallFit.maxBlocksPerPage,
            readerFontScale = tallFit.readerFontScale,
        )
        val tallLargeTextPages = readerPagesForBlocks(
            blocks = tallLargeTextLayout.blocks,
            maxPageWeight = tallLargeTextFit.maxPageWeight,
            charsPerLine = tallLargeTextFit.charsPerLine,
            blockGapLineCost = tallLargeTextFit.blockGapLineCost,
            maxBlocksPerPage = tallLargeTextFit.maxBlocksPerPage,
            readerFontScale = tallLargeTextFit.readerFontScale,
        )
        val tallSplitTailPages = readerPagesForBlocks(
            blocks = tallSplitTailLayout.blocks,
            maxPageWeight = tallFit.maxPageWeight,
            charsPerLine = tallFit.charsPerLine,
            blockGapLineCost = tallFit.blockGapLineCost,
            maxBlocksPerPage = tallFit.maxBlocksPerPage,
            readerFontScale = tallFit.readerFontScale,
        )
        val tallAdjacentWholePages = readerPagesForBlocks(
            blocks = tallAdjacentWholeLayout.blocks,
            maxPageWeight = tallFit.maxPageWeight,
            charsPerLine = tallFit.charsPerLine,
            blockGapLineCost = tallFit.blockGapLineCost,
            maxBlocksPerPage = tallFit.maxBlocksPerPage,
            readerFontScale = tallFit.readerFontScale,
        )
        val tallMixedCodeAndBodyPages = readerPagesForBlocks(
            blocks = tallMixedCodeAndBodyLayout.blocks,
            maxPageWeight = tallFit.maxPageWeight,
            charsPerLine = tallFit.charsPerLine,
            blockGapLineCost = tallFit.blockGapLineCost,
            maxBlocksPerPage = tallFit.maxBlocksPerPage,
            readerFontScale = tallFit.readerFontScale,
        )

        assertEquals(listOf(17, 17, 6), tallLayout.blocks.map { block -> block.text.text.lines().size })
        assertEquals(listOf(15, 15, 10), tallLargeTextLayout.blocks.map { block -> block.text.text.lines().size })
        assertEquals(listOf(17, 17, 2), tallSplitTailLayout.blocks.map { block -> block.text.text.lines().size })
        assertEquals(listOf(17, 2, 17), tallAdjacentWholeLayout.blocks.map { block -> block.text.text.lines().size })
        assertEquals(listOf(17, 17, 1), tallMixedCodeAndBodyLayout.blocks.map { block -> block.text.text.lines().size })
        assertEquals(listOf(0, 0, 1), tallAdjacentWholeLayout.blocks.map { block -> block.sourceBlockIndex })
        assertEquals(listOf(0, 0, 1), tallMixedCodeAndBodyLayout.blocks.map { block -> block.sourceBlockIndex })
        assertEquals(listOf(0, 59, 127), tallLayout.blocks.map { block -> block.sourceTextStartOffset })
        assertEquals(listOf(0, 59, 127), tallSplitTailLayout.blocks.map { block -> block.sourceTextStartOffset })
        assertTrue("Default oversized short-line CODE should need multiple pages.", tallPages.size > 1)
        assertTrue("Large-text oversized short-line CODE should need multiple pages.", tallLargeTextPages.size > 1)
        assertTrue("Default split-tail short-line CODE should need multiple pages.", tallSplitTailPages.size > 1)
        assertTrue("Adjacent whole short-line CODE should need multiple pages.", tallAdjacentWholePages.size > 1)
        assertTrue("Mixed CODE and body should need multiple pages.", tallMixedCodeAndBodyPages.size > 1)
        assertEquals("Default oversized CODE first chunk should remain page-contained.", 0, tallPages.first().start)
        assertEquals("Default oversized CODE first page should keep only one 17-line chunk above the footer.", 0, tallPages.first().endInclusive)
        assertEquals("Large-text oversized CODE first chunk should remain page-contained.", 0, tallLargeTextPages.first().start)
        assertEquals("Large-text oversized CODE first page should reject the next 15-line chunk.", 0, tallLargeTextPages.first().endInclusive)
        assertEquals("Large-text oversized CODE second page should start at the second 15-line chunk.", 1, tallLargeTextPages[1].start)
        assertEquals("Large-text oversized CODE second page should safely admit the 10-line tail.", 2, tallLargeTextPages[1].endInclusive)
        assertEquals("Default split-tail CODE first page should keep only one 17-line chunk above the footer.", 0, tallSplitTailPages.first().endInclusive)
        assertEquals("Adjacent whole CODE first source block should split before the unsafe 19+17 geometry.", 1, tallAdjacentWholePages.first().endInclusive)
        assertEquals("Adjacent whole CODE should move the following 17-line block to the next page.", 2, tallAdjacentWholePages[1].start)
        assertEquals("Mixed CODE and body first page should keep only one 17-line CODE chunk above the footer.", 0, tallMixedCodeAndBodyPages.first().endInclusive)
        assertEquals("Mixed CODE and body should move the second code chunk to the next page.", 1, tallMixedCodeAndBodyPages[1].start)
        assertEquals("Mixed CODE and body second page should safely admit the body-like tail.", 2, tallMixedCodeAndBodyPages[1].endInclusive)
    }

    @Test
    fun readerProgressSourceIndexSurvivesAdaptiveResplitting() {
        val longParagraph = (1..60)
            .joinToString(separator = " ") { index -> "sourceword$index" }
            .plus(".")
        val sourceBlocks = listOf(
            readerMarkdownBlock(rawBlock = longParagraph, sourceBlockIndex = 0),
            readerMarkdownBlock(rawBlock = "Stable second source block for progress.", sourceBlockIndex = 1),
            readerMarkdownBlock(rawBlock = "Stable third source block for progress.", sourceBlockIndex = 2),
        )
        val compactLayout = splitOversizedReaderBlocks(
            blocks = sourceBlocks,
            maxBlockWeight = 7,
            charsPerLine = 28,
        )
        val compactPages = readerPagesForBlocks(
            blocks = compactLayout.blocks,
            maxPageWeight = 18,
            charsPerLine = 28,
        )
        val savedSourcePosition = compactLayout.sourcePositionForDisplayBlock(compactPages.first().endInclusive)
        val roomyLayout = splitOversizedReaderBlocks(
            blocks = sourceBlocks,
            maxBlockWeight = 12,
            charsPerLine = 42,
        )
        val restoredDisplayIndex = roomyLayout.displayBlockIndexForSourcePosition(
            sourceBlockIndex = savedSourcePosition.sourceBlockIndex,
            textOffset = savedSourcePosition.textOffset,
        )

        assertEquals(savedSourcePosition.sourceBlockIndex, roomyLayout.blocks[restoredDisplayIndex].sourceBlockIndex)
        assertTrue(roomyLayout.blocks[restoredDisplayIndex].sourceTextStartOffset < savedSourcePosition.textOffset)
        assertTrue(roomyLayout.blocks[restoredDisplayIndex].sourceTextEndOffset() >= savedSourcePosition.textOffset)
    }

    @Test
    fun readerLayoutMapsSourceSelectorIntoSplitDisplayBlock() {
        val longParagraph = (1..18)
            .joinToString(separator = " ") { index ->
                "Sentence $index has enough calm reader prose to wrap across the adaptive page."
            }
        val blocks = listOf(readerMarkdownBlock(rawBlock = longParagraph, sourceBlockIndex = 0))
        val layout = splitOversizedReaderBlocks(
            blocks = blocks,
            maxBlockWeight = 6,
            charsPerLine = 34,
        )
        val laterChunk = layout.blocks.indexOfFirst { block -> block.sourceTextStartOffset > 0 }

        assertTrue("Expected the source paragraph to split into multiple display chunks.", laterChunk > 0)
        val selector = ReadingAnnotationSelector(
            sourceBlockIndex = 0,
            textStartOffset = layout.blocks[laterChunk].sourceTextStartOffset + 4,
            textEndOffset = layout.blocks[laterChunk].sourceTextStartOffset + 24,
        )

        assertEquals(laterChunk, layout.displayBlockIndexForSelector(selector))
    }

    @Test
    fun readerAnnotationMappingPrefersSourceSelectorOverStaleParagraphIndex() {
        val firstBlock = readerMarkdownBlock(
            rawBlock = "Repeated quote lives in the first source paragraph only.",
            sourceBlockIndex = 0,
        )
        val secondBlock = readerMarkdownBlock(
            rawBlock = "Repeated quote appears in display text but is not the saved source.",
            sourceBlockIndex = 1,
        )
        val sourceAnchoredAnnotation = ReadingAnnotation(
            id = "annotation-1",
            contentId = "content-1",
            paragraphIndex = 1,
            quotedText = "Repeated quote",
            noteText = "source anchored",
            createdAtMillis = 1L,
            updatedAtMillis = 2L,
            selector = ReadingAnnotationSelector(
                sourceBlockIndex = 0,
                textStartOffset = 0,
                textEndOffset = 14,
            ),
        )
        val annotationsByParagraph = mapOf(sourceAnchoredAnnotation.paragraphIndex to sourceAnchoredAnnotation)
        val annotations = listOf(sourceAnchoredAnnotation)

        assertEquals(
            sourceAnchoredAnnotation,
            readingAnnotationForBlock(
                paragraphIndex = 0,
                block = firstBlock,
                annotationsByParagraph = annotationsByParagraph,
                annotationsForContent = annotations,
            ),
        )
        assertEquals(
            null,
            readingAnnotationForBlock(
                paragraphIndex = 1,
                block = secondBlock,
                annotationsByParagraph = annotationsByParagraph,
                annotationsForContent = annotations,
            ),
        )
    }

    @Test
    fun readerSelectionRangesRefineWithinSingleSentence() {
        val sentence = "A long sentence can begin with one idea, continue with a second phrase, and finish with enough concrete words to refine annotation boundaries."

        val ranges = readerSelectionRanges(sentence)

        assertTrue("Expected phrase-level selection handles inside the sentence.", ranges.size >= 3)
        assertEquals(0, ranges.first().start)
        assertEquals(sentence.length, ranges.last().endExclusive)
        assertTrue(ranges.all { range -> range.start >= 0 && range.endExclusive <= sentence.length })
        assertTrue(ranges.zipWithNext().all { (left, right) -> left.endExclusive <= right.start })
    }

    @Test
    fun splitReaderBlocksPreserveSourceOffsetsForCrossPageSelection() {
        val longSentence = (1..50)
            .joinToString(separator = " ") { index -> "sourceword$index" }
            .plus(".")
        val block = readerMarkdownBlock(longSentence)

        val layout = splitOversizedReaderBlocks(blocks = listOf(block), maxBlockWeight = 8)

        assertTrue("Expected the long source block to split across display pages.", layout.blocks.size > 1)
        assertEquals(longSentence, layout.blocks.first().sourceFullText)
        assertEquals(longSentence, layout.blocks.last().sourceFullText)
        assertTrue(layout.blocks.last().sourceTextStartOffset > 0)
    }

    @Test
    fun readerPagesKeepSplitSourceChunksContinuousAcrossAdjacentPages() {
        val longSentence = (1..120)
            .chunked(8)
            .joinToString(separator = " ") { chunk ->
                chunk.joinToString(separator = " ") { index -> "anchor$index" }.plus(".")
            }
        val maxPageWeight = adaptiveReaderPageWeight(
            viewportWidthDp = 360f,
            viewportHeightDp = 780f,
            readerFontScale = 1.3,
        )
        val charsPerLine = adaptiveReaderCharsPerLine(
            viewportWidthDp = 360f,
            readerFontScale = 1.3,
        )
        val layout = splitOversizedReaderBlocks(
            blocks = listOf(readerMarkdownBlock(longSentence)),
            maxBlockWeight = adaptiveReaderBlockChunkWeight(maxPageWeight),
            charsPerLine = charsPerLine,
        )
        val pages = readerPagesForBlocks(
            blocks = layout.blocks,
            maxPageWeight = maxPageWeight,
            charsPerLine = charsPerLine,
        )

        assertTrue("Expected the source paragraph to split across reader pages.", pages.size > 1)
        pages.zipWithNext().forEach { (leftPage, rightPage) ->
            val leftBlock = layout.blocks[leftPage.endInclusive]
            val rightBlock = layout.blocks[rightPage.start]
            if (leftBlock.sourceBlockIndex == rightBlock.sourceBlockIndex) {
                val sourceText = leftBlock.sourceFullText ?: longSentence
                val leftEnd = leftBlock.sourceTextStartOffset + leftBlock.text.text.length
                val rightStart = rightBlock.sourceTextStartOffset
                assertTrue("Adjacent page chunks must not move backward in source text.", rightStart >= leftEnd)
                assertTrue(
                    "Adjacent reader pages must not skip non-blank source text.",
                    sourceText.substring(leftEnd, rightStart).isBlank(),
                )
            }
        }
    }

    @Test
    fun readerAnnotationSelectionEndCanMoveBeyondCurrentPage() {
        val longSentence = (1..120)
            .chunked(8)
            .joinToString(separator = " ") { chunk ->
                chunk.joinToString(separator = " ") { index -> "anchor$index" }.plus(".")
            }
        val maxPageWeight = 8
        val charsPerLine = 34
        val layout = splitOversizedReaderBlocks(
            blocks = listOf(readerMarkdownBlock(rawBlock = longSentence, sourceBlockIndex = 0)),
            maxBlockWeight = 6,
            charsPerLine = charsPerLine,
        )
        val pages = readerPagesForBlocks(
            blocks = layout.blocks,
            maxPageWeight = maxPageWeight,
            charsPerLine = charsPerLine,
        )
        assertTrue("Expected split source to create multiple reader pages.", pages.size > 1)
        val firstDisplayBlock = layout.blocks[pages.first().start].copy(sourceFullText = longSentence)
        val startOffset = firstDisplayBlock.text.text.indexOf("anchor8").coerceAtLeast(0)
        var selection = initialReaderAnnotationSelection(
            paragraphIndex = pages.first().start,
            block = firstDisplayBlock,
            charOffset = startOffset,
            annotation = null,
        )
        val initialEndPage = readerPageIndexForAnnotationSelectionFocus(
            selection = selection,
            focus = ReaderAnnotationSelectionFocus.END,
            layout = layout,
            pages = pages,
        )

        repeat(40) {
            if (!selection.quotedText.contains("anchor96") && selection.canExpandEnd) {
                selection = selection.expandEnd()
            }
        }

        assertTrue("Expected end expansion to reach a later source chunk.", selection.quotedText.contains("anchor96"))
        assertTrue(
            "Selection end should move beyond the original display page.",
            selection.selector.textEndOffset > layout.blocks[pages[initialEndPage].endInclusive].sourceTextEndOffset(),
        )
        assertTrue(
            "Selection focus should resolve to a later reader page.",
            readerPageIndexForAnnotationSelectionFocus(
                selection = selection,
                focus = ReaderAnnotationSelectionFocus.END,
                layout = layout,
                pages = pages,
            ) > initialEndPage,
        )
        val expandedEndOffset = selection.selector.textEndOffset
        val shrunkSelection = selection.shrinkEnd()
        assertTrue("End contraction should refine the cross-page range.", shrunkSelection.selector.textEndOffset < expandedEndOffset)
    }

    @Test
    fun readerAnnotationSelectionStartCanMoveBeforeCurrentPage() {
        val longSentence = (1..120)
            .chunked(8)
            .joinToString(separator = " ") { chunk ->
                chunk.joinToString(separator = " ") { index -> "anchor$index" }.plus(".")
            }
        val maxPageWeight = 8
        val charsPerLine = 34
        val layout = splitOversizedReaderBlocks(
            blocks = listOf(readerMarkdownBlock(rawBlock = longSentence, sourceBlockIndex = 0)),
            maxBlockWeight = 6,
            charsPerLine = charsPerLine,
        )
        val sourceSentenceCount = readerSentenceRanges(longSentence).size
        val pages = readerPagesForBlocks(
            blocks = layout.blocks,
            maxPageWeight = maxPageWeight,
            charsPerLine = charsPerLine,
        )
        val laterDisplayBlockIndex = layout.blocks.indexOfFirst { block -> block.text.text.contains("anchor96") }
        assertTrue("Expected anchor96 to live in a later split display chunk.", laterDisplayBlockIndex > 0)
        val laterDisplayBlock = layout.blocks[laterDisplayBlockIndex].copy(sourceFullText = longSentence)
        val laterCharOffset = laterDisplayBlock.text.text.indexOf("anchor96").coerceAtLeast(0)
        var selection = initialReaderAnnotationSelection(
            paragraphIndex = laterDisplayBlockIndex,
            block = laterDisplayBlock,
            charOffset = laterCharOffset,
            annotation = null,
        )
        val initialStartPage = readerPageIndexForAnnotationSelectionFocus(
            selection = selection,
            focus = ReaderAnnotationSelectionFocus.START,
            layout = layout,
            pages = pages,
        )
        assertTrue(
            "Expected initial selection to start after the first page. " +
                "initialStartPage=$initialStartPage laterDisplayBlockIndex=$laterDisplayBlockIndex " +
                "sourceStart=${laterDisplayBlock.sourceTextStartOffset} selectorStart=${selection.selector.textStartOffset} " +
                "sourceSentenceCount=$sourceSentenceCount quote=${selection.quotedText.take(80)}",
            initialStartPage > 0,
        )

        val earlyTargetOffset = longSentence.indexOf("anchor8").coerceAtLeast(0)
        repeat(40) {
            if (selection.selector.textStartOffset > earlyTargetOffset && selection.canExpandStart) {
                selection = selection.expandStart()
            }
        }

        assertTrue("Expected start expansion to reach an earlier source chunk.", selection.selector.textStartOffset <= earlyTargetOffset)
        assertTrue(
            "Selection start should resolve to an earlier reader page.",
            readerPageIndexForAnnotationSelectionFocus(
                selection = selection,
                focus = ReaderAnnotationSelectionFocus.START,
                layout = layout,
                pages = pages,
            ) < initialStartPage,
        )
    }

    @Test
    fun readerAnnotationSelectionStartCanMoveIntoPreviousSourceBlock() {
        val sourceBlocks = listOf(
            readerMarkdownBlock(
                rawBlock = "alpha1 alpha2 alpha3 alpha4. alpha5 alpha6 alpha7 alpha8.",
                sourceBlockIndex = 0,
            ),
            readerMarkdownBlock(
                rawBlock = "middle1 middle2 middle3 middle4. middle5 middle6 middle7 middle8.",
                sourceBlockIndex = 1,
            ),
            readerMarkdownBlock(
                rawBlock = "omega1 omega2 omega3 omega4. omega5 omega6 omega7 omega8.",
                sourceBlockIndex = 2,
            ),
        )
        val layout = splitOversizedReaderBlocks(blocks = sourceBlocks, maxBlockWeight = 40)
        val laterBlock = layout.blocks.last()
        var selection = initialReaderAnnotationSelection(
            paragraphIndex = 2,
            block = laterBlock,
            charOffset = laterBlock.text.text.indexOf("omega5").coerceAtLeast(0),
            annotation = null,
            selectionBlocks = layout.blocks,
        )

        repeat(20) {
            if (selection.selector.sourceBlockIndex > 0 && selection.canExpandStart) {
                selection = selection.expandStart()
            }
        }

        assertEquals(0, selection.selector.sourceBlockIndex)
        assertEquals(2, selection.selector.endSourceBlockIndex)
        assertTrue("Expected the quote to include text before the original page block.", selection.quotedText.contains("alpha"))
        assertTrue("Expected the quote to preserve the original later-page anchor.", selection.quotedText.contains("omega5"))
        assertTrue("Expected a multi-block selector to keep an end offset in the ending block.", selection.selector.textEndOffset > 0)
    }

    @Test
    fun readerAnnotationSelectionStartPreservesEndAcrossManySourceBlocks() {
        val sourceBlocks = (0..11).map { index ->
            readerMarkdownBlock(
                rawBlock = "sourceblock$index carries one compact sentence for cross page start selection.",
                sourceBlockIndex = index,
            )
        }
        val layout = splitOversizedReaderBlocks(blocks = sourceBlocks, maxBlockWeight = 40)
        val laterBlock = layout.blocks.first { block -> block.sourceBlockIndex == 9 }
        var selection = initialReaderAnnotationSelection(
            paragraphIndex = 9,
            block = laterBlock,
            charOffset = 0,
            annotation = null,
            selectionBlocks = layout.blocks,
        )

        repeat(32) {
            if (selection.canExpandStart) {
                selection = selection.expandStart()
            }
        }

        assertEquals(0, selection.selector.sourceBlockIndex)
        assertEquals(9, selection.selector.endSourceBlockIndex)
        assertTrue("Expected the quote to include the earliest source block.", selection.quotedText.contains("sourceblock0"))
        assertTrue("Expected the quote to preserve the original ending block.", selection.quotedText.contains("sourceblock9"))
    }

    @Test
    fun dayLabelsUseSingularAndPluralCopy() {
        assertEquals("1 day", dayCountLabel(count = 1, singular = "day"))
        assertEquals("2 days", dayCountLabel(count = 2, singular = "day"))
        assertEquals("1 app", quantityLabel(count = 1, singular = "app"))
        assertEquals("2 apps", quantityLabel(count = 2, singular = "app"))
        assertEquals("1 item", quantityLabel(count = 1, singular = "item"))
        assertEquals("2 items", quantityLabel(count = 2, singular = "item"))
        assertEquals("day converted", convertedDayNounLabel(count = 1))
        assertEquals("days converted", convertedDayNounLabel(count = 2))
    }

    @Test
    fun readerBlocksForDisplayDoesNotTreatIntrawordUnderscoresAsItalic() {
        val blocks = readerBlocksForDisplay(
            body = "Keep imported_notes_v1.md readable while _intentional emphasis_ still works.",
            fallback = "Fallback.",
        )

        assertEquals(1, blocks.size)
        assertEquals(
            "Keep imported_notes_v1.md readable while intentional emphasis still works.",
            blocks[0].text.text,
        )
        assertEquals(1, blocks[0].text.spanStyles.count { range -> range.item.fontStyle == FontStyle.Italic })
    }

    @Test
    fun readerProgressPercentTracksVisibleParagraphsInsteadOfElapsedTime() {
        assertEquals(0, readerProgressPercent(lastVisibleItemIndex = 0, paragraphCount = 10))
        assertEquals(30, readerProgressPercent(lastVisibleItemIndex = 3, paragraphCount = 10))
        assertEquals(100, readerProgressPercent(lastVisibleItemIndex = 20, paragraphCount = 10))
    }

    @Test
    fun readerProgressPercentForReaderListIgnoresHeaderItem() {
        assertEquals(0, readerProgressPercentForReaderList(lastVisibleItemIndex = 0, paragraphCount = 10))
        assertEquals(0, readerProgressPercentForReaderList(lastVisibleItemIndex = 1, paragraphCount = 10))
        assertEquals(30, readerProgressPercentForReaderList(lastVisibleItemIndex = 4, paragraphCount = 10))
        assertEquals(100, readerProgressPercentForReaderList(lastVisibleItemIndex = 20, paragraphCount = 10))
    }

    private fun event(
        type: AnalyticsEventType,
        interventionId: String,
        timestampMillis: Long = 1_000L,
        metadata: Map<String, String> = emptyMap(),
    ): AnalyticsEvent {
        return AnalyticsEvent(
            type = type,
            timestampMillis = timestampMillis,
            interventionId = interventionId,
            targetAppPackage = "com.fixture",
            metadata = metadata,
        )
    }

    private fun replacementEntry(
        sessionId: String,
        acceptedAtMillis: Long,
        completedAtMillis: Long? = null,
        contentDurationMinutes: Int = 10,
        contentId: String = "content-$sessionId",
    ): ReplacementHistoryEntry {
        return ReplacementHistoryEntry(
            sessionId = sessionId,
            interventionId = "intervention-$sessionId",
            targetAppPackage = "com.fixture",
            targetAppDisplayName = "Fixture Feed",
            interventionShownAtMillis = acceptedAtMillis - 100L,
            primaryContentId = contentId,
            backupContentIds = emptyList(),
            contentId = contentId,
            contentTitle = "Replacement $sessionId",
            contentDescription = "A finite replacement.",
            contentDurationMinutes = contentDurationMinutes,
            contentTopics = setOf(TopicTag.SCIENCE),
            packId = "science",
            recommendationSource = RecommendationSource.PRIMARY,
            acceptedAtMillis = acceptedAtMillis,
            completedAtMillis = completedAtMillis,
        )
    }

    private fun LocalDate.toMillis(): Long {
        return atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    }
}

private fun androidx.compose.ui.text.AnnotatedString.hasSpan(
    predicate: (androidx.compose.ui.text.SpanStyle) -> Boolean,
): Boolean {
    return spanStyles.any { range -> predicate(range.item) }
}
