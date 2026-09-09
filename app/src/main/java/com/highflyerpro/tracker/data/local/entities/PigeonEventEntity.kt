package com.highflyerpro.tracker.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pigeon_events",
    indices = [
        Index(value = ["pigeonId"]),
        Index(value = ["timestamp"])
    ]
)
data class PigeonEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pigeonId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String, // PIGEON_CREATED, PIGEON_EDITED, STATUS_CHANGED, FLIGHT_STARTED, FLIGHT_COMPLETED, FLIGHT_CORRECTED, PERSONAL_RECORD, TOURNAMENT_JOINED, TOURNAMENT_WON, HEALTH_RECORD, BREEDING_EVENT, ACHIEVEMENT_UNLOCKED, PHOTO_ADDED, NOTE_ADDED
    val title: String,
    val description: String,
    val relatedRecordId: String? = null
)
