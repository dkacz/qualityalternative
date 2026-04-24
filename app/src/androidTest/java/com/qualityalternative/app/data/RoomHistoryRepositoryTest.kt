package com.qualityalternative.app.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.qualityalternative.app.data.local.QualityAlternativeDatabase
import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.RecommendationSource
import com.qualityalternative.app.domain.model.SessionFeedback
import com.qualityalternative.app.domain.model.TopicTag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomHistoryRepositoryTest {
    @Test
    fun acceptThenImmediateMutationsRemainVisible() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(
            context,
            QualityAlternativeDatabase::class.java,
        ).allowMainThreadQueries().build()
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        try {
            val repository = RoomHistoryRepository(
                dao = database.replacementSessionDao(),
                scope = appScope,
            )
            repository.observeReady().first { it }

            val targetApp = SupportedCatalog.distractingApps.first()
            val content = ContentItem(
                id = "content-1",
                packId = "philosophy",
                title = "Stoic note",
                description = "A short reflective text",
                durationMinutes = 7,
                format = ContentFormat.MARKDOWN,
                topicTags = setOf(TopicTag.PHILOSOPHY),
                bodyAssetPath = "unused",
            )

            val sessionId = repository.recordAcceptedSession(
                targetApp = targetApp,
                interventionId = "intervention-1",
                interventionShownAtMillis = 1_000L,
                primaryContentId = content.id,
                backupContentIds = listOf("backup-1", "backup-2"),
                content = content,
                source = RecommendationSource.PRIMARY,
                acceptedAtMillis = 1_100L,
            )
            repository.markCompleted(sessionId = sessionId, completedAtMillis = 2_000L)
            repository.attachFeedback(
                sessionId = sessionId,
                feedback = SessionFeedback(
                    wasGoodFit = true,
                    helpedAvoidScrolling = true,
                    fitRating = "great",
                    scrollRating = "yes",
                    submittedAtMillis = 2_100L,
                ),
            )
            repository.markReturnedToTarget(targetAppPackage = targetApp.packageName, returnedAtMillis = 3_000L)

            val historyEntry = repository.recentHistory(nowMillis = 3_000L).single()
            val completedIds = withTimeout(10_000L) {
                repository.observeCompletedContentIds().first { it == setOf(content.id) }
            }

            assertTrue(historyEntry.isCompleted())
            assertEquals(7, historyEntry.contentDurationMinutes)
            assertEquals(3_000L, historyEntry.returnedToTargetAtMillis)
            assertEquals(true, historyEntry.feedbackHelpedAvoidScrolling)
            assertEquals("great", historyEntry.feedbackFitRating)
            assertEquals("yes", historyEntry.feedbackScrollRating)
            assertEquals(setOf(content.id), completedIds)
        } finally {
            appScope.cancel()
            delay(100)
            database.close()
        }
    }

    @Test
    fun repeatedMarkReturnedToTarget_replaysPersistedSignalWithoutMutatingTimestamp() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(
            context,
            QualityAlternativeDatabase::class.java,
        ).allowMainThreadQueries().build()
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        try {
            val repository = RoomHistoryRepository(
                dao = database.replacementSessionDao(),
                scope = appScope,
            )
            repository.observeReady().first { it }

            val targetApp = SupportedCatalog.distractingApps.first()
            val content = ContentItem(
                id = "content-1",
                packId = "philosophy",
                title = "Stoic note",
                description = "A short reflective text",
                durationMinutes = 7,
                format = ContentFormat.MARKDOWN,
                topicTags = setOf(TopicTag.PHILOSOPHY),
                bodyAssetPath = "unused",
            )

            repository.recordAcceptedSession(
                targetApp = targetApp,
                interventionId = "intervention-1",
                interventionShownAtMillis = 1_000L,
                primaryContentId = content.id,
                backupContentIds = listOf("backup-1", "backup-2"),
                content = content,
                source = RecommendationSource.PRIMARY,
                acceptedAtMillis = 1_100L,
            )
            val firstSignal = repository.markReturnedToTarget(
                targetAppPackage = targetApp.packageName,
                returnedAtMillis = 3_000L,
            )
            val replayedSignal = repository.markReturnedToTarget(
                targetAppPackage = targetApp.packageName,
                returnedAtMillis = 9_000L,
            )
            val historyEntry = repository.recentHistory(nowMillis = 9_000L).single()

            assertEquals(3_000L, firstSignal?.returnedAtMillis)
            assertEquals(3_000L, replayedSignal?.returnedAtMillis)
            assertEquals(true, replayedSignal?.within15Minutes)
            assertEquals(3_000L, historyEntry.returnedToTargetAtMillis)
        } finally {
            appScope.cancel()
            delay(100)
            database.close()
        }
    }

    @Test
    fun updateAcceptedSessionContent_rewritesStoredContentMetadata() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(
            context,
            QualityAlternativeDatabase::class.java,
        ).allowMainThreadQueries().build()
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        try {
            val repository = RoomHistoryRepository(
                dao = database.replacementSessionDao(),
                scope = appScope,
            )
            repository.observeReady().first { it }

            val targetApp = SupportedCatalog.distractingApps.first()
            val content = ContentItem(
                id = "meditation-reset",
                packId = "utility",
                title = "3-minute reset",
                description = "A short reset",
                durationMinutes = 3,
                format = ContentFormat.MARKDOWN,
                topicTags = setOf(TopicTag.PSYCHOLOGY),
            )
            val updatedContent = content.copy(
                title = "5-minute reset",
                description = "A longer reset",
                durationMinutes = 5,
            )

            val sessionId = repository.recordAcceptedSession(
                targetApp = targetApp,
                interventionId = "intervention-1",
                interventionShownAtMillis = 1_000L,
                primaryContentId = content.id,
                backupContentIds = emptyList(),
                content = content,
                source = RecommendationSource.PRIMARY,
                acceptedAtMillis = 1_100L,
            )
            repository.updateAcceptedSessionContent(sessionId = sessionId, content = updatedContent)

            val historyEntry = repository.recentHistory(nowMillis = 2_000L).single()

            assertEquals("meditation-reset", historyEntry.contentId)
            assertEquals("5-minute reset", historyEntry.contentTitle)
            assertEquals("A longer reset", historyEntry.contentDescription)
            assertEquals(5, historyEntry.contentDurationMinutes)
            assertEquals(setOf(TopicTag.PSYCHOLOGY), historyEntry.contentTopics)
        } finally {
            appScope.cancel()
            delay(100)
            database.close()
        }
    }
}
