package com.qualityalternative.app.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QualityAlternativeDatabaseMigrationInstrumentedTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        QualityAlternativeDatabase::class.java,
    )

    @Test
    fun migration8To12ValidatesRoomSchemaAndCreatesAnnotationColumnsOnce() {
        val databaseName = "qa-migration-8-12.db"
        deleteDatabase(databaseName)
        helper.createDatabase(databaseName, 8).close()

        val migrated = helper.runMigrationsAndValidate(
            databaseName,
            12,
            true,
            QualityAlternativeDatabase.MIGRATION_8_9,
            QualityAlternativeDatabase.MIGRATION_9_10,
            QualityAlternativeDatabase.MIGRATION_10_11,
            QualityAlternativeDatabase.MIGRATION_11_12,
        )
        try {
            val columns = readingAnnotationColumns(migrated)
            assertEquals(1, columns.count { column -> column == "sourceTitle" })
            assertTrue(columns.containsAll(version10AnnotationColumns))
            assertTrue(userDocumentColumns(migrated).contains("documentFingerprintSha256"))
            assertTrue(userDocumentColumns(migrated).contains("documentFingerprintSizeBytes"))

            migrated.execSQL(
                """
                INSERT INTO reading_annotations (
                    id,
                    contentId,
                    paragraphIndex,
                    quotedText,
                    noteText,
                    createdAtMillis,
                    updatedAtMillis,
                    sourceTitle,
                    sourceBlockIndex,
                    textStartOffset,
                    textEndOffset,
                    prefixText,
                    suffixText
                ) VALUES (
                    'annotation-1',
                    'content-1',
                    2,
                    'Selected quote',
                    'Stored note',
                    1000,
                    1000,
                    'Private EPUB',
                    4,
                    20,
                    34,
                    'Before',
                    'After'
                )
                """.trimIndent(),
            )
            migrated.query("SELECT sourceTitle, sourceBlockIndex, textStartOffset, textEndOffset FROM reading_annotations")
                .use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    assertEquals("Private EPUB", cursor.getString(0))
                    assertEquals(4, cursor.getInt(1))
                    assertEquals(20, cursor.getInt(2))
                    assertEquals(34, cursor.getInt(3))
                }
        } finally {
            migrated.close()
            deleteDatabase(databaseName)
        }
    }

    @Test
    fun migration9To12ValidatesRoomSchemaAndPreservesLegacyAnnotationRows() {
        val databaseName = "qa-migration-9-12.db"
        deleteDatabase(databaseName)
        val legacy = helper.createDatabase(databaseName, 9)
        legacy.execSQL(
            """
            INSERT INTO reading_annotations (
                id,
                contentId,
                paragraphIndex,
                quotedText,
                noteText,
                createdAtMillis,
                updatedAtMillis
            ) VALUES (
                'legacy-annotation',
                'legacy-content',
                1,
                'Legacy quote',
                'Legacy note',
                100,
                200
            )
            """.trimIndent(),
        )
        legacy.close()

        val migrated = helper.runMigrationsAndValidate(
            databaseName,
            12,
            true,
            QualityAlternativeDatabase.MIGRATION_9_10,
            QualityAlternativeDatabase.MIGRATION_10_11,
            QualityAlternativeDatabase.MIGRATION_11_12,
        )
        try {
            assertTrue(readingAnnotationColumns(migrated).containsAll(version10AnnotationColumns))
            assertTrue(userDocumentColumns(migrated).contains("documentFingerprintSha256"))
            assertTrue(userDocumentColumns(migrated).contains("documentFingerprintSizeBytes"))
            migrated.query(
                """
                SELECT
                    quotedText,
                    noteText,
                    sourceTitle,
                    sourceBlockIndex,
                    textStartOffset,
                    textEndOffset,
                    prefixText,
                    suffixText
                FROM reading_annotations
                WHERE id = 'legacy-annotation'
                """.trimIndent(),
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("Legacy quote", cursor.getString(0))
                assertEquals("Legacy note", cursor.getString(1))
                assertEquals("", cursor.getString(2))
                assertEquals(0, cursor.getInt(3))
                assertEquals(0, cursor.getInt(4))
                assertEquals(0, cursor.getInt(5))
                assertEquals("", cursor.getString(6))
                assertEquals("", cursor.getString(7))
            }
        } finally {
            migrated.close()
            deleteDatabase(databaseName)
        }
    }

    @Test
    fun migration12To13ValidatesRoomSchemaAndDefaultsReadingProgressOffset() {
        val databaseName = "qa-migration-12-13.db"
        deleteDatabase(databaseName)
        val legacy = helper.createDatabase(databaseName, 12)
        legacy.execSQL(
            """
            INSERT INTO reading_progress (
                contentId,
                progressPercent,
                lastVisibleParagraphIndex,
                paragraphCount,
                updatedAtMillis,
                completedAtMillis
            ) VALUES (
                'content-1',
                42,
                7,
                20,
                1234,
                NULL
            )
            """.trimIndent(),
        )
        legacy.close()

        val migrated = helper.runMigrationsAndValidate(
            databaseName,
            13,
            true,
            QualityAlternativeDatabase.MIGRATION_12_13,
        )
        try {
            assertTrue(readingProgressColumns(migrated).contains("lastVisibleTextOffset"))
            migrated.query(
                """
                SELECT
                    progressPercent,
                    lastVisibleParagraphIndex,
                    paragraphCount,
                    lastVisibleTextOffset
                FROM reading_progress
                WHERE contentId = 'content-1'
                """.trimIndent(),
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(42, cursor.getInt(0))
                assertEquals(7, cursor.getInt(1))
                assertEquals(20, cursor.getInt(2))
                assertEquals(0, cursor.getInt(3))
            }
        } finally {
            migrated.close()
            deleteDatabase(databaseName)
        }
    }

    @Test
    fun migration13To14ValidatesRoomSchemaAndBackfillsAnnotationEndSourceBlock() {
        val databaseName = "qa-migration-13-14.db"
        deleteDatabase(databaseName)
        val legacy = helper.createDatabase(databaseName, 13)
        legacy.execSQL(
            """
            INSERT INTO reading_annotations (
                id,
                contentId,
                paragraphIndex,
                quotedText,
                noteText,
                createdAtMillis,
                updatedAtMillis,
                sourceTitle,
                sourceBlockIndex,
                textStartOffset,
                textEndOffset,
                prefixText,
                suffixText
            ) VALUES (
                'legacy-range',
                'content-1',
                3,
                'Selected quote',
                'Stored note',
                1000,
                1200,
                'Private EPUB',
                7,
                12,
                42,
                'Before',
                'After'
            )
            """.trimIndent(),
        )
        legacy.close()

        val migrated = helper.runMigrationsAndValidate(
            databaseName,
            14,
            true,
            QualityAlternativeDatabase.MIGRATION_13_14,
        )
        try {
            assertTrue(readingAnnotationColumns(migrated).contains("endSourceBlockIndex"))
            migrated.query(
                """
                SELECT sourceBlockIndex, endSourceBlockIndex, textStartOffset, textEndOffset
                FROM reading_annotations
                WHERE id = 'legacy-range'
                """.trimIndent(),
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(7, cursor.getInt(0))
                assertEquals(7, cursor.getInt(1))
                assertEquals(12, cursor.getInt(2))
                assertEquals(42, cursor.getInt(3))
            }
        } finally {
            migrated.close()
            deleteDatabase(databaseName)
        }
    }

    @Test
    fun migration14To15ValidatesRoomSchemaAndDefaultsMarkdownImageAttachmentManifest() {
        val databaseName = "qa-migration-14-15.db"
        deleteDatabase(databaseName)
        val legacy = helper.createDatabase(databaseName, 14)
        legacy.execSQL(
            """
            INSERT INTO user_documents (
                id,
                uri,
                displayName,
                mimeType,
                documentFormat,
                title,
                description,
                durationMinutes,
                topicTagsCsv,
                availability,
                createdAtMillis,
                updatedAtMillis,
                documentFingerprintSha256,
                documentFingerprintSizeBytes
            ) VALUES (
                'user-document-1',
                'content://provider/book.md',
                'book.md',
                'text/markdown',
                'MARKDOWN',
                'Book',
                'Markdown book',
                10,
                'OTHER',
                'AVAILABLE',
                1000,
                1200,
                NULL,
                NULL
            )
            """.trimIndent(),
        )
        legacy.close()

        val migrated = helper.runMigrationsAndValidate(
            databaseName,
            15,
            true,
            QualityAlternativeDatabase.MIGRATION_14_15,
        )
        try {
            assertTrue(userDocumentColumns(migrated).contains("imageAttachmentUrisJson"))
            migrated.query(
                """
                SELECT imageAttachmentUrisJson
                FROM user_documents
                WHERE id = 'user-document-1'
                """.trimIndent(),
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("{}", cursor.getString(0))
            }
        } finally {
            migrated.close()
            deleteDatabase(databaseName)
        }
    }

    @Test
    fun migration15To16ValidatesRoomSchemaAndIndexesAnalyticsTimestamp() {
        val databaseName = "qa-migration-15-16.db"
        deleteDatabase(databaseName)
        val legacy = helper.createDatabase(databaseName, 15)
        legacy.execSQL(
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
                'semantic-1',
                NULL,
                NULL,
                NULL,
                NULL,
                '',
                NULL,
                '{}'
            )
            """.trimIndent(),
        )
        legacy.close()

        val migrated = helper.runMigrationsAndValidate(
            databaseName,
            16,
            true,
            QualityAlternativeDatabase.MIGRATION_15_16,
        )
        try {
            assertTrue(analyticsEventIndexNames(migrated).contains("index_analytics_events_timestampMillis"))
            migrated.query("SELECT timestampMillis FROM analytics_events WHERE semanticKey = 'semantic-1'")
                .use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    assertEquals(1000, cursor.getInt(0))
                }
        } finally {
            migrated.close()
            deleteDatabase(databaseName)
        }
    }

    private fun analyticsEventIndexNames(db: SupportSQLiteDatabase): List<String> {
        return db.query("PRAGMA index_list(analytics_events)").use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
                }
            }
        }
    }

    private fun readingProgressColumns(db: SupportSQLiteDatabase): List<String> {
        return db.query("PRAGMA table_info(reading_progress)").use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
                }
            }
        }
    }

    private fun readingAnnotationColumns(db: SupportSQLiteDatabase): List<String> {
        return db.query("PRAGMA table_info(reading_annotations)").use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
                }
            }
        }
    }

    private fun userDocumentColumns(db: SupportSQLiteDatabase): List<String> {
        return db.query("PRAGMA table_info(user_documents)").use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
                }
            }
        }
    }

    private fun deleteDatabase(name: String) {
        InstrumentationRegistry.getInstrumentation().targetContext.deleteDatabase(name)
    }

    private val version10AnnotationColumns = listOf(
        "id",
        "contentId",
        "paragraphIndex",
        "quotedText",
        "noteText",
        "createdAtMillis",
        "updatedAtMillis",
        "sourceTitle",
        "sourceLabel",
        "sourceType",
        "sourceFormat",
        "sourceHref",
        "sourceAnchor",
        "sourceBlockIndex",
        "textStartOffset",
        "textEndOffset",
        "prefixText",
        "suffixText",
    )
}
