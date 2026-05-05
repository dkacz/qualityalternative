package com.qualityalternative.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserLinkDao {
    @Query("SELECT * FROM user_links ORDER BY createdAtMillis DESC")
    fun observeAll(): Flow<List<UserLinkEntity>>

    @Query("SELECT * FROM user_links WHERE normalizedUrl = :normalizedUrl LIMIT 1")
    suspend fun findByNormalizedUrl(normalizedUrl: String): UserLinkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(link: UserLinkEntity)

    @Query("UPDATE user_links SET availability = :availability, updatedAtMillis = :updatedAtMillis WHERE id = :id")
    suspend fun updateAvailability(
        id: String,
        availability: String,
        updatedAtMillis: Long,
    )

    @Query("DELETE FROM user_links WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM user_links")
    suspend fun deleteAll()

    @Query("DELETE FROM user_links WHERE id NOT IN (:ids)")
    suspend fun deleteAllExcept(ids: Set<String>)
}
