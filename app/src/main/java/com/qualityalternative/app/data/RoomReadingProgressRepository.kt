package com.qualityalternative.app.data

import com.qualityalternative.app.data.local.ReadingProgressDao
import com.qualityalternative.app.data.local.ReadingProgressEntity
import com.qualityalternative.app.domain.model.ReadingProgress
import com.qualityalternative.app.domain.service.ReadingProgressRepository
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

    init {
        scope.launch {
            dao.observeAll()
                .map { rows -> rows.map(ReadingProgressEntity::toModel) }
                .collect { loadedProgress ->
                    progress.value = loadedProgress.sortedByDescending(ReadingProgress::updatedAtMillis)
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

    override suspend fun saveProgress(progress: ReadingProgress) {
        writeMutex.withLock {
            val normalized = progress.normalized()
            dao.insertOrReplace(normalized.toEntity())
            this.progress.value = upsertProgress(this.progress.value, normalized)
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
}

private fun ReadingProgress.normalized(): ReadingProgress {
    val safeParagraphCount = paragraphCount.coerceAtLeast(1)
    val safePercent = progressPercent.coerceIn(0, 100)
    return copy(
        progressPercent = if (completedAtMillis == null) safePercent.coerceAtMost(99) else 100,
        lastVisibleParagraphIndex = lastVisibleParagraphIndex.coerceIn(0, safeParagraphCount - 1),
        paragraphCount = safeParagraphCount,
    )
}

private fun upsertProgress(
    currentProgress: List<ReadingProgress>,
    updatedProgress: ReadingProgress,
): List<ReadingProgress> {
    return currentProgress
        .filterNot { it.contentId == updatedProgress.contentId }
        .plus(updatedProgress)
        .sortedByDescending(ReadingProgress::updatedAtMillis)
}

private fun ReadingProgress.toEntity(): ReadingProgressEntity {
    return ReadingProgressEntity(
        contentId = contentId,
        progressPercent = progressPercent,
        lastVisibleParagraphIndex = lastVisibleParagraphIndex,
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
        paragraphCount = paragraphCount,
        updatedAtMillis = updatedAtMillis,
        completedAtMillis = completedAtMillis,
    )
}
