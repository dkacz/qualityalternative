package com.qualityalternative.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ReplacementSessionDao {
    @Query("SELECT * FROM replacement_sessions ORDER BY acceptedAtMillis DESC")
    suspend fun getAll(): List<ReplacementSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(session: ReplacementSessionEntity)
}
