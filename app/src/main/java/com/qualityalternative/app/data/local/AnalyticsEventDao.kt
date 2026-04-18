package com.qualityalternative.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalyticsEventDao {
    @Query("SELECT * FROM analytics_events ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<AnalyticsEventEntity>>

    @Insert
    suspend fun insert(event: AnalyticsEventEntity)
}
