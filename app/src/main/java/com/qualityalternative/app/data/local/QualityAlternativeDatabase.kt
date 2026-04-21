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
    ],
    version = 5,
    exportSchema = true,
)
abstract class QualityAlternativeDatabase : RoomDatabase() {
    abstract fun analyticsEventDao(): AnalyticsEventDao

    abstract fun replacementSessionDao(): ReplacementSessionDao

    abstract fun userLinkDao(): UserLinkDao

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
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
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
    }
}
