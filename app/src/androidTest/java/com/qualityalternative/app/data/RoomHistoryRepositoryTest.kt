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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomHistoryRepositoryTest {
    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun acceptThenImmediateMutationsRemainVisible() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(
            context,
            QualityAlternativeDatabase::class.java,
        ).allowMainThreadQueries().build()

        try {
            val repository = RoomHistoryRepository(
                dao = database.replacementSessionDao(),
                scope = backgroundScope,
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
                    submittedAtMillis = 2_100L,
                ),
            )
            repository.markReturnedToTarget(targetAppPackage = targetApp.packageName, returnedAtMillis = 3_000L)
            advanceUntilIdle()

            val historyEntry = repository.recentHistory(nowMillis = 3_000L).single()
            val completedIds = repository.observeCompletedContentIds().first()

            assertTrue(historyEntry.isCompleted())
            assertEquals(3_000L, historyEntry.returnedToTargetAtMillis)
            assertEquals(true, historyEntry.feedbackHelpedAvoidScrolling)
            assertEquals(setOf(content.id), completedIds)
        } finally {
            database.close()
        }
    }
}
