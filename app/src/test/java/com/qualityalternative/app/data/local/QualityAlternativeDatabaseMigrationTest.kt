package com.qualityalternative.app.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import java.lang.reflect.Proxy
import org.junit.Assert.assertTrue
import org.junit.Test

class QualityAlternativeDatabaseMigrationTest {
    @Test
    fun migration9To10AddsAnnotationSelectorAndSourceColumns() {
        val statements = mutableListOf<String>()
        val db = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java),
        ) { _, method, args ->
            if (method.name == "execSQL" && args?.firstOrNull() is String) {
                statements += args.first() as String
            }
            when (method.returnType) {
                java.lang.Boolean.TYPE -> false
                java.lang.Integer.TYPE -> 0
                java.lang.Long.TYPE -> 0L
                else -> null
            }
        } as SupportSQLiteDatabase

        QualityAlternativeDatabase.MIGRATION_9_10.migrate(db)

        assertTrue(statements.any { it.contains("sourceTitle TEXT NOT NULL DEFAULT ''") })
        assertTrue(statements.any { it.contains("sourceHref TEXT") })
        assertTrue(statements.any { it.contains("sourceBlockIndex INTEGER NOT NULL DEFAULT 0") })
        assertTrue(statements.any { it.contains("textStartOffset INTEGER NOT NULL DEFAULT 0") })
        assertTrue(statements.any { it.contains("prefixText TEXT NOT NULL DEFAULT ''") })
        assertTrue(statements.any { it.contains("suffixText TEXT NOT NULL DEFAULT ''") })
    }

    @Test
    fun migration10To11AddsUserDocumentFingerprintColumnAndIndex() {
        val statements = mutableListOf<String>()
        val db = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java),
        ) { _, method, args ->
            if (method.name == "execSQL" && args?.firstOrNull() is String) {
                statements += args.first() as String
            }
            when (method.returnType) {
                java.lang.Boolean.TYPE -> false
                java.lang.Integer.TYPE -> 0
                java.lang.Long.TYPE -> 0L
                else -> null
            }
        } as SupportSQLiteDatabase

        QualityAlternativeDatabase.MIGRATION_10_11.migrate(db)

        assertTrue(statements.any { it.contains("ADD COLUMN documentFingerprintSha256 TEXT") })
        assertTrue(
            statements.any {
                it.contains("index_user_documents_documentFingerprintSha256") &&
                    it.contains("documentFingerprintSha256")
            },
        )
    }

    @Test
    fun migration11To12AddsUserDocumentFingerprintSizeColumn() {
        val statements = mutableListOf<String>()
        val db = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java),
        ) { _, method, args ->
            if (method.name == "execSQL" && args?.firstOrNull() is String) {
                statements += args.first() as String
            }
            when (method.returnType) {
                java.lang.Boolean.TYPE -> false
                java.lang.Integer.TYPE -> 0
                java.lang.Long.TYPE -> 0L
                else -> null
            }
        } as SupportSQLiteDatabase

        QualityAlternativeDatabase.MIGRATION_11_12.migrate(db)

        assertTrue(statements.any { it.contains("ADD COLUMN documentFingerprintSizeBytes INTEGER") })
    }

    @Test
    fun migration12To13AddsReadingProgressSourceTextOffsetColumn() {
        val statements = mutableListOf<String>()
        val db = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java),
        ) { _, method, args ->
            if (method.name == "execSQL" && args?.firstOrNull() is String) {
                statements += args.first() as String
            }
            when (method.returnType) {
                java.lang.Boolean.TYPE -> false
                java.lang.Integer.TYPE -> 0
                java.lang.Long.TYPE -> 0L
                else -> null
            }
        } as SupportSQLiteDatabase

        QualityAlternativeDatabase.MIGRATION_12_13.migrate(db)

        assertTrue(
            statements.any {
                it.contains("ALTER TABLE reading_progress") &&
                    it.contains("ADD COLUMN lastVisibleTextOffset INTEGER NOT NULL DEFAULT 0")
            },
        )
    }

    @Test
    fun migration13To14AddsAnnotationEndSourceBlockColumnAndBackfillsIt() {
        val statements = mutableListOf<String>()
        val db = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java),
        ) { _, method, args ->
            if (method.name == "execSQL" && args?.firstOrNull() is String) {
                statements += args.first() as String
            }
            when (method.returnType) {
                java.lang.Boolean.TYPE -> false
                java.lang.Integer.TYPE -> 0
                java.lang.Long.TYPE -> 0L
                else -> null
            }
        } as SupportSQLiteDatabase

        QualityAlternativeDatabase.MIGRATION_13_14.migrate(db)

        assertTrue(
            statements.any {
                it.contains("ALTER TABLE reading_annotations") &&
                    it.contains("ADD COLUMN endSourceBlockIndex INTEGER NOT NULL DEFAULT 0")
            },
        )
        assertTrue(
            statements.any {
                it.contains("UPDATE reading_annotations SET endSourceBlockIndex = sourceBlockIndex")
            },
        )
    }
}
