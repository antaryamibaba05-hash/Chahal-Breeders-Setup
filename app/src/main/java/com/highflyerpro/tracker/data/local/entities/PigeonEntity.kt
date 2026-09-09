package com.highflyerpro.tracker.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pigeons",
    indices = [
        Index(value = ["pigeonId"], unique = true),
        Index(value = ["ringNumber"]),
        Index(value = ["status"]),
        Index(value = ["isArchived"]),
        Index(value = ["inTrash"]),
        Index(value = ["isFavorite"]),
        Index(value = ["isPinned"])
    ]
)
data class PigeonEntity(
    @PrimaryKey val pigeonId: String, // e.g. PIGEON-2026-000001
    val name: String,
    val nickname: String = "",
    val ringNumber: String = "",
    val ringYear: String = "",
    val color: String = "",
    val markings: String = "",
    val gender: String = "Male", // Male, Female, Unknown
    val breed: String = "High Flyer", // Pakistani High Flyer, Tipler, Teddy, Kamagar, Sialkoti, Kasuri, etc.
    val birthDate: Long? = null, // epoch millis
    val acquisitionDate: Long? = null,
    val fatherId: String? = null,
    val motherId: String? = null,
    val breeder: String = "",
    val source: String = "",
    val purchasePrice: Double? = null,
    val currentValue: Double? = null,
    val status: String = "Active", // ACTIVE, FLYING, TRAINING, BREEDER, YOUNG, RESTING, RECOVERY, SICK, INJURED, RETIRED, ARCHIVED, SOLD, GIFTED, LOST, DECEASED, ACHIEVED
    val retirementType: String = "", // Retired from Flying, Retired from Breeding, Fully Retired, Retired Champion
    val notes: String = "",
    val photoUri: String = "",
    val isArchived: Boolean = false,
    val isFavorite: Boolean = false,
    val isPinned: Boolean = false,
    val inTrash: Boolean = false,
    val trashDate: Long? = null,
    val isAchieved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
