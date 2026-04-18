package com.qualityalternative.app.domain.service

import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.ContentItem
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
    fun generate_prefersMatchingDurationAndKeepsBackupsNoLongerThanPrimary() {
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
        assertTrue(result!!.backups.all { it.durationMinutes <= result.primary.durationMinutes })
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
    fun generate_prefersPrimaryThatCanProduceTwoBackupsWhenInventoryAllowsIt() {
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
    }

    private fun item(id: String, packId: String = "pack", minutes: Int, topics: Set<TopicTag>): ContentItem = ContentItem(
        id = id,
        packId = packId,
        title = id,
        description = "desc",
        durationMinutes = minutes,
        format = ContentFormat.MARKDOWN,
        topicTags = topics,
        bodyAssetPath = "unused",
    )
}
