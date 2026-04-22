package com.qualityalternative.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDocumentDao {
    @Query("SELECT * FROM user_documents ORDER BY createdAtMillis DESC")
    fun observeAll(): Flow<List<UserDocumentEntity>>

    @Query("SELECT * FROM user_documents WHERE uri = :uri LIMIT 1")
    suspend fun findByUri(uri: String): UserDocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(document: UserDocumentEntity)

    @Query("UPDATE user_documents SET availability = :availability, updatedAtMillis = :updatedAtMillis WHERE id = :id")
    suspend fun updateAvailability(
        id: String,
        availability: String,
        updatedAtMillis: Long,
    )
}
