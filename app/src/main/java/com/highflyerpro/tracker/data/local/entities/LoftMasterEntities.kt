package com.highflyerpro.tracker.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a designated breeding pair in the loft.
 */
@Entity(
    tableName = "breeding_pairs",
    indices = [
        Index(value = ["pairId"], unique = true),
        Index(value = ["maleId"]),
        Index(value = ["femaleId"]),
        Index(value = ["status"])
    ]
)
data class BreedingPairEntity(
    @PrimaryKey val pairId: String, // e.g. PAIR-2026-000001
    val maleId: String,
    val femaleId: String,
    val pairName: String = "",
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val status: String = "Active", // Active, Resting, Separated, Retired
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Represents a breeding event/clutch in a nest box.
 */
@Entity(
    tableName = "breeding_events",
    indices = [
        Index(value = ["eventId"], unique = true),
        Index(value = ["pairId"]),
        Index(value = ["maleId"]),
        Index(value = ["femaleId"]),
        Index(value = ["date"])
    ]
)
data class BreedingEventEntity(
    @PrimaryKey val eventId: String, // e.g. BR-EVT-2026-000001
    val pairId: String,
    val maleId: String,
    val femaleId: String,
    val date: Long = System.currentTimeMillis(),
    val nestNumber: String = "",
    val egg1Date: Long? = null,
    val egg2Date: Long? = null,
    val expectedHatchDate: Long? = null,
    val actualHatchDate: Long? = null,
    val eggStatus: String = "Fertile", // Fertile, Infertile, Broken, Lost, Hatched
    val youngPigeonId1: String? = null,
    val youngPigeonId2: String? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Medication tracking record.
 */
@Entity(
    tableName = "medication_records",
    indices = [
        Index(value = ["pigeonId"]),
        Index(value = ["status"]),
        Index(value = ["startDate"])
    ]
)
data class MedicationRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pigeonId: String,
    val medicineName: String,
    val genericName: String = "",
    val purpose: String = "",
    val dosage: String = "",
    val unit: String = "ml", // ml, mg, drops, tablet
    val administrationMethod: String = "Drinking Water", // Drinking Water, Crop Tube, Oral Tablet, Injection, Eye Drop
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val frequency: String = "Once Daily", // Once Daily, Twice Daily, Weekly
    val reason: String = "",
    val prescribedBy: String = "",
    val notes: String = "",
    val status: String = "Active", // Active, Completed, Stopped
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Dedicated Deworming record with due date automation.
 */
@Entity(
    tableName = "deworming_records",
    indices = [
        Index(value = ["pigeonId"]),
        Index(value = ["date"]),
        Index(value = ["nextDueDate"])
    ]
)
data class DewormingRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pigeonId: String,
    val date: Long = System.currentTimeMillis(),
    val medicineUsed: String,
    val dosage: String = "",
    val nextDueDate: Long = System.currentTimeMillis() + (90L * 24 * 60 * 60 * 1000L), // default ~3 months
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Vitamins & Supplements tracking entity.
 */
@Entity(
    tableName = "supplement_records",
    indices = [
        Index(value = ["pigeonId"]),
        Index(value = ["category"]),
        Index(value = ["startDate"])
    ]
)
data class SupplementRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pigeonId: String, // Can be "ALL_LOFT" or groupId or individual pigeonId
    val supplementName: String,
    val category: String = "Multivitamin", // Multivitamin, Vitamin B Complex, Vitamin E, Vitamin D, Calcium, Minerals, Electrolytes, Probiotics, Amino Acids, Fish Oil, Energy Supplement, Recovery Supplement, Custom
    val dosage: String = "",
    val unit: String = "g/L",
    val frequency: String = "Daily",
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val reason: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Nutrition: Food mixtures and formulations.
 */
@Entity(
    tableName = "food_mixtures",
    indices = [
        Index(value = ["foodId"], unique = true)
    ]
)
data class FoodEntity(
    @PrimaryKey val foodId: String, // FOOD-2026-000001
    val name: String,
    val ingredients: String = "",
    val proteinPercentage: Double? = null,
    val energyKcal: Double? = null,
    val purpose: String = "Maintenance", // Flying, Breeding, Young, Recovery, Molting, Maintenance
    val season: String = "All Season",
    val suitableFor: String = "All Birds",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Feeding schedule & logs.
 */
@Entity(
    tableName = "feeding_records",
    indices = [
        Index(value = ["foodId"]),
        Index(value = ["date"])
    ]
)
data class FeedingRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val foodId: String,
    val foodName: String,
    val quantityGrams: Double,
    val date: Long = System.currentTimeMillis(),
    val timeSlot: String = "Morning", // Morning, Evening, Custom
    val targetType: String = "Whole Loft", // Whole Loft, Group, Individual
    val targetId: String = "", // groupId or pigeonId if targeted
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Food & Grain inventory.
 */
@Entity(
    tableName = "food_inventory",
    indices = [
        Index(value = ["inventoryId"], unique = true)
    ]
)
data class FoodInventoryEntity(
    @PrimaryKey val inventoryId: String, // INV-2026-000001
    val foodName: String,
    val currentQuantity: Double,
    val unit: String = "kg", // kg, lbs, bags
    val minimumStock: Double = 5.0,
    val purchaseDate: Long? = null,
    val expiryDate: Long? = null,
    val supplier: String = "",
    val cost: Double? = null,
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Disease and illness record.
 */
@Entity(
    tableName = "disease_records",
    indices = [
        Index(value = ["pigeonId"]),
        Index(value = ["status"]),
        Index(value = ["dateDetected"])
    ]
)
data class DiseaseRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pigeonId: String,
    val diseaseName: String,
    val symptoms: String = "",
    val dateDetected: Long = System.currentTimeMillis(),
    val severity: String = "Mild", // Mild, Moderate, Severe, Critical
    val affectedGroup: String = "",
    val diagnosis: String = "",
    val treatment: String = "",
    val medication: String = "",
    val recoveryDate: Long? = null,
    val outcome: String = "Under Observation", // Under Observation, Under Treatment, Recovered, Chronic, Deceased
    val notes: String = "",
    val status: String = "Active", // Active, Resolved
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Weight Tracking Entity.
 */
@Entity(
    tableName = "weight_records",
    indices = [
        Index(value = ["pigeonId"]),
        Index(value = ["date"])
    ]
)
data class WeightRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pigeonId: String,
    val date: Long = System.currentTimeMillis(),
    val weightGrams: Double,
    val condition: String = "Prime", // Lean, Prime, Heavy, Underweight
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Local reminders and alerts.
 */
@Entity(
    tableName = "reminders",
    indices = [
        Index(value = ["reminderId"], unique = true),
        Index(value = ["pigeonId"]),
        Index(value = ["dueDate"]),
        Index(value = ["isCompleted"])
    ]
)
data class ReminderEntity(
    @PrimaryKey val reminderId: String, // REM-2026-000001
    val pigeonId: String? = null, // Optional target pigeon
    val title: String,
    val description: String = "",
    val category: String = "General", // Deworming, Medication, Vaccination, Health Check, Supplement, Breeding Check, Egg Hatch Date, Food Stock, Custom
    val dueDate: Long,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Tag master entity.
 */
@Entity(
    tableName = "tags",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class TagEntity(
    @PrimaryKey val name: String,
    val colorHex: String = "#1E88E5",
    val isSystem: Boolean = false
)

/**
 * Cross-reference for pigeon tags.
 */
@Entity(
    tableName = "pigeon_tags",
    primaryKeys = ["pigeonId", "tagName"],
    indices = [
        Index(value = ["pigeonId"]),
        Index(value = ["tagName"])
    ]
)
data class PigeonTagEntity(
    val pigeonId: String,
    val tagName: String
)
