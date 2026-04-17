package com.qualityalternative.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AnalyticsEventDao {
    @Query("SELECT * FROM analytics_events ORDER BY timestampMillis DESC")
    suspend fun getAll(): List<AnalyticsEventEntity>

    @Insert
    suspend fun insert(event: AnalyticsEventEntity)
}
