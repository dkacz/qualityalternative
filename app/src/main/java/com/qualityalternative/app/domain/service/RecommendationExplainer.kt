package com.qualityalternative.app.domain.service

import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.ContentSourceType
import com.qualityalternative.app.domain.model.DurationBucket
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.model.UserPreferences

data class RecommendationExplanation(
    val headline: String,
    val chips: List<String>,
)

object RecommendationExplainer {
    fun explain(item: ContentItem, preferences: UserPreferences): RecommendationExplanation {
        val matchedTopics = item.topicTags
            .intersect(preferences.preferredTopics)
            .sortedBy(TopicTag::name)

        val headline = item.whyThisNow
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?: fallbackHeadline(item = item, preferences = preferences, matchedTopics = matchedTopics)

        val chips = buildList {
            if (item.id in preferences.unfinishedContentIds) {
                add("Unfinished")
            }
            if (item.id in preferences.priorityContentIds) {
                add("Priority pick")
            }
            if (matchedTopics.isNotEmpty()) {
                add("Matches ${matchedTopics.take(2).joinToString(" + ") { it.displayName() }}")
            }
            add(durationChip(item.durationMinutes, preferences.preferredDurationBucket))
            add(item.sourceChip())
        }.distinct()

        return RecommendationExplanation(
            headline = headline,
            chips = chips,
        )
    }

    private fun fallbackHeadline(
        item: ContentItem,
        preferences: UserPreferences,
        matchedTopics: List<TopicTag>,
    ): String {
        return when {
            item.id in preferences.unfinishedContentIds -> "Continue what you already started."
            item.id in preferences.priorityContentIds -> "You marked this as a priority pick for replacement moments."
            matchedTopics.isNotEmpty() -> "Picked because it matches your ${matchedTopics.first().displayName()} interest."
            item.sourceType == ContentSourceType.MEDITATION -> "A short reset for creating space before opening the app."
            preferences.preferredDurationBucket.contains(item.durationMinutes) -> "A short replacement that fits your session length."
            else -> "A bounded replacement for this moment."
        }
    }

    private fun durationChip(minutes: Int, bucket: DurationBucket): String {
        return when {
            bucket.contains(minutes) -> "Fits ${bucket.displayRange()}"
            minutes < bucket.minMinutes -> "Shorter than ${bucket.displayRange()}"
            else -> "Longer than ${bucket.displayRange()}"
        }
    }

    private fun DurationBucket.displayRange(): String {
        return "$minMinutes-$maxMinutes min"
    }

    private fun ContentItem.sourceChip(): String {
        return when (sourceType) {
            ContentSourceType.EDITORIAL -> "Editorial"
            ContentSourceType.USER_LINK -> "Saved link"
            ContentSourceType.USER_DOCUMENT -> "Your file"
            ContentSourceType.MEDITATION -> "Reset timer"
        }
    }

    private fun TopicTag.displayName(): String {
        return when (this) {
            TopicTag.ATTENTION -> "Attention"
            TopicTag.PRACTICAL -> "Practical"
            TopicTag.BODY -> "Body"
            TopicTag.NATURE -> "Nature"
            TopicTag.HISTORY_CULTURE -> "History & culture"
            TopicTag.ESSAYS -> "Essays"
            TopicTag.PHILOSOPHY -> "Philosophy"
            TopicTag.SCIENCE -> "Science"
            TopicTag.DESIGN -> "Design"
            TopicTag.POETRY -> "Poetry"
            TopicTag.HISTORY -> "History"
            TopicTag.TECH -> "Tech"
            TopicTag.FICTION -> "Fiction"
            TopicTag.CLIMATE -> "Climate"
            TopicTag.ECONOMICS -> "Economics"
            TopicTag.FOOD -> "Food"
            TopicTag.ARCHITECTURE -> "Architecture"
            TopicTag.CREATIVITY -> "Creativity"
            TopicTag.PSYCHOLOGY -> "Psychology"
            TopicTag.OTHER -> "Other"
        }
    }
}
