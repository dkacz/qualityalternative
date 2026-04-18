package com.qualityalternative.app.domain.service

import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.model.DurationBucket
import com.qualityalternative.app.domain.model.RecommendationSet
import com.qualityalternative.app.domain.model.RecommendationSignals
import com.qualityalternative.app.domain.model.TimeOfDayBucket
import com.qualityalternative.app.domain.model.UserPreferences
import kotlin.math.abs

class DefaultRecommendationEngine : RecommendationEngine {
    override fun generate(
        targetApp: DistractingApp,
        preferences: UserPreferences,
        inventory: List<ContentItem>,
        primaryExcludedIds: Set<String>,
        signals: RecommendationSignals,
        nowMillis: Long,
    ): RecommendationSet? {
        val scoredCandidates = inventory
            .sortedWith(
                compareByDescending<ContentItem> { score(item = it, preferences = preferences, signals = signals) }
                    .thenBy { abs(it.durationMinutes - preferences.preferredDurationBucket.midpoint) }
                    .thenBy { it.title },
            )

        val primary = scoredCandidates
            .firstOrNull { it.id !in primaryExcludedIds }
            ?: scoredCandidates.firstOrNull()
            ?: return null

        val backups = scoredCandidates
            .filterNot { it.id == primary.id }
            .filter { it.durationMinutes <= primary.durationMinutes }
            .sortedWith(
                compareBy<ContentItem> { abs(it.durationMinutes - DurationBucket.QUICK.midpoint) }
                    .thenByDescending { score(item = it, preferences = preferences, signals = signals) },
            )
            .take(2)

        return RecommendationSet(
            primary = primary,
            backups = backups,
            inventoryShortage = backups.size < 2,
            generatedAtMillis = nowMillis,
        )
    }

    private fun score(
        item: ContentItem,
        preferences: UserPreferences,
        signals: RecommendationSignals,
    ): Int {
        val topicScore = item.topicTags.intersect(preferences.preferredTopics).size * 30
        val durationScore = if (preferences.preferredDurationBucket.contains(item.durationMinutes)) {
            100
        } else {
            val distance = abs(item.durationMinutes - preferences.preferredDurationBucket.midpoint)
            maxOf(0, 80 - distance * 8)
        }
        val completionBoost = item.topicTags.intersect(signals.completedTopics).size * 12
        val skipPenalty = item.topicTags.intersect(signals.skippedTopics).size * 18
        val packBoost = if (item.packId in signals.successfulPackIds) 20 else 0
        val timeOfDayBoost = when (signals.timeOfDay) {
            TimeOfDayBucket.MORNING -> when {
                item.durationMinutes <= DurationBucket.QUICK.maxMinutes -> 18
                item.durationMinutes <= DurationBucket.FOCUS.maxMinutes -> 10
                else -> 0
            }

            TimeOfDayBucket.MIDDAY -> if (DurationBucket.FOCUS.contains(item.durationMinutes)) 18 else 4
            TimeOfDayBucket.EVENING -> if (DurationBucket.DEEP.contains(item.durationMinutes)) 18 else 6
            TimeOfDayBucket.NIGHT -> if (item.durationMinutes <= DurationBucket.FOCUS.maxMinutes) 14 else 0
        }
        return topicScore + durationScore + completionBoost + packBoost + timeOfDayBoost - skipPenalty
    }
}
