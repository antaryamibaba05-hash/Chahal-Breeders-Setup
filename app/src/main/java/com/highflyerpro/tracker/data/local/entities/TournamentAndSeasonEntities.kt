package com.highflyerpro.tracker.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "seasons",
    indices = [
        Index(value = ["seasonId"], unique = true)
    ]
)
data class SeasonEntity(
    @PrimaryKey val seasonId: String,
    val name: String,
    val startDate: Long,
    val endDate: Long,
    val description: String = "",
    val status: String = "Active", // Active, Completed, Archived
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "tournaments",
    indices = [
        Index(value = ["tournamentId"], unique = true),
        Index(value = ["seasonId"])
    ]
)
data class TournamentEntity(
    @PrimaryKey val tournamentId: String,
    val seasonId: String? = null,
    val name: String,
    val location: String = "",
    val startDate: Long,
    val endDate: Long,
    val description: String = "",
    val type: String = "Single Day", // Single Day, Multi Day, Multi Session
    val rules: String = "",
    val scoringSystem: String = "Total Hours",
    val status: String = "Upcoming", // Upcoming, Active, Completed, Archived
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "tournament_participants",
    primaryKeys = ["tournamentId", "pigeonId"],
    indices = [
        Index(value = ["tournamentId"]),
        Index(value = ["pigeonId"])
    ]
)
data class TournamentParticipantEntity(
    val tournamentId: String,
    val pigeonId: String,
    val registeredAt: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(
    tableName = "tournament_results",
    indices = [
        Index(value = ["tournamentId"]),
        Index(value = ["pigeonId"])
    ]
)
data class TournamentResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tournamentId: String,
    val pigeonId: String,
    val pigeonName: String,
    val totalFlightHours: Double = 0.0,
    val points: Int = 0,
    val rank: Int = 0,
    val notes: String = ""
)

@Entity(tableName = "point_rules")
data class PointRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val minHours: Double,
    val maxHours: Double,
    val points: Int,
    val label: String
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val key: String,
    val value: String
)
