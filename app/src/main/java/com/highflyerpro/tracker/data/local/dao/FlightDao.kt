package com.highflyerpro.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.highflyerpro.tracker.data.local.entities.FlightRecordEntity
import com.highflyerpro.tracker.data.local.entities.FlightSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FlightDao {
    // Flight Sessions
    @Query("SELECT * FROM flight_sessions ORDER BY releaseTimestamp DESC")
    fun getAllSessionsFlow(): Flow<List<FlightSessionEntity>>

    @Query("SELECT * FROM flight_sessions WHERE status = 'ACTIVE' LIMIT 1")
    fun getActiveSessionFlow(): Flow<FlightSessionEntity?>

    @Query("SELECT * FROM flight_sessions WHERE status = 'ACTIVE' LIMIT 1")
    suspend fun getActiveSession(): FlightSessionEntity?

    @Query("SELECT * FROM flight_sessions WHERE sessionId = :sessionId LIMIT 1")
    fun getSessionByIdFlow(sessionId: String): Flow<FlightSessionEntity?>

    @Query("SELECT * FROM flight_sessions WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): FlightSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FlightSessionEntity)

    @Update
    suspend fun updateSession(session: FlightSessionEntity)

    @Query("UPDATE flight_sessions SET status = :status, completedTimestamp = :completedTime WHERE sessionId = :sessionId")
    suspend fun updateSessionStatus(sessionId: String, status: String, completedTime: Long? = null)

    @Query("UPDATE flight_sessions SET totalParticipants = :total, landedCount = :landed, flyingCount = :flying WHERE sessionId = :sessionId")
    suspend fun updateSessionCounts(sessionId: String, total: Int, landed: Int, flying: Int)

    // Flight Records
    @Query("SELECT * FROM flight_records WHERE sessionId = :sessionId ORDER BY CASE WHEN landingPosition IS NULL THEN 1 ELSE 0 END, landingPosition ASC, releaseTimestamp DESC")
    fun getRecordsBySessionFlow(sessionId: String): Flow<List<FlightRecordEntity>>

    @Query("SELECT * FROM flight_records WHERE sessionId = :sessionId ORDER BY CASE WHEN landingPosition IS NULL THEN 1 ELSE 0 END, landingPosition ASC, releaseTimestamp DESC")
    suspend fun getRecordsBySession(sessionId: String): List<FlightRecordEntity>

    @Query("SELECT * FROM flight_records WHERE pigeonId = :pigeonId ORDER BY releaseTimestamp DESC")
    fun getRecordsByPigeonFlow(pigeonId: String): Flow<List<FlightRecordEntity>>

    @Query("SELECT * FROM flight_records WHERE pigeonId = :pigeonId ORDER BY releaseTimestamp DESC")
    suspend fun getRecordsByPigeon(pigeonId: String): List<FlightRecordEntity>

    @Query("SELECT * FROM flight_records WHERE status = 'LANDED' ORDER BY durationMillis DESC")
    fun getAllLandedRecordsFlow(): Flow<List<FlightRecordEntity>>

    @Query("SELECT * FROM flight_records ORDER BY releaseTimestamp DESC")
    fun getAllRecordsFlow(): Flow<List<FlightRecordEntity>>

    @Query("SELECT * FROM flight_records ORDER BY releaseTimestamp DESC")
    suspend fun getAllRecords(): List<FlightRecordEntity>

    @Query("SELECT * FROM flight_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: Long): FlightRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: FlightRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<FlightRecordEntity>)

    @Update
    suspend fun updateRecord(record: FlightRecordEntity)

    @Query("DELETE FROM flight_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("DELETE FROM flight_records WHERE sessionId = :sessionId")
    suspend fun deleteRecordsBySession(sessionId: String)

    @androidx.room.Delete
    suspend fun deleteSession(session: FlightSessionEntity)

    @Transaction
    suspend fun recalculateLandingPositions(sessionId: String?) {
        if (sessionId == null) return
        val records = getRecordsBySession(sessionId)
        val landedRecords = records.filter { it.status == "LANDED" && it.landingTimestamp != null }
            .sortedBy { it.landingTimestamp }

        var currentPosition = 1
        for (record in landedRecords) {
            val updated = record.copy(landingPosition = currentPosition)
            updateRecord(updated)
            currentPosition++
        }

        // For non-landed records, ensure landingPosition is null
        val nonLanded = records.filter { it.status != "LANDED" }
        for (record in nonLanded) {
            if (record.landingPosition != null) {
                updateRecord(record.copy(landingPosition = null))
            }
        }

        // Update session counts
        val flyingCount = records.count { it.status == "FLYING" }
        val landedCount = landedRecords.size
        updateSessionCounts(sessionId, records.size, landedCount, flyingCount)
    }
}
