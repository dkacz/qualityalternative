package com.qualityalternative.app.data

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.qualityalternative.app.data.local.QualityAlternativeDatabase
import com.qualityalternative.app.domain.model.ContentAvailability
import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.ContentRenderMode
import com.qualityalternative.app.domain.model.ContentRightsMetadata
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
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
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
                idProvider = { "user-document:test" },
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
                idProvider = { "user-document:unused" },
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
    fun importPortableDocuments_mergeSkipsExistingAvailableDocumentCollision() = runBlocking {
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
                bodyLoader = UserDocumentBodyLoader { _, _ -> "Private body" },
                idProvider = { "user-document-44444444-4444-4444-8444-444444444444" },
            )
            repository.observeReady().first { it }
            repository.addDocument(
                draft = UserDocumentDraft(
                    uri = "content://quality/local-book",
                    displayName = "local-book.epub",
                    mimeType = "application/epub+zip",
                    title = "Local book",
                    durationMinutes = 25,
                    topicTags = setOf(TopicTag.PHILOSOPHY),
                ),
                nowMillis = 1_000L,
            )
            val local = withTimeout(10_000L) {
                repository.observeUserDocuments().first { it.size == 1 }.single()
            }
            val importedMissingCollision = ContentItem(
                id = local.id,
                packId = "user-documents",
                title = "Imported missing book",
                description = "Reattach to continue reading.",
                durationMinutes = 25,
                format = ContentFormat.EPUB,
                topicTags = setOf(TopicTag.PHILOSOPHY),
                bodyAssetPath = null,
                externalUrl = null,
                sourceLabel = "book.epub (missing)",
                sourceType = ContentSourceType.USER_DOCUMENT,
                availability = ContentAvailability.UNAVAILABLE,
                rights = ContentRightsMetadata.userPrivateReader(
                    sourceUrl = "portable-missing:${local.id}",
                    attribution = "book.epub",
                ),
                addedAtMillis = 2_000L,
            )

            repository.importPortableDocuments(
                documents = listOf(importedMissingCollision),
                replaceExisting = false,
                nowMillis = 3_000L,
            )

            val afterMerge = withTimeout(10_000L) {
                repository.observeUserDocuments().first { it.size == 1 }.single()
            }
            assertEquals(local.id, afterMerge.id)
            assertEquals("Local book", afterMerge.title)
            assertEquals(ContentAvailability.AVAILABLE, afterMerge.availability)
            assertEquals("content://quality/local-book", afterMerge.rights.sourceUrl)
        } finally {
            appScope.cancel()
            delay(100)
            database.close()
        }
    }

    @Test
    fun importPortableDocuments_mergeMapsVerifiedFingerprintCollisionToLocalDocument() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(
            context,
            QualityAlternativeDatabase::class.java,
        ).allowMainThreadQueries().build()
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val sharedFingerprint = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"

        try {
            val repository = RoomUserDocumentRepository(
                dao = database.userDocumentDao(),
                scope = appScope,
                bodyLoader = FixedFingerprintBodyLoader(
                    fingerprints = mapOf("content://quality/local-book" to sharedFingerprint),
                ),
                idProvider = { "user-document-11111111-1111-4111-8111-111111111111" },
            )
            repository.observeReady().first { it }
            repository.addDocument(
                draft = UserDocumentDraft(
                    uri = "content://quality/local-book",
                    displayName = "local-book.epub",
                    mimeType = "application/epub+zip",
                    title = "Local book",
                    durationMinutes = 25,
                    topicTags = setOf(TopicTag.PHILOSOPHY),
                ),
                nowMillis = 1_000L,
            )
            val local = withTimeout(10_000L) {
                repository.observeUserDocuments().first { it.size == 1 }.single()
            }
            val importedMissingCollision = missingImportedDocument(
                id = "user-document-33333333-3333-4333-8333-333333333333",
                fingerprintSha256 = sharedFingerprint,
            )

            val acceptedIds = repository.importPortableDocuments(
                documents = listOf(importedMissingCollision),
                replaceExisting = false,
                nowMillis = 3_000L,
            )

            val afterMerge = withTimeout(10_000L) {
                repository.observeUserDocuments().first { it.size == 1 }.single()
            }
            assertEquals(setOf(local.id), acceptedIds)
            assertEquals(local.id, afterMerge.id)
            assertEquals("Local book", afterMerge.title)
            assertEquals(ContentAvailability.AVAILABLE, afterMerge.availability)
        } finally {
            appScope.cancel()
            delay(100)
            database.close()
        }
    }

    @Test
    fun importPortableDocuments_throwsBeforeMutationWhenIdAndFingerprintMatchDifferentRows() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(
            context,
            QualityAlternativeDatabase::class.java,
        ).allowMainThreadQueries().build()
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val firstFingerprint = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        val secondFingerprint = "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
        val ids = ArrayDeque(
            listOf(
                "user-document-11111111-1111-4111-8111-111111111111",
                "user-document-22222222-2222-4222-8222-222222222222",
            ),
        )

        try {
            val repository = RoomUserDocumentRepository(
                dao = database.userDocumentDao(),
                scope = appScope,
                bodyLoader = FixedFingerprintBodyLoader(
                    fingerprints = mapOf(
                        "content://quality/first" to firstFingerprint,
                        "content://quality/second" to secondFingerprint,
                    ),
                ),
                idProvider = { ids.removeFirst() },
            )
            repository.observeReady().first { it }
            repository.addDocument(
                draft = UserDocumentDraft(
                    uri = "content://quality/first",
                    displayName = "first.epub",
                    mimeType = "application/epub+zip",
                    title = "First local book",
                    durationMinutes = 25,
                    topicTags = setOf(TopicTag.PHILOSOPHY),
                ),
                nowMillis = 1_000L,
            )
            repository.addDocument(
                draft = UserDocumentDraft(
                    uri = "content://quality/second",
                    displayName = "second.epub",
                    mimeType = "application/epub+zip",
                    title = "Second local book",
                    durationMinutes = 25,
                    topicTags = setOf(TopicTag.HISTORY),
                ),
                nowMillis = 2_000L,
            )
            val beforeMerge = withTimeout(10_000L) {
                repository.observeUserDocuments().first { it.size == 2 }
            }

            assertThrows(PortableContentImportConflictException::class.java) {
                runBlocking {
                    repository.importPortableDocuments(
                        documents = listOf(
                            missingImportedDocument(
                                id = "user-document-11111111-1111-4111-8111-111111111111",
                                fingerprintSha256 = secondFingerprint,
                            ),
                        ),
                        replaceExisting = false,
                        nowMillis = 3_000L,
                    )
                }
            }

            val afterMerge = withTimeout(10_000L) {
                repository.observeUserDocuments().first { it.size == 2 }
            }
            assertEquals(beforeMerge.map(ContentItem::id).sorted(), afterMerge.map(ContentItem::id).sorted())
            assertEquals(
                beforeMerge.map(ContentItem::title).sorted(),
                afterMerge.map(ContentItem::title).sorted(),
            )
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
                idProvider = { "user-document:pdf" },
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
    fun deleteDocument_removesPersistedDocumentAndReloadedState() = runBlocking {
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
                bodyLoader = UserDocumentBodyLoader { _, _ -> "Private body" },
                idProvider = { "user-document:test" },
            )
            repository.observeReady().first { it }
            repository.addDocument(
                draft = UserDocumentDraft(
                    uri = "content://quality/notes",
                    displayName = "notes.md",
                    mimeType = "text/markdown",
                    title = "Saved notes",
                    durationMinutes = 8,
                    topicTags = setOf(TopicTag.SCIENCE),
                ),
                nowMillis = 1_000L,
            )
            withTimeout(10_000L) {
                repository.observeUserDocuments().first { it.size == 1 }
            }

            repository.deleteDocument("user-document:test")

            withTimeout(10_000L) {
                repository.observeUserDocuments().first { it.isEmpty() }
            }
            val reloadedScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            val reloadedRepository = RoomUserDocumentRepository(
                dao = database.userDocumentDao(),
                scope = reloadedScope,
                bodyLoader = UserDocumentBodyLoader { _, _ -> "Reloaded body" },
                idProvider = { "user-document:unused" },
            )
            try {
                assertEquals(
                    emptyList<Any>(),
                    withTimeout(10_000L) { reloadedRepository.observeUserDocuments().first() },
                )
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
                idProvider = { "user-document:epub" },
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
    fun addDocument_defaultIdsAreRandomUuidV4PerIndependentRecord() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val idRegex = Regex("^user-document-[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")

        suspend fun createSavedId(): String {
            val database = Room.inMemoryDatabaseBuilder(
                context,
                QualityAlternativeDatabase::class.java,
            ).allowMainThreadQueries().build()
            val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            return try {
                val repository = RoomUserDocumentRepository(
                    dao = database.userDocumentDao(),
                    scope = appScope,
                    bodyLoader = UserDocumentBodyLoader { _, _ -> "Private body" },
                )
                repository.observeReady().first { it }
                val result = repository.addDocument(
                    draft = UserDocumentDraft(
                        uri = "content://quality/same-notes",
                        displayName = "same-notes.md",
                        mimeType = "text/markdown",
                        title = "Same notes",
                        durationMinutes = 8,
                        topicTags = setOf(TopicTag.PSYCHOLOGY),
                    ),
                    nowMillis = 1_000L,
                ) as AddUserDocumentResult.Added
                result.item.id
            } finally {
                appScope.cancel()
                delay(100)
                database.close()
            }
        }

        val firstId = createSavedId()
        val secondId = createSavedId()

        assertTrue(firstId.matches(idRegex))
        assertTrue(secondId.matches(idRegex))
        assertNotEquals(firstId, secondId)
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
                migrated.openHelper.writableDatabase.query("PRAGMA table_info('replacement_sessions')").use { cursor ->
                    var foundDurationColumn = false
                    while (cursor.moveToNext()) {
                        val columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                        val defaultValue = cursor.getString(cursor.getColumnIndexOrThrow("dflt_value"))
                        if (columnName == "contentDurationMinutes" && defaultValue == "10") {
                            foundDurationColumn = true
                        }
                    }
                    assertTrue(foundDurationColumn)
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

    private fun missingImportedDocument(
        id: String,
        fingerprintSha256: String,
    ): ContentItem {
        return ContentItem(
            id = id,
            packId = "user-documents",
            title = "Imported missing book",
            description = "Reattach to continue reading.",
            durationMinutes = 25,
            format = ContentFormat.EPUB,
            topicTags = setOf(TopicTag.PHILOSOPHY),
            bodyAssetPath = null,
            externalUrl = null,
            sourceLabel = "book.epub (missing)",
            sourceType = ContentSourceType.USER_DOCUMENT,
            availability = ContentAvailability.UNAVAILABLE,
            rights = ContentRightsMetadata.userPrivateReader(
                sourceUrl = "portable-missing:$id",
                attribution = "book.epub",
            ),
            addedAtMillis = 2_000L,
            documentFingerprintSha256 = fingerprintSha256,
        )
    }

    private class FixedFingerprintBodyLoader(
        private val fingerprints: Map<String, String>,
    ) : UserDocumentBodyLoader, UserDocumentFingerprintProvider {
        override fun loadBody(uri: String, format: ContentFormat): String = "Private body"

        override fun documentFingerprintSha256(uri: String): String? = fingerprints[uri]
    }
}
