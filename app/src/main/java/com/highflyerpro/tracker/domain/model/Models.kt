package com.highflyerpro.tracker.domain.model

import com.highflyerpro.tracker.data.local.entities.AchievementEntity
import com.highflyerpro.tracker.data.local.entities.BreedingRecordEntity
import com.highflyerpro.tracker.data.local.entities.FlightRecordEntity
import com.highflyerpro.tracker.data.local.entities.FlightSessionEntity
import com.highflyerpro.tracker.data.local.entities.GroupEntity
import com.highflyerpro.tracker.data.local.entities.GroupMemberEntity
import com.highflyerpro.tracker.data.local.entities.HealthRecordEntity
import com.highflyerpro.tracker.data.local.entities.NoteEntity
import com.highflyerpro.tracker.data.local.entities.PhotoEntity
import com.highflyerpro.tracker.data.local.entities.PigeonEntity
import com.highflyerpro.tracker.data.local.entities.PigeonEventEntity
import com.highflyerpro.tracker.data.local.entities.PointRuleEntity
import com.highflyerpro.tracker.data.local.entities.SeasonEntity
import com.highflyerpro.tracker.data.local.entities.TournamentEntity
import com.highflyerpro.tracker.data.local.entities.TournamentParticipantEntity
import com.highflyerpro.tracker.data.local.entities.TournamentResultEntity

data class PigeonWithStats(
    val pigeon: PigeonEntity,
    val totalFlights: Int = 0,
    val totalFlightMillis: Long = 0L,
    val averageFlightMillis: Long = 0L,
    val bestFlightMillis: Long = 0L,
    val worstFlightMillis: Long = 0L,
    val currentForm: String = "Good", // Excellent, Good, Average, Below Average, Poor, Declining
    val performanceScore: Int = 0, // 0 - 100
    val consistencyScore: Int = 0, // 0 - 100
    val lastFlightTimestamp: Long? = null,
    val lastFlightDurationMillis: Long? = null,
    val groups: List<GroupEntity> = emptyList(),
    val points: Int = 0,
    val rank: Int = 0
)

data class LeaderboardEntry(
    val rank: Int,
    val pigeonId: String,
    val name: String,
    val ringNumber: String,
    val photoUri: String = "",
    val flightsCount: Int = 0,
    val totalDurationMillis: Long = 0L,
    val averageDurationMillis: Long = 0L,
    val bestDurationMillis: Long = 0L,
    val points: Int = 0,
    val consistencyScore: Int = 0,
    val performanceScore: Int = 0
)

data class FlightSummary(
    val sessionId: String,
    val sessionName: String,
    val totalParticipants: Int,
    val landedCount: Int,
    val flyingCount: Int,
    val releaseTimestamp: Long,
    val firstLandingTimestamp: Long? = null,
    val lastLandingTimestamp: Long? = null,
    val longestDurationMillis: Long = 0L,
    val shortestDurationMillis: Long = 0L,
    val averageDurationMillis: Long = 0L,
    val totalDurationMillis: Long = 0L
)

data class PersonalRecords(
    val longestFlightEverMillis: Long = 0L,
    val bestWeeklyAverageMillis: Long = 0L,
    val bestMonthlyAverageMillis: Long = 0L,
    val mostFlightsInOneMonth: Int = 0,
    val longestFlightStreak: Int = 0,
    val mostConsecutiveTop3: Int = 0,
    val bestLandingRank: Int = 0
)

data class BackupData(
    val applicationName: String = "High Flyer Pro Tracker",
    val organizationName: String = "",
    val ownerName: String = "",
    val digitalSystemDeveloper: String = "High Flyer Pro Tracker",
    val location: String = "",
    val backupVersion: String = "1.0.0",
    val createdTimestamp: Long = System.currentTimeMillis(),
    val version: Int = 1,
    val exportTimestamp: Long = System.currentTimeMillis(),
    val pigeons: List<PigeonEntity> = emptyList(),
    val flightSessions: List<FlightSessionEntity> = emptyList(),
    val flightRecords: List<FlightRecordEntity> = emptyList(),
    val pigeonEvents: List<PigeonEventEntity> = emptyList(),
    val groups: List<GroupEntity> = emptyList(),
    val groupMembers: List<GroupMemberEntity> = emptyList(),
    val healthRecords: List<HealthRecordEntity> = emptyList(),
    val breedingRecords: List<BreedingRecordEntity> = emptyList(),
    val photos: List<PhotoEntity> = emptyList(),
    val notes: List<NoteEntity> = emptyList(),
    val achievements: List<AchievementEntity> = emptyList(),
    val seasons: List<SeasonEntity> = emptyList(),
    val tournaments: List<TournamentEntity> = emptyList(),
    val tournamentParticipants: List<TournamentParticipantEntity> = emptyList(),
    val tournamentResults: List<TournamentResultEntity> = emptyList(),
    val pointRules: List<PointRuleEntity> = emptyList()
)
