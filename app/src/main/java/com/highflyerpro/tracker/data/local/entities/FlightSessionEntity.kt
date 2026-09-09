package com.highflyerpro.tracker.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "flight_sessions",
    indices = [
        Index(value = ["sessionId"], unique = true),
        Index(value = ["status"]),
        Index(value = ["releaseTimestamp"])
    ]
)
data class FlightSessionEntity(
    @PrimaryKey val sessionId: String, // e.g. SESSION-20260908-123456
    val name: String,
    val date: Long, // date epoch (start of day or session release)
    val releaseTimestamp: Long, // exact release epoch millis
    val notes: String = "",
    val status: String = "ACTIVE", // ACTIVE, COMPLETED, CANCELLED
    val totalParticipants: Int = 0,
    val landedCount: Int = 0,
    val flyingCount: Int = 0,
    val completedTimestamp: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val eventType: String = "Training", // Training, Practice, Competition, Tournament, Cup, Personal Record, Custom
    val location: String = "",
    val rules: String = "",
    val description: String = "",
    val isArchived: Boolean = false
)
