package com.highflyerpro.tracker.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "health_records",
    indices = [
        Index(value = ["pigeonId"]),
        Index(value = ["date"])
    ]
)
data class HealthRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pigeonId: String,
    val date: Long = System.currentTimeMillis(),
    val type: String, // General Check, Injury, Treatment, Supplement, Vaccination, Recovery, Other
    val title: String,
    val description: String = "",
    val status: String = "Resolved", // Active, Under Treatment, Recovering, Resolved
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "breeding_records",
    indices = [
        Index(value = ["pigeonId"]),
        Index(value = ["partnerId"]),
        Index(value = ["date"])
    ]
)
data class BreedingRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pigeonId: String,
    val partnerId: String? = null,
    val partnerName: String = "",
    val date: Long = System.currentTimeMillis(),
    val eggCount: Int = 2,
    val offspringCount: Int = 0,
    val offspringIds: String = "", // Comma separated pigeon IDs
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "photos",
    indices = [
        Index(value = ["pigeonId"]),
        Index(value = ["isProfilePhoto"]),
        Index(value = ["dateAdded"])
    ]
)
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pigeonId: String,
    val photoUri: String = "",
    val filePath: String = "",
    val thumbnailPath: String? = null,
    val category: String = "Profile", // Profile, Young, Current, Training, Flight, Achievement, Breeding, Other
    val caption: String = "",
    val isProfilePhoto: Boolean = false,
    val dateAdded: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
) {
    val photoId: Long
        get() = id

    val effectivePath: String
        get() = filePath.ifBlank { photoUri }

    val effectiveThumbnail: String
        get() = thumbnailPath?.ifBlank { null } ?: effectivePath
}

object PhotoCategories {
    const val PROFILE = "Profile"
    const val YOUNG = "Young"
    const val CURRENT = "Current"
    const val TRAINING = "Training"
    const val FLIGHT = "Flight"
    const val ACHIEVEMENT = "Achievement"
    const val BREEDING = "Breeding"
    const val OTHER = "Other"

    val ALL = listOf(PROFILE, YOUNG, CURRENT, TRAINING, FLIGHT, ACHIEVEMENT, BREEDING, OTHER)
}

typealias PigeonPhotoEntity = PhotoEntity

@Entity(
    tableName = "notes",
    indices = [
        Index(value = ["pigeonId"])
    ]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pigeonId: String,
    val title: String = "",
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "achievements",
    indices = [
        Index(value = ["pigeonId"]),
        Index(value = ["achievementKey"])
    ]
)
data class AchievementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pigeonId: String,
    val achievementKey: String, // FIRST_FLIGHT, TEN_FLIGHTS, FIFTY_FLIGHTS, HUNDRED_FLIGHTS, TEN_HOUR_CLUB, CHAMPION, TOP_THREE_STREAK, CONSISTENT_FLYER, MOST_IMPROVED
    val title: String,
    val description: String,
    val unlockedAt: Long = System.currentTimeMillis(),
    val badgeIcon: String = "trophy"
)
