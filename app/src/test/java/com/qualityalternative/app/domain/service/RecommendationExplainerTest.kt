package com.qualityalternative.app.domain.service

import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.ContentSourceType
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.model.DurationBucket
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.model.UserPreferences
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendationExplainerTest {
    @Test
    fun explain_usesEditorialWhyThisNowAndDynamicFitChips() {
        val item = item(
            id = "focus-piece",
            minutes = 7,
            topics = setOf(TopicTag.SCIENCE, TopicTag.ESSAYS),
            whyThisNow = "A good fit when curiosity is a better first move than scrolling.",
        )
        val preferences = preferences(
            preferredTopics = setOf(TopicTag.SCIENCE),
            priorityContentIds = setOf("focus-piece"),
        )

        val explanation = RecommendationExplainer.explain(item = item, preferences = preferences)

        assertEquals("A good fit when curiosity is a better first move than scrolling.", explanation.headline)
        assertEquals(
            listOf("Priority pick", "Matches Science", "Fits 5-10 min", "Editorial"),
            explanation.chips,
        )
    }

    @Test
    fun explain_fallsBackToTopicReasonWhenItemHasNoWhyThisNow() {
        val item = item(
            id = "saved-link",
            minutes = 4,
            topics = setOf(TopicTag.HISTORY),
            sourceType = ContentSourceType.USER_LINK,
            whyThisNow = null,
        )
        val preferences = preferences(preferredTopics = setOf(TopicTag.HISTORY))

        val explanation = RecommendationExplainer.explain(item = item, preferences = preferences)

        assertEquals("Picked because it matches your History interest.", explanation.headline)
        assertTrue("Shorter than 5-10 min" in explanation.chips)
        assertTrue("Saved link" in explanation.chips)
    }

    @Test
    fun explain_marksMeditationAsFiniteResetTimer() {
        val item = item(
            id = "meditation-timer",
            minutes = 3,
            topics = setOf(TopicTag.PSYCHOLOGY),
            sourceType = ContentSourceType.MEDITATION,
            whyThisNow = null,
        )
        val preferences = preferences(preferredTopics = setOf(TopicTag.PHILOSOPHY))

        val explanation = RecommendationExplainer.explain(item = item, preferences = preferences)

        assertEquals("A short reset for creating space before opening the app.", explanation.headline)
        assertEquals(listOf("Shorter than 5-10 min", "Reset timer"), explanation.chips)
    }

    @Test
    fun explain_marksUnfinishedContentAsContinuePath() {
        val item = item(
            id = "half-read",
            minutes = 8,
            topics = setOf(TopicTag.HISTORY),
            whyThisNow = null,
        )
        val preferences = preferences(
            preferredTopics = setOf(TopicTag.SCIENCE),
            unfinishedContentIds = setOf("half-read"),
        )

        val explanation = RecommendationExplainer.explain(item = item, preferences = preferences)

        assertEquals("Continue what you already started.", explanation.headline)
        assertEquals(listOf("Unfinished", "Fits 5-10 min", "Editorial"), explanation.chips)
    }

    @Test
    fun surfacedStarterPackCopyDoesNotExposeInternalCurationLanguage() {
        val asset = File("src/main/assets/editorial/starter_packs.json").readText()
        val surfacedValues = Regex("\"(description|whyThisNow)\"\\s*:\\s*\"([^\"]+)\"")
            .findAll(asset)
            .map { match -> "${match.groupValues[1]}: ${match.groupValues[2]}" }
            .toList()

        assertTrue("Expected surfaced starter-pack copy in starter inventory.", surfacedValues.isNotEmpty())
        val forbidden = Regex(
            pattern = "\\b(the user|the product|backup|use when|use as|works as|candidate|candidates|requiring|requires|needs|review|reviewed|selection|jurisdiction|safe|external-only|link-only|in-app|rehosting|Sprint 9|if excerpt|overreach)\\b",
            option = RegexOption.IGNORE_CASE,
        )
        val internalCopy = surfacedValues.filter { value -> forbidden.containsMatchIn(value) }

        assertFalse("Internal curation language leaked into surfaced starter-pack copy: $internalCopy", internalCopy.isNotEmpty())
    }

    private fun preferences(
        preferredTopics: Set<TopicTag>,
        priorityContentIds: Set<String> = emptySet(),
        unfinishedContentIds: Set<String> = emptySet(),
    ): UserPreferences {
        return UserPreferences(
            selectedApps = listOf(DistractingApp(packageName = "pkg", displayName = "Fixture Feed")),
            preferredTopics = preferredTopics,
            preferredDurationBucket = DurationBucket.FOCUS,
            selectedPackIds = setOf("pack"),
            priorityContentIds = priorityContentIds,
            unfinishedContentIds = unfinishedContentIds,
        )
    }

    private fun item(
        id: String,
        minutes: Int,
        topics: Set<TopicTag>,
        sourceType: ContentSourceType = ContentSourceType.EDITORIAL,
        whyThisNow: String?,
    ): ContentItem {
        return ContentItem(
            id = id,
            packId = "pack",
            title = "A useful replacement",
            description = "A short piece for a better impulse.",
            durationMinutes = minutes,
            format = ContentFormat.MARKDOWN,
            topicTags = topics,
            sourceType = sourceType,
            whyThisNow = whyThisNow,
        )
    }
}
