package com.qualityalternative.app.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.qualityalternative.app.data.local.QualityAlternativeDatabase
import com.qualityalternative.app.domain.model.ReadingProgress
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
class RoomReadingProgressRepositoryTest {
    @Test
    fun saveCompleteDelete_roundTripsReadingProgress() {
        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val database = Room.inMemoryDatabaseBuilder(
                context,
                QualityAlternativeDatabase::class.java,
            ).allowMainThreadQueries().build()
            val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

            try {
                val repository = RoomReadingProgressRepository(
                    dao = database.readingProgressDao(),
                    scope = appScope,
                )
                repository.observeReady().first { it }

                repository.saveProgress(
                    ReadingProgress(
                        contentId = "content-1",
                        progressPercent = 44,
                        lastVisibleParagraphIndex = 3,
                        lastVisibleTextOffset = 128,
                        paragraphCount = 10,
                        updatedAtMillis = 1_000L,
                    ),
                )

                val saved = withTimeout(10_000L) {
                    repository.observeReadingProgress().first { it.size == 1 }.single()
                }
                assertEquals("content-1", saved.contentId)
                assertEquals(44, saved.progressPercent)
                assertEquals(128, saved.lastVisibleTextOffset)
                assertTrue(saved.isUnfinished())

                repository.saveProgress(
                    saved.copy(
                        progressPercent = 100,
                        lastVisibleParagraphIndex = 9,
                        updatedAtMillis = 2_000L,
                        completedAtMillis = 2_000L,
                    ),
                )

                val completedIds = withTimeout(10_000L) {
                    repository.observeCompletedContentIds().first { it == setOf("content-1") }
                }
                assertEquals(setOf("content-1"), completedIds)

                val reloadedScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
                val reloadedRepository = RoomReadingProgressRepository(
                    dao = database.readingProgressDao(),
                    scope = reloadedScope,
                )
                try {
                    val reloaded = withTimeout(10_000L) {
                        reloadedRepository.observeReadingProgress().first { it.size == 1 }.single()
                    }
                    assertTrue(reloaded.isCompleted())
                    assertEquals(100, reloaded.progressPercent)
                } finally {
                    reloadedScope.cancel()
                }

                repository.deleteProgress("content-1")
                withTimeout(10_000L) {
                    repository.observeReadingProgress().first { it.isEmpty() }
                }
            } finally {
                appScope.cancel()
                delay(100)
                database.close()
            }
        }
    }
}
