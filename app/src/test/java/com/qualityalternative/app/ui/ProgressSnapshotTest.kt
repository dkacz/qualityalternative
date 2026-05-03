package com.qualityalternative.app.ui

import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.qualityalternative.app.domain.model.AnalyticsEvent
import com.qualityalternative.app.domain.model.AnalyticsEventType
import com.qualityalternative.app.domain.model.MEDITATION_TIMER_CONTENT_ID
import com.qualityalternative.app.domain.model.RecommendationSource
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

        val pages = readerPagesForBlocks(blocks = blocks, maxPageWeight = 3)

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
    fun readerPagesKeepLongSingleBlockOnOnePage() {
        val blocks = listOf(
            readerMarkdownBlock("Short first paragraph."),
            readerMarkdownBlock("Long ".repeat(500)),
            readerMarkdownBlock("Short final paragraph."),
        )

        val pages = readerPagesForBlocks(blocks = blocks, maxPageWeight = 3)

        assertEquals(3, pages.size)
        assertEquals(ReaderPage(start = 1, endInclusive = 1), pages[1])
        assertEquals(2, readerPageIndexForParagraph(pages = pages, paragraphIndex = 99))
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
