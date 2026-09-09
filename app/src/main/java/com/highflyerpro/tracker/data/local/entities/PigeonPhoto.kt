package com.highflyerpro.tracker.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "pigeon_photos",
    foreignKeys = [
        ForeignKey(
            entity = PigeonEntity::class,
            parentColumns = ["pigeonId"],
            childColumns = ["pigeonId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["pigeonId"]),
        Index(value = ["isProfilePhoto"]),
        Index(value = ["dateAdded"])
    ]
)
data class PigeonPhoto(
    @PrimaryKey
    val photoId: String = UUID.randomUUID().toString(),
    val pigeonId: String,
    val filePath: String,
    val category: String = "Profile",
    val caption: String? = null,
    val dateAdded: Long = System.currentTimeMillis(),
    val isProfilePhoto: Boolean = false
)
