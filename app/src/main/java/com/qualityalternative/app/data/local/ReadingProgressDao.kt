package com.qualityalternative.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingProgressDao {
    @Query("SELECT * FROM reading_progress ORDER BY updatedAtMillis DESC")
    fun observeAll(): Flow<List<ReadingProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(progress: ReadingProgressEntity)

    @Query("DELETE FROM reading_progress WHERE contentId = :contentId")
    suspend fun delete(contentId: String)

    @Query("DELETE FROM reading_progress WHERE contentId IN (:contentIds)")
    suspend fun deleteAll(contentIds: Set<String>)
}
