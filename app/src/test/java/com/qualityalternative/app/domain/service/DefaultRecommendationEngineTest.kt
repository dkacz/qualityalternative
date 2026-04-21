package com.qualityalternative.app.domain.service

import com.qualityalternative.app.domain.model.ContentAvailability
import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.ContentSourceType
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.model.DurationBucket
import com.qualityalternative.app.domain.model.RecommendationSignals
import com.qualityalternative.app.domain.model.TimeOfDayBucket
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.model.UserPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultRecommendationEngineTest {
    private val engine = DefaultRecommendationEngine()

    @Test
    fun generate_prefersMatchingDurationAndKeepsExactlyTwoFiniteBackups() {
        val preferences = UserPreferences(
            selectedApps = listOf(DistractingApp(packageName = "pkg", displayName = "Instagram")),
            preferredTopics = setOf(TopicTag.PHILOSOPHY, TopicTag.PSYCHOLOGY),
            preferredDurationBucket = DurationBucket.FOCUS,
            selectedPackIds = setOf("pack"),
        )

        val inventory = listOf(
            item(id = "a", minutes = 7, topics = setOf(TopicTag.PHILOSOPHY)),
            item(id = "b", minutes = 5, topics = setOf(TopicTag.PSYCHOLOGY)),
            item(id = "c", minutes = 4, topics = setOf(TopicTag.HISTORY)),
            item(id = "d", minutes = 12, topics = setOf(TopicTag.PHILOSOPHY)),
        )

        val result = engine.generate(
            targetApp = DistractingApp(packageName = "pkg", displayName = "Instagram"),
            preferences = preferences,
            inventory = inventory,
            primaryExcludedIds = setOf("d"),
            signals = RecommendationSignals(timeOfDay = TimeOfDayBucket.MIDDAY),
            nowMillis = 0L,
        )

        assertNotNull(result)
        assertEquals("a", result?.primary?.id)
        assertEquals(2, result?.backups?.size)
        assertEquals(setOf("b", "c"), result!!.backups.map(ContentItem::id).toSet())
        assertTrue(result.backups.all { backup -> backup.durationMinutes <= result.primary.durationMinutes })
    }

    @Test
    fun generate_penalizesSkippedTopicsAndBoostsSuccessfulPacks() {
        val preferences = UserPreferences(
            selectedApps = listOf(DistractingApp(packageName = "pkg", displayName = "Instagram")),
            preferredTopics = setOf(TopicTag.SCIENCE),
            preferredDurationBucket = DurationBucket.FOCUS,
            selectedPackIds = setOf("science", "history"),
        )

        val inventory = listOf(
            item(id = "science-skip", packId = "science", minutes = 6, topics = setOf(TopicTag.SCIENCE)),
            item(id = "history-win", packId = "history", minutes = 6, topics = setOf(TopicTag.SCIENCE)),
        )

        val result = engine.generate(
            targetApp = DistractingApp(packageName = "pkg", displayName = "Instagram"),
            preferences = preferences,
            inventory = inventory,
            primaryExcludedIds = emptySet(),
            signals = RecommendationSignals(
                skippedTopics = setOf(TopicTag.SCIENCE),
                successfulPackIds = setOf("history"),
                timeOfDay = TimeOfDayBucket.MIDDAY,
            ),
            nowMillis = 0L,
        )

        assertEquals("history-win", result?.primary?.id)
    }

    @Test
    fun generate_excludesCompletedItemsFromPrimaryButCanReuseThemAsBackups() {
        val preferences = UserPreferences(
            selectedApps = listOf(DistractingApp(packageName = "pkg", displayName = "Instagram")),
            preferredTopics = setOf(TopicTag.PHILOSOPHY),
            preferredDurationBucket = DurationBucket.FOCUS,
            selectedPackIds = setOf("pack"),
        )

        val inventory = listOf(
            item(id = "primary-done", minutes = 7, topics = setOf(TopicTag.PHILOSOPHY)),
            item(id = "fresh", minutes = 6, topics = setOf(TopicTag.PHILOSOPHY)),
            item(id = "done-backup", minutes = 5, topics = setOf(TopicTag.HISTORY)),
        )

        val result = engine.generate(
            targetApp = DistractingApp(packageName = "pkg", displayName = "Instagram"),
            preferences = preferences,
            inventory = inventory,
            primaryExcludedIds = setOf("primary-done", "done-backup"),
            signals = RecommendationSignals(timeOfDay = TimeOfDayBucket.MIDDAY),
            nowMillis = 0L,
        )

        assertEquals("fresh", result?.primary?.id)
        assertTrue(result?.backups?.any { it.id == "done-backup" } == true)
    }

    @Test
    fun generate_returnsNullWhenOnlyCompletedCandidatesRemainForPrimary() {
        val preferences = UserPreferences(
            selectedApps = listOf(DistractingApp(packageName = "pkg", displayName = "Instagram")),
            preferredTopics = setOf(TopicTag.PHILOSOPHY),
            preferredDurationBucket = DurationBucket.FOCUS,
            selectedPackIds = setOf("pack"),
        )

        val inventory = listOf(
            item(id = "done-1", minutes = 7, topics = setOf(TopicTag.PHILOSOPHY)),
            item(id = "done-2", minutes = 6, topics = setOf(TopicTag.PHILOSOPHY)),
        )

        val result = engine.generate(
            targetApp = DistractingApp(packageName = "pkg", displayName = "Instagram"),
            preferences = preferences,
            inventory = inventory,
            primaryExcludedIds = setOf("done-1", "done-2"),
            signals = RecommendationSignals(timeOfDay = TimeOfDayBucket.MIDDAY),
            nowMillis = 0L,
        )

        assertEquals(null, result)
    }

    @Test
    fun generate_prefersBestScoredPrimaryAndStillKeepsTwoBackupsWhenInventoryAllowsIt() {
        val preferences = UserPreferences(
            selectedApps = listOf(DistractingApp(packageName = "pkg", displayName = "Instagram")),
            preferredTopics = setOf(TopicTag.PHILOSOPHY),
            preferredDurationBucket = DurationBucket.FOCUS,
            selectedPackIds = setOf("pack"),
        )

        val inventory = listOf(
            item(id = "longer-fit", minutes = 7, topics = setOf(TopicTag.PHILOSOPHY)),
            item(id = "shorter-greedy", minutes = 5, topics = setOf(TopicTag.PHILOSOPHY)),
            item(id = "quick-backup", minutes = 4, topics = setOf(TopicTag.HISTORY)),
        )

        val result = engine.generate(
            targetApp = DistractingApp(packageName = "pkg", displayName = "Instagram"),
            preferences = preferences,
            inventory = inventory,
            primaryExcludedIds = emptySet(),
            signals = RecommendationSignals(timeOfDay = TimeOfDayBucket.MORNING),
            nowMillis = 0L,
        )

        assertEquals("longer-fit", result?.primary?.id)
        assertEquals(setOf("shorter-greedy", "quick-backup"), result?.backups?.map(ContentItem::id)?.toSet())
        assertEquals(false, result?.inventoryShortage)
        assertTrue(result!!.backups.all { backup -> backup.durationMinutes <= result.primary.durationMinutes })
    }

    @Test
    fun generate_canSelectUserLinkWhenItIsTheBestFit() {
        val preferences = UserPreferences(
            selectedApps = listOf(DistractingApp(packageName = "pkg", displayName = "Instagram")),
            preferredTopics = setOf(TopicTag.SCIENCE),
            preferredDurationBucket = DurationBucket.FOCUS,
            selectedPackIds = setOf("pack", "user-links"),
        )

        val inventory = listOf(
            item(id = "editorial-mismatch", minutes = 12, topics = setOf(TopicTag.HISTORY)),
            item(
                id = "saved-link",
                packId = "user-links",
                minutes = 7,
                topics = setOf(TopicTag.SCIENCE),
                format = ContentFormat.HTML,
                sourceType = ContentSourceType.USER_LINK,
                availability = ContentAvailability.NEEDS_FALLBACK,
                externalUrl = "https://example.com/essay",
            ),
        )

        val result = engine.generate(
            targetApp = DistractingApp(packageName = "pkg", displayName = "Instagram"),
            preferences = preferences,
            inventory = inventory,
            primaryExcludedIds = emptySet(),
            signals = RecommendationSignals(timeOfDay = TimeOfDayBucket.MIDDAY),
            nowMillis = 0L,
        )

        assertEquals("saved-link", result?.primary?.id)
    }

    @Test
    fun generate_canMixEditorialAndUserLinkBackups() {
        val preferences = UserPreferences(
            selectedApps = listOf(DistractingApp(packageName = "pkg", displayName = "Instagram")),
            preferredTopics = setOf(TopicTag.PHILOSOPHY),
            preferredDurationBucket = DurationBucket.FOCUS,
            selectedPackIds = setOf("pack", "user-links"),
        )

        val inventory = listOf(
            item(id = "primary", minutes = 7, topics = setOf(TopicTag.PHILOSOPHY)),
            item(
                id = "saved-backup",
                packId = "user-links",
                minutes = 5,
                topics = setOf(TopicTag.PHILOSOPHY),
                format = ContentFormat.HTML,
                sourceType = ContentSourceType.USER_LINK,
                availability = ContentAvailability.NEEDS_FALLBACK,
                externalUrl = "https://example.com/short",
            ),
            item(id = "editorial-backup", minutes = 4, topics = setOf(TopicTag.HISTORY)),
        )

        val result = engine.generate(
            targetApp = DistractingApp(packageName = "pkg", displayName = "Instagram"),
            preferences = preferences,
            inventory = inventory,
            primaryExcludedIds = emptySet(),
            signals = RecommendationSignals(timeOfDay = TimeOfDayBucket.MIDDAY),
            nowMillis = 0L,
        )

        assertEquals("primary", result?.primary?.id)
        assertEquals(setOf("saved-backup", "editorial-backup"), result?.backups?.map(ContentItem::id)?.toSet())
    }

    @Test
    fun generate_prefersEditorialBeforeUserLinksWhenBackupScoresTie() {
        val preferences = UserPreferences(
            selectedApps = listOf(DistractingApp(packageName = "pkg", displayName = "Instagram")),
            preferredTopics = setOf(TopicTag.ESSAYS, TopicTag.SCIENCE),
            preferredDurationBucket = DurationBucket.FOCUS,
            selectedPackIds = setOf("pack", "user-links"),
        )

        val inventory = listOf(
            item(id = "quiet-hours", minutes = 7, topics = setOf(TopicTag.ESSAYS)),
            item(id = "how-bees-decide", minutes = 6, topics = setOf(TopicTag.SCIENCE)),
            item(
                id = "sample-link:notes-on-taste",
                packId = "user-links",
                minutes = 6,
                topics = setOf(TopicTag.ECONOMICS),
                format = ContentFormat.HTML,
                sourceType = ContentSourceType.USER_LINK,
                availability = ContentAvailability.NEEDS_FALLBACK,
                externalUrl = "https://example.com/taste",
            ),
            item(
                id = "fixture-link:short-convenience-essay",
                packId = "user-links",
                minutes = 6,
                topics = setOf(TopicTag.ESSAYS),
                format = ContentFormat.HTML,
                sourceType = ContentSourceType.USER_LINK,
                availability = ContentAvailability.NEEDS_FALLBACK,
                externalUrl = "https://example.com/convenience",
            ),
        )

        val result = engine.generate(
            targetApp = DistractingApp(packageName = "pkg", displayName = "Instagram"),
            preferences = preferences,
            inventory = inventory,
            primaryExcludedIds = emptySet(),
            signals = RecommendationSignals(timeOfDay = TimeOfDayBucket.MIDDAY),
            nowMillis = 0L,
        )

        assertEquals("quiet-hours", result?.primary?.id)
        assertEquals(listOf("how-bees-decide", "fixture-link:short-convenience-essay"), result?.backups?.map(ContentItem::id))
        assertTrue(result!!.backups.all { backup -> backup.durationMinutes <= result.primary.durationMinutes })
    }

    @Test
    fun generate_excludesCompletedUserLinksFromPrimary() {
        val preferences = UserPreferences(
            selectedApps = listOf(DistractingApp(packageName = "pkg", displayName = "Instagram")),
            preferredTopics = setOf(TopicTag.SCIENCE),
            preferredDurationBucket = DurationBucket.FOCUS,
            selectedPackIds = setOf("pack", "user-links"),
        )

        val inventory = listOf(
            item(
                id = "completed-link",
                packId = "user-links",
                minutes = 7,
                topics = setOf(TopicTag.SCIENCE),
                format = ContentFormat.HTML,
                sourceType = ContentSourceType.USER_LINK,
                availability = ContentAvailability.NEEDS_FALLBACK,
                externalUrl = "https://example.com/done",
            ),
            item(id = "fresh-editorial", minutes = 7, topics = setOf(TopicTag.SCIENCE)),
        )

        val result = engine.generate(
            targetApp = DistractingApp(packageName = "pkg", displayName = "Instagram"),
            preferences = preferences,
            inventory = inventory,
            primaryExcludedIds = setOf("completed-link"),
            signals = RecommendationSignals(timeOfDay = TimeOfDayBucket.MIDDAY),
            nowMillis = 0L,
        )

        assertEquals("fresh-editorial", result?.primary?.id)
    }

    private fun item(
        id: String,
        packId: String = "pack",
        minutes: Int,
        topics: Set<TopicTag>,
        format: ContentFormat = ContentFormat.MARKDOWN,
        sourceType: ContentSourceType = ContentSourceType.EDITORIAL,
        availability: ContentAvailability = ContentAvailability.AVAILABLE,
        externalUrl: String? = null,
    ): ContentItem = ContentItem(
        id = id,
        packId = packId,
        title = id,
        description = "desc",
        durationMinutes = minutes,
        format = format,
        topicTags = topics,
        bodyAssetPath = "unused",
        externalUrl = externalUrl,
        sourceType = sourceType,
        availability = availability,
    )
}
