package com.qualityalternative.app.domain.service

import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.model.DurationBucket
import com.qualityalternative.app.domain.model.RecommendationSet
import com.qualityalternative.app.domain.model.UserPreferences
import kotlin.math.abs

class DefaultRecommendationEngine : RecommendationEngine {
    override fun generate(
        targetApp: DistractingApp,
        preferences: UserPreferences,
        inventory: List<ContentItem>,
        excludedIds: Set<String>,
        nowMillis: Long,
    ): RecommendationSet? {
        val candidates = inventory
            .filterNot { it.id in excludedIds }
            .sortedWith(
                compareByDescending<ContentItem> { score(item = it, preferences = preferences) }
                    .thenBy { abs(it.durationMinutes - preferences.preferredDurationBucket.midpoint) }
                    .thenBy { it.title },
            )

        val primary = candidates.firstOrNull() ?: return null
        val backups = candidates
            .drop(1)
            .filter { it.durationMinutes <= primary.durationMinutes }
            .sortedWith(
                compareBy<ContentItem> { abs(it.durationMinutes - DurationBucket.QUICK.midpoint) }
                    .thenByDescending { score(item = it, preferences = preferences) },
            )
            .take(2)

        return RecommendationSet(
            primary = primary,
            backups = backups,
            inventoryShortage = backups.size < 2,
            generatedAtMillis = nowMillis,
        )
    }

    private fun score(item: ContentItem, preferences: UserPreferences): Int {
        val topicScore = item.topicTags.intersect(preferences.preferredTopics).size * 30
        val durationScore = if (preferences.preferredDurationBucket.contains(item.durationMinutes)) {
            100
        } else {
            val distance = abs(item.durationMinutes - preferences.preferredDurationBucket.midpoint)
            maxOf(0, 80 - distance * 8)
        }
        return topicScore + durationScore
    }
}
