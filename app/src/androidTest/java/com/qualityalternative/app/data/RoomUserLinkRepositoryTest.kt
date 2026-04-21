package com.qualityalternative.app.data

import android.database.sqlite.SQLiteDatabase
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
                idProvider = { _ -> "user-link:test" },
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

            val reloadedScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            val reloadedRepository = RoomUserLinkRepository(
                dao = database.userLinkDao(),
                scope = reloadedScope,
                idProvider = { _ -> "user-link:unused" },
            )
            try {
                val reloaded = withTimeout(10_000L) {
                    reloadedRepository.observeUserLinks().first { it.size == 1 }.single()
                }
                assertEquals(saved, reloaded)
            } finally {
                reloadedScope.cancel()
            }
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
                idProvider = { _ -> "user-link:test" },
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
                idProvider = { _ -> "user-link:test" },
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

            val reloadedScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            val reloadedRepository = RoomUserLinkRepository(
                dao = database.userLinkDao(),
                scope = reloadedScope,
                idProvider = { _ -> "user-link:unused" },
            )
            try {
                val reloaded = withTimeout(10_000L) {
                    reloadedRepository.observeUserLinks().first {
                        it.singleOrNull()?.availability == ContentAvailability.UNAVAILABLE
                    }.single()
                }
                assertEquals(ContentAvailability.UNAVAILABLE, reloaded.availability)
            } finally {
                reloadedScope.cancel()
            }
        } finally {
            appScope.cancel()
            delay(100)
            database.close()
        }
    }

    @Test
    fun addLink_preservesContentIdentityWhenSameUrlIsAddedAgain() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(
            context,
            QualityAlternativeDatabase::class.java,
        ).allowMainThreadQueries().build()
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        var nextId = 0

        try {
            val repository = RoomUserLinkRepository(
                dao = database.userLinkDao(),
                scope = appScope,
                idProvider = { _ -> "user-link:${++nextId}" },
            )
            repository.observeReady().first { it }

            val first = repository.addLink(
                draft = UserLinkDraft(
                    url = "https://example.com/essay",
                    title = "Original title",
                    durationMinutes = 8,
                    topicTags = setOf(TopicTag.PSYCHOLOGY),
                ),
                nowMillis = 1_000L,
            ) as AddUserLinkResult.Added
            val second = repository.addLink(
                draft = UserLinkDraft(
                    url = "https://EXAMPLE.com/essay",
                    title = "Updated title",
                    durationMinutes = 10,
                    topicTags = setOf(TopicTag.SCIENCE),
                ),
                nowMillis = 2_000L,
            ) as AddUserLinkResult.Added

            val links = withTimeout(10_000L) {
                repository.observeUserLinks().first { it.size == 1 }
            }

            assertEquals(first.item.id, second.item.id)
            assertEquals("user-link:1", second.item.id)
            assertEquals("Updated title", links.single().title)
            assertEquals(1, nextId)
        } finally {
            appScope.cancel()
            delay(100)
            database.close()
        }
    }

    @Test
    fun migrationFromVersion3PreservesExistingRowsAndCreatesUserLinks() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseName = "user-link-migration-${System.nanoTime()}.db"
        context.deleteDatabase(databaseName)

        try {
            SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(databaseName), null).use { database ->
                database.execSQL(
                    """
                    CREATE TABLE analytics_events (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        type TEXT NOT NULL,
                        timestampMillis INTEGER NOT NULL,
                        semanticKey TEXT,
                        interventionId TEXT,
                        sessionId TEXT,
                        targetAppPackage TEXT,
                        primaryContentId TEXT,
                        backupContentIdsCsv TEXT NOT NULL,
                        contentId TEXT,
                        metadataJson TEXT NOT NULL
                    )
                    """.trimIndent(),
                )
                database.execSQL(
                    "CREATE UNIQUE INDEX index_analytics_events_semanticKey ON analytics_events(semanticKey)",
                )
                database.execSQL(
                    """
                    CREATE TABLE replacement_sessions (
                        sessionId TEXT NOT NULL,
                        interventionId TEXT NOT NULL,
                        targetAppPackage TEXT NOT NULL,
                        targetAppDisplayName TEXT NOT NULL,
                        interventionShownAtMillis INTEGER NOT NULL,
                        primaryContentId TEXT NOT NULL,
                        backupContentIdsCsv TEXT NOT NULL,
                        contentId TEXT NOT NULL,
                        contentTitle TEXT NOT NULL,
                        contentDescription TEXT NOT NULL,
                        contentTopicsCsv TEXT NOT NULL,
                        packId TEXT NOT NULL,
                        recommendationSource TEXT NOT NULL,
                        acceptedAtMillis INTEGER NOT NULL,
                        completedAtMillis INTEGER,
                        skippedAtMillis INTEGER,
                        returnedToTargetAtMillis INTEGER,
                        feedbackGoodFit INTEGER,
                        feedbackHelpedAvoidScrolling INTEGER,
                        PRIMARY KEY(sessionId)
                    )
                    """.trimIndent(),
                )
                database.execSQL(
                    """
                    INSERT INTO analytics_events (
                        type,
                        timestampMillis,
                        semanticKey,
                        interventionId,
                        sessionId,
                        targetAppPackage,
                        primaryContentId,
                        backupContentIdsCsv,
                        contentId,
                        metadataJson
                    ) VALUES (
                        'INTERVENTION_SHOWN',
                        1000,
                        'event:1',
                        'intervention-1',
                        'session-1',
                        'pkg',
                        'content-1',
                        'backup-1',
                        'content-1',
                        '{}'
                    )
                    """.trimIndent(),
                )
                database.execSQL(
                    """
                    INSERT INTO replacement_sessions (
                        sessionId,
                        interventionId,
                        targetAppPackage,
                        targetAppDisplayName,
                        interventionShownAtMillis,
                        primaryContentId,
                        backupContentIdsCsv,
                        contentId,
                        contentTitle,
                        contentDescription,
                        contentTopicsCsv,
                        packId,
                        recommendationSource,
                        acceptedAtMillis,
                        completedAtMillis,
                        skippedAtMillis,
                        returnedToTargetAtMillis,
                        feedbackGoodFit,
                        feedbackHelpedAvoidScrolling
                    ) VALUES (
                        'session-1',
                        'intervention-1',
                        'pkg',
                        'Target',
                        1000,
                        'content-1',
                        'backup-1',
                        'content-1',
                        'Title',
                        'Description',
                        'SCIENCE',
                        'pack',
                        'PRIMARY',
                        1100,
                        NULL,
                        NULL,
                        NULL,
                        NULL,
                        NULL
                    )
                    """.trimIndent(),
                )
                database.execSQL("PRAGMA user_version = 3")
            }

            val migrated = QualityAlternativeDatabase.build(context, databaseName = databaseName)
            try {
                migrated.openHelper.writableDatabase.query("SELECT COUNT(*) FROM analytics_events").use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    assertEquals(1, cursor.getInt(0))
                }
                migrated.openHelper.writableDatabase.query("SELECT COUNT(*) FROM replacement_sessions").use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    assertEquals(1, cursor.getInt(0))
                }
                migrated.openHelper.writableDatabase.query("SELECT COUNT(*) FROM user_links").use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    assertEquals(0, cursor.getInt(0))
                }
                migrated.openHelper.writableDatabase.query(
                    """
                    SELECT
                        type,
                        timestampMillis,
                        semanticKey,
                        interventionId,
                        sessionId,
                        targetAppPackage,
                        primaryContentId,
                        backupContentIdsCsv,
                        contentId,
                        metadataJson
                    FROM analytics_events
                    """.trimIndent(),
                ).use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    assertEquals("INTERVENTION_SHOWN", cursor.getString(0))
                    assertEquals(1000L, cursor.getLong(1))
                    assertEquals("event:1", cursor.getString(2))
                    assertEquals("intervention-1", cursor.getString(3))
                    assertEquals("session-1", cursor.getString(4))
                    assertEquals("pkg", cursor.getString(5))
                    assertEquals("content-1", cursor.getString(6))
                    assertEquals("backup-1", cursor.getString(7))
                    assertEquals("content-1", cursor.getString(8))
                    assertEquals("{}", cursor.getString(9))
                }
                migrated.openHelper.writableDatabase.query(
                    """
                    SELECT
                        sessionId,
                        interventionId,
                        targetAppPackage,
                        targetAppDisplayName,
                        interventionShownAtMillis,
                        primaryContentId,
                        backupContentIdsCsv,
                        contentId,
                        contentTitle,
                        contentDescription,
                        contentTopicsCsv,
                        packId,
                        recommendationSource,
                        acceptedAtMillis,
                        completedAtMillis,
                        skippedAtMillis,
                        returnedToTargetAtMillis,
                        feedbackGoodFit,
                        feedbackHelpedAvoidScrolling,
                        feedbackFitRating,
                        feedbackScrollRating
                    FROM replacement_sessions
                    """.trimIndent(),
                ).use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    assertEquals("session-1", cursor.getString(0))
                    assertEquals("intervention-1", cursor.getString(1))
                    assertEquals("pkg", cursor.getString(2))
                    assertEquals("Target", cursor.getString(3))
                    assertEquals(1000L, cursor.getLong(4))
                    assertEquals("content-1", cursor.getString(5))
                    assertEquals("backup-1", cursor.getString(6))
                    assertEquals("content-1", cursor.getString(7))
                    assertEquals("Title", cursor.getString(8))
                    assertEquals("Description", cursor.getString(9))
                    assertEquals("SCIENCE", cursor.getString(10))
                    assertEquals("pack", cursor.getString(11))
                    assertEquals("PRIMARY", cursor.getString(12))
                    assertEquals(1100L, cursor.getLong(13))
                    assertTrue(cursor.isNull(14))
                    assertTrue(cursor.isNull(15))
                    assertTrue(cursor.isNull(16))
                    assertTrue(cursor.isNull(17))
                    assertTrue(cursor.isNull(18))
                    assertTrue(cursor.isNull(19))
                    assertTrue(cursor.isNull(20))
                }
                migrated.openHelper.writableDatabase.query("PRAGMA index_list('user_links')").use { cursor ->
                    var foundUniqueNormalizedUrlIndex = false
                    while (cursor.moveToNext()) {
                        val indexName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                        val isUnique = cursor.getInt(cursor.getColumnIndexOrThrow("unique")) == 1
                        if (indexName == "index_user_links_normalizedUrl" && isUnique) {
                            foundUniqueNormalizedUrlIndex = true
                        }
                    }
                    assertTrue(foundUniqueNormalizedUrlIndex)
                }
            } finally {
                migrated.close()
            }
        } finally {
            context.deleteDatabase(databaseName)
        }
    }
}
