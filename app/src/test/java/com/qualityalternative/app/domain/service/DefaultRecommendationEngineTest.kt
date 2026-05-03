package com.qualityalternative.app.domain.service

import com.qualityalternative.app.domain.model.ContentAvailability
import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.ContentPriority
import com.qualityalternative.app.domain.model.ContentRenderMode
import com.qualityalternative.app.domain.model.ContentRightsClass
import com.qualityalternative.app.domain.model.ContentRightsMetadata
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
    fun generate_prefersTopicFitAndKeepsFiniteBackups() {
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
            excludedContentIds = setOf("d"),
            signals = RecommendationSignals(timeOfDay = TimeOfDayBucket.MIDDAY),
            nowMillis = 0L,
        )

        assertNotNull(result)
        assertEquals("a", result?.primary?.id)
        assertEquals(2, result?.backups?.size)
        assertEquals(setOf("b", "c"), result!!.backups.map(ContentItem::id).toSet())
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
            excludedContentIds = emptySet(),
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
    fun generate_excludesCompletedItemsFromPrimaryAndBackups() {
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
            excludedContentIds = setOf("primary-done", "done-backup"),
            signals = RecommendationSignals(timeOfDay = TimeOfDayBucket.MIDDAY),
            nowMillis = 0L,
        )

        assertEquals("fresh", result?.primary?.id)
        assertTrue(result?.backups.orEmpty().none { it.id == "done-backup" })
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
            excludedContentIds = setOf("done-1", "done-2"),
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
            excludedContentIds = emptySet(),
            signals = RecommendationSignals(timeOfDay = TimeOfDayBucket.MORNING),
            nowMillis = 0L,
        )

        assertEquals("longer-fit", result?.primary?.id)
        assertEquals(setOf("shorter-greedy", "quick-backup"), result?.backups?.map(ContentItem::id)?.toSet())
        assertEquals(false, result?.inventoryShortage)
        assertNotNull(result)
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
            excludedContentIds = emptySet(),
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
            excludedContentIds = emptySet(),
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
            item(id = "leave-the-crowd", minutes = 7, topics = setOf(TopicTag.ESSAYS)),
            item(id = "a-candle-opens-natural-philosophy", minutes = 6, topics = setOf(TopicTag.SCIENCE)),
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
            excludedContentIds = emptySet(),
            signals = RecommendationSignals(timeOfDay = TimeOfDayBucket.MIDDAY),
            nowMillis = 0L,
        )

        assertEquals("a-candle-opens-natural-philosophy", result?.primary?.id)
        assertEquals(
            listOf("leave-the-crowd", "fixture-link:short-convenience-essay"),
            result?.backups?.map(ContentItem::id)?.take(2),
        )
        assertNotNull(result)
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
            excludedContentIds = setOf("completed-link"),
            signals = RecommendationSignals(timeOfDay = TimeOfDayBucket.MIDDAY),
            nowMillis = 0L,
        )

        assertEquals("fresh-editorial", result?.primary?.id)
    }

    @Test
    fun generate_keepsFiniteScrollableChoiceSetWhenInventoryIsLargeAndMixed() {
        val preferences = UserPreferences(
            selectedApps = listOf(DistractingApp(packageName = "pkg", displayName = "Instagram")),
            preferredTopics = setOf(TopicTag.SCIENCE, TopicTag.PHILOSOPHY),
            preferredDurationBucket = DurationBucket.FOCUS,
            selectedPackIds = setOf("renderable", "modern-links", "user-links", "meditation"),
        )
        val renderableItems = (1..8).map { index ->
            item(
                id = "renderable-$index",
                packId = "renderable",
                minutes = 6 + (index % 4),
                topics = setOf(TopicTag.SCIENCE),
                rights = ContentRightsMetadata(
                    rightsClass = ContentRightsClass.RENDERABLE,
                    renderMode = ContentRenderMode.IN_APP_READER,
                ),
            )
        }
        val linkOnlyItems = (1..20).map { index ->
            item(
                id = "link-only-$index",
                packId = "modern-links",
                minutes = 6 + (index % 10),
                topics = setOf(if (index % 2 == 0) TopicTag.SCIENCE else TopicTag.PHILOSOPHY),
                format = ContentFormat.HTML,
                externalUrl = "https://example.com/deep-read-$index",
                bodyAssetPath = null,
                rights = ContentRightsMetadata(
                    rightsClass = ContentRightsClass.LINK_ONLY,
                    renderMode = ContentRenderMode.EXTERNAL_HANDOFF,
                ),
            )
        }
        val userLinks = (1..4).map { index ->
            item(
                id = "user-link-$index",
                packId = "user-links",
                minutes = 5 + index,
                topics = setOf(TopicTag.PHILOSOPHY),
                format = ContentFormat.HTML,
                sourceType = ContentSourceType.USER_LINK,
                availability = ContentAvailability.NEEDS_FALLBACK,
                externalUrl = "https://example.com/user-$index",
                bodyAssetPath = null,
                rights = ContentRightsMetadata(
                    rightsClass = ContentRightsClass.USER_PRIVATE,
                    renderMode = ContentRenderMode.EXTERNAL_HANDOFF,
                ),
            )
        }

        val result = engine.generate(
            targetApp = DistractingApp(packageName = "pkg", displayName = "Instagram"),
            preferences = preferences,
            inventory = renderableItems + linkOnlyItems + userLinks,
            excludedContentIds = emptySet(),
            signals = RecommendationSignals(timeOfDay = TimeOfDayBucket.MIDDAY),
            nowMillis = 0L,
        )

        assertNotNull(result)
        assertEquals(6, result?.backups?.size)
        assertEquals(7, listOfNotNull(result?.primary).plus(result?.backups.orEmpty()).size)
        assertEquals(
            7,
            listOfNotNull(result?.primary).plus(result?.backups.orEmpty()).map(ContentItem::id).toSet().size,
        )
        assertEquals(false, result?.inventoryShortage)
    }

    @Test
    fun generate_boostsSelectedContentPriorityWithoutExpandingFiniteSet() {
        val preferences = UserPreferences(
            selectedApps = listOf(DistractingApp(packageName = "pkg", displayName = "Instagram")),
            preferredTopics = setOf(TopicTag.SCIENCE),
            preferredDurationBucket = DurationBucket.FOCUS,
            selectedPackIds = setOf("pack", "documents"),
            contentPriority = ContentPriority.MY_FILES,
        )
        val inventory = listOf(
            item(id = "editorial", minutes = 6, topics = setOf(TopicTag.SCIENCE)),
            item(
                id = "document",
                packId = "documents",
                minutes = 6,
                topics = setOf(TopicTag.SCIENCE),
                sourceType = ContentSourceType.USER_DOCUMENT,
                rights = ContentRightsMetadata(
                    rightsClass = ContentRightsClass.USER_PRIVATE,
                    renderMode = ContentRenderMode.USER_PRIVATE_READER,
                ),
            ),
            item(id = "backup", minutes = 5, topics = setOf(TopicTag.HISTORY)),
        )

        val result = engine.generate(
            targetApp = DistractingApp(packageName = "pkg", displayName = "Instagram"),
            preferences = preferences,
            inventory = inventory,
            excludedContentIds = emptySet(),
            signals = RecommendationSignals(timeOfDay = TimeOfDayBucket.MIDDAY),
            nowMillis = 0L,
        )

        assertEquals("document", result?.primary?.id)
        assertEquals(2, result?.backups?.size)
    }

    @Test
    fun generate_boostsIndividualPriorityContentWithoutExpandingFiniteSet() {
        val preferences = UserPreferences(
            selectedApps = listOf(DistractingApp(packageName = "pkg", displayName = "Instagram")),
            preferredTopics = setOf(TopicTag.SCIENCE),
            preferredDurationBucket = DurationBucket.FOCUS,
            selectedPackIds = setOf("pack"),
            priorityContentIds = setOf("priority"),
        )
        val inventory = listOf(
            item(id = "topic-match", minutes = 6, topics = setOf(TopicTag.SCIENCE)),
            item(id = "priority", minutes = 6, topics = setOf(TopicTag.HISTORY)),
            item(id = "backup", minutes = 5, topics = setOf(TopicTag.SCIENCE)),
        )

        val result = engine.generate(
            targetApp = DistractingApp(packageName = "pkg", displayName = "Instagram"),
            preferences = preferences,
            inventory = inventory,
            excludedContentIds = emptySet(),
            signals = RecommendationSignals(timeOfDay = TimeOfDayBucket.MIDDAY),
            nowMillis = 0L,
        )

        assertEquals("priority", result?.primary?.id)
        assertEquals(2, result?.backups?.size)
    }

    @Test
    fun generate_prefersNewestUnprioritizedUserDocumentOverOlderUserDocuments() {
        val preferences = UserPreferences(
            selectedApps = listOf(DistractingApp(packageName = "pkg", displayName = "Instagram")),
            preferredTopics = setOf(TopicTag.SCIENCE),
            preferredDurationBucket = DurationBucket.FOCUS,
            selectedPackIds = setOf("pack", "user-documents"),
        )
        val inventory = listOf(
            item(
                id = "old-markdown",
                packId = "user-documents",
                minutes = 6,
                topics = setOf(TopicTag.SCIENCE),
                sourceType = ContentSourceType.USER_DOCUMENT,
                addedAtMillis = 1_000L,
            ),
            item(
                id = "new-epub",
                packId = "user-documents",
                minutes = 6,
                topics = setOf(TopicTag.SCIENCE),
                format = ContentFormat.EPUB,
                sourceType = ContentSourceType.USER_DOCUMENT,
                addedAtMillis = 3_000L,
            ),
            item(id = "backup", minutes = 5, topics = setOf(TopicTag.SCIENCE)),
        )

        val result = engine.generate(
            targetApp = DistractingApp(packageName = "pkg", displayName = "Instagram"),
            preferences = preferences,
            inventory = inventory,
            excludedContentIds = emptySet(),
            signals = RecommendationSignals(timeOfDay = TimeOfDayBucket.MIDDAY),
            nowMillis = 0L,
        )

        assertEquals("new-epub", result?.primary?.id)
    }

    @Test
    fun generate_keepsExplicitPriorityAheadOfNewestUserDocument() {
        val preferences = UserPreferences(
            selectedApps = listOf(DistractingApp(packageName = "pkg", displayName = "Instagram")),
            preferredTopics = setOf(TopicTag.SCIENCE),
            preferredDurationBucket = DurationBucket.FOCUS,
            selectedPackIds = setOf("pack", "user-documents"),
            priorityContentIds = setOf("old-priority"),
        )
        val inventory = listOf(
            item(
                id = "old-priority",
                packId = "user-documents",
                minutes = 6,
                topics = setOf(TopicTag.SCIENCE),
                sourceType = ContentSourceType.USER_DOCUMENT,
                addedAtMillis = 1_000L,
            ),
            item(
                id = "new-epub",
                packId = "user-documents",
                minutes = 6,
                topics = setOf(TopicTag.SCIENCE),
                format = ContentFormat.EPUB,
                sourceType = ContentSourceType.USER_DOCUMENT,
                addedAtMillis = 3_000L,
            ),
            item(id = "backup", minutes = 5, topics = setOf(TopicTag.SCIENCE)),
        )

        val result = engine.generate(
            targetApp = DistractingApp(packageName = "pkg", displayName = "Instagram"),
            preferences = preferences,
            inventory = inventory,
            excludedContentIds = emptySet(),
            signals = RecommendationSignals(timeOfDay = TimeOfDayBucket.MIDDAY),
            nowMillis = 0L,
        )

        assertEquals("old-priority", result?.primary?.id)
    }

    @Test
    fun generate_keepsExplicitPriorityAheadOfFresherRelevantUserDocument() {
        val preferences = UserPreferences(
            selectedApps = listOf(DistractingApp(packageName = "pkg", displayName = "Instagram")),
            preferredTopics = setOf(TopicTag.SCIENCE),
            preferredDurationBucket = DurationBucket.FOCUS,
            selectedPackIds = setOf("pack", "user-documents"),
            priorityContentIds = setOf("old-priority"),
        )
        val inventory = listOf(
            item(
                id = "old-priority",
                packId = "user-documents",
                minutes = 6,
                topics = setOf(TopicTag.HISTORY),
                sourceType = ContentSourceType.USER_DOCUMENT,
                addedAtMillis = 1_000L,
            ),
            item(
                id = "new-epub",
                packId = "user-documents",
                minutes = 6,
                topics = setOf(TopicTag.SCIENCE),
                format = ContentFormat.EPUB,
                sourceType = ContentSourceType.USER_DOCUMENT,
                addedAtMillis = 3_000L,
            ),
            item(id = "backup", minutes = 5, topics = setOf(TopicTag.SCIENCE)),
        )

        val result = engine.generate(
            targetApp = DistractingApp(packageName = "pkg", displayName = "Instagram"),
            preferences = preferences,
            inventory = inventory,
            excludedContentIds = emptySet(),
            signals = RecommendationSignals(timeOfDay = TimeOfDayBucket.MIDDAY),
            nowMillis = 0L,
        )

        assertEquals("old-priority", result?.primary?.id)
    }

    @Test
    fun generate_prefersReadableContentOverMeditationWhenBalancedScoresTie() {
        val preferences = UserPreferences(
            selectedApps = listOf(DistractingApp(packageName = "pkg", displayName = "Instagram")),
            preferredTopics = setOf(TopicTag.BODY),
            preferredDurationBucket = DurationBucket.FOCUS,
            selectedPackIds = setOf("pack", "meditation"),
        )
        val inventory = listOf(
            item(
                id = "a-breathing-meditation",
                packId = "meditation",
                minutes = 6,
                topics = setOf(TopicTag.BODY),
                sourceType = ContentSourceType.MEDITATION,
            ),
            item(id = "body-reading", minutes = 6, topics = setOf(TopicTag.BODY)),
        )

        val result = engine.generate(
            targetApp = DistractingApp(packageName = "pkg", displayName = "Instagram"),
            preferences = preferences,
            inventory = inventory,
            excludedContentIds = emptySet(),
            signals = RecommendationSignals(timeOfDay = TimeOfDayBucket.NIGHT),
            nowMillis = 0L,
        )

        assertEquals("body-reading", result?.primary?.id)

        val meditationPriorityResult = engine.generate(
            targetApp = DistractingApp(packageName = "pkg", displayName = "Instagram"),
            preferences = preferences.copy(contentPriority = ContentPriority.MEDITATION),
            inventory = inventory,
            excludedContentIds = emptySet(),
            signals = RecommendationSignals(timeOfDay = TimeOfDayBucket.NIGHT),
            nowMillis = 0L,
        )

        assertEquals("a-breathing-meditation", meditationPriorityResult?.primary?.id)
    }

    @Test
    fun generate_keepsShortExplicitPriorityAheadOfFreshDocumentWithoutDurationBackupGate() {
        val preferences = UserPreferences(
            selectedApps = listOf(DistractingApp(packageName = "pkg", displayName = "Instagram")),
            preferredTopics = setOf(TopicTag.SCIENCE),
            preferredDurationBucket = DurationBucket.FOCUS,
            selectedPackIds = setOf("pack", "user-documents"),
            priorityContentIds = setOf("old-priority-short"),
        )
        val inventory = listOf(
            item(
                id = "old-priority-short",
                packId = "user-documents",
                minutes = 4,
                topics = setOf(TopicTag.HISTORY),
                sourceType = ContentSourceType.USER_DOCUMENT,
                addedAtMillis = 1_000L,
            ),
            item(
                id = "new-epub",
                packId = "user-documents",
                minutes = 6,
                topics = setOf(TopicTag.SCIENCE),
                format = ContentFormat.EPUB,
                sourceType = ContentSourceType.USER_DOCUMENT,
                addedAtMillis = 3_000L,
            ),
            item(id = "backup-one", minutes = 6, topics = setOf(TopicTag.SCIENCE)),
            item(id = "backup-two", minutes = 5, topics = setOf(TopicTag.SCIENCE)),
        )

        val result = engine.generate(
            targetApp = DistractingApp(packageName = "pkg", displayName = "Instagram"),
            preferences = preferences,
            inventory = inventory,
            excludedContentIds = emptySet(),
            signals = RecommendationSignals(timeOfDay = TimeOfDayBucket.MIDDAY),
            nowMillis = 0L,
        )

        assertEquals("old-priority-short", result?.primary?.id)
        assertEquals(false, result?.inventoryShortage)
        assertTrue(result?.backups.orEmpty().any { item -> item.id == "new-epub" })
    }

    @Test
    fun generate_doesNotPenalizeFreshUserDocumentBecauseWholeSourceIsLong() {
        assertFreshLongUserDocumentWins(format = ContentFormat.EPUB, freshId = "fresh-long-epub")
        assertFreshLongUserDocumentWins(format = ContentFormat.MARKDOWN, freshId = "fresh-long-markdown")
    }

    @Test
    fun generate_allowsLongerBackupsWhenTheyAreUseful() {
        val preferences = UserPreferences(
            selectedApps = listOf(DistractingApp(packageName = "pkg", displayName = "Instagram")),
            preferredTopics = setOf(TopicTag.PRACTICAL),
            preferredDurationBucket = DurationBucket.QUICK,
            selectedPackIds = setOf("pack"),
            priorityContentIds = setOf("primary-short"),
        )
        val inventory = listOf(
            item(id = "primary-short", minutes = 3, topics = setOf(TopicTag.PRACTICAL)),
            item(id = "backup-long", minutes = 20, topics = setOf(TopicTag.PRACTICAL)),
            item(id = "backup-medium", minutes = 12, topics = setOf(TopicTag.PRACTICAL)),
        )

        val result = engine.generate(
            targetApp = DistractingApp(packageName = "pkg", displayName = "Instagram"),
            preferences = preferences,
            inventory = inventory,
            excludedContentIds = emptySet(),
            signals = RecommendationSignals(timeOfDay = TimeOfDayBucket.MORNING),
            nowMillis = 0L,
        )

        assertEquals("primary-short", result?.primary?.id)
        assertEquals(setOf("backup-long", "backup-medium"), result?.backups?.map(ContentItem::id)?.toSet())
    }

    @Test
    fun generate_boostsNewestEligibleUserDocumentWhenNewerDocumentIsExcluded() {
        val preferences = UserPreferences(
            selectedApps = listOf(DistractingApp(packageName = "pkg", displayName = "Instagram")),
            preferredTopics = setOf(TopicTag.SCIENCE, TopicTag.PHILOSOPHY, TopicTag.ESSAYS),
            preferredDurationBucket = DurationBucket.FOCUS,
            selectedPackIds = setOf("pack", "user-documents"),
        )
        val inventory = listOf(
            item(
                id = "editorial",
                packId = "pack",
                minutes = 6,
                topics = setOf(TopicTag.SCIENCE, TopicTag.PHILOSOPHY, TopicTag.ESSAYS),
            ),
            item(
                id = "eligible-new",
                packId = "user-documents",
                minutes = 6,
                topics = setOf(TopicTag.SCIENCE),
                sourceType = ContentSourceType.USER_DOCUMENT,
                addedAtMillis = 2_000L,
            ),
            item(
                id = "excluded-newer",
                packId = "user-documents",
                minutes = 6,
                topics = setOf(TopicTag.SCIENCE),
                sourceType = ContentSourceType.USER_DOCUMENT,
                addedAtMillis = 3_000L,
            ),
        )

        val result = engine.generate(
            targetApp = DistractingApp(packageName = "pkg", displayName = "Instagram"),
            preferences = preferences,
            inventory = inventory,
            excludedContentIds = setOf("excluded-newer"),
            signals = RecommendationSignals(timeOfDay = TimeOfDayBucket.MIDDAY),
            nowMillis = 0L,
        )

        assertEquals("eligible-new", result?.primary?.id)
    }

    @Test
    fun generate_givesUnfinishedContentAbsolutePrimaryPriorityUnlessCompleted() {
        val preferences = UserPreferences(
            selectedApps = listOf(DistractingApp(packageName = "pkg", displayName = "Instagram")),
            preferredTopics = setOf(TopicTag.SCIENCE),
            preferredDurationBucket = DurationBucket.FOCUS,
            selectedPackIds = setOf("pack"),
            priorityContentIds = setOf("fresh-match"),
            unfinishedContentIds = setOf("unfinished"),
        )
        val inventory = listOf(
            item(id = "fresh-match", minutes = 7, topics = setOf(TopicTag.SCIENCE, TopicTag.TECH, TopicTag.ESSAYS)),
            item(id = "unfinished", minutes = 5, topics = setOf(TopicTag.HISTORY)),
            item(id = "backup-one", minutes = 4, topics = setOf(TopicTag.SCIENCE)),
            item(id = "backup-two", minutes = 3, topics = setOf(TopicTag.SCIENCE)),
        )

        val result = engine.generate(
            targetApp = DistractingApp(packageName = "pkg", displayName = "Instagram"),
            preferences = preferences,
            inventory = inventory,
            excludedContentIds = emptySet(),
            signals = RecommendationSignals(timeOfDay = TimeOfDayBucket.MIDDAY),
            nowMillis = 0L,
        )

        assertEquals("unfinished", result?.primary?.id)

        val completedResult = engine.generate(
            targetApp = DistractingApp(packageName = "pkg", displayName = "Instagram"),
            preferences = preferences,
            inventory = inventory,
            excludedContentIds = setOf("unfinished"),
            signals = RecommendationSignals(timeOfDay = TimeOfDayBucket.MIDDAY),
            nowMillis = 0L,
        )

        assertEquals("fresh-match", completedResult?.primary?.id)
    }

    @Test
    fun generate_doesNotRecommendUnavailableUnfinishedContentAsPrimary() {
        val preferences = UserPreferences(
            selectedApps = listOf(DistractingApp(packageName = "pkg", displayName = "Instagram")),
            preferredTopics = setOf(TopicTag.SCIENCE),
            preferredDurationBucket = DurationBucket.FOCUS,
            selectedPackIds = setOf("pack"),
            priorityContentIds = setOf("fresh-match"),
            unfinishedContentIds = setOf("unavailable-unfinished"),
        )
        val inventory = listOf(
            item(
                id = "unavailable-unfinished",
                minutes = 5,
                topics = setOf(TopicTag.HISTORY),
                availability = ContentAvailability.UNAVAILABLE,
            ),
            item(id = "fresh-match", minutes = 7, topics = setOf(TopicTag.SCIENCE, TopicTag.TECH, TopicTag.ESSAYS)),
            item(id = "backup-one", minutes = 4, topics = setOf(TopicTag.SCIENCE)),
            item(id = "backup-two", minutes = 3, topics = setOf(TopicTag.SCIENCE)),
        )

        val result = engine.generate(
            targetApp = DistractingApp(packageName = "pkg", displayName = "Instagram"),
            preferences = preferences,
            inventory = inventory,
            excludedContentIds = emptySet(),
            signals = RecommendationSignals(timeOfDay = TimeOfDayBucket.MIDDAY),
            nowMillis = 0L,
        )

        assertEquals("fresh-match", result?.primary?.id)
        assertTrue(result?.backups.orEmpty().none { item -> item.id == "unavailable-unfinished" })
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
        bodyAssetPath: String? = "unused",
        rights: ContentRightsMetadata = ContentRightsMetadata.renderableEditorial(),
        addedAtMillis: Long? = null,
    ): ContentItem = ContentItem(
        id = id,
        packId = packId,
        title = id,
        description = "desc",
        durationMinutes = minutes,
        format = format,
        topicTags = topics,
        bodyAssetPath = bodyAssetPath,
        externalUrl = externalUrl,
        sourceType = sourceType,
        availability = availability,
        rights = rights,
        addedAtMillis = addedAtMillis,
    )

    private fun assertFreshLongUserDocumentWins(format: ContentFormat, freshId: String) {
        val preferences = UserPreferences(
            selectedApps = listOf(DistractingApp(packageName = "pkg", displayName = "Instagram")),
            preferredTopics = setOf(TopicTag.SCIENCE),
            preferredDurationBucket = DurationBucket.QUICK,
            selectedPackIds = setOf("pack", "user-documents"),
        )
        val inventory = listOf(
            item(
                id = "old-short-markdown",
                packId = "user-documents",
                minutes = 4,
                topics = setOf(TopicTag.SCIENCE),
                format = ContentFormat.MARKDOWN,
                sourceType = ContentSourceType.USER_DOCUMENT,
                addedAtMillis = 1_000L,
            ),
            item(
                id = freshId,
                packId = "user-documents",
                minutes = 20,
                topics = setOf(TopicTag.SCIENCE),
                format = format,
                sourceType = ContentSourceType.USER_DOCUMENT,
                addedAtMillis = 5_000L,
            ),
            item(id = "editorial-backup", minutes = 5, topics = setOf(TopicTag.SCIENCE)),
        )

        val result = engine.generate(
            targetApp = DistractingApp(packageName = "pkg", displayName = "Instagram"),
            preferences = preferences,
            inventory = inventory,
            excludedContentIds = emptySet(),
            signals = RecommendationSignals(timeOfDay = TimeOfDayBucket.MIDDAY),
            nowMillis = 0L,
        )

        assertEquals(freshId, result?.primary?.id)
    }
}
