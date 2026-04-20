package com.qualityalternative.app.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.qualityalternative.app.data.local.QualityAlternativeDatabase
import com.qualityalternative.app.domain.model.ContentAvailability
import com.qualityalternative.app.domain.model.ContentSourceType
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.model.UserLinkDraft
import com.qualityalternative.app.domain.model.UserLinkValidationError
import com.qualityalternative.app.domain.service.AddUserLinkResult
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
class RoomUserLinkRepositoryTest {
    @Test
    fun addLink_roundTripsAsRankableUserContent() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(
            context,
            QualityAlternativeDatabase::class.java,
        ).allowMainThreadQueries().build()
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        try {
            val repository = RoomUserLinkRepository(
                dao = database.userLinkDao(),
                scope = appScope,
                idProvider = { "user-link:test" },
            )
            repository.observeReady().first { it }

            val result = repository.addLink(
                draft = UserLinkDraft(
                    url = "https://Example.com/essay",
                    title = "Saved essay",
                    description = "",
                    durationMinutes = 8,
                    topicTags = setOf(TopicTag.PSYCHOLOGY, TopicTag.SCIENCE),
                ),
                nowMillis = 1_000L,
            )

            assertTrue(result is AddUserLinkResult.Added)
            val saved = withTimeout(10_000L) {
                repository.observeUserLinks().first { it.size == 1 }.single()
            }

            assertEquals("user-link:test", saved.id)
            assertEquals("Saved essay", saved.title)
            assertEquals("https://example.com/essay", saved.externalUrl)
            assertEquals(ContentSourceType.USER_LINK, saved.sourceType)
            assertEquals(ContentAvailability.NEEDS_FALLBACK, saved.availability)
            assertEquals(setOf(TopicTag.PSYCHOLOGY, TopicTag.SCIENCE), saved.topicTags)
        } finally {
            appScope.cancel()
            delay(100)
            database.close()
        }
    }

    @Test
    fun addLink_rejectsInvalidDraftWithoutPersisting() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(
            context,
            QualityAlternativeDatabase::class.java,
        ).allowMainThreadQueries().build()
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        try {
            val repository = RoomUserLinkRepository(
                dao = database.userLinkDao(),
                scope = appScope,
                idProvider = { "user-link:test" },
            )
            repository.observeReady().first { it }

            val result = repository.addLink(
                draft = UserLinkDraft(
                    url = "quality://not-web",
                    title = "",
                    durationMinutes = 8,
                    topicTags = emptySet(),
                ),
                nowMillis = 1_000L,
            )

            assertTrue(result is AddUserLinkResult.Rejected)
            assertEquals(
                setOf(
                    UserLinkValidationError.UNSUPPORTED_SCHEME,
                    UserLinkValidationError.BLANK_TITLE,
                    UserLinkValidationError.NO_TOPICS,
                ),
                (result as AddUserLinkResult.Rejected).errors,
            )
            assertEquals(emptyList<Any>(), repository.userLinks())
        } finally {
            appScope.cancel()
            delay(100)
            database.close()
        }
    }

    @Test
    fun markUnavailable_updatesVisibleInventoryState() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(
            context,
            QualityAlternativeDatabase::class.java,
        ).allowMainThreadQueries().build()
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        try {
            val repository = RoomUserLinkRepository(
                dao = database.userLinkDao(),
                scope = appScope,
                idProvider = { "user-link:test" },
            )
            repository.observeReady().first { it }
            repository.addLink(
                draft = UserLinkDraft(
                    url = "https://example.com/essay",
                    title = "Saved essay",
                    durationMinutes = 8,
                    topicTags = setOf(TopicTag.PSYCHOLOGY),
                ),
                nowMillis = 1_000L,
            )

            repository.markUnavailable(contentId = "user-link:test", nowMillis = 2_000L)

            val updated = withTimeout(10_000L) {
                repository.observeUserLinks().first {
                    it.singleOrNull()?.availability == ContentAvailability.UNAVAILABLE
                }.single()
            }
            assertEquals(ContentAvailability.UNAVAILABLE, updated.availability)
        } finally {
            appScope.cancel()
            delay(100)
            database.close()
        }
    }
}
