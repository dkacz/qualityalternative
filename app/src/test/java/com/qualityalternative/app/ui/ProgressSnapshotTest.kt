package com.qualityalternative.app.ui

import com.qualityalternative.app.domain.model.AnalyticsEvent
import com.qualityalternative.app.domain.model.AnalyticsEventType
import com.qualityalternative.app.domain.model.RecommendationSource
import com.qualityalternative.app.domain.model.ReplacementHistoryEntry
import com.qualityalternative.app.domain.model.TopicTag
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
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
    fun readerProgressPercentTracksVisibleParagraphsInsteadOfElapsedTime() {
        assertEquals(0, readerProgressPercent(lastVisibleItemIndex = 0, paragraphCount = 10))
        assertEquals(30, readerProgressPercent(lastVisibleItemIndex = 3, paragraphCount = 10))
        assertEquals(100, readerProgressPercent(lastVisibleItemIndex = 20, paragraphCount = 10))
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
    ): ReplacementHistoryEntry {
        return ReplacementHistoryEntry(
            sessionId = sessionId,
            interventionId = "intervention-$sessionId",
            targetAppPackage = "com.fixture",
            targetAppDisplayName = "Fixture Feed",
            interventionShownAtMillis = acceptedAtMillis - 100L,
            primaryContentId = "content-$sessionId",
            backupContentIds = emptyList(),
            contentId = "content-$sessionId",
            contentTitle = "Replacement $sessionId",
            contentDescription = "A finite replacement.",
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
