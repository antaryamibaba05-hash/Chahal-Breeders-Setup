package com.highflyerpro.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.highflyerpro.tracker.data.local.entities.PigeonPhoto
import kotlinx.coroutines.flow.Flow

@Dao
interface PigeonPhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PigeonPhoto): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<PigeonPhoto>)

    @Delete
    suspend fun deletePhoto(photo: PigeonPhoto)

    @Query("DELETE FROM pigeon_photos WHERE photoId = :photoId")
    suspend fun deletePhotoById(photoId: String)

    @Query("SELECT * FROM pigeon_photos WHERE pigeonId = :pigeonId ORDER BY dateAdded DESC")
    suspend fun getPhotosByPigeonId(pigeonId: String): List<PigeonPhoto>

    @Query("SELECT * FROM pigeon_photos WHERE pigeonId = :pigeonId ORDER BY dateAdded DESC")
    suspend fun getPhotosByPigeon(pigeonId: String): List<PigeonPhoto>

    @Query("SELECT * FROM pigeon_photos WHERE pigeonId = :pigeonId ORDER BY dateAdded DESC")
    fun getPhotosByPigeonIdFlow(pigeonId: String): Flow<List<PigeonPhoto>>

    @Query("SELECT * FROM pigeon_photos WHERE photoId = :photoId")
    suspend fun getPhotoById(photoId: String): PigeonPhoto?

    @Query("SELECT * FROM pigeon_photos ORDER BY dateAdded DESC")
    suspend fun getAllPhotos(): List<PigeonPhoto>

    @Update
    suspend fun updatePhoto(photo: PigeonPhoto)

    @Query("UPDATE pigeon_photos SET isProfilePhoto = 0 WHERE pigeonId = :pigeonId")
    suspend fun clearProfilePhoto(pigeonId: String)

    @Query("UPDATE pigeon_photos SET isProfilePhoto = 1 WHERE photoId = :photoId")
    suspend fun setProfilePhoto(photoId: String)

    @Query("DELETE FROM pigeon_photos WHERE pigeonId = :pigeonId")
    suspend fun deletePhotosByPigeon(pigeonId: String)
}
