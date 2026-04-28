package com.qualityalternative.app.domain.service

import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.ContentAvailability
import com.qualityalternative.app.domain.model.ContentPriority
import com.qualityalternative.app.domain.model.ContentSourceType
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
            .filter { item -> item.availability != ContentAvailability.UNAVAILABLE }
            .map { item ->
                ScoredCandidate(
                    item = item,
                    score = score(item = item, preferences = preferences, signals = signals),
                    durationDistance = abs(item.durationMinutes - preferences.preferredDurationBucket.midpoint),
                )
            }
            .sortedWith(
                compareByDescending<ScoredCandidate> { it.score }
                    .thenBy { it.durationDistance }
                    .thenBy { it.item.title },
            )

        val candidateSets = scoredCandidates
            .filter { it.item.id !in primaryExcludedIds }
            .map { primary ->
                CandidateRecommendation(
                    primary = primary,
                    backups = backupsFor(primary = primary, scoredCandidates = scoredCandidates),
                )
            }

        val unfinishedChoice = candidateSets.firstOrNull { candidate ->
            candidate.primary.item.id in preferences.unfinishedContentIds
        }
        if (unfinishedChoice != null) {
            return RecommendationSet(
                primary = unfinishedChoice.primary.item,
                backups = unfinishedChoice.backups.map(ScoredCandidate::item),
                inventoryShortage = unfinishedChoice.backups.size < 2,
                generatedAtMillis = nowMillis,
            )
        }

        val canBuildFullSet = candidateSets.any { it.backups.size == 2 }
        val chosen = candidateSets
            .firstOrNull { !canBuildFullSet || it.backups.size == 2 }
            ?: return null

        return RecommendationSet(
            primary = chosen.primary.item,
            backups = chosen.backups.map(ScoredCandidate::item),
            inventoryShortage = chosen.backups.size < 2,
            generatedAtMillis = nowMillis,
        )
    }

    private fun backupsFor(
        primary: ScoredCandidate,
        scoredCandidates: List<ScoredCandidate>,
    ): List<ScoredCandidate> {
        return scoredCandidates
            .filter { candidate ->
                candidate.item.id != primary.item.id &&
                    candidate.item.durationMinutes <= primary.item.durationMinutes
            }
            .sortedWith(
                compareByDescending<ScoredCandidate> { it.score }
                    .thenBy { it.item.sourceType.backupPriority() }
                    .thenBy { abs(it.item.durationMinutes - DurationBucket.QUICK.midpoint) }
                    .thenBy { it.durationDistance }
                    .thenBy { it.item.title },
            )
            .take(2)
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
        val utilityBoost = if (item.sourceType == ContentSourceType.MEDITATION) 24 else 0
        val priorityBoost = preferences.contentPriority.boostFor(item.sourceType)
        val priorityPickBoost = if (item.id in preferences.priorityContentIds) 96 else 0
        val unfinishedBoost = if (item.id in preferences.unfinishedContentIds) 10_000 else 0
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
        return topicScore + durationScore + completionBoost + packBoost + utilityBoost + priorityBoost + priorityPickBoost + unfinishedBoost + timeOfDayBoost - skipPenalty
    }

    private data class ScoredCandidate(
        val item: ContentItem,
        val score: Int,
        val durationDistance: Int,
    )

    private data class CandidateRecommendation(
        val primary: ScoredCandidate,
        val backups: List<ScoredCandidate>,
    )

    private fun ContentSourceType.backupPriority(): Int {
        return when (this) {
            ContentSourceType.EDITORIAL -> 0
            ContentSourceType.MEDITATION -> 1
            ContentSourceType.USER_DOCUMENT -> 2
            ContentSourceType.USER_LINK -> 3
        }
    }

    private fun ContentPriority.boostFor(sourceType: ContentSourceType): Int {
        return when (this) {
            ContentPriority.BALANCED -> 0
            ContentPriority.READINGS -> if (sourceType == ContentSourceType.EDITORIAL) 48 else 0
            ContentPriority.MY_FILES -> if (sourceType == ContentSourceType.USER_DOCUMENT) 48 else 0
            ContentPriority.SAVED_LINKS -> if (sourceType == ContentSourceType.USER_LINK) 48 else 0
            ContentPriority.MEDITATION -> if (sourceType == ContentSourceType.MEDITATION) 48 else 0
        }
    }
}
