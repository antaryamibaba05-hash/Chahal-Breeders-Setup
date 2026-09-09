package com.highflyerpro.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.highflyerpro.tracker.data.local.entities.PigeonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PigeonDao {
    @Query("SELECT * FROM pigeons WHERE isArchived = 0 AND inTrash = 0 ORDER BY name ASC")
    fun getAllActivePigeonsFlow(): Flow<List<PigeonEntity>>

    @Query("SELECT * FROM pigeons WHERE inTrash = 0 ORDER BY name ASC")
    fun getAllPigeonsFlow(): Flow<List<PigeonEntity>>

    @Query("SELECT * FROM pigeons WHERE inTrash = 1 ORDER BY trashDate DESC")
    fun getTrashPigeonsFlow(): Flow<List<PigeonEntity>>

    @Query("SELECT * FROM pigeons WHERE isArchived = 1 AND inTrash = 0 ORDER BY updatedAt DESC")
    fun getArchivedPigeonsFlow(): Flow<List<PigeonEntity>>

    @Query("SELECT * FROM pigeons WHERE isFavorite = 1 AND inTrash = 0 ORDER BY name ASC")
    fun getFavoritePigeonsFlow(): Flow<List<PigeonEntity>>

    @Query("SELECT * FROM pigeons WHERE isPinned = 1 AND inTrash = 0 ORDER BY name ASC")
    fun getPinnedPigeonsFlow(): Flow<List<PigeonEntity>>

    @Query("SELECT * FROM pigeons WHERE pigeonId = :pigeonId LIMIT 1")
    fun getPigeonByIdFlow(pigeonId: String): Flow<PigeonEntity?>

    @Query("SELECT * FROM pigeons WHERE pigeonId = :pigeonId LIMIT 1")
    suspend fun getPigeonById(pigeonId: String): PigeonEntity?

    @Query("SELECT * FROM pigeons WHERE pigeonId IN (:ids)")
    suspend fun getPigeonsByIds(ids: List<String>): List<PigeonEntity>

    @Query("SELECT * FROM pigeons WHERE (fatherId = :pigeonId OR motherId = :pigeonId) AND inTrash = 0")
    fun getOffspringFlow(pigeonId: String): Flow<List<PigeonEntity>>

    @Query("SELECT * FROM pigeons WHERE (fatherId = :pigeonId OR motherId = :pigeonId) AND inTrash = 0")
    suspend fun getOffspringList(pigeonId: String): List<PigeonEntity>

    @Query("SELECT * FROM pigeons WHERE (fatherId = :pigeonId OR motherId = :pigeonId) AND gender = 'Male' AND inTrash = 0")
    fun getSonsFlow(pigeonId: String): Flow<List<PigeonEntity>>

    @Query("SELECT * FROM pigeons WHERE (fatherId = :pigeonId OR motherId = :pigeonId) AND gender = 'Female' AND inTrash = 0")
    fun getDaughtersFlow(pigeonId: String): Flow<List<PigeonEntity>>

    @Query("SELECT * FROM pigeons WHERE fatherId = :fatherId AND motherId = :motherId AND pigeonId != :excludeId AND inTrash = 0")
    suspend fun getSiblings(fatherId: String, motherId: String, excludeId: String): List<PigeonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPigeon(pigeon: PigeonEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPigeons(pigeons: List<PigeonEntity>)

    @Update
    suspend fun updatePigeon(pigeon: PigeonEntity)

    @Query("UPDATE pigeons SET isArchived = :isArchived, updatedAt = :timestamp WHERE pigeonId = :pigeonId")
    suspend fun setArchiveStatus(pigeonId: String, isArchived: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE pigeons SET isArchived = :isArchived, updatedAt = :timestamp WHERE pigeonId IN (:pigeonIds)")
    suspend fun setArchiveStatusBulk(pigeonIds: List<String>, isArchived: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE pigeons SET inTrash = :inTrash, trashDate = :trashDate, updatedAt = :timestamp WHERE pigeonId = :pigeonId")
    suspend fun setTrashStatus(pigeonId: String, inTrash: Boolean, trashDate: Long? = if (inTrash) System.currentTimeMillis() else null, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE pigeons SET inTrash = :inTrash, trashDate = :trashDate, updatedAt = :timestamp WHERE pigeonId IN (:pigeonIds)")
    suspend fun setTrashStatusBulk(pigeonIds: List<String>, inTrash: Boolean, trashDate: Long? = if (inTrash) System.currentTimeMillis() else null, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE pigeons SET isFavorite = :isFavorite, updatedAt = :timestamp WHERE pigeonId = :pigeonId")
    suspend fun setFavoriteStatus(pigeonId: String, isFavorite: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE pigeons SET isPinned = :isPinned, updatedAt = :timestamp WHERE pigeonId = :pigeonId")
    suspend fun setPinnedStatus(pigeonId: String, isPinned: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE pigeons SET status = :status, updatedAt = :timestamp WHERE pigeonId = :pigeonId")
    suspend fun updateStatus(pigeonId: String, status: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE pigeons SET status = :status, updatedAt = :timestamp WHERE pigeonId IN (:pigeonIds)")
    suspend fun updateStatusBulk(pigeonIds: List<String>, status: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM pigeons WHERE ringNumber = :ringNumber AND (:excludePigeonId IS NULL OR pigeonId != :excludePigeonId)")
    suspend fun countRingNumber(ringNumber: String, excludePigeonId: String?): Int

    @Query("SELECT COUNT(*) FROM pigeons WHERE inTrash = 0")
    suspend fun getTotalPigeonCount(): Int

    @Query("SELECT * FROM pigeons WHERE inTrash = 0 AND (name LIKE '%' || :query || '%' OR nickname LIKE '%' || :query || '%' OR ringNumber LIKE '%' || :query || '%' OR color LIKE '%' || :query || '%' OR breed LIKE '%' || :query || '%' OR breeder LIKE '%' || :query || '%')")
    fun searchPigeonsFlow(query: String): Flow<List<PigeonEntity>>

    @Query("SELECT * FROM pigeons")
    suspend fun getAllPigeonsList(): List<PigeonEntity>

    @Query("DELETE FROM pigeons WHERE pigeonId = :pigeonId")
    suspend fun deletePigeonPermanently(pigeonId: String)
}
