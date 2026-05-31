package com.qualityalternative.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        AnalyticsEventEntity::class,
        ReplacementSessionEntity::class,
        UserLinkEntity::class,
        UserDocumentEntity::class,
        ReadingProgressEntity::class,
        ReadingAnnotationEntity::class,
    ],
    version = 15,
    exportSchema = true,
)
abstract class QualityAlternativeDatabase : RoomDatabase() {
    abstract fun analyticsEventDao(): AnalyticsEventDao

    abstract fun replacementSessionDao(): ReplacementSessionDao

    abstract fun userLinkDao(): UserLinkDao

    abstract fun userDocumentDao(): UserDocumentDao

    abstract fun readingProgressDao(): ReadingProgressDao

    abstract fun readingAnnotationDao(): ReadingAnnotationDao

    companion object {
        fun build(
            context: Context,
            databaseName: String = "quality_alternative.db",
        ): QualityAlternativeDatabase {
            return Room.databaseBuilder(
                context,
                QualityAlternativeDatabase::class.java,
                databaseName,
            )
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6,
                    MIGRATION_6_7,
                    MIGRATION_7_8,
                    MIGRATION_8_9,
                    MIGRATION_9_10,
                    MIGRATION_10_11,
                    MIGRATION_11_12,
                    MIGRATION_12_13,
                    MIGRATION_13_14,
                    MIGRATION_14_15,
                )
                .build()
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE analytics_events_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        type TEXT NOT NULL,
                        timestampMillis INTEGER NOT NULL,
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
                db.execSQL(
                    """
                    INSERT INTO analytics_events_new (
                        id,
                        type,
                        timestampMillis,
                        interventionId,
                        sessionId,
                        targetAppPackage,
                        primaryContentId,
                        backupContentIdsCsv,
                        contentId,
                        metadataJson
                    )
                    SELECT
                        id,
                        type,
                        timestampMillis,
                        NULL,
                        NULL,
                        targetAppPackage,
                        contentId,
                        '',
                        contentId,
                        metadataJson
                    FROM analytics_events
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE analytics_events")
                db.execSQL("ALTER TABLE analytics_events_new RENAME TO analytics_events")

                db.execSQL(
                    """
                    CREATE TABLE replacement_sessions_new (
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
                db.execSQL(
                    """
                    INSERT INTO replacement_sessions_new (
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
                    )
                    SELECT
                        sessionId,
                        'legacy:' || sessionId,
                        targetAppPackage,
                        targetAppDisplayName,
                        acceptedAtMillis,
                        contentId,
                        '',
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
                    FROM replacement_sessions
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE replacement_sessions")
                db.execSQL("ALTER TABLE replacement_sessions_new RENAME TO replacement_sessions")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE analytics_events ADD COLUMN semanticKey TEXT")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_analytics_events_semanticKey ON analytics_events(semanticKey)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                createUserLinksTable(db)
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE replacement_sessions ADD COLUMN feedbackFitRating TEXT")
                db.execSQL("ALTER TABLE replacement_sessions ADD COLUMN feedbackScrollRating TEXT")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                createUserDocumentsTableV6(db)
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE replacement_sessions ADD COLUMN contentDurationMinutes INTEGER NOT NULL DEFAULT 10",
                )
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS reading_progress (
                        contentId TEXT NOT NULL,
                        progressPercent INTEGER NOT NULL,
                        lastVisibleParagraphIndex INTEGER NOT NULL,
                        paragraphCount INTEGER NOT NULL,
                        updatedAtMillis INTEGER NOT NULL,
                        completedAtMillis INTEGER,
                        PRIMARY KEY(contentId)
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_reading_progress_updatedAtMillis ON reading_progress(updatedAtMillis)",
                )
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                createReadingAnnotationsTableV9(db)
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE reading_annotations ADD COLUMN sourceTitle TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE reading_annotations ADD COLUMN sourceLabel TEXT")
                db.execSQL("ALTER TABLE reading_annotations ADD COLUMN sourceType TEXT")
                db.execSQL("ALTER TABLE reading_annotations ADD COLUMN sourceFormat TEXT")
                db.execSQL("ALTER TABLE reading_annotations ADD COLUMN sourceHref TEXT")
                db.execSQL("ALTER TABLE reading_annotations ADD COLUMN sourceAnchor TEXT")
                db.execSQL("ALTER TABLE reading_annotations ADD COLUMN sourceBlockIndex INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE reading_annotations ADD COLUMN textStartOffset INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE reading_annotations ADD COLUMN textEndOffset INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE reading_annotations ADD COLUMN prefixText TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE reading_annotations ADD COLUMN suffixText TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_documents ADD COLUMN documentFingerprintSha256 TEXT")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_user_documents_documentFingerprintSha256 ON user_documents(documentFingerprintSha256)",
                )
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_documents ADD COLUMN documentFingerprintSizeBytes INTEGER")
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE reading_progress ADD COLUMN lastVisibleTextOffset INTEGER NOT NULL DEFAULT 0",
                )
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE reading_annotations ADD COLUMN endSourceBlockIndex INTEGER NOT NULL DEFAULT 0",
                )
                db.execSQL("UPDATE reading_annotations SET endSourceBlockIndex = sourceBlockIndex")
            }
        }

        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_documents ADD COLUMN imageAttachmentUrisJson TEXT NOT NULL DEFAULT '{}'")
            }
        }

        private fun createUserLinksTable(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS user_links (
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
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_user_links_normalizedUrl ON user_links(normalizedUrl)",
            )
        }

        private fun createUserDocumentsTableV6(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS user_documents (
                    id TEXT NOT NULL,
                    uri TEXT NOT NULL,
                    displayName TEXT NOT NULL,
                    mimeType TEXT,
                    documentFormat TEXT NOT NULL,
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
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_user_documents_uri ON user_documents(uri)",
            )
        }

        private fun createReadingAnnotationsTableV9(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS reading_annotations (
                    id TEXT NOT NULL,
                    contentId TEXT NOT NULL,
                    paragraphIndex INTEGER NOT NULL,
                    quotedText TEXT NOT NULL,
                    noteText TEXT NOT NULL,
                    createdAtMillis INTEGER NOT NULL,
                    updatedAtMillis INTEGER NOT NULL,
                    PRIMARY KEY(id)
                )
                """.trimIndent(),
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_reading_annotations_contentId ON reading_annotations(contentId)",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_reading_annotations_contentId_paragraphIndex ON reading_annotations(contentId, paragraphIndex)",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_reading_annotations_updatedAtMillis ON reading_annotations(updatedAtMillis)",
            )
        }
    }
}
