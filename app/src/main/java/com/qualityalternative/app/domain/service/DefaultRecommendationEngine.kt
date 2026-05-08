package com.qualityalternative.app.domain.service

import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.ContentAvailability
import com.qualityalternative.app.domain.model.ContentPriority
import com.qualityalternative.app.domain.model.ContentSourceType
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.model.RecommendationSet
import com.qualityalternative.app.domain.model.RecommendationSignals
import com.qualityalternative.app.domain.model.TimeOfDayBucket
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.model.UserPreferences

class DefaultRecommendationEngine : RecommendationEngine {
    override fun generate(
        targetApp: DistractingApp,
        preferences: UserPreferences,
        inventory: List<ContentItem>,
        excludedContentIds: Set<String>,
        signals: RecommendationSignals,
        nowMillis: Long,
    ): RecommendationSet? {
        val eligibleInventory = inventory
            .filter { item ->
                item.availability != ContentAvailability.UNAVAILABLE &&
                    item.id !in excludedContentIds
            }
        val newestUserDocumentAddedAtMillis = eligibleInventory
            .asSequence()
            .filter { item -> item.sourceType == ContentSourceType.USER_DOCUMENT }
            .mapNotNull(ContentItem::addedAtMillis)
            .maxOrNull()
        val scoredCandidates = eligibleInventory
            .map { item ->
                ScoredCandidate(
                    item = item,
                    selectionRank = selectionRank(item = item, preferences = preferences),
                    score = score(
                        item = item,
                        preferences = preferences,
                        signals = signals,
                        newestUserDocumentAddedAtMillis = newestUserDocumentAddedAtMillis,
                    ),
                    addedAtMillis = item.addedAtMillis ?: Long.MIN_VALUE,
                )
            }
            .sortedWith(
                compareByDescending<ScoredCandidate> { it.selectionRank }
                    .thenByDescending { it.score }
                    .thenBy { it.item.sourceType.primaryTiePriority() }
                    .thenByDescending { it.addedAtMillis }
                    .thenBy { it.item.title },
            )

        val candidateSets = scoredCandidates
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
                inventoryShortage = unfinishedChoice.backups.size < MIN_BACKUP_OPTIONS,
                generatedAtMillis = nowMillis,
            )
        }

        val highestSelectionRank = candidateSets.firstOrNull()?.primary?.selectionRank ?: return null
        val highestRankCandidateSets = candidateSets
            .filter { candidate -> candidate.primary.selectionRank == highestSelectionRank }
        val canBuildMinimumSet = highestRankCandidateSets.any { it.backups.size >= MIN_BACKUP_OPTIONS }
        val chosen = highestRankCandidateSets
            .firstOrNull { !canBuildMinimumSet || it.backups.size >= MIN_BACKUP_OPTIONS }
            ?: return null

        return RecommendationSet(
            primary = chosen.primary.item,
            backups = chosen.backups.map(ScoredCandidate::item),
            inventoryShortage = chosen.backups.size < MIN_BACKUP_OPTIONS,
            generatedAtMillis = nowMillis,
        )
    }

    private fun backupsFor(
        primary: ScoredCandidate,
        scoredCandidates: List<ScoredCandidate>,
    ): List<ScoredCandidate> {
        val sortedBackups = scoredCandidates
            .filter { candidate ->
                candidate.item.id != primary.item.id
            }
            .sortedWith(
                compareByDescending<ScoredCandidate> { it.selectionRank }
                    .thenByDescending { it.score }
                    .thenBy { it.item.sourceType.backupPriority() }
                    .thenByDescending { it.addedAtMillis }
                    .thenBy { it.item.title },
            )
        val cappedBackups = sortedBackups.take(MAX_BACKUP_OPTIONS)
        if (
            primary.item.sourceType == ContentSourceType.MEDITATION ||
            cappedBackups.any { candidate -> candidate.item.sourceType == ContentSourceType.MEDITATION }
        ) {
            return cappedBackups
        }
        val meditationBackup = sortedBackups.firstOrNull { candidate ->
            candidate.item.sourceType == ContentSourceType.MEDITATION
        } ?: return cappedBackups
        return if (cappedBackups.size < MAX_BACKUP_OPTIONS) {
            cappedBackups + meditationBackup
        } else {
            cappedBackups.dropLast(1) + meditationBackup
        }
    }

    private fun selectionRank(item: ContentItem, preferences: UserPreferences): Int {
        return when {
            item.id in preferences.unfinishedContentIds -> 2
            item.id in preferences.priorityContentIds -> 1
            else -> 0
        }
    }

    private fun score(
        item: ContentItem,
        preferences: UserPreferences,
        signals: RecommendationSignals,
        newestUserDocumentAddedAtMillis: Long?,
    ): Int {
        val topicScore = item.topicTags.intersect(preferences.preferredTopics).size * 30
        val completionBoost = item.topicTags.intersect(signals.completedTopics).size * 12
        val skipPenalty = item.topicTags.intersect(signals.skippedTopics).size * 18
        val packBoost = if (item.packId in signals.successfulPackIds) 20 else 0
        val priorityBoost = preferences.contentPriority.boostFor(item.sourceType)
        val priorityPickBoost = if (item.id in preferences.priorityContentIds) 96 else 0
        val unfinishedBoost = if (item.id in preferences.unfinishedContentIds) 10_000 else 0
        val freshUserDocumentBoost = if (
            item.sourceType == ContentSourceType.USER_DOCUMENT &&
            item.addedAtMillis != null &&
            item.addedAtMillis == newestUserDocumentAddedAtMillis &&
            item.id !in preferences.priorityContentIds &&
            item.id !in preferences.unfinishedContentIds
        ) {
            72
        } else {
            0
        }
        val timeOfDayBoost = when (signals.timeOfDay) {
            TimeOfDayBucket.MORNING -> item.topicFitBoost(TopicTag.PRACTICAL, TopicTag.BODY, TopicTag.SCIENCE)
            TimeOfDayBucket.MIDDAY -> item.topicFitBoost(TopicTag.SCIENCE, TopicTag.TECH, TopicTag.ESSAYS)
            TimeOfDayBucket.EVENING -> item.topicFitBoost(TopicTag.PHILOSOPHY, TopicTag.FICTION, TopicTag.POETRY)
            TimeOfDayBucket.NIGHT -> item.topicFitBoost(TopicTag.BODY, TopicTag.PSYCHOLOGY, TopicTag.PHILOSOPHY)
        }
        return topicScore + completionBoost + packBoost + priorityBoost + priorityPickBoost +
            unfinishedBoost + freshUserDocumentBoost + timeOfDayBoost - skipPenalty
    }

    private data class ScoredCandidate(
        val item: ContentItem,
        val selectionRank: Int,
        val score: Int,
        val addedAtMillis: Long,
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

    private fun ContentSourceType.primaryTiePriority(): Int {
        return if (this == ContentSourceType.MEDITATION) 1 else 0
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

    private fun ContentItem.topicFitBoost(vararg topics: TopicTag): Int {
        return if (topicTags.any { topic -> topic in topics }) 18 else 4
    }

    private companion object {
        const val MIN_BACKUP_OPTIONS = 2
        const val MAX_BACKUP_OPTIONS = 6
    }
}
