package com.qualityalternative.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReplacementSessionDao {
    @Query("SELECT * FROM replacement_sessions ORDER BY acceptedAtMillis DESC")
    fun observeAll(): Flow<List<ReplacementSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(session: ReplacementSessionEntity)
}
