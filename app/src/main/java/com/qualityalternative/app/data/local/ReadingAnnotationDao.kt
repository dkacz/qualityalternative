package com.qualityalternative.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingAnnotationDao {
    @Query("SELECT * FROM reading_annotations ORDER BY updatedAtMillis DESC")
    fun observeAll(): Flow<List<ReadingAnnotationEntity>>

    @Query("SELECT * FROM reading_annotations WHERE contentId = :contentId ORDER BY paragraphIndex ASC, updatedAtMillis DESC")
    fun observeForContent(contentId: String): Flow<List<ReadingAnnotationEntity>>

    @Query("SELECT * FROM reading_annotations WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): ReadingAnnotationEntity?

    @Query("SELECT * FROM reading_annotations WHERE contentId = :contentId AND paragraphIndex = :paragraphIndex ORDER BY updatedAtMillis DESC LIMIT 1")
    suspend fun findByContentAndParagraph(contentId: String, paragraphIndex: Int): ReadingAnnotationEntity?

    @Query("SELECT * FROM reading_annotations WHERE contentId IN (:contentIds)")
    suspend fun findByContentIds(contentIds: Set<String>): List<ReadingAnnotationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(annotation: ReadingAnnotationEntity)

    @Query("DELETE FROM reading_annotations WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM reading_annotations WHERE contentId IN (:contentIds)")
    suspend fun deleteAllForContentIds(contentIds: Set<String>)
}
