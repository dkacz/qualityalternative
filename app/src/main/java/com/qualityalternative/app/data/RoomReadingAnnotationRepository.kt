package com.qualityalternative.app.data

import com.qualityalternative.app.data.local.ReadingAnnotationDao
import com.qualityalternative.app.data.local.ReadingAnnotationEntity
import com.qualityalternative.app.domain.model.AnalyticsEvent
import com.qualityalternative.app.domain.model.AnalyticsEventType
import com.qualityalternative.app.domain.model.ReadingAnnotation
import com.qualityalternative.app.domain.model.ReadingAnnotationDraft
import com.qualityalternative.app.domain.service.AnalyticsTracker
import com.qualityalternative.app.domain.service.ReadingAnnotationRepository
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RoomReadingAnnotationRepository(
    private val dao: ReadingAnnotationDao,
    private val analyticsTracker: AnalyticsTracker,
    private val scope: CoroutineScope,
    private val idProvider: () -> String = { "reading-annotation:${UUID.randomUUID()}" },
) : ReadingAnnotationRepository {
    private val annotations = MutableStateFlow<List<ReadingAnnotation>>(emptyList())
    private val ready = MutableStateFlow(false)
    private val writeMutex = Mutex()

    init {
        scope.launch {
            dao.observeAll()
                .map { rows -> rows.map(ReadingAnnotationEntity::toModel) }
                .collect { loadedAnnotations ->
                    annotations.value = loadedAnnotations.sortedByDescending(ReadingAnnotation::updatedAtMillis)
                    ready.value = true
                }
        }
    }

    override fun readingAnnotations(): List<ReadingAnnotation> = annotations.value

    override fun observeReadingAnnotations(): Flow<List<ReadingAnnotation>> = annotations.asStateFlow()

    override fun observeAnnotationsForContent(contentId: String): Flow<List<ReadingAnnotation>> {
        return dao.observeForContent(contentId = contentId)
            .map { rows -> rows.map(ReadingAnnotationEntity::toModel) }
    }

    override suspend fun saveAnnotation(
        draft: ReadingAnnotationDraft,
        nowMillis: Long,
    ): ReadingAnnotation {
        return writeMutex.withLock {
            val normalizedContentId = draft.contentId.trim()
            val normalizedParagraphIndex = draft.paragraphIndex.coerceAtLeast(0)
            val existing = draft.id
                ?.let { id -> dao.findById(id) }
                ?: dao.findByContentAndParagraph(
                    contentId = normalizedContentId,
                    paragraphIndex = normalizedParagraphIndex,
                )
            val normalized = draft.copy(
                contentId = normalizedContentId,
                paragraphIndex = normalizedParagraphIndex,
            ).toAnnotation(
                id = existing?.id ?: draft.id?.takeIf(String::isNotBlank) ?: idProvider(),
                createdAtMillis = existing?.createdAtMillis ?: nowMillis,
                updatedAtMillis = nowMillis,
            )
            dao.insertOrReplace(normalized.toEntity())
            annotations.value = upsertAnnotation(annotations.value, normalized)
            recordAnnotationEvent(
                type = if (existing == null) {
                    AnalyticsEventType.READING_ANNOTATION_CREATED
                } else {
                    AnalyticsEventType.READING_ANNOTATION_UPDATED
                },
                annotation = normalized,
                timestampMillis = nowMillis,
            )
            normalized
        }
    }

    override suspend fun deleteAnnotation(
        annotationId: String,
        nowMillis: Long,
    ) {
        writeMutex.withLock {
            val existing = dao.findById(annotationId)?.toModel()
            if (existing != null) {
                dao.deleteById(annotationId)
                annotations.value = annotations.value.filterNot { annotation -> annotation.id == annotationId }
                recordAnnotationEvent(
                    type = AnalyticsEventType.READING_ANNOTATION_DELETED,
                    annotation = existing,
                    timestampMillis = nowMillis,
                )
            }
        }
    }

    override suspend fun deleteAnnotationsForContentIds(
        contentIds: Set<String>,
        nowMillis: Long,
    ) {
        if (contentIds.isEmpty()) {
            return
        }
        writeMutex.withLock {
            val existing = dao.findByContentIds(contentIds)
                .map(ReadingAnnotationEntity::toModel)
            if (existing.isNotEmpty()) {
                dao.deleteAllForContentIds(contentIds)
                annotations.value = annotations.value.filterNot { annotation -> annotation.contentId in contentIds }
                existing.forEach { annotation ->
                    recordAnnotationEvent(
                        type = AnalyticsEventType.READING_ANNOTATION_DELETED,
                        annotation = annotation,
                        timestampMillis = nowMillis,
                    )
                }
            }
        }
    }

    override fun isReady(): Boolean = ready.value

    override fun observeReady(): Flow<Boolean> = ready.asStateFlow()

    private suspend fun recordAnnotationEvent(
        type: AnalyticsEventType,
        annotation: ReadingAnnotation,
        timestampMillis: Long,
    ) {
        analyticsTracker.recordDurably(
            AnalyticsEvent(
                type = type,
                timestampMillis = timestampMillis,
                contentId = annotation.contentId,
                metadata = mapOf(
                    "annotationId" to annotation.id,
                    "paragraphIndex" to annotation.paragraphIndex.toString(),
                    "quotedTextLength" to annotation.quotedText.length.toString(),
                    "noteTextLength" to annotation.noteText.length.toString(),
                ),
            ),
        )
    }
}

private fun ReadingAnnotationDraft.toAnnotation(
    id: String,
    createdAtMillis: Long,
    updatedAtMillis: Long,
): ReadingAnnotation {
    val safeContentId = contentId.trim()
    require(safeContentId.isNotBlank()) { "Reading annotation contentId cannot be blank." }
    val safeQuotedText = quotedText.trim()
    require(safeQuotedText.isNotBlank()) { "Reading annotation quote cannot be blank." }
    val safeNoteText = noteText.trim()
    require(safeNoteText.isNotBlank()) { "Reading annotation note cannot be blank." }
    return ReadingAnnotation(
        id = id,
        contentId = safeContentId,
        paragraphIndex = paragraphIndex.coerceAtLeast(0),
        quotedText = safeQuotedText,
        noteText = safeNoteText,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
    )
}

private fun upsertAnnotation(
    currentAnnotations: List<ReadingAnnotation>,
    updatedAnnotation: ReadingAnnotation,
): List<ReadingAnnotation> {
    return currentAnnotations
        .filterNot { annotation -> annotation.id == updatedAnnotation.id }
        .plus(updatedAnnotation)
        .sortedByDescending(ReadingAnnotation::updatedAtMillis)
}

private fun ReadingAnnotation.toEntity(): ReadingAnnotationEntity {
    return ReadingAnnotationEntity(
        id = id,
        contentId = contentId,
        paragraphIndex = paragraphIndex,
        quotedText = quotedText,
        noteText = noteText,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
    )
}

private fun ReadingAnnotationEntity.toModel(): ReadingAnnotation {
    return ReadingAnnotation(
        id = id,
        contentId = contentId,
        paragraphIndex = paragraphIndex,
        quotedText = quotedText,
        noteText = noteText,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
    )
}
