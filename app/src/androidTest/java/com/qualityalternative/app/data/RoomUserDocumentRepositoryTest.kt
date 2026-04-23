package com.qualityalternative.app.data

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.qualityalternative.app.data.local.QualityAlternativeDatabase
import com.qualityalternative.app.domain.model.ContentAvailability
import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.ContentRenderMode
import com.qualityalternative.app.domain.model.ContentRightsClass
import com.qualityalternative.app.domain.model.ContentSourceType
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.model.UserDocumentDraft
import com.qualityalternative.app.domain.service.AddUserDocumentResult
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
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomUserDocumentRepositoryTest {
    @Test
    fun addMarkdownDocument_roundTripsAsPrivateReaderContent() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(
            context,
            QualityAlternativeDatabase::class.java,
        ).allowMainThreadQueries().build()
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        try {
            val repository = RoomUserDocumentRepository(
                dao = database.userDocumentDao(),
                scope = appScope,
                bodyLoader = UserDocumentBodyLoader { _, _ -> "Private **Markdown** body." },
                idProvider = { _ -> "user-document:test" },
            )
            repository.observeReady().first { it }

            val result = repository.addDocument(
                draft = UserDocumentDraft(
                    uri = "content://quality/notes",
                    displayName = "notes.md",
                    mimeType = "text/markdown",
                    title = "Saved notes",
                    durationMinutes = 8,
                    topicTags = setOf(TopicTag.PSYCHOLOGY, TopicTag.SCIENCE),
                ),
                nowMillis = 1_000L,
            )

            assertTrue(result is AddUserDocumentResult.Added)
            val saved = withTimeout(10_000L) {
                repository.observeUserDocuments().first { it.size == 1 }.single()
            }

            assertEquals("user-document:test", saved.id)
            assertEquals("Saved notes", saved.title)
            assertEquals(ContentFormat.MARKDOWN, saved.format)
            assertEquals(null, saved.externalUrl)
            assertEquals(ContentSourceType.USER_DOCUMENT, saved.sourceType)
            assertEquals(ContentAvailability.AVAILABLE, saved.availability)
            assertEquals(ContentRightsClass.USER_PRIVATE, saved.rights.rightsClass)
            assertEquals(ContentRenderMode.USER_PRIVATE_READER, saved.rights.renderMode)
            assertEquals("content://quality/notes", saved.rights.sourceUrl)
            assertEquals("Private **Markdown** body.", repository.contentBody(saved))

            val reloadedScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            val reloadedRepository = RoomUserDocumentRepository(
                dao = database.userDocumentDao(),
                scope = reloadedScope,
                bodyLoader = UserDocumentBodyLoader { _, _ -> "Reloaded body" },
                idProvider = { _ -> "user-document:unused" },
            )
            try {
                val reloaded = withTimeout(10_000L) {
                    reloadedRepository.observeUserDocuments().first { it.size == 1 }.single()
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
    fun addPdfDocument_roundTripsAsExternalPrivateHandoffContent() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(
            context,
            QualityAlternativeDatabase::class.java,
        ).allowMainThreadQueries().build()
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        try {
            val repository = RoomUserDocumentRepository(
                dao = database.userDocumentDao(),
                scope = appScope,
                bodyLoader = UserDocumentBodyLoader { _, _ -> error("PDF should not be loaded into reader") },
                idProvider = { _ -> "user-document:pdf" },
            )
            repository.observeReady().first { it }

            repository.addDocument(
                draft = UserDocumentDraft(
                    uri = "content://quality/book",
                    displayName = "book.pdf",
                    mimeType = "application/pdf",
                    title = "Saved PDF",
                    durationMinutes = 15,
                    topicTags = setOf(TopicTag.HISTORY),
                ),
                nowMillis = 1_000L,
            )

            val saved = withTimeout(10_000L) {
                repository.observeUserDocuments().first { it.size == 1 }.single()
            }

            assertEquals(ContentFormat.PDF, saved.format)
            assertEquals("content://quality/book", saved.externalUrl)
            assertEquals(ContentAvailability.NEEDS_FALLBACK, saved.availability)
            assertEquals(ContentRenderMode.EXTERNAL_HANDOFF, saved.rights.renderMode)
        } finally {
            appScope.cancel()
            delay(100)
            database.close()
        }
    }

    @Test
    fun addEpubDocument_roundTripsAsPrivateReaderContent() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(
            context,
            QualityAlternativeDatabase::class.java,
        ).allowMainThreadQueries().build()
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        try {
            val repository = RoomUserDocumentRepository(
                dao = database.userDocumentDao(),
                scope = appScope,
                bodyLoader = UserDocumentBodyLoader { _, format ->
                    assertEquals(ContentFormat.EPUB, format)
                    "Extracted EPUB text."
                },
                idProvider = { _ -> "user-document:epub" },
            )
            repository.observeReady().first { it }

            repository.addDocument(
                draft = UserDocumentDraft(
                    uri = "content://quality/book",
                    displayName = "book.epub",
                    mimeType = "application/epub+zip",
                    title = "Saved EPUB",
                    durationMinutes = 20,
                    topicTags = setOf(TopicTag.HISTORY),
                ),
                nowMillis = 1_000L,
            )

            val saved = withTimeout(10_000L) {
                repository.observeUserDocuments().first { it.size == 1 }.single()
            }

            assertEquals(ContentFormat.EPUB, saved.format)
            assertEquals(null, saved.externalUrl)
            assertEquals(ContentAvailability.AVAILABLE, saved.availability)
            assertEquals(ContentRenderMode.USER_PRIVATE_READER, saved.rights.renderMode)
            assertEquals("Extracted EPUB text.", repository.contentBody(saved))
        } finally {
            appScope.cancel()
            delay(100)
            database.close()
        }
    }

    @Test
    fun androidBodyLoaderReportsUnreadableMarkdownUri() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val loader = AndroidUserDocumentBodyLoader(context)

        try {
            loader.loadBody(
                uri = "content://com.qualityalternative.missing.provider/notes.md",
                format = ContentFormat.MARKDOWN,
            )
            fail("Expected unreadable Markdown URI to throw")
        } catch (error: UserDocumentBodyLoadException) {
            assertTrue(error.message.orEmpty().contains("content://com.qualityalternative.missing.provider/notes.md"))
        }
    }

    @Test
    fun migrationFromVersion5CreatesUserDocumentsTable() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseName = "user-document-migration-${System.nanoTime()}.db"
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
                database.execSQL("CREATE UNIQUE INDEX index_analytics_events_semanticKey ON analytics_events(semanticKey)")
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
                        feedbackFitRating TEXT,
                        feedbackScrollRating TEXT,
                        PRIMARY KEY(sessionId)
                    )
                    """.trimIndent(),
                )
                database.execSQL(
                    """
                    CREATE TABLE user_links (
                        id TEXT NOT NULL,
                        normalizedUrl TEXT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        durationMinutes INTEGER NOT NULL,
                        topicTagsCsv TEXT NOT NULL,
                        availability TEXT NOT NULL,
                        createdAtMillis INTEGER NOT NULL,
                        updatedAtMillis INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent(),
                )
                database.execSQL("CREATE UNIQUE INDEX index_user_links_normalizedUrl ON user_links(normalizedUrl)")
                database.execSQL("PRAGMA user_version = 5")
            }

            val migrated = QualityAlternativeDatabase.build(context, databaseName = databaseName)
            try {
                migrated.openHelper.writableDatabase.query("SELECT COUNT(*) FROM user_documents").use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    assertEquals(0, cursor.getInt(0))
                }
                migrated.openHelper.writableDatabase.query("PRAGMA index_list('user_documents')").use { cursor ->
                    var foundUniqueUriIndex = false
                    while (cursor.moveToNext()) {
                        val indexName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                        val isUnique = cursor.getInt(cursor.getColumnIndexOrThrow("unique")) == 1
                        if (indexName == "index_user_documents_uri" && isUnique) {
                            foundUniqueUriIndex = true
                        }
                    }
                    assertTrue(foundUniqueUriIndex)
                }
            } finally {
                migrated.close()
            }
        } finally {
            context.deleteDatabase(databaseName)
        }
    }
}
