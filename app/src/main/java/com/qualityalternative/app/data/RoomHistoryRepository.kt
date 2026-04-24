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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RoomHistoryRepository(
    private val dao: ReplacementSessionDao,
    private val scope: CoroutineScope,
) : HistoryRepository {
    private val entries = MutableStateFlow<List<ReplacementHistoryEntry>>(emptyList())
    private val nowMillis = MutableStateFlow(System.currentTimeMillis())
    private val ready = MutableStateFlow(false)
    private val writeMutex = Mutex()

    init {
        scope.launch {
            dao.observeAll()
                .map { rows -> rows.map(ReplacementSessionEntity::toModel) }
                .collect { loadedEntries ->
                    entries.value = loadedEntries.sortedByDescending(ReplacementHistoryEntry::acceptedAtMillis)
                    ready.value = true
                }
        }
        scope.launch {
            while (true) {
                nowMillis.value = System.currentTimeMillis()
                delay(HISTORY_REFRESH_INTERVAL_MILLIS)
            }
        }
    }

    override fun recentHistory(nowMillis: Long, windowDays: Int): List<ReplacementHistoryEntry> {
        return filterRecentHistory(entries = entries.value, nowMillis = nowMillis, windowDays = windowDays)
    }

    override fun observeRecentHistory(nowMillis: Long, windowDays: Int): Flow<List<ReplacementHistoryEntry>> {
        return entries.asStateFlow().combine(this.nowMillis.asStateFlow()) { allEntries, currentTime ->
            filterRecentHistory(entries = allEntries, nowMillis = currentTime, windowDays = windowDays)
        }
    }

    override fun observeCompletedContentIds(): Flow<Set<String>> {
        return entries.asStateFlow().map { currentEntries ->
            currentEntries.filter(ReplacementHistoryEntry::isCompleted)
                .mapTo(mutableSetOf(), ReplacementHistoryEntry::contentId)
        }
    }

    override suspend fun recordAcceptedSession(
        targetApp: DistractingApp,
        interventionId: String,
        interventionShownAtMillis: Long,
        primaryContentId: String,
        backupContentIds: List<String>,
        content: ContentItem,
        source: RecommendationSource,
        acceptedAtMillis: Long,
    ): String {
        return writeMutex.withLock {
            val sessionId = UUID.randomUUID().toString()
            val entry = ReplacementHistoryEntry(
                sessionId = sessionId,
                interventionId = interventionId,
                targetAppPackage = targetApp.packageName,
                targetAppDisplayName = targetApp.displayName,
                interventionShownAtMillis = interventionShownAtMillis,
                primaryContentId = primaryContentId,
                backupContentIds = backupContentIds,
                contentId = content.id,
                contentTitle = content.title,
                contentDescription = content.description,
                contentTopics = content.topicTags,
                packId = content.packId,
                recommendationSource = source,
                acceptedAtMillis = acceptedAtMillis,
            )
            persist(entry)
            sessionId
        }
    }

    override suspend fun markCompleted(sessionId: String, completedAtMillis: Long) {
        writeMutex.withLock {
            updateEntry(sessionId) { entry ->
                entry.copy(completedAtMillis = completedAtMillis, skippedAtMillis = null)
            }
        }
    }

    override suspend fun updateAcceptedSessionContent(sessionId: String, content: ContentItem) {
        writeMutex.withLock {
            updateEntry(sessionId) { entry ->
                entry.copy(
                    contentId = content.id,
                    contentTitle = content.title,
                    contentDescription = content.description,
                    contentTopics = content.topicTags,
                    packId = content.packId,
                )
            }
        }
    }

    override suspend fun markSkipped(sessionId: String, skippedAtMillis: Long) {
        writeMutex.withLock {
            updateEntry(sessionId) { entry ->
                entry.copy(skippedAtMillis = skippedAtMillis)
            }
        }
    }

    override suspend fun attachFeedback(sessionId: String, feedback: SessionFeedback) {
        writeMutex.withLock {
            updateEntry(sessionId) { entry ->
                entry.copy(
                    feedbackGoodFit = feedback.wasGoodFit,
                    feedbackHelpedAvoidScrolling = feedback.helpedAvoidScrolling,
                    feedbackFitRating = feedback.fitRating,
                    feedbackScrollRating = feedback.scrollRating,
                )
            }
        }
    }

    override suspend fun markReturnedToTarget(
        targetAppPackage: String,
        returnedAtMillis: Long,
    ): ReturnToTargetSignal? {
        return writeMutex.withLock {
            val candidate = entries.value.firstOrNull { entry ->
                entry.targetAppPackage == targetAppPackage
            } ?: return@withLock null

            val effectiveReturnedAtMillis = candidate.returnedToTargetAtMillis ?: returnedAtMillis
            if (candidate.returnedToTargetAtMillis == null) {
                persist(candidate.copy(returnedToTargetAtMillis = returnedAtMillis))
            }
            val delta = effectiveReturnedAtMillis - candidate.interventionShownAtMillis
            ReturnToTargetSignal(
                sessionId = candidate.sessionId,
                interventionId = candidate.interventionId,
                targetAppPackage = candidate.targetAppPackage,
                primaryContentId = candidate.primaryContentId,
                backupContentIds = candidate.backupContentIds,
                contentId = candidate.contentId,
                returnedAtMillis = effectiveReturnedAtMillis,
                within15Minutes = delta <= 15 * 60_000L,
                within60Minutes = delta <= 60 * 60_000L,
            )
        }
    }

    override fun isReady(): Boolean = ready.value

    override fun observeReady(): Flow<Boolean> = ready.asStateFlow()

    private suspend fun updateEntry(
        sessionId: String,
        transform: (ReplacementHistoryEntry) -> ReplacementHistoryEntry,
    ) {
        val existing = entries.value.firstOrNull { it.sessionId == sessionId } ?: return
        persist(transform(existing))
    }

    private suspend fun persist(entry: ReplacementHistoryEntry) {
        dao.insertOrReplace(entry.toEntity())
        entries.value = upsertEntry(entries.value, entry)
    }

    private fun filterRecentHistory(
        entries: List<ReplacementHistoryEntry>,
        nowMillis: Long,
        windowDays: Int,
    ): List<ReplacementHistoryEntry> {
        val cutoff = nowMillis - windowDays * DAY_IN_MILLIS
        return entries
            .filter { it.acceptedAtMillis >= cutoff }
            .sortedByDescending(ReplacementHistoryEntry::acceptedAtMillis)
    }

    private companion object {
        const val DAY_IN_MILLIS = 24 * 60 * 60 * 1000L
        const val HISTORY_REFRESH_INTERVAL_MILLIS = 60_000L
    }
}

private fun upsertEntry(
    currentEntries: List<ReplacementHistoryEntry>,
    updatedEntry: ReplacementHistoryEntry,
): List<ReplacementHistoryEntry> {
    return currentEntries
        .filterNot { it.sessionId == updatedEntry.sessionId }
        .plus(updatedEntry)
        .sortedByDescending(ReplacementHistoryEntry::acceptedAtMillis)
}

private fun ReplacementHistoryEntry.toEntity(): ReplacementSessionEntity {
    return ReplacementSessionEntity(
        sessionId = sessionId,
        interventionId = interventionId,
        targetAppPackage = targetAppPackage,
        targetAppDisplayName = targetAppDisplayName,
        interventionShownAtMillis = interventionShownAtMillis,
        primaryContentId = primaryContentId,
        backupContentIdsCsv = backupContentIds.joinToString(","),
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
        feedbackFitRating = feedbackFitRating,
        feedbackScrollRating = feedbackScrollRating,
    )
}

private fun ReplacementSessionEntity.toModel(): ReplacementHistoryEntry {
    return ReplacementHistoryEntry(
        sessionId = sessionId,
        interventionId = interventionId,
        targetAppPackage = targetAppPackage,
        targetAppDisplayName = targetAppDisplayName,
        interventionShownAtMillis = interventionShownAtMillis,
        primaryContentId = primaryContentId,
        backupContentIds = backupContentIdsCsv.toStringList(),
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
        feedbackFitRating = feedbackFitRating,
        feedbackScrollRating = feedbackScrollRating,
    )
}

private fun String.toStringList(): List<String> {
    if (isBlank()) {
        return emptyList()
    }
    return split(",").filter(String::isNotBlank)
}
