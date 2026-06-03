package com.qualityalternative.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDocumentDao {
    @Query("SELECT * FROM user_documents ORDER BY createdAtMillis DESC")
    fun observeAll(): Flow<List<UserDocumentEntity>>

    @Query("SELECT * FROM user_documents WHERE uri = :uri LIMIT 1")
    suspend fun findByUri(uri: String): UserDocumentEntity?

    @Query("SELECT * FROM user_documents WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): UserDocumentEntity?

    @Query("SELECT * FROM user_documents WHERE documentFingerprintSha256 = :sha256 LIMIT 1")
    suspend fun findByDocumentFingerprintSha256(sha256: String): UserDocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(document: UserDocumentEntity)

    @Query("UPDATE user_documents SET durationMinutes = :durationMinutes, updatedAtMillis = :updatedAtMillis WHERE id = :id")
    suspend fun updateDurationMinutes(
        id: String,
        durationMinutes: Int,
        updatedAtMillis: Long,
    )

    @Query("UPDATE user_documents SET availability = :availability, updatedAtMillis = :updatedAtMillis WHERE id = :id")
    suspend fun updateAvailability(
        id: String,
        availability: String,
        updatedAtMillis: Long,
    )

    @Query("DELETE FROM user_documents WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM user_documents")
    suspend fun deleteAll()

    @Query("DELETE FROM user_documents WHERE id NOT IN (:ids)")
    suspend fun deleteAllExcept(ids: Set<String>)

    // Atomic delete-then-insert so an interrupted import cannot leave the table in a half-replaced
    // state (some rows deleted, new rows not yet written). All statements run in one transaction.
    @Transaction
    suspend fun replacePortableDocuments(
        replaceExisting: Boolean,
        retainedIds: Set<String>,
        documentsToImport: List<UserDocumentEntity>,
    ) {
        if (replaceExisting) {
            if (retainedIds.isEmpty()) {
                deleteAll()
            } else {
                deleteAllExcept(retainedIds)
            }
        }
        documentsToImport.forEach { document -> insertOrReplace(document) }
    }
}
