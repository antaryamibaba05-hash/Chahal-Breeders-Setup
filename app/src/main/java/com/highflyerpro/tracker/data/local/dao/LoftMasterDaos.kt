package com.highflyerpro.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.highflyerpro.tracker.data.local.entities.BreedingEventEntity
import com.highflyerpro.tracker.data.local.entities.BreedingPairEntity
import com.highflyerpro.tracker.data.local.entities.DewormingRecordEntity
import com.highflyerpro.tracker.data.local.entities.DiseaseRecordEntity
import com.highflyerpro.tracker.data.local.entities.FeedingRecordEntity
import com.highflyerpro.tracker.data.local.entities.FoodEntity
import com.highflyerpro.tracker.data.local.entities.FoodInventoryEntity
import com.highflyerpro.tracker.data.local.entities.MedicationRecordEntity
import com.highflyerpro.tracker.data.local.entities.PigeonTagEntity
import com.highflyerpro.tracker.data.local.entities.ReminderEntity
import com.highflyerpro.tracker.data.local.entities.SupplementRecordEntity
import com.highflyerpro.tracker.data.local.entities.TagEntity
import com.highflyerpro.tracker.data.local.entities.WeightRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BreedingPairDao {
    @Query("SELECT * FROM breeding_pairs ORDER BY startDate DESC")
    fun getAllPairsFlow(): Flow<List<BreedingPairEntity>>

    @Query("SELECT * FROM breeding_pairs WHERE pairId = :pairId LIMIT 1")
    suspend fun getPairById(pairId: String): BreedingPairEntity?

    @Query("SELECT * FROM breeding_pairs WHERE maleId = :pigeonId OR femaleId = :pigeonId ORDER BY startDate DESC")
    fun getPairsForPigeonFlow(pigeonId: String): Flow<List<BreedingPairEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPair(pair: BreedingPairEntity)

    @Update
    suspend fun updatePair(pair: BreedingPairEntity)

    @Query("DELETE FROM breeding_pairs WHERE pairId = :pairId")
    suspend fun deletePair(pairId: String)
}

@Dao
interface BreedingEventDao {
    @Query("SELECT * FROM breeding_events ORDER BY date DESC")
    fun getAllEventsFlow(): Flow<List<BreedingEventEntity>>

    @Query("SELECT * FROM breeding_events WHERE pairId = :pairId ORDER BY date DESC")
    fun getEventsByPairFlow(pairId: String): Flow<List<BreedingEventEntity>>

    @Query("SELECT * FROM breeding_events WHERE maleId = :pigeonId OR femaleId = :pigeonId ORDER BY date DESC")
    fun getEventsForPigeonFlow(pigeonId: String): Flow<List<BreedingEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: BreedingEventEntity)

    @Update
    suspend fun updateEvent(event: BreedingEventEntity)

    @Query("DELETE FROM breeding_events WHERE eventId = :eventId")
    suspend fun deleteEvent(eventId: String)
}

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medication_records WHERE pigeonId = :pigeonId ORDER BY startDate DESC")
    fun getMedicationsByPigeonFlow(pigeonId: String): Flow<List<MedicationRecordEntity>>

    @Query("SELECT * FROM medication_records WHERE status = 'Active' ORDER BY startDate DESC")
    fun getActiveMedicationsFlow(): Flow<List<MedicationRecordEntity>>

    @Query("SELECT * FROM medication_records ORDER BY startDate DESC")
    fun getAllMedicationsFlow(): Flow<List<MedicationRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedications(medications: List<MedicationRecordEntity>)

    @Update
    suspend fun updateMedication(medication: MedicationRecordEntity)

    @Query("DELETE FROM medication_records WHERE id = :id")
    suspend fun deleteMedication(id: Long)
}

@Dao
interface DewormingDao {
    @Query("SELECT * FROM deworming_records WHERE pigeonId = :pigeonId ORDER BY date DESC")
    fun getDewormingByPigeonFlow(pigeonId: String): Flow<List<DewormingRecordEntity>>

    @Query("SELECT * FROM deworming_records ORDER BY date DESC")
    fun getAllDewormingFlow(): Flow<List<DewormingRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeworming(record: DewormingRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDewormingBulk(records: List<DewormingRecordEntity>)

    @Query("DELETE FROM deworming_records WHERE id = :id")
    suspend fun deleteDeworming(id: Long)
}

@Dao
interface SupplementDao {
    @Query("SELECT * FROM supplement_records WHERE pigeonId = :pigeonId OR pigeonId = 'ALL_LOFT' ORDER BY startDate DESC")
    fun getSupplementsByPigeonFlow(pigeonId: String): Flow<List<SupplementRecordEntity>>

    @Query("SELECT * FROM supplement_records ORDER BY startDate DESC")
    fun getAllSupplementsFlow(): Flow<List<SupplementRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplement(record: SupplementRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplements(records: List<SupplementRecordEntity>)

    @Update
    suspend fun updateSupplement(record: SupplementRecordEntity)

    @Query("DELETE FROM supplement_records WHERE id = :id")
    suspend fun deleteSupplement(id: Long)
}

@Dao
interface NutritionDao {
    // Food Mixtures
    @Query("SELECT * FROM food_mixtures ORDER BY name ASC")
    fun getAllFoodsFlow(): Flow<List<FoodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodEntity)

    @Update
    suspend fun updateFood(food: FoodEntity)

    @Query("DELETE FROM food_mixtures WHERE foodId = :foodId")
    suspend fun deleteFood(foodId: String)

    // Feeding Records
    @Query("SELECT * FROM feeding_records ORDER BY date DESC LIMIT 100")
    fun getFeedingRecordsFlow(): Flow<List<FeedingRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeeding(record: FeedingRecordEntity): Long

    @Query("DELETE FROM feeding_records WHERE id = :id")
    suspend fun deleteFeeding(id: Long)

    // Inventory
    @Query("SELECT * FROM food_inventory ORDER BY foodName ASC")
    fun getInventoryFlow(): Flow<List<FoodInventoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventory(item: FoodInventoryEntity)

    @Update
    suspend fun updateInventory(item: FoodInventoryEntity)

    @Query("DELETE FROM food_inventory WHERE inventoryId = :inventoryId")
    suspend fun deleteInventory(inventoryId: String)
}

@Dao
interface DiseaseDao {
    @Query("SELECT * FROM disease_records WHERE pigeonId = :pigeonId ORDER BY dateDetected DESC")
    fun getDiseasesByPigeonFlow(pigeonId: String): Flow<List<DiseaseRecordEntity>>

    @Query("SELECT * FROM disease_records ORDER BY dateDetected DESC")
    fun getAllDiseasesFlow(): Flow<List<DiseaseRecordEntity>>

    @Query("SELECT * FROM disease_records WHERE status = 'Active' ORDER BY dateDetected DESC")
    fun getActiveDiseasesFlow(): Flow<List<DiseaseRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDisease(record: DiseaseRecordEntity): Long

    @Update
    suspend fun updateDisease(record: DiseaseRecordEntity)

    @Query("DELETE FROM disease_records WHERE id = :id")
    suspend fun deleteDisease(id: Long)
}

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_records WHERE pigeonId = :pigeonId ORDER BY date DESC")
    fun getWeightsByPigeonFlow(pigeonId: String): Flow<List<WeightRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeight(record: WeightRecordEntity): Long

    @Query("DELETE FROM weight_records WHERE id = :id")
    suspend fun deleteWeight(id: Long)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE isCompleted = 0 ORDER BY dueDate ASC")
    fun getActiveRemindersFlow(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders ORDER BY dueDate DESC")
    fun getAllRemindersFlow(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE pigeonId = :pigeonId ORDER BY dueDate ASC")
    fun getRemindersByPigeonFlow(pigeonId: String): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity)

    @Query("UPDATE reminders SET isCompleted = :isCompleted WHERE reminderId = :reminderId")
    suspend fun setCompleted(reminderId: String, isCompleted: Boolean)

    @Query("DELETE FROM reminders WHERE reminderId = :reminderId")
    suspend fun deleteReminder(reminderId: String)
}

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTagsFlow(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity)

    @Query("DELETE FROM tags WHERE name = :name AND isSystem = 0")
    suspend fun deleteTag(name: String)

    @Query("SELECT tagName FROM pigeon_tags WHERE pigeonId = :pigeonId")
    fun getTagsForPigeonFlow(pigeonId: String): Flow<List<String>>

    @Query("SELECT tagName FROM pigeon_tags WHERE pigeonId = :pigeonId")
    suspend fun getTagsForPigeon(pigeonId: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTagToPigeon(pigeonTag: PigeonTagEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTagsToPigeonsBulk(pigeonTags: List<PigeonTagEntity>)

    @Query("DELETE FROM pigeon_tags WHERE pigeonId = :pigeonId AND tagName = :tagName")
    suspend fun removeTagFromPigeon(pigeonId: String, tagName: String)

    @Query("SELECT pigeonId FROM pigeon_tags WHERE tagName = :tagName")
    suspend fun getPigeonIdsWithTag(tagName: String): List<String>
}
