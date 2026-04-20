package com.qualityalternative.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        AnalyticsEventEntity::class,
        ReplacementSessionEntity::class,
        UserLinkEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class QualityAlternativeDatabase : RoomDatabase() {
    abstract fun analyticsEventDao(): AnalyticsEventDao

    abstract fun replacementSessionDao(): ReplacementSessionDao

    abstract fun userLinkDao(): UserLinkDao

    companion object {
        fun build(context: Context): QualityAlternativeDatabase {
            return Room.databaseBuilder(
                context,
                QualityAlternativeDatabase::class.java,
                "quality_alternative.db",
            )
                .fallbackToDestructiveMigration(true)
                .build()
        }
    }
}
