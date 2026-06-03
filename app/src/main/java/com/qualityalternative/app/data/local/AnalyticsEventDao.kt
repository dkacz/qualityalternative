package com.qualityalternative.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalyticsEventDao {
    // The analytics table is append-only on the write path, so the observed window is bounded to the
    // most recent [limit] rows. The timestampMillis index backs this ORDER BY so each Flow emission is
    // an index range read instead of a full-table scan plus sort.
    @Query("SELECT * FROM analytics_events ORDER BY timestampMillis DESC LIMIT :limit")
    fun observeMostRecent(limit: Int): Flow<List<AnalyticsEventEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(event: AnalyticsEventEntity): Long

    // Keeps the newest [keep] rows and deletes everything strictly older than the boundary timestamp.
    // This bounds unbounded historical growth. Ties on the boundary timestamp are retained, so the
    // table may briefly hold a few more than [keep] rows, which is harmless.
    @Query(
        """
        DELETE FROM analytics_events
        WHERE timestampMillis < (
            SELECT MIN(timestampMillis) FROM (
                SELECT timestampMillis FROM analytics_events
                ORDER BY timestampMillis DESC
                LIMIT :keep
            )
        )
        """,
    )
    suspend fun pruneToMostRecent(keep: Int)
}
