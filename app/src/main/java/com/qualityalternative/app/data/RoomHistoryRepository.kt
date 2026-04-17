package com.qualityalternative.app.data

import com.qualityalternative.app.data.local.ReplacementSessionDao
import com.qualityalternative.app.data.local.ReplacementSessionEntity
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.model.RecommendationSource
import com.qualityalternative.app.domain.model.ReplacementHistoryEntry
import com.qualityalternative.app.domain.model.ReturnToTargetSignal
import com.qualityalternative.app.domain.model.SessionFeedback
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.service.HistoryRepository
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class RoomHistoryRepository(
    private val dao: ReplacementSessionDao,
    private val scope: CoroutineScope,
) : HistoryRepository {
    private val entries = MutableStateFlow<List<ReplacementHistoryEntry>>(emptyList())

    init {
        scope.launch {
            val loaded = dao.getAll().map(ReplacementSessionEntity::toModel)
            if (entries.value.isEmpty()) {
                entries.value = loaded
            }
        }
    }

    override fun recentHistory(nowMillis: Long, windowDays: Int): List<ReplacementHistoryEntry> {
        val cutoff = nowMillis - windowDays * DAY_IN_MILLIS
        return entries.value
            .filter { it.acceptedAtMillis >= cutoff }
            .sortedByDescending(ReplacementHistoryEntry::acceptedAtMillis)
    }

    override fun observeRecentHistory(nowMillis: Long, windowDays: Int): Flow<List<ReplacementHistoryEntry>> {
        return entries.asStateFlow().map { all ->
            val cutoff = nowMillis - windowDays * DAY_IN_MILLIS
            all.filter { it.acceptedAtMillis >= cutoff }
                .sortedByDescending(ReplacementHistoryEntry::acceptedAtMillis)
        }
    }

    override fun recordAcceptedSession(
        targetApp: DistractingApp,
        content: ContentItem,
        source: RecommendationSource,
        acceptedAtMillis: Long,
    ): String {
        val sessionId = UUID.randomUUID().toString()
        val entry = ReplacementHistoryEntry(
            sessionId = sessionId,
            targetAppPackage = targetApp.packageName,
            targetAppDisplayName = targetApp.displayName,
            contentId = content.id,
            contentTitle = content.title,
            contentDescription = content.description,
            contentTopics = content.topicTags,
            packId = content.packId,
            recommendationSource = source,
            acceptedAtMillis = acceptedAtMillis,
        )
        applyUpdate(entry.sessionId) { entry }
        persist(entry)
        return sessionId
    }

    override fun markCompleted(sessionId: String, completedAtMillis: Long) {
        updateEntry(sessionId) { entry ->
            entry.copy(completedAtMillis = completedAtMillis, skippedAtMillis = null)
        }
    }

    override fun markSkipped(sessionId: String, skippedAtMillis: Long) {
        updateEntry(sessionId) { entry ->
            entry.copy(skippedAtMillis = skippedAtMillis)
        }
    }

    override fun attachFeedback(sessionId: String, feedback: SessionFeedback) {
        updateEntry(sessionId) { entry ->
            entry.copy(
                feedbackGoodFit = feedback.wasGoodFit,
                feedbackHelpedAvoidScrolling = feedback.helpedAvoidScrolling,
            )
        }
    }

    override fun markReturnedToTarget(
        targetAppPackage: String,
        returnedAtMillis: Long,
    ): ReturnToTargetSignal? {
        val candidate = entries.value.firstOrNull { entry ->
            entry.targetAppPackage == targetAppPackage &&
                entry.returnedToTargetAtMillis == null
        } ?: return null

        val delta = returnedAtMillis - candidate.lastInteractionAtMillis()
        val updated = candidate.copy(returnedToTargetAtMillis = returnedAtMillis)
        applyUpdate(candidate.sessionId) { updated }
        persist(updated)
        return ReturnToTargetSignal(
            sessionId = candidate.sessionId,
            targetAppPackage = candidate.targetAppPackage,
            contentId = candidate.contentId,
            returnedAtMillis = returnedAtMillis,
            within15Minutes = delta <= 15 * 60_000L,
            within60Minutes = delta <= 60 * 60_000L,
        )
    }

    private fun updateEntry(
        sessionId: String,
        transform: (ReplacementHistoryEntry) -> ReplacementHistoryEntry,
    ) {
        val existing = entries.value.firstOrNull { it.sessionId == sessionId } ?: return
        val updated = transform(existing)
        applyUpdate(sessionId) { updated }
        persist(updated)
    }

    private fun applyUpdate(
        sessionId: String,
        createOrUpdate: (ReplacementHistoryEntry?) -> ReplacementHistoryEntry,
    ) {
        val existing = entries.value.firstOrNull { it.sessionId == sessionId }
        val next = createOrUpdate(existing)
        entries.value = (entries.value.filterNot { it.sessionId == sessionId } + next)
            .sortedByDescending(ReplacementHistoryEntry::acceptedAtMillis)
    }

    private fun persist(entry: ReplacementHistoryEntry) {
        scope.launch {
            dao.insertOrReplace(entry.toEntity())
        }
    }

    private companion object {
        const val DAY_IN_MILLIS = 24 * 60 * 60 * 1000L
    }
}

private fun ReplacementHistoryEntry.toEntity(): ReplacementSessionEntity {
    return ReplacementSessionEntity(
        sessionId = sessionId,
        targetAppPackage = targetAppPackage,
        targetAppDisplayName = targetAppDisplayName,
        contentId = contentId,
        contentTitle = contentTitle,
        contentDescription = contentDescription,
        contentTopicsCsv = contentTopics.joinToString(",") { it.name },
        packId = packId,
        recommendationSource = recommendationSource.name,
        acceptedAtMillis = acceptedAtMillis,
        completedAtMillis = completedAtMillis,
        skippedAtMillis = skippedAtMillis,
        returnedToTargetAtMillis = returnedToTargetAtMillis,
        feedbackGoodFit = feedbackGoodFit,
        feedbackHelpedAvoidScrolling = feedbackHelpedAvoidScrolling,
    )
}

private fun ReplacementSessionEntity.toModel(): ReplacementHistoryEntry {
    return ReplacementHistoryEntry(
        sessionId = sessionId,
        targetAppPackage = targetAppPackage,
        targetAppDisplayName = targetAppDisplayName,
        contentId = contentId,
        contentTitle = contentTitle,
        contentDescription = contentDescription,
        contentTopics = if (contentTopicsCsv.isBlank()) {
            emptySet()
        } else {
            contentTopicsCsv.split(",")
                .mapNotNullTo(mutableSetOf()) { raw ->
                    runCatching { TopicTag.valueOf(raw) }.getOrNull()
                }
        },
        packId = packId,
        recommendationSource = RecommendationSource.valueOf(recommendationSource),
        acceptedAtMillis = acceptedAtMillis,
        completedAtMillis = completedAtMillis,
        skippedAtMillis = skippedAtMillis,
        returnedToTargetAtMillis = returnedToTargetAtMillis,
        feedbackGoodFit = feedbackGoodFit,
        feedbackHelpedAvoidScrolling = feedbackHelpedAvoidScrolling,
    )
}
