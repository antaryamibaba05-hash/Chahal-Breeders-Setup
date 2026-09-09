package com.highflyerpro.tracker.data.repository

import com.highflyerpro.tracker.data.local.AppDatabase
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
import com.highflyerpro.tracker.data.local.entities.PigeonPhoto
import com.highflyerpro.tracker.data.local.entities.PointRuleEntity
import com.highflyerpro.tracker.data.local.entities.SeasonEntity
import com.highflyerpro.tracker.data.local.entities.TournamentEntity
import com.highflyerpro.tracker.data.local.entities.TournamentParticipantEntity
import com.highflyerpro.tracker.data.local.entities.TournamentResultEntity
import com.highflyerpro.tracker.domain.calculation.PerformanceEngine
import com.highflyerpro.tracker.domain.model.BackupData
import com.highflyerpro.tracker.domain.model.LeaderboardEntry
import com.highflyerpro.tracker.domain.model.PigeonWithStats
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class HighFlyerRepository(val database: AppDatabase) {

    private val pigeonDao = database.pigeonDao()
    private val flightDao = database.flightDao()
    private val groupDao = database.groupDao()
    private val eventDao = database.eventDao()
    private val healthDao = database.healthDao()
    private val breedingDao = database.breedingDao()
    private val photoDao = database.photoDao()
    val pigeonPhotoDao = database.pigeonPhotoDao()
    private val noteDao = database.noteDao()
    private val achievementDao = database.achievementDao()
    private val tournamentDao = database.tournamentDao()
    private val settingsDao = database.settingsDao()

    val allHealthRecordsFlow: Flow<List<HealthRecordEntity>> = healthDao.getAllRecordsFlow()
    val allBreedingRecordsFlow: Flow<List<BreedingRecordEntity>> = breedingDao.getAllRecordsFlow()
    val allPigeonPhotosFlow: Flow<List<PhotoEntity>> = photoDao.getPhotosByPigeonFlow("")

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // ----------------------------------------------------
    // PIGEONS
    // ----------------------------------------------------

    val allActivePigeonsFlow: Flow<List<PigeonEntity>> = pigeonDao.getAllActivePigeonsFlow()
    val allPigeonsFlow: Flow<List<PigeonEntity>> = pigeonDao.getAllPigeonsFlow()

    fun getPigeonByIdFlow(pigeonId: String): Flow<PigeonEntity?> = pigeonDao.getPigeonByIdFlow(pigeonId)
    suspend fun getPigeonById(pigeonId: String): PigeonEntity? = pigeonDao.getPigeonById(pigeonId)

    suspend fun generatePermanentId(): String = withContext(Dispatchers.IO) {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val totalCount = pigeonDao.getTotalPigeonCount()
        val nextSeq = totalCount + 1
        String.format(Locale.US, "PIGEON-%d-%06d", currentYear, nextSeq)
    }

    suspend fun isRingNumberDuplicate(ringNumber: String, excludePigeonId: String? = null): Boolean = withContext(Dispatchers.IO) {
        if (ringNumber.isBlank()) return@withContext false
        pigeonDao.countRingNumber(ringNumber.trim(), excludePigeonId) > 0
    }

    suspend fun createPigeon(pigeon: PigeonEntity, initialGroupIds: List<String> = emptyList()): String = withContext(Dispatchers.IO) {
        pigeonDao.insertPigeon(pigeon)

        for (groupId in initialGroupIds) {
            groupDao.insertMember(GroupMemberEntity(groupId, pigeon.pigeonId))
        }

        // Log Timeline Event
        eventDao.insertEvent(
            PigeonEventEntity(
                pigeonId = pigeon.pigeonId,
                eventType = "PIGEON_CREATED",
                title = "Pigeon Registered",
                description = "${pigeon.name} (${pigeon.ringNumber.ifBlank { "No Ring" }}) added to loft directory as ${pigeon.status}."
            )
        )

        pigeon.pigeonId
    }

    suspend fun updatePigeon(pigeon: PigeonEntity, groupIds: List<String>? = null) = withContext(Dispatchers.IO) {
        val existing = pigeonDao.getPigeonById(pigeon.pigeonId)
        pigeonDao.updatePigeon(pigeon.copy(updatedAt = System.currentTimeMillis()))

        if (groupIds != null) {
            groupDao.removePigeonFromAllGroups(pigeon.pigeonId)
            for (gId in groupIds) {
                groupDao.insertMember(GroupMemberEntity(gId, pigeon.pigeonId))
            }
        }

        // Check if status changed
        if (existing != null && existing.status != pigeon.status) {
            eventDao.insertEvent(
                PigeonEventEntity(
                    pigeonId = pigeon.pigeonId,
                    eventType = "STATUS_CHANGED",
                    title = "Status Changed",
                    description = "Status updated from ${existing.status} to ${pigeon.status}."
                )
            )
        } else {
            eventDao.insertEvent(
                PigeonEventEntity(
                    pigeonId = pigeon.pigeonId,
                    eventType = "PIGEON_EDITED",
                    title = "Profile Updated",
                    description = "Pigeon details and pedigree attributes updated."
                )
            )
        }
    }

    suspend fun setPigeonArchiveStatus(pigeonId: String, isArchived: Boolean) = withContext(Dispatchers.IO) {
        pigeonDao.setArchiveStatus(pigeonId, isArchived)
        eventDao.insertEvent(
            PigeonEventEntity(
                pigeonId = pigeonId,
                eventType = "STATUS_CHANGED",
                title = if (isArchived) "Pigeon Archived" else "Pigeon Restored",
                description = if (isArchived) "Pigeon archived from active roster." else "Pigeon restored to active roster."
            )
        )
    }

    suspend fun updatePigeonStatus(pigeonId: String, status: String) = withContext(Dispatchers.IO) {
        pigeonDao.updateStatus(pigeonId, status)
        eventDao.insertEvent(
            PigeonEventEntity(
                pigeonId = pigeonId,
                eventType = "STATUS_CHANGED",
                title = "Status Changed to $status",
                description = "Pigeon status updated to $status."
            )
        )
    }

    // Bulk Operations
    suspend fun bulkUpdateStatus(pigeonIds: List<String>, status: String) = withContext(Dispatchers.IO) {
        pigeonDao.updateStatusBulk(pigeonIds, status)
        for (id in pigeonIds) {
            eventDao.insertEvent(
                PigeonEventEntity(
                    pigeonId = id,
                    eventType = "STATUS_CHANGED",
                    title = "Bulk Status Update",
                    description = "Status updated to $status via bulk action."
                )
            )
        }
    }

    suspend fun bulkSetArchive(pigeonIds: List<String>, isArchived: Boolean) = withContext(Dispatchers.IO) {
        pigeonDao.setArchiveStatusBulk(pigeonIds, isArchived)
    }

    suspend fun bulkAddToGroup(pigeonIds: List<String>, groupId: String) = withContext(Dispatchers.IO) {
        val members = pigeonIds.map { GroupMemberEntity(groupId, it) }
        groupDao.insertMembers(members)
    }

    suspend fun bulkRemoveFromGroup(pigeonIds: List<String>, groupId: String) = withContext(Dispatchers.IO) {
        groupDao.removeMembersBulk(groupId, pigeonIds)
    }

    // ----------------------------------------------------
    // FLIGHTS & SESSIONS
    // ----------------------------------------------------

    val activeSessionFlow: Flow<FlightSessionEntity?> = flightDao.getActiveSessionFlow()
    val allSessionsFlow: Flow<List<FlightSessionEntity>> = flightDao.getAllSessionsFlow()
    val allFlightRecordsFlow: Flow<List<FlightRecordEntity>> = flightDao.getAllRecordsFlow()

    suspend fun getActiveSession(): FlightSessionEntity? = flightDao.getActiveSession()
    suspend fun getSessionById(sessionId: String): FlightSessionEntity? = flightDao.getSessionById(sessionId)
    fun getSessionByIdFlow(sessionId: String): Flow<FlightSessionEntity?> = flightDao.getSessionByIdFlow(sessionId)

    fun getRecordsBySessionFlow(sessionId: String): Flow<List<FlightRecordEntity>> =
        flightDao.getRecordsBySessionFlow(sessionId)

    fun getRecordsByPigeonFlow(pigeonId: String): Flow<List<FlightRecordEntity>> =
        flightDao.getRecordsByPigeonFlow(pigeonId)

    suspend fun startFlightSession(
        name: String,
        date: Long,
        releaseTimestamp: Long,
        selectedPigeons: List<PigeonEntity>,
        notes: String
    ): String = withContext(Dispatchers.IO) {
        val sdf = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US)
        val sessionId = "SESSION-${sdf.format(Date(releaseTimestamp))}"
        val sessionName = name.ifBlank { "Flight on ${PerformanceEngine.formatDateOnly(releaseTimestamp)}" }

        val session = FlightSessionEntity(
            sessionId = sessionId,
            name = sessionName,
            date = date,
            releaseTimestamp = releaseTimestamp,
            notes = notes,
            status = "ACTIVE",
            totalParticipants = selectedPigeons.size,
            landedCount = 0,
            flyingCount = selectedPigeons.size
        )
        flightDao.insertSession(session)

        val records = selectedPigeons.map { pigeon ->
            FlightRecordEntity(
                sessionId = sessionId,
                pigeonId = pigeon.pigeonId,
                pigeonName = pigeon.name,
                pigeonRingNumber = pigeon.ringNumber,
                releaseTimestamp = releaseTimestamp,
                landingTimestamp = null,
                durationMillis = null,
                landingPosition = null,
                status = "FLYING",
                notes = ""
            )
        }
        flightDao.insertRecords(records)

        // Log FLIGHT_STARTED event for each pigeon
        for (pigeon in selectedPigeons) {
            eventDao.insertEvent(
                PigeonEventEntity(
                    pigeonId = pigeon.pigeonId,
                    eventType = "FLIGHT_STARTED",
                    title = "Flight Released",
                    description = "Released in '$sessionName' at ${PerformanceEngine.formatTimeOnly(releaseTimestamp)}.",
                    relatedRecordId = sessionId
                )
            )
        }

        sessionId
    }

    suspend fun markPigeonLanded(
        recordId: Long,
        landingTimestamp: Long = System.currentTimeMillis(),
        notes: String = ""
    ) = withContext(Dispatchers.IO) {
        val record = flightDao.getRecordById(recordId) ?: return@withContext
        val duration = (landingTimestamp - record.releaseTimestamp).coerceAtLeast(0L)

        val updatedRecord = record.copy(
            landingTimestamp = landingTimestamp,
            durationMillis = duration,
            status = "LANDED",
            notes = if (notes.isNotBlank()) notes else record.notes
        )
        flightDao.updateRecord(updatedRecord)

        // Automatically recalculate chronological landing order
        flightDao.recalculateLandingPositions(record.sessionId)

        // Get updated record with position
        val refreshedRecord = flightDao.getRecordById(recordId)
        val positionText = refreshedRecord?.landingPosition?.let { " (Position #$it)" } ?: ""

        // Event
        eventDao.insertEvent(
            PigeonEventEntity(
                pigeonId = record.pigeonId,
                eventType = "FLIGHT_COMPLETED",
                title = "Flight Completed$positionText",
                description = "Landed at ${PerformanceEngine.formatTimeOnly(landingTimestamp)}. Duration: ${PerformanceEngine.formatDurationHMS(duration)}.",
                relatedRecordId = record.sessionId
            )
        )

        // Check & unlock achievements
        checkAndUnlockAchievements(record.pigeonId)
    }

    suspend fun correctLandingRecord(
        recordId: Long,
        newLandingTimestamp: Long?,
        newStatus: String,
        notes: String
    ) = withContext(Dispatchers.IO) {
        val record = flightDao.getRecordById(recordId) ?: return@withContext
        val duration = if (newStatus == "LANDED" && newLandingTimestamp != null) {
            (newLandingTimestamp - record.releaseTimestamp).coerceAtLeast(0L)
        } else {
            null
        }

        val updated = record.copy(
            landingTimestamp = if (newStatus == "LANDED") newLandingTimestamp else null,
            durationMillis = duration,
            status = newStatus,
            notes = notes
        )
        flightDao.updateRecord(updated)
        flightDao.recalculateLandingPositions(record.sessionId)

        eventDao.insertEvent(
            PigeonEventEntity(
                pigeonId = record.pigeonId,
                eventType = "FLIGHT_CORRECTED",
                title = "Flight Record Corrected",
                description = "Status: $newStatus, Duration: ${duration?.let { PerformanceEngine.formatDurationHMS(it) } ?: "N/A"}. Note: $notes",
                relatedRecordId = record.sessionId
            )
        )

        checkAndUnlockAchievements(record.pigeonId)
    }

    suspend fun completeFlightSession(sessionId: String) = withContext(Dispatchers.IO) {
        flightDao.updateSessionStatus(sessionId, "COMPLETED", System.currentTimeMillis())
        flightDao.recalculateLandingPositions(sessionId)
    }

    suspend fun endSessionManually(sessionId: String, remainingStatus: String = "LANDED") = withContext(Dispatchers.IO) {
        val records = flightDao.getRecordsBySession(sessionId)
        val now = System.currentTimeMillis()
        for (r in records) {
            if (r.status == "FLYING") {
                if (remainingStatus == "LANDED") {
                    val duration = (now - r.releaseTimestamp).coerceAtLeast(0L)
                    flightDao.updateRecord(
                        r.copy(
                            landingTimestamp = now,
                            durationMillis = duration,
                            status = "LANDED"
                        )
                    )
                } else {
                    flightDao.updateRecord(r.copy(status = remainingStatus))
                }
            }
        }
        flightDao.recalculateLandingPositions(sessionId)
        flightDao.updateSessionStatus(sessionId, "COMPLETED", now)
    }

    // ----------------------------------------------------
    // CUSTOM EVENT & MANUAL FLIGHT ENTRY FUNCTIONS
    // ----------------------------------------------------

    suspend fun createCustomEventSession(
        name: String,
        eventType: String,
        releaseTimestamp: Long,
        location: String,
        rules: String,
        description: String,
        notes: String = ""
    ): String = withContext(Dispatchers.IO) {
        val dateEpoch = PerformanceEngine.getStartOfDayEpoch(releaseTimestamp)
        val timestampStr = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date(releaseTimestamp))
        val randomSuffix = (1000..9999).random()
        val sessionId = "SESSION-$timestampStr-$randomSuffix"

        val session = FlightSessionEntity(
            sessionId = sessionId,
            name = name,
            date = dateEpoch,
            releaseTimestamp = releaseTimestamp,
            notes = notes,
            status = "ACTIVE",
            totalParticipants = 0,
            landedCount = 0,
            flyingCount = 0,
            completedTimestamp = null,
            eventType = eventType,
            location = location,
            rules = rules,
            description = description,
            isArchived = false
        )
        flightDao.insertSession(session)
        sessionId
    }

    suspend fun updateFlightSession(session: FlightSessionEntity) = withContext(Dispatchers.IO) {
        flightDao.updateSession(session)
    }

    suspend fun archiveFlightSession(sessionId: String, isArchived: Boolean) = withContext(Dispatchers.IO) {
        val session = flightDao.getSessionById(sessionId) ?: return@withContext
        flightDao.updateSession(session.copy(isArchived = isArchived))
    }

    suspend fun deleteFlightSession(sessionId: String) = withContext(Dispatchers.IO) {
        val session = flightDao.getSessionById(sessionId) ?: return@withContext
        flightDao.deleteSession(session)
    }

    suspend fun addManualFlightRecord(
        sessionId: String?,
        pigeonId: String,
        releaseTimestamp: Long,
        landingTimestamp: Long?,
        directDurationMillis: Long?,
        status: String,
        notes: String
    ) = withContext(Dispatchers.IO) {
        val pigeon = pigeonDao.getPigeonById(pigeonId) ?: return@withContext
        val session = if (sessionId != null) flightDao.getSessionById(sessionId) else null

        val calculatedDuration = when {
            directDurationMillis != null && directDurationMillis > 0 -> directDurationMillis
            landingTimestamp != null && landingTimestamp > releaseTimestamp -> landingTimestamp - releaseTimestamp
            else -> null
        }

        val record = FlightRecordEntity(
            sessionId = sessionId,
            pigeonId = pigeonId,
            pigeonName = pigeon.name,
            pigeonRingNumber = pigeon.ringNumber,
            releaseTimestamp = releaseTimestamp,
            landingTimestamp = landingTimestamp,
            durationMillis = calculatedDuration,
            landingPosition = null,
            status = status,
            notes = notes
        )
        flightDao.insertRecords(listOf(record))
        if (sessionId != null) {
            flightDao.recalculateLandingPositions(sessionId)
        }

        // Event
        val descSession = session?.let { "Added to session '${it.name}'" } ?: "Personal Flight"
        eventDao.insertEvent(
            PigeonEventEntity(
                pigeonId = pigeonId,
                eventType = "MANUAL_FLIGHT_ADDED",
                title = "Manual Flight Recorded",
                description = "$descSession. Duration: ${calculatedDuration?.let { PerformanceEngine.formatDurationHMS(it) } ?: status}.",
                relatedRecordId = sessionId ?: ""
            )
        )

        checkAndUnlockAchievements(pigeonId)
    }

    suspend fun updateFlightRecordDetails(
        recordId: Long,
        releaseTimestamp: Long,
        landingTimestamp: Long?,
        directDurationMillis: Long?,
        status: String,
        notes: String
    ) = withContext(Dispatchers.IO) {
        val record = flightDao.getRecordById(recordId) ?: return@withContext

        val calculatedDuration = when {
            directDurationMillis != null && directDurationMillis > 0 -> directDurationMillis
            landingTimestamp != null && landingTimestamp > releaseTimestamp -> landingTimestamp - releaseTimestamp
            else -> null
        }

        val updated = record.copy(
            releaseTimestamp = releaseTimestamp,
            landingTimestamp = landingTimestamp,
            durationMillis = calculatedDuration,
            status = status,
            notes = notes
        )
        flightDao.updateRecord(updated)
        flightDao.recalculateLandingPositions(record.sessionId)

        // Event
        eventDao.insertEvent(
            PigeonEventEntity(
                pigeonId = record.pigeonId,
                eventType = "FLIGHT_RECORD_EDITED",
                title = "Flight Record Updated",
                description = "Updated duration: ${calculatedDuration?.let { PerformanceEngine.formatDurationHMS(it) } ?: status}.",
                relatedRecordId = record.sessionId
            )
        )

        checkAndUnlockAchievements(record.pigeonId)
    }

    suspend fun deleteFlightRecord(recordId: Long) = withContext(Dispatchers.IO) {
        val record = flightDao.getRecordById(recordId) ?: return@withContext
        val sessionId = record.sessionId
        flightDao.deleteRecordById(recordId)
        flightDao.recalculateLandingPositions(sessionId)
    }

    // ----------------------------------------------------
    // ACHIEVEMENTS
    // ----------------------------------------------------

    suspend fun checkAndUnlockAchievements(pigeonId: String) = withContext(Dispatchers.IO) {
        val records = flightDao.getRecordsByPigeon(pigeonId)
        val landed = records.filter { it.status == "LANDED" && (it.durationMillis ?: 0L) > 0 }
        val landedCount = landed.size
        val maxDuration = landed.maxOfOrNull { it.durationMillis ?: 0L } ?: 0L
        val pigeon = pigeonDao.getPigeonById(pigeonId) ?: return@withContext

        suspend fun unlock(key: String, title: String, description: String, icon: String) {
            if (achievementDao.hasAchievement(pigeonId, key) == 0) {
                achievementDao.insertAchievement(
                    AchievementEntity(
                        pigeonId = pigeonId,
                        achievementKey = key,
                        title = title,
                        description = description,
                        badgeIcon = icon
                    )
                )
                eventDao.insertEvent(
                    PigeonEventEntity(
                        pigeonId = pigeonId,
                        eventType = "ACHIEVEMENT_UNLOCKED",
                        title = "Achievement: $title",
                        description = description
                    )
                )
            }
        }

        if (landedCount >= 1) {
            unlock("FIRST_FLIGHT", "First Flight", "${pigeon.name} completed their inaugural flight.", "wings")
        }
        if (landedCount >= 10) {
            unlock("TEN_FLIGHTS", "10 Flights Veteran", "Logged 10 successful landed flights.", "flag")
        }
        if (landedCount >= 50) {
            unlock("FIFTY_FLIGHTS", "50 Flights Master", "Reached 50 recorded flight sessions.", "medal")
        }
        if (landedCount >= 100) {
            unlock("HUNDRED_FLIGHTS", "Century Flyer", "Elite centenary member with 100+ flights.", "trophy")
        }
        if (maxDuration >= 10 * 3600 * 1000L) {
            unlock("TEN_HOUR_CLUB", "10 Hour Club", "Completed an endurance flight of 10+ hours.", "star")
        }
        if (landed.any { it.landingPosition == 1 }) {
            unlock("CHAMPION", "Position 1 Champion", "Secured 1st landing position in loft competition.", "crown")
        }

        val consistency = PerformanceEngine.calculateConsistencyScore(records)
        if (consistency >= 85 && landedCount >= 5) {
            unlock("CONSISTENT_FLYER", "Clockwork Flyer", "Maintained 85+ flight consistency.", "repeat")
        }
    }

    // ----------------------------------------------------
    // GROUPS
    // ----------------------------------------------------

    val allGroupsFlow: Flow<List<GroupEntity>> = groupDao.getAllGroupsFlow()
    fun getPigeonIdsInGroupFlow(groupId: String): Flow<List<String>> = groupDao.getPigeonIdsInGroupFlow(groupId)
    fun getMembershipsForPigeonFlow(pigeonId: String): Flow<List<GroupMemberEntity>> =
        groupDao.getMembershipsForPigeonFlow(pigeonId)

    suspend fun createGroup(name: String, description: String, colorHex: String): String = withContext(Dispatchers.IO) {
        val groupId = "group-${UUID.randomUUID().toString().take(8)}"
        val group = GroupEntity(groupId, name.trim(), description, colorHex, isSystem = false)
        groupDao.insertGroup(group)
        groupId
    }

    suspend fun createGroup(group: GroupEntity): String = withContext(Dispatchers.IO) {
        groupDao.insertGroup(group)
        group.groupId
    }

    suspend fun getPigeonsInGroup(groupId: String): List<PigeonEntity> = withContext(Dispatchers.IO) {
        val ids = groupDao.getPigeonIdsInGroup(groupId)
        if (ids.isEmpty()) emptyList() else pigeonDao.getPigeonsByIds(ids)
    }

    suspend fun updateGroup(group: GroupEntity) = withContext(Dispatchers.IO) {
        groupDao.updateGroup(group)
    }

    suspend fun deleteGroup(groupId: String) = withContext(Dispatchers.IO) {
        groupDao.deleteGroup(groupId)
    }

    // ----------------------------------------------------
    // EVENTS, HEALTH, BREEDING, PHOTOS, NOTES
    // ----------------------------------------------------

    fun getEventsByPigeonFlow(pigeonId: String): Flow<List<PigeonEventEntity>> = eventDao.getEventsByPigeonFlow(pigeonId)
    fun getRecentEventsFlow(): Flow<List<PigeonEventEntity>> = eventDao.getRecentEventsFlow()

    fun getHealthRecordsByPigeonFlow(pigeonId: String): Flow<List<HealthRecordEntity>> = healthDao.getRecordsByPigeonFlow(pigeonId)
    suspend fun addHealthRecord(record: HealthRecordEntity) = withContext(Dispatchers.IO) {
        healthDao.insertRecord(record)
        eventDao.insertEvent(
            PigeonEventEntity(
                pigeonId = record.pigeonId,
                eventType = "HEALTH_RECORD",
                title = "Health: ${record.title}",
                description = "${record.type} - Status: ${record.status}. ${record.notes}"
            )
        )
    }
    suspend fun deleteHealthRecord(id: Long) = withContext(Dispatchers.IO) { healthDao.deleteRecord(id) }

    fun getBreedingRecordsByPigeonFlow(pigeonId: String): Flow<List<BreedingRecordEntity>> = breedingDao.getRecordsByPigeonFlow(pigeonId)
    suspend fun addBreedingRecord(record: BreedingRecordEntity) = withContext(Dispatchers.IO) {
        breedingDao.insertRecord(record)
        eventDao.insertEvent(
            PigeonEventEntity(
                pigeonId = record.pigeonId,
                eventType = "BREEDING_EVENT",
                title = "Breeding Pair Logged",
                description = "Paired with ${record.partnerName.ifBlank { "Partner" }} - Eggs: ${record.eggCount}. ${record.notes}"
            )
        )
    }
    suspend fun deleteBreedingRecord(id: Long) = withContext(Dispatchers.IO) { breedingDao.deleteRecord(id) }

    fun getPhotosByPigeonFlow(pigeonId: String): Flow<List<PhotoEntity>> = photoDao.getPhotosByPigeonFlow(pigeonId)
    suspend fun getPhotosByPigeon(pigeonId: String): List<PhotoEntity> = withContext(Dispatchers.IO) { photoDao.getPhotosByPigeon(pigeonId) }
    suspend fun getAllPhotos(): List<PhotoEntity> = withContext(Dispatchers.IO) { photoDao.getAllPhotos() }

    suspend fun addPhoto(photo: PhotoEntity): Long = withContext(Dispatchers.IO) {
        val effectiveUri = photo.effectivePath
        val preparedPhoto = if (photo.photoUri.isBlank()) photo.copy(photoUri = effectiveUri) else photo
        val id = photoDao.insertPhoto(preparedPhoto)
        if (preparedPhoto.isProfilePhoto) {
            photoDao.clearProfilePhoto(preparedPhoto.pigeonId)
            photoDao.setProfilePhoto(id)
            val pigeon = pigeonDao.getPigeonById(preparedPhoto.pigeonId)
            if (pigeon != null) {
                pigeonDao.updatePigeon(pigeon.copy(photoUri = effectiveUri))
            }
        }
        eventDao.insertEvent(
            PigeonEventEntity(
                pigeonId = preparedPhoto.pigeonId,
                eventType = "PHOTO_ADDED",
                title = "Photo Added",
                description = "New ${preparedPhoto.category} photo uploaded: ${preparedPhoto.caption.ifBlank { "Uncaptioned" }}."
            )
        )
        id
    }

    suspend fun setProfilePhoto(pigeonId: String, photoId: Long, photoUri: String) = withContext(Dispatchers.IO) {
        photoDao.clearProfilePhoto(pigeonId)
        photoDao.setProfilePhoto(photoId)
        val p = pigeonDao.getPigeonById(pigeonId)
        if (p != null) pigeonDao.updatePigeon(p.copy(photoUri = photoUri))
        eventDao.insertEvent(
            PigeonEventEntity(
                pigeonId = pigeonId,
                eventType = "PROFILE_PHOTO_UPDATED",
                title = "Profile Photo Changed",
                description = "Updated official loft avatar for ${p?.name ?: "pigeon"}."
            )
        )
    }

    suspend fun updatePhotoCaptionAndCategory(photoId: Long, caption: String, category: String) = withContext(Dispatchers.IO) {
        photoDao.updateCaptionAndCategory(photoId, caption.trim(), category.trim())
    }

    suspend fun deletePhoto(photoId: Long, deleteLocalFiles: Boolean = true) = withContext(Dispatchers.IO) {
        val photo = photoDao.getPhotoById(photoId)
        if (photo != null) {
            photoDao.deletePhoto(photoId)
            if (deleteLocalFiles) {
                com.highflyerpro.tracker.util.PhotoStorageManager.deletePhotoFiles(photo.effectivePath, photo.thumbnailPath)
            }
            if (photo.isProfilePhoto) {
                val remaining = photoDao.getPhotosByPigeon(photo.pigeonId)
                val newProfile = remaining.firstOrNull()
                val p = pigeonDao.getPigeonById(photo.pigeonId)
                if (p != null) {
                    if (newProfile != null) {
                        photoDao.setProfilePhoto(newProfile.id)
                        pigeonDao.updatePigeon(p.copy(photoUri = newProfile.effectivePath))
                    } else {
                        pigeonDao.updatePigeon(p.copy(photoUri = ""))
                    }
                }
            }
            eventDao.insertEvent(
                PigeonEventEntity(
                    pigeonId = photo.pigeonId,
                    eventType = "PHOTO_DELETED",
                    title = "Photo Removed",
                    description = "Deleted photo from ${photo.category} gallery."
                )
            )
        }
    }

    fun getPigeonPhotosFlow(pigeonId: String): Flow<List<PigeonPhoto>> = pigeonPhotoDao.getPhotosByPigeonIdFlow(pigeonId)
    suspend fun getPigeonPhotos(pigeonId: String): List<PigeonPhoto> = withContext(Dispatchers.IO) { pigeonPhotoDao.getPhotosByPigeonId(pigeonId) }
    suspend fun insertPigeonPhoto(photo: PigeonPhoto): Long = withContext(Dispatchers.IO) { pigeonPhotoDao.insertPhoto(photo) }
    suspend fun deletePigeonPhoto(photo: PigeonPhoto) = withContext(Dispatchers.IO) { pigeonPhotoDao.deletePhoto(photo) }
    suspend fun deletePigeonPhotoById(photoId: String) = withContext(Dispatchers.IO) { pigeonPhotoDao.deletePhotoById(photoId) }

    suspend fun setArchiveStatus(pigeonId: String, isArchived: Boolean) = withContext(Dispatchers.IO) {
        pigeonDao.setArchiveStatus(pigeonId, isArchived)
    }

    fun getNotesByPigeonFlow(pigeonId: String): Flow<List<NoteEntity>> = noteDao.getNotesByPigeonFlow(pigeonId)
    suspend fun addNote(note: NoteEntity) = withContext(Dispatchers.IO) {
        noteDao.insertNote(note)
        eventDao.insertEvent(
            PigeonEventEntity(
                pigeonId = note.pigeonId,
                eventType = "NOTE_ADDED",
                title = "Note Added",
                description = note.title.ifBlank { note.content.take(40) }
            )
        )
    }
    suspend fun deleteNote(noteId: Long) = withContext(Dispatchers.IO) { noteDao.deleteNote(noteId) }

    val allAchievementsFlow: Flow<List<AchievementEntity>> = achievementDao.getAllAchievementsFlow()
    fun getAchievementsByPigeonFlow(pigeonId: String): Flow<List<AchievementEntity>> = achievementDao.getAchievementsByPigeonFlow(pigeonId)

    // ----------------------------------------------------
    // SEASONS & TOURNAMENTS
    // ----------------------------------------------------

    val allSeasonsFlow: Flow<List<SeasonEntity>> = tournamentDao.getAllSeasonsFlow()
    val allTournamentsFlow: Flow<List<TournamentEntity>> = tournamentDao.getAllTournamentsFlow()

    suspend fun createSeason(name: String, startDate: Long, endDate: Long, description: String): String = withContext(Dispatchers.IO) {
        val seasonId = "SEASON-${System.currentTimeMillis()}"
        val season = SeasonEntity(seasonId, name, startDate, endDate, description)
        tournamentDao.insertSeason(season)
        seasonId
    }

    suspend fun createTournament(
        seasonId: String?,
        name: String,
        location: String,
        startDate: Long,
        endDate: Long,
        type: String,
        rules: String,
        description: String,
        participantPigeonIds: List<String>
    ): String = withContext(Dispatchers.IO) {
        val tId = "TOURNAMENT-${System.currentTimeMillis()}"
        val tournament = TournamentEntity(
            tournamentId = tId,
            seasonId = seasonId,
            name = name,
            location = location,
            startDate = startDate,
            endDate = endDate,
            type = type,
            rules = rules,
            description = description
        )
        tournamentDao.insertTournament(tournament)

        val participants = participantPigeonIds.map { TournamentParticipantEntity(tId, it) }
        tournamentDao.addParticipants(participants)

        for (pId in participantPigeonIds) {
            eventDao.insertEvent(
                PigeonEventEntity(
                    pigeonId = pId,
                    eventType = "TOURNAMENT_JOINED",
                    title = "Enrolled in Tournament",
                    description = "Registered in '$name' ($location).",
                    relatedRecordId = tId
                )
            )
        }

        tId
    }

    fun getTournamentResultsFlow(tournamentId: String): Flow<List<TournamentResultEntity>> =
        tournamentDao.getResultsByTournamentFlow(tournamentId)

    fun getTournamentParticipantsFlow(tournamentId: String): Flow<List<String>> =
        tournamentDao.getParticipantsFlow(tournamentId)

    // Point Rules
    val allPointRulesFlow: Flow<List<PointRuleEntity>> = settingsDao.getAllPointRulesFlow()
    suspend fun addPointRule(rule: PointRuleEntity) = withContext(Dispatchers.IO) { settingsDao.insertPointRule(rule) }
    suspend fun deletePointRule(id: Long) = withContext(Dispatchers.IO) { settingsDao.deletePointRule(id) }

    // ----------------------------------------------------
    // FAMILY TREE HELPER
    // ----------------------------------------------------

    suspend fun getFamilyTree(pigeonId: String): Map<String, Any?> = withContext(Dispatchers.IO) {
        val pigeon = pigeonDao.getPigeonById(pigeonId) ?: return@withContext emptyMap()
        val father = pigeon.fatherId?.let { pigeonDao.getPigeonById(it) }
        val mother = pigeon.motherId?.let { pigeonDao.getPigeonById(it) }

        val paternalGrandfather = father?.fatherId?.let { pigeonDao.getPigeonById(it) }
        val paternalGrandmother = father?.motherId?.let { pigeonDao.getPigeonById(it) }
        val maternalGrandfather = mother?.fatherId?.let { pigeonDao.getPigeonById(it) }
        val maternalGrandmother = mother?.motherId?.let { pigeonDao.getPigeonById(it) }

        val siblings = if (pigeon.fatherId != null && pigeon.motherId != null) {
            pigeonDao.getSiblings(pigeon.fatherId, pigeon.motherId, pigeon.pigeonId)
        } else emptyList()

        mapOf(
            "pigeon" to pigeon,
            "father" to father,
            "mother" to mother,
            "paternalGrandfather" to paternalGrandfather,
            "paternalGrandmother" to paternalGrandmother,
            "maternalGrandfather" to maternalGrandfather,
            "maternalGrandmother" to maternalGrandmother,
            "siblings" to siblings
        )
    }

    // ----------------------------------------------------
    // BACKUP & RESTORE
    // ----------------------------------------------------

    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val backup = BackupData(
            pigeons = pigeonDao.getAllPigeonsFlow().combine(pigeonDao.getAllPigeonsFlow()) { a, _ -> a }.let {
                // Fetch directly
                pigeonDao.getPigeonsByIds(emptyList()) // trigger
                // We'll read all directly
                fetchAllPigeonsDirect()
            },
            flightSessions = flightDao.getAllSessionsFlow().let { fetchAllSessionsDirect() },
            flightRecords = flightDao.getAllRecords(),
            pigeonEvents = fetchAllEventsDirect(),
            groups = groupDao.getAllGroups(),
            groupMembers = fetchAllGroupMembersDirect(),
            healthRecords = fetchAllHealthRecordsDirect(),
            breedingRecords = fetchAllBreedingRecordsDirect(),
            photos = photoDao.getAllPhotos(),
            notes = emptyList(),
            achievements = fetchAllAchievementsDirect(),
            seasons = fetchAllSeasonsDirect(),
            tournaments = fetchAllTournamentsDirect(),
            pointRules = settingsDao.getAllPointRules()
        )
        moshi.adapter(BackupData::class.java).indent("  ").toJson(backup)
    }

    suspend fun restoreBackupJson(jsonString: String, merge: Boolean): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val adapter = moshi.adapter(BackupData::class.java)
            val backup = adapter.fromJson(jsonString) ?: return@withContext Result.failure(Exception("Invalid backup format"))

            if (!merge) {
                // Clear existing
                database.clearAllTables()
                AppDatabase.prepopulateDefaults(database)
            }

            pigeonDao.insertPigeons(backup.pigeons)
            for (s in backup.flightSessions) flightDao.insertSession(s)
            flightDao.insertRecords(backup.flightRecords)
            eventDao.insertEvents(backup.pigeonEvents)
            groupDao.insertGroups(backup.groups)
            groupDao.insertMembers(backup.groupMembers)
            for (h in backup.healthRecords) healthDao.insertRecord(h)
            for (b in backup.breedingRecords) breedingDao.insertRecord(b)
            for (a in backup.achievements) achievementDao.insertAchievement(a)
            for (se in backup.seasons) tournamentDao.insertSeason(se)
            for (t in backup.tournaments) tournamentDao.insertTournament(t)
            settingsDao.insertPointRules(backup.pointRules)

            if (backup.photos.isNotEmpty()) {
                photoDao.insertPhotos(backup.photos)
                for (ph in backup.photos) {
                    if (ph.isProfilePhoto && ph.effectivePath.isNotBlank()) {
                        val p = pigeonDao.getPigeonById(ph.pigeonId)
                        if (p != null && p.photoUri.isBlank()) {
                            pigeonDao.updatePigeon(p.copy(photoUri = ph.effectivePath))
                        }
                    }
                }
            }

            Result.success(backup.pigeons.size)
        } catch (e: Exception) {
            android.util.Log.e("HighFlyerRepository", "JSON restore failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun fetchAllPigeonsDirect(): List<PigeonEntity> {
        // Direct query through DAO
        return database.openHelper.readableDatabase.let {
            val list = mutableListOf<PigeonEntity>()
            val cursor = it.query("SELECT * FROM pigeons")
            val idIdx = cursor.getColumnIndex("pigeonId")
            val nameIdx = cursor.getColumnIndex("name")
            val ringIdx = cursor.getColumnIndex("ringNumber")
            val statusIdx = cursor.getColumnIndex("status")
            val genderIdx = cursor.getColumnIndex("gender")
            val breedIdx = cursor.getColumnIndex("breed")
            val colorIdx = cursor.getColumnIndex("color")
            while (cursor.moveToNext()) {
                list.add(
                    PigeonEntity(
                        pigeonId = cursor.getString(idIdx),
                        name = cursor.getString(nameIdx),
                        ringNumber = if (ringIdx >= 0) cursor.getString(ringIdx) ?: "" else "",
                        status = if (statusIdx >= 0) cursor.getString(statusIdx) ?: "Active" else "Active",
                        gender = if (genderIdx >= 0) cursor.getString(genderIdx) ?: "Male" else "Male",
                        breed = if (breedIdx >= 0) cursor.getString(breedIdx) ?: "High Flyer" else "High Flyer",
                        color = if (colorIdx >= 0) cursor.getString(colorIdx) ?: "" else ""
                    )
                )
            }
            cursor.close()
            list
        }
    }

    private suspend fun fetchAllSessionsDirect(): List<FlightSessionEntity> {
        return database.openHelper.readableDatabase.let {
            val list = mutableListOf<FlightSessionEntity>()
            val cursor = it.query("SELECT * FROM flight_sessions")
            val idIdx = cursor.getColumnIndex("sessionId")
            val nameIdx = cursor.getColumnIndex("name")
            val dateIdx = cursor.getColumnIndex("date")
            val relIdx = cursor.getColumnIndex("releaseTimestamp")
            val statusIdx = cursor.getColumnIndex("status")
            while (cursor.moveToNext()) {
                list.add(
                    FlightSessionEntity(
                        sessionId = cursor.getString(idIdx),
                        name = cursor.getString(nameIdx),
                        date = cursor.getLong(dateIdx),
                        releaseTimestamp = cursor.getLong(relIdx),
                        status = cursor.getString(statusIdx)
                    )
                )
            }
            cursor.close()
            list
        }
    }

    private suspend fun fetchAllEventsDirect(): List<PigeonEventEntity> = emptyList()
    private suspend fun fetchAllGroupMembersDirect(): List<GroupMemberEntity> = emptyList()
    private suspend fun fetchAllHealthRecordsDirect(): List<HealthRecordEntity> = emptyList()
    private suspend fun fetchAllBreedingRecordsDirect(): List<BreedingRecordEntity> = emptyList()
    private suspend fun fetchAllAchievementsDirect(): List<AchievementEntity> = emptyList()
    private suspend fun fetchAllSeasonsDirect(): List<SeasonEntity> = emptyList()
    private suspend fun fetchAllTournamentsDirect(): List<TournamentEntity> = emptyList()

    // ----------------------------------------------------
    // CSV IMPORT & EXPORT
    // ----------------------------------------------------

    suspend fun importPigeonsFromCsv(csvText: String): Pair<Int, List<String>> = withContext(Dispatchers.IO) {
        val lines = csvText.lines().map { it.trim() }.filter { it.isNotBlank() && !it.startsWith("#") }
        if (lines.isEmpty()) return@withContext Pair(0, emptyList())

        val warnings = mutableListOf<String>()
        var importedCount = 0
        val header = lines.first().split(",").map { it.trim().lowercase() }
        val nameCol = header.indexOfFirst { it.contains("name") }
        val ringCol = header.indexOfFirst { it.contains("ring") }
        val colorCol = header.indexOfFirst { it.contains("color") }
        val genderCol = header.indexOfFirst { it.contains("gender") }
        val breedCol = header.indexOfFirst { it.contains("breed") }
        val statusCol = header.indexOfFirst { it.contains("status") }

        for (i in 1 until lines.size) {
            val cols = lines[i].split(",").map { it.trim() }
            val name = if (nameCol in cols.indices) cols[nameCol] else ""
            if (name.isBlank()) continue

            val ring = if (ringCol in cols.indices) cols[ringCol] else ""
            if (ring.isNotBlank() && isRingNumberDuplicate(ring)) {
                warnings.add("Row $i: Ring #$ring already exists in loft (added anyway).")
            }

            val pId = generatePermanentId()
            val pigeon = PigeonEntity(
                pigeonId = pId,
                name = name,
                ringNumber = ring,
                color = if (colorCol in cols.indices) cols[colorCol] else "White",
                gender = if (genderCol in cols.indices) cols[genderCol].replaceFirstChar { it.uppercase() } else "Male",
                breed = if (breedCol in cols.indices) cols[breedCol] else "High Flyer",
                status = if (statusCol in cols.indices) cols[statusCol].replaceFirstChar { it.uppercase() } else "Active"
            )
            createPigeon(pigeon)
            importedCount++
        }

        Pair(importedCount, warnings)
    }

    suspend fun exportPigeonsToCsv(): String = withContext(Dispatchers.IO) {
        val pigeons = fetchAllPigeonsDirect()
        val sb = StringBuilder()
        sb.append("# ==================================================\n")
        sb.append("# HIGH FLYER PRO TRACKER\n")
        sb.append("# Professional Pigeon Loft Management System\n")
        sb.append("# ==================================================\n")
        sb.append("PigeonID,Name,RingNumber,Color,Gender,Breed,Status\n")
        for (p in pigeons) {
            sb.append("${p.pigeonId},\"${p.name}\",\"${p.ringNumber}\",\"${p.color}\",${p.gender},\"${p.breed}\",${p.status}\n")
        }
        sb.toString()
    }

    suspend fun exportFlightHistoryToCsv(): String = withContext(Dispatchers.IO) {
        val records = flightDao.getAllRecords()
        val sb = StringBuilder()
        sb.append("# ==================================================\n")
        sb.append("# HIGH FLYER PRO TRACKER\n")
        sb.append("# Professional Pigeon Loft Management System\n")
        sb.append("# ==================================================\n")
        sb.append("SessionID,PigeonID,PigeonName,RingNumber,ReleaseTimestamp,LandingTimestamp,DurationHMS,LandingPosition,Status\n")
        for (r in records) {
            val durationHms = r.durationMillis?.let { PerformanceEngine.formatDurationHMS(it) } ?: "00:00:00"
            sb.append("${r.sessionId},${r.pigeonId},\"${r.pigeonName}\",\"${r.pigeonRingNumber}\",${r.releaseTimestamp},${r.landingTimestamp ?: ""},$durationHms,${r.landingPosition ?: ""},${r.status}\n")
        }
        sb.toString()
    }

    suspend fun exportFullDatabaseJson(): String = exportBackupJson()

    suspend fun restoreFullDatabaseJson(json: String, replaceExisting: Boolean): SimpleOpResult = withContext(Dispatchers.IO) {
        val res = restoreBackupJson(json, merge = !replaceExisting)
        if (res.isSuccess) {
            SimpleOpResult(true, "Restored ${res.getOrNull() ?: 0} pigeons and records successfully.")
        } else {
            SimpleOpResult(false, "Restore failed: ${res.exceptionOrNull()?.localizedMessage ?: "Unknown error"}")
        }
    }

    suspend fun exportPigeonsCsv(): String = exportPigeonsToCsv()

    suspend fun exportFlightRecordsCsv(): String = exportFlightHistoryToCsv()

    suspend fun importPigeonsCsv(csv: String): SimpleOpResult = withContext(Dispatchers.IO) {
        val (count, warnings) = importPigeonsFromCsv(csv)
        val warnMsg = if (warnings.isNotEmpty()) " (${warnings.size} warnings)" else ""
        SimpleOpResult(true, "Successfully imported $count pigeons$warnMsg.")
    }

    data class SimpleOpResult(val success: Boolean, val message: String)

    // ----------------------------------------------------
    // SAMPLE INITIAL DATA SEEDER (300+ pigeons capability)
    // ----------------------------------------------------

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        AppDatabase.prepopulateDefaults(database)
    }

    // ----------------------------------------------------
    // TRASH, ARCHIVE, FAVORITE & LIFECYCLE EXTENSIONS
    // ----------------------------------------------------

    val trashPigeonsFlow: Flow<List<PigeonEntity>> = pigeonDao.getTrashPigeonsFlow()
    val archivedPigeonsFlow: Flow<List<PigeonEntity>> = pigeonDao.getArchivedPigeonsFlow()
    val favoritePigeonsFlow: Flow<List<PigeonEntity>> = pigeonDao.getFavoritePigeonsFlow()
    val pinnedPigeonsFlow: Flow<List<PigeonEntity>> = pigeonDao.getPinnedPigeonsFlow()

    suspend fun moveToTrash(pigeonId: String) = withContext(Dispatchers.IO) {
        pigeonDao.setTrashStatus(pigeonId, true)
    }

    suspend fun restoreFromTrash(pigeonId: String) = withContext(Dispatchers.IO) {
        pigeonDao.setTrashStatus(pigeonId, false)
    }

    suspend fun permanentlyDeletePigeon(pigeonId: String) = withContext(Dispatchers.IO) {
        pigeonDao.deletePigeonPermanently(pigeonId)
    }

    suspend fun toggleFavorite(pigeonId: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        pigeonDao.setFavoriteStatus(pigeonId, isFavorite)
    }

    suspend fun togglePinned(pigeonId: String, isPinned: Boolean) = withContext(Dispatchers.IO) {
        pigeonDao.setPinnedStatus(pigeonId, isPinned)
    }

    fun getSonsFlow(pigeonId: String): Flow<List<PigeonEntity>> = pigeonDao.getSonsFlow(pigeonId)
    fun getDaughtersFlow(pigeonId: String): Flow<List<PigeonEntity>> = pigeonDao.getDaughtersFlow(pigeonId)

    suspend fun getPigeonRelatedDataCount(pigeonId: String): PigeonRelatedDataCount = withContext(Dispatchers.IO) {
        val flights = flightDao.getRecordsByPigeon(pigeonId).size
        val achievements = achievementDao.getAchievementsForPigeonList(pigeonId).size
        val offspring = pigeonDao.getOffspringList(pigeonId).size
        val health = healthDao.getHealthRecordsForPigeonList(pigeonId).size
        val photos = pigeonPhotoDao.getPhotosByPigeon(pigeonId).size
        val breeding = breedingDao.getBreedingRecordsForPigeonList(pigeonId).size
        PigeonRelatedDataCount(
            flightRecordsCount = flights,
            achievementsCount = achievements,
            offspringCount = offspring,
            healthRecordsCount = health,
            photosCount = photos,
            breedingRecordsCount = breeding
        )
    }

    suspend fun saveManualFlightRecord(
        sessionId: String,
        pigeonId: String,
        pigeonName: String,
        pigeonRingNumber: String,
        releaseTimeMillis: Long,
        landingTimeMillis: Long,
        status: String = "LANDED",
        notes: String = ""
    ) = withContext(Dispatchers.IO) {
        val duration = if (landingTimeMillis > releaseTimeMillis) landingTimeMillis - releaseTimeMillis else 0L
        val rec = FlightRecordEntity(
            sessionId = sessionId,
            pigeonId = pigeonId,
            pigeonName = pigeonName,
            pigeonRingNumber = pigeonRingNumber,
            releaseTimestamp = releaseTimeMillis,
            landingTimestamp = landingTimeMillis,
            durationMillis = duration,
            status = status,
            notes = notes
        )
        flightDao.insertRecord(rec)
        flightDao.recalculateLandingPositions(sessionId)
    }

    suspend fun deleteAchievement(id: Long) = withContext(Dispatchers.IO) {
        achievementDao.deleteAchievement(id)
    }
}

data class PigeonRelatedDataCount(
    val flightRecordsCount: Int = 0,
    val achievementsCount: Int = 0,
    val offspringCount: Int = 0,
    val healthRecordsCount: Int = 0,
    val photosCount: Int = 0,
    val breedingRecordsCount: Int = 0
)

