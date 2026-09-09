package com.highflyerpro.tracker.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "flight_records",
    indices = [
        Index(value = ["sessionId"]),
        Index(value = ["pigeonId"]),
        Index(value = ["status"]),
        Index(value = ["landingTimestamp"]),
        Index(value = ["landingPosition"])
    ]
)
data class FlightRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String? = null,
    val pigeonId: String,
    val pigeonName: String,
    val pigeonRingNumber: String,
    val releaseTimestamp: Long,
    val landingTimestamp: Long? = null,
    val durationMillis: Long? = null,
    val landingPosition: Int? = null, // 1, 2, 3...
    val status: String = "FLYING", // FLYING, LANDED, DID NOT FLY, MISSING, INJURED, DISQUALIFIED
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
