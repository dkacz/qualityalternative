package com.qualityalternative.app.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.qualityalternative.app.data.local.QualityAlternativeDatabase
import com.qualityalternative.app.domain.model.AnalyticsEvent
import com.qualityalternative.app.domain.model.AnalyticsEventType
import com.qualityalternative.app.domain.model.ReadingAnnotationDraft
import com.qualityalternative.app.domain.service.AnalyticsTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomReadingAnnotationRepositoryTest {
    @Test
    fun saveUpdateDelete_roundTripsAnchoredAnnotationAndAnalytics() {
        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val database = Room.inMemoryDatabaseBuilder(
                context,
                QualityAlternativeDatabase::class.java,
            ).allowMainThreadQueries().build()
            val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            val analyticsTracker = RecordingAnalyticsTracker()

            try {
                val repository = RoomReadingAnnotationRepository(
                    dao = database.readingAnnotationDao(),
                    analyticsTracker = analyticsTracker,
                    scope = appScope,
                    idProvider = { "annotation-1" },
                )
                repository.observeReady().first { it }

                val created = repository.saveAnnotation(
                    draft = ReadingAnnotationDraft(
                        contentId = "content-1",
                        paragraphIndex = 3,
                        quotedText = "A sentence worth keeping.",
                        noteText = "Connect this to the impulse loop.",
                    ),
                    nowMillis = 1_000L,
                )

                assertEquals("annotation-1", created.id)
                assertEquals("content-1", created.contentId)
                assertEquals(3, created.paragraphIndex)
                assertEquals("A sentence worth keeping.", created.quotedText)
                assertEquals("Connect this to the impulse loop.", created.noteText)
                assertEquals(1_000L, created.createdAtMillis)
                assertEquals(1_000L, created.updatedAtMillis)

                withTimeout(10_000L) {
                    repository.observeReadingAnnotations().first { it.singleOrNull()?.id == "annotation-1" }
                }

                val reloadedScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
                val reloadedRepository = RoomReadingAnnotationRepository(
                    dao = database.readingAnnotationDao(),
                    analyticsTracker = analyticsTracker,
                    scope = reloadedScope,
                    idProvider = { "unused" },
                )
                try {
                    val reloaded = withTimeout(10_000L) {
                        reloadedRepository.observeReadingAnnotations().first { it.size == 1 }.single()
                    }
                    assertEquals("content-1", reloaded.contentId)
                    assertEquals(3, reloaded.paragraphIndex)
                    assertEquals("A sentence worth keeping.", reloaded.quotedText)
                } finally {
                    reloadedScope.cancel()
                }

                val updated = repository.saveAnnotation(
                    draft = ReadingAnnotationDraft(
                        id = created.id,
                        contentId = created.contentId,
                        paragraphIndex = created.paragraphIndex,
                        quotedText = created.quotedText,
                        noteText = "Updated note.",
                    ),
                    nowMillis = 2_000L,
                )
                assertEquals(1_000L, updated.createdAtMillis)
                assertEquals(2_000L, updated.updatedAtMillis)
                assertEquals("Updated note.", updated.noteText)

                repository.deleteAnnotation(annotationId = created.id, nowMillis = 3_000L)
                withTimeout(10_000L) {
                    repository.observeReadingAnnotations().first { it.isEmpty() }
                }

                val events = analyticsTracker.allEvents()
                assertEquals(
                    listOf(
                        AnalyticsEventType.READING_ANNOTATION_CREATED,
                        AnalyticsEventType.READING_ANNOTATION_UPDATED,
                        AnalyticsEventType.READING_ANNOTATION_DELETED,
                    ),
                    events.map(AnalyticsEvent::type),
                )
                assertTrue(events.all { event -> event.contentId == "content-1" })
                assertTrue(events.all { event -> event.metadata["annotationId"] == "annotation-1" })
                assertEquals("3", events.first().metadata["paragraphIndex"])
            } finally {
                appScope.cancel()
                delay(100)
                database.close()
            }
        }
    }

    @Test
    fun saveAnnotationWithoutId_updatesExistingFragmentInsteadOfDuplicating() {
        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val database = Room.inMemoryDatabaseBuilder(
                context,
                QualityAlternativeDatabase::class.java,
            ).allowMainThreadQueries().build()
            val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            val analyticsTracker = RecordingAnalyticsTracker()
            var nextId = 1

            try {
                val repository = RoomReadingAnnotationRepository(
                    dao = database.readingAnnotationDao(),
                    analyticsTracker = analyticsTracker,
                    scope = appScope,
                    idProvider = { "annotation-${nextId++}" },
                )
                repository.observeReady().first { it }

                val created = repository.saveAnnotation(
                    draft = ReadingAnnotationDraft(
                        contentId = "content-1",
                        paragraphIndex = 4,
                        quotedText = "Same fragment.",
                        noteText = "First note.",
                    ),
                    nowMillis = 1_000L,
                )
                val updated = repository.saveAnnotation(
                    draft = ReadingAnnotationDraft(
                        contentId = "content-1",
                        paragraphIndex = 4,
                        quotedText = "Same fragment.",
                        noteText = "Updated note.",
                    ),
                    nowMillis = 2_000L,
                )

                assertEquals(created.id, updated.id)
                assertEquals(1_000L, updated.createdAtMillis)
                assertEquals("Updated note.", updated.noteText)
                withTimeout(10_000L) {
                    repository.observeReadingAnnotations().first { annotations ->
                        annotations.size == 1 && annotations.single().noteText == "Updated note."
                    }
                }
                assertEquals(
                    listOf(
                        AnalyticsEventType.READING_ANNOTATION_CREATED,
                        AnalyticsEventType.READING_ANNOTATION_UPDATED,
                    ),
                    analyticsTracker.allEvents().map(AnalyticsEvent::type),
                )
            } finally {
                appScope.cancel()
                delay(100)
                database.close()
            }
        }
    }

    @Test
    fun deleteAnnotationsForContentIds_deletesOnlyMatchingAnnotationsAndRecordsDeleteEvents() {
        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val database = Room.inMemoryDatabaseBuilder(
                context,
                QualityAlternativeDatabase::class.java,
            ).allowMainThreadQueries().build()
            val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            val analyticsTracker = RecordingAnalyticsTracker()
            var nextId = 1

            try {
                val repository = RoomReadingAnnotationRepository(
                    dao = database.readingAnnotationDao(),
                    analyticsTracker = analyticsTracker,
                    scope = appScope,
                    idProvider = { "annotation-${nextId++}" },
                )
                repository.observeReady().first { it }

                val first = repository.saveAnnotation(
                    draft = ReadingAnnotationDraft(
                        contentId = "content-a",
                        paragraphIndex = 1,
                        quotedText = "First quote.",
                        noteText = "First note.",
                    ),
                    nowMillis = 1_000L,
                )
                val second = repository.saveAnnotation(
                    draft = ReadingAnnotationDraft(
                        contentId = "content-b",
                        paragraphIndex = 2,
                        quotedText = "Second quote.",
                        noteText = "Second note.",
                    ),
                    nowMillis = 2_000L,
                )
                withTimeout(10_000L) {
                    repository.observeReadingAnnotations().first { it.size == 2 }
                }

                repository.deleteAnnotationsForContentIds(setOf("content-a"), nowMillis = 3_000L)

                val remaining = withTimeout(10_000L) {
                    repository.observeReadingAnnotations().first { annotations ->
                        annotations.size == 1 && annotations.single().id == second.id
                    }
                }
                assertEquals(second.id, remaining.single().id)
                assertEquals("content-b", remaining.single().contentId)

                val deleteEvents = analyticsTracker.allEvents()
                    .filter { event -> event.type == AnalyticsEventType.READING_ANNOTATION_DELETED }
                assertEquals(1, deleteEvents.size)
                assertEquals(first.id, deleteEvents.single().metadata["annotationId"])
                assertEquals("content-a", deleteEvents.single().contentId)
            } finally {
                appScope.cancel()
                delay(100)
                database.close()
            }
        }
    }

    private class RecordingAnalyticsTracker : AnalyticsTracker {
        private val events = MutableStateFlow<List<AnalyticsEvent>>(emptyList())

        override fun record(event: AnalyticsEvent) {
            events.value = events.value + event
        }

        override suspend fun recordDurably(event: AnalyticsEvent) {
            record(event)
        }

        override fun allEvents(): List<AnalyticsEvent> = events.value

        override fun observeEvents(): Flow<List<AnalyticsEvent>> = events.asStateFlow()
    }
}
