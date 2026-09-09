package com.highflyerpro.tracker.data.repository

import com.highflyerpro.tracker.data.local.AppDatabase
import com.highflyerpro.tracker.data.local.entities.BreedingEventEntity
import com.highflyerpro.tracker.data.local.entities.BreedingPairEntity
import com.highflyerpro.tracker.data.local.entities.DewormingRecordEntity
import com.highflyerpro.tracker.data.local.entities.DiseaseRecordEntity
import com.highflyerpro.tracker.data.local.entities.FeedingRecordEntity
import com.highflyerpro.tracker.data.local.entities.FoodEntity
import com.highflyerpro.tracker.data.local.entities.FoodInventoryEntity
import com.highflyerpro.tracker.data.local.entities.MedicationRecordEntity
import com.highflyerpro.tracker.data.local.entities.PigeonEventEntity
import com.highflyerpro.tracker.data.local.entities.PigeonTagEntity
import com.highflyerpro.tracker.data.local.entities.ReminderEntity
import com.highflyerpro.tracker.data.local.entities.SupplementRecordEntity
import com.highflyerpro.tracker.data.local.entities.TagEntity
import com.highflyerpro.tracker.data.local.entities.WeightRecordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class LoftMasterRepository(private val database: AppDatabase) {

    private val breedingPairDao = database.breedingPairDao()
    private val breedingEventDao = database.breedingEventDao()
    private val medicationDao = database.medicationDao()
    private val dewormingDao = database.dewormingDao()
    private val supplementDao = database.supplementDao()
    private val nutritionDao = database.nutritionDao()
    private val diseaseDao = database.diseaseDao()
    private val weightDao = database.weightDao()
    private val reminderDao = database.reminderDao()
    private val tagDao = database.tagDao()
    private val eventDao = database.eventDao()
    private val pigeonDao = database.pigeonDao()

    // ----------------------------------------------------
    // BREEDING
    // ----------------------------------------------------
    val allBreedingPairsFlow: Flow<List<BreedingPairEntity>> = breedingPairDao.getAllPairsFlow()
    val allBreedingEventsFlow: Flow<List<BreedingEventEntity>> = breedingEventDao.getAllEventsFlow()

    fun getPairsForPigeonFlow(pigeonId: String): Flow<List<BreedingPairEntity>> =
        breedingPairDao.getPairsForPigeonFlow(pigeonId)

    fun getEventsByPairFlow(pairId: String): Flow<List<BreedingEventEntity>> =
        breedingEventDao.getEventsByPairFlow(pairId)

    fun getEventsForPigeonFlow(pigeonId: String): Flow<List<BreedingEventEntity>> =
        breedingEventDao.getEventsForPigeonFlow(pigeonId)

    suspend fun createBreedingPair(maleId: String, femaleId: String, pairName: String, notes: String = ""): String = withContext(Dispatchers.IO) {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val pairId = "PAIR-$year-${UUID.randomUUID().toString().take(6).uppercase(Locale.US)}"
        val pair = BreedingPairEntity(
            pairId = pairId,
            maleId = maleId,
            femaleId = femaleId,
            pairName = pairName.ifBlank { "Breeding Pair $pairId" },
            notes = notes
        )
        breedingPairDao.insertPair(pair)

        eventDao.insertEvent(
            PigeonEventEntity(
                pigeonId = maleId,
                eventType = "BREEDING_MATED",
                title = "Mated with Female",
                description = "Paired in $pairName ($pairId)",
                relatedRecordId = pairId
            )
        )
        eventDao.insertEvent(
            PigeonEventEntity(
                pigeonId = femaleId,
                eventType = "BREEDING_MATED",
                title = "Mated with Male",
                description = "Paired in $pairName ($pairId)",
                relatedRecordId = pairId
            )
        )
        pairId
    }

    suspend fun updateBreedingPair(pair: BreedingPairEntity) = withContext(Dispatchers.IO) {
        breedingPairDao.updatePair(pair)
    }

    suspend fun deleteBreedingPair(pairId: String) = withContext(Dispatchers.IO) {
        breedingPairDao.deletePair(pairId)
    }

    suspend fun createBreedingEvent(event: BreedingEventEntity) = withContext(Dispatchers.IO) {
        breedingEventDao.insertEvent(event)
        // Also log timeline events
        eventDao.insertEvent(
            PigeonEventEntity(
                pigeonId = event.maleId,
                eventType = "BREEDING_CLUTCH",
                title = "Clutch / Breeding Event in ${event.nestNumber.ifBlank { "Nest" }}",
                description = "Breeding clutch recorded. Status: ${event.eggStatus}.",
                relatedRecordId = event.eventId
            )
        )
        eventDao.insertEvent(
            PigeonEventEntity(
                pigeonId = event.femaleId,
                eventType = "BREEDING_CLUTCH",
                title = "Clutch / Breeding Event in ${event.nestNumber.ifBlank { "Nest" }}",
                description = "Breeding clutch recorded. Status: ${event.eggStatus}.",
                relatedRecordId = event.eventId
            )
        )
        // If expected hatch date set, automatically create a Reminder!
        if (event.expectedHatchDate != null && event.expectedHatchDate > System.currentTimeMillis()) {
            val reminder = ReminderEntity(
                reminderId = "REM-${UUID.randomUUID().toString().take(8)}",
                title = "Egg Hatch Expected: Nest ${event.nestNumber}",
                description = "Expected hatching for pair event ${event.eventId}",
                category = "Egg Hatch Date",
                dueDate = event.expectedHatchDate
            )
            reminderDao.insertReminder(reminder)
        }
    }

    suspend fun deleteBreedingEvent(eventId: String) = withContext(Dispatchers.IO) {
        breedingEventDao.deleteEvent(eventId)
    }

    // ----------------------------------------------------
    // HEALTH & MEDICATIONS
    // ----------------------------------------------------
    val allMedicationsFlow: Flow<List<MedicationRecordEntity>> = medicationDao.getAllMedicationsFlow()
    val activeMedicationsFlow: Flow<List<MedicationRecordEntity>> = medicationDao.getActiveMedicationsFlow()

    fun getMedicationsByPigeonFlow(pigeonId: String): Flow<List<MedicationRecordEntity>> =
        medicationDao.getMedicationsByPigeonFlow(pigeonId)

    suspend fun addMedication(med: MedicationRecordEntity) = withContext(Dispatchers.IO) {
        medicationDao.insertMedication(med)
        eventDao.insertEvent(
            PigeonEventEntity(
                pigeonId = med.pigeonId,
                eventType = "HEALTH_MEDICATION",
                title = "Medication Started: ${med.medicineName}",
                description = "Dosage: ${med.dosage} ${med.unit} via ${med.administrationMethod}. Reason: ${med.reason.ifBlank { "Treatment" }}."
            )
        )
    }

    suspend fun updateMedication(med: MedicationRecordEntity) = withContext(Dispatchers.IO) {
        medicationDao.updateMedication(med)
    }

    suspend fun deleteMedication(id: Long) = withContext(Dispatchers.IO) {
        medicationDao.deleteMedication(id)
    }

    // ----------------------------------------------------
    // DEWORMING
    // ----------------------------------------------------
    val allDewormingFlow: Flow<List<DewormingRecordEntity>> = dewormingDao.getAllDewormingFlow()

    fun getDewormingByPigeonFlow(pigeonId: String): Flow<List<DewormingRecordEntity>> =
        dewormingDao.getDewormingByPigeonFlow(pigeonId)

    suspend fun recordDeworming(record: DewormingRecordEntity) = withContext(Dispatchers.IO) {
        dewormingDao.insertDeworming(record)
        eventDao.insertEvent(
            PigeonEventEntity(
                pigeonId = record.pigeonId,
                eventType = "HEALTH_DEWORMING",
                title = "Deworming Administered",
                description = "Medicine: ${record.medicineUsed} (${record.dosage}). Next due on ${java.text.SimpleDateFormat("dd MMM yyyy", Locale.US).format(record.nextDueDate)}."
            )
        )
        // Auto-create reminder for next due date
        reminderDao.insertReminder(
            ReminderEntity(
                reminderId = "REM-DEWORM-${UUID.randomUUID().toString().take(6)}",
                pigeonId = record.pigeonId,
                title = "Deworming Due for ${record.pigeonId}",
                description = "Follow up deworming (${record.medicineUsed})",
                category = "Deworming",
                dueDate = record.nextDueDate
            )
        )
    }

    suspend fun recordDewormingBulk(pigeonIds: List<String>, medicine: String, dosage: String, nextDueDays: Int = 90) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val nextDue = now + (nextDueDays.toLong() * 24 * 3600 * 1000L)
        val records = pigeonIds.map { id ->
            DewormingRecordEntity(
                pigeonId = id,
                date = now,
                medicineUsed = medicine,
                dosage = dosage,
                nextDueDate = nextDue,
                notes = "Bulk loft deworming"
            )
        }
        dewormingDao.insertDewormingBulk(records)
        reminderDao.insertReminder(
            ReminderEntity(
                reminderId = "REM-DEWORM-BULK-${UUID.randomUUID().toString().take(6)}",
                title = "Loft Deworming Cycle Due",
                description = "Routine deworming cycle ($medicine)",
                category = "Deworming",
                dueDate = nextDue
            )
        )
    }

    suspend fun deleteDeworming(id: Long) = withContext(Dispatchers.IO) {
        dewormingDao.deleteDeworming(id)
    }

    // ----------------------------------------------------
    // SUPPLEMENTS & VITAMINS
    // ----------------------------------------------------
    val allSupplementsFlow: Flow<List<SupplementRecordEntity>> = supplementDao.getAllSupplementsFlow()

    fun getSupplementsByPigeonFlow(pigeonId: String): Flow<List<SupplementRecordEntity>> =
        supplementDao.getSupplementsByPigeonFlow(pigeonId)

    suspend fun addSupplement(record: SupplementRecordEntity) = withContext(Dispatchers.IO) {
        supplementDao.insertSupplement(record)
    }

    suspend fun updateSupplement(record: SupplementRecordEntity) = withContext(Dispatchers.IO) {
        supplementDao.updateSupplement(record)
    }

    suspend fun deleteSupplement(id: Long) = withContext(Dispatchers.IO) {
        supplementDao.deleteSupplement(id)
    }

    // ----------------------------------------------------
    // DISEASES & ILLNESSES
    // ----------------------------------------------------
    val allDiseasesFlow: Flow<List<DiseaseRecordEntity>> = diseaseDao.getAllDiseasesFlow()
    val activeDiseasesFlow: Flow<List<DiseaseRecordEntity>> = diseaseDao.getActiveDiseasesFlow()

    fun getDiseasesByPigeonFlow(pigeonId: String): Flow<List<DiseaseRecordEntity>> =
        diseaseDao.getDiseasesByPigeonFlow(pigeonId)

    suspend fun addDisease(record: DiseaseRecordEntity) = withContext(Dispatchers.IO) {
        diseaseDao.insertDisease(record)
        eventDao.insertEvent(
            PigeonEventEntity(
                pigeonId = record.pigeonId,
                eventType = "HEALTH_DISEASE",
                title = "Condition Detected: ${record.diseaseName}",
                description = "Severity: ${record.severity}. Symptoms: ${record.symptoms}. Treatment: ${record.treatment}."
            )
        )
    }

    suspend fun updateDisease(record: DiseaseRecordEntity) = withContext(Dispatchers.IO) {
        diseaseDao.updateDisease(record)
    }

    suspend fun deleteDisease(id: Long) = withContext(Dispatchers.IO) {
        diseaseDao.deleteDisease(id)
    }

    // ----------------------------------------------------
    // WEIGHT TRACKING
    // ----------------------------------------------------
    fun getWeightsByPigeonFlow(pigeonId: String): Flow<List<WeightRecordEntity>> =
        weightDao.getWeightsByPigeonFlow(pigeonId)

    suspend fun recordWeight(record: WeightRecordEntity) = withContext(Dispatchers.IO) {
        weightDao.insertWeight(record)
        eventDao.insertEvent(
            PigeonEventEntity(
                pigeonId = record.pigeonId,
                eventType = "HEALTH_WEIGHT",
                title = "Weight Logged: ${record.weightGrams}g",
                description = "Condition: ${record.condition}. ${record.notes}"
            )
        )
    }

    suspend fun deleteWeight(id: Long) = withContext(Dispatchers.IO) {
        weightDao.deleteWeight(id)
    }

    // ----------------------------------------------------
    // NUTRITION & FEEDING & INVENTORY
    // ----------------------------------------------------
    val allFoodsFlow: Flow<List<FoodEntity>> = nutritionDao.getAllFoodsFlow()
    val feedingRecordsFlow: Flow<List<FeedingRecordEntity>> = nutritionDao.getFeedingRecordsFlow()
    val inventoryFlow: Flow<List<FoodInventoryEntity>> = nutritionDao.getInventoryFlow()

    suspend fun addFood(food: FoodEntity) = withContext(Dispatchers.IO) {
        nutritionDao.insertFood(food)
    }

    suspend fun updateFood(food: FoodEntity) = withContext(Dispatchers.IO) {
        nutritionDao.updateFood(food)
    }

    suspend fun deleteFood(foodId: String) = withContext(Dispatchers.IO) {
        nutritionDao.deleteFood(foodId)
    }

    suspend fun logFeeding(record: FeedingRecordEntity) = withContext(Dispatchers.IO) {
        nutritionDao.insertFeeding(record)
    }

    suspend fun deleteFeeding(id: Long) = withContext(Dispatchers.IO) {
        nutritionDao.deleteFeeding(id)
    }

    suspend fun addInventory(item: FoodInventoryEntity) = withContext(Dispatchers.IO) {
        nutritionDao.insertInventory(item)
    }

    suspend fun updateInventory(item: FoodInventoryEntity) = withContext(Dispatchers.IO) {
        nutritionDao.updateInventory(item)
    }

    suspend fun deleteInventory(inventoryId: String) = withContext(Dispatchers.IO) {
        nutritionDao.deleteInventory(inventoryId)
    }

    // ----------------------------------------------------
    // REMINDERS
    // ----------------------------------------------------
    val activeRemindersFlow: Flow<List<ReminderEntity>> = reminderDao.getActiveRemindersFlow()
    val allRemindersFlow: Flow<List<ReminderEntity>> = reminderDao.getAllRemindersFlow()

    fun getRemindersByPigeonFlow(pigeonId: String): Flow<List<ReminderEntity>> =
        reminderDao.getRemindersByPigeonFlow(pigeonId)

    suspend fun createReminder(reminder: ReminderEntity) = withContext(Dispatchers.IO) {
        reminderDao.insertReminder(reminder)
    }

    suspend fun setReminderCompleted(reminderId: String, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        reminderDao.setCompleted(reminderId, isCompleted)
    }

    suspend fun deleteReminder(reminderId: String) = withContext(Dispatchers.IO) {
        reminderDao.deleteReminder(reminderId)
    }

    // ----------------------------------------------------
    // TAGS
    // ----------------------------------------------------
    val allTagsFlow: Flow<List<TagEntity>> = tagDao.getAllTagsFlow()

    fun getTagsForPigeonFlow(pigeonId: String): Flow<List<String>> =
        tagDao.getTagsForPigeonFlow(pigeonId)

    suspend fun createTag(name: String, colorHex: String) = withContext(Dispatchers.IO) {
        tagDao.insertTag(TagEntity(name = name.trim(), colorHex = colorHex, isSystem = false))
    }

    suspend fun deleteTag(name: String) = withContext(Dispatchers.IO) {
        tagDao.deleteTag(name)
    }

    suspend fun addTagToPigeon(pigeonId: String, tagName: String) = withContext(Dispatchers.IO) {
        tagDao.addTagToPigeon(PigeonTagEntity(pigeonId, tagName))
    }

    suspend fun addTagsToPigeonsBulk(pigeonIds: List<String>, tagName: String) = withContext(Dispatchers.IO) {
        val pairs = pigeonIds.map { PigeonTagEntity(it, tagName) }
        tagDao.addTagsToPigeonsBulk(pairs)
    }

    suspend fun removeTagFromPigeon(pigeonId: String, tagName: String) = withContext(Dispatchers.IO) {
        tagDao.removeTagFromPigeon(pigeonId, tagName)
    }
}
