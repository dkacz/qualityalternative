package com.qualityalternative.app.domain.service

import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.model.DurationBucket
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
            excludedIds = setOf("d"),
            nowMillis = 0L,
        )

        assertNotNull(result)
        assertEquals("a", result?.primary?.id)
        assertEquals(2, result?.backups?.size)
        assertTrue(result!!.backups.all { it.durationMinutes <= result.primary.durationMinutes })
    }

    private fun item(id: String, minutes: Int, topics: Set<TopicTag>): ContentItem = ContentItem(
        id = id,
        packId = "pack",
        title = id,
        description = "desc",
        durationMinutes = minutes,
        format = ContentFormat.MARKDOWN,
        topicTags = topics,
        bodyAssetPath = "unused",
    )
}
