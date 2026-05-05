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
