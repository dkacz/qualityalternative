package com.qualityalternative.app.data

import com.qualityalternative.app.data.local.ReadingProgressDao
import com.qualityalternative.app.data.local.ReadingProgressEntity
import com.qualityalternative.app.domain.model.ReadingProgress
import com.qualityalternative.app.domain.service.ReadingProgressRepository
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RoomReadingProgressRepository(
    private val dao: ReadingProgressDao,
    private val scope: CoroutineScope,
) : ReadingProgressRepository {
    private val progress = MutableStateFlow<List<ReadingProgress>>(emptyList())
    private val ready = MutableStateFlow(false)
    private val writeMutex = Mutex()
    private val nextUnfinishedSaveDelayForTests = AtomicReference<SaveDelayForTests?>(null)

    class SaveDelayForTests internal constructor() {
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
    }

    init {
        scope.launch {
            dao.observeAll()
                .map { rows -> rows.map(ReadingProgressEntity::toModel) }
                .collect { loadedProgress ->
                    progress.mergeLoadedProgress(loadedProgress)
                    ready.value = true
                }
        }
    }

    override fun readingProgress(): List<ReadingProgress> = progress.value

    override fun observeReadingProgress(): Flow<List<ReadingProgress>> = progress.asStateFlow()

    override fun observeCompletedContentIds(): Flow<Set<String>> {
        return progress.asStateFlow().map { currentProgress ->
            currentProgress.filter(ReadingProgress::isCompleted)
                .mapTo(mutableSetOf(), ReadingProgress::contentId)
        }
    }

    override fun cachePendingProgress(progress: ReadingProgress) {
        val normalized = progress.normalized()
        this.progress.tryUpsertNewestProgress(normalized)
    }

    override suspend fun saveProgress(progress: ReadingProgress) {
        writeMutex.withLock {
            val normalized = progress.normalized()
            if (!this.progress.tryUpsertNewestProgress(normalized)) {
                return
            }
            awaitSaveDelayForTestsIfNeeded(normalized)
            dao.insertOrReplace(normalized.toEntity())
        }
    }

    override suspend fun deleteProgress(contentId: String) {
        writeMutex.withLock {
            dao.delete(contentId)
            progress.value = progress.value.filterNot { it.contentId == contentId }
        }
    }

    override suspend fun deleteProgressForContentIds(contentIds: Set<String>) {
        if (contentIds.isEmpty()) {
            return
        }
        writeMutex.withLock {
            dao.deleteAll(contentIds)
            progress.value = progress.value.filterNot { it.contentId in contentIds }
        }
    }

    override fun isReady(): Boolean = ready.value

    override fun observeReady(): Flow<Boolean> = ready.asStateFlow()

    fun delayNextUnfinishedSaveForTests(): SaveDelayForTests {
        val delay = SaveDelayForTests()
        nextUnfinishedSaveDelayForTests.set(delay)
        return delay
    }

    private suspend fun awaitSaveDelayForTestsIfNeeded(progress: ReadingProgress) {
        if (!progress.isUnfinished()) {
            return
        }
        nextUnfinishedSaveDelayForTests.getAndSet(null)?.let { delay ->
            delay.started.complete(Unit)
            delay.release.await()
        }
    }
}

private fun ReadingProgress.normalized(): ReadingProgress {
    val safeParagraphCount = paragraphCount.coerceAtLeast(1)
    val safePercent = progressPercent.coerceIn(0, 100)
    return copy(
        progressPercent = if (completedAtMillis == null) safePercent.coerceAtMost(99) else 100,
        lastVisibleParagraphIndex = lastVisibleParagraphIndex.coerceIn(0, safeParagraphCount - 1),
        lastVisibleTextOffset = lastVisibleTextOffset.coerceAtLeast(0),
        paragraphCount = safeParagraphCount,
    )
}

private fun mergeProgressLists(
    currentProgress: List<ReadingProgress>,
    loadedProgress: List<ReadingProgress>,
): List<ReadingProgress> {
    return (currentProgress + loadedProgress)
        .groupBy(ReadingProgress::contentId)
        .values
        .map { candidates ->
            candidates.reduce { kept, candidate ->
                if (shouldKeepExistingProgress(existing = kept, incoming = candidate)) kept else candidate
            }
        }
        .sortedByDescending(ReadingProgress::updatedAtMillis)
}

private fun MutableStateFlow<List<ReadingProgress>>.mergeLoadedProgress(
    loadedProgress: List<ReadingProgress>,
) {
    while (true) {
        val currentProgress = value
        val mergedProgress = mergeProgressLists(currentProgress, loadedProgress)
        if (mergedProgress == currentProgress || compareAndSet(currentProgress, mergedProgress)) {
            return
        }
    }
}

private fun MutableStateFlow<List<ReadingProgress>>.tryUpsertNewestProgress(
    updatedProgress: ReadingProgress,
): Boolean {
    while (true) {
        val currentProgress = value
        val nextProgress = upsertNewestProgress(currentProgress, updatedProgress) ?: return false
        if (compareAndSet(currentProgress, nextProgress)) {
            return true
        }
    }
}

private fun upsertNewestProgress(
    currentProgress: List<ReadingProgress>,
    updatedProgress: ReadingProgress,
): List<ReadingProgress>? {
    val existing = currentProgress.firstOrNull { it.contentId == updatedProgress.contentId }
    if (shouldKeepExistingProgress(existing = existing, incoming = updatedProgress)) {
        return null
    }
    return currentProgress
        .filterNot { it.contentId == updatedProgress.contentId }
        .plus(updatedProgress)
        .sortedByDescending(ReadingProgress::updatedAtMillis)
}

private fun shouldKeepExistingProgress(
    existing: ReadingProgress?,
    incoming: ReadingProgress,
): Boolean {
    if (existing == null) {
        return false
    }
    if (existing.isCompleted() && incoming.isUnfinished()) {
        return true
    }
    if (existing.isUnfinished() && incoming.isCompleted()) {
        return false
    }
    if (incoming.updatedAtMillis < existing.updatedAtMillis) {
        return true
    }
    if (incoming.updatedAtMillis > existing.updatedAtMillis) {
        return false
    }
    if (incoming.lastVisibleParagraphIndex < existing.lastVisibleParagraphIndex) {
        return true
    }
    return incoming.lastVisibleParagraphIndex == existing.lastVisibleParagraphIndex &&
        incoming.lastVisibleTextOffset < existing.lastVisibleTextOffset
}

private fun ReadingProgress.toEntity(): ReadingProgressEntity {
    return ReadingProgressEntity(
        contentId = contentId,
        progressPercent = progressPercent,
        lastVisibleParagraphIndex = lastVisibleParagraphIndex,
        lastVisibleTextOffset = lastVisibleTextOffset,
        paragraphCount = paragraphCount,
        updatedAtMillis = updatedAtMillis,
        completedAtMillis = completedAtMillis,
    )
}

private fun ReadingProgressEntity.toModel(): ReadingProgress {
    return ReadingProgress(
        contentId = contentId,
        progressPercent = progressPercent,
        lastVisibleParagraphIndex = lastVisibleParagraphIndex,
        lastVisibleTextOffset = lastVisibleTextOffset,
        paragraphCount = paragraphCount,
        updatedAtMillis = updatedAtMillis,
        completedAtMillis = completedAtMillis,
    )
}
