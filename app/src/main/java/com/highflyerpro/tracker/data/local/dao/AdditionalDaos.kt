package com.highflyerpro.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.highflyerpro.tracker.data.local.entities.AchievementEntity
import com.highflyerpro.tracker.data.local.entities.AppSettingsEntity
import com.highflyerpro.tracker.data.local.entities.BreedingRecordEntity
import com.highflyerpro.tracker.data.local.entities.GroupEntity
import com.highflyerpro.tracker.data.local.entities.GroupMemberEntity
import com.highflyerpro.tracker.data.local.entities.HealthRecordEntity
import com.highflyerpro.tracker.data.local.entities.NoteEntity
import com.highflyerpro.tracker.data.local.entities.PhotoEntity
import com.highflyerpro.tracker.data.local.entities.PigeonEventEntity
import com.highflyerpro.tracker.data.local.entities.PointRuleEntity
import com.highflyerpro.tracker.data.local.entities.SeasonEntity
import com.highflyerpro.tracker.data.local.entities.TournamentEntity
import com.highflyerpro.tracker.data.local.entities.TournamentParticipantEntity
import com.highflyerpro.tracker.data.local.entities.TournamentResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Query("SELECT * FROM groups ORDER BY isSystem DESC, name ASC")
    fun getAllGroupsFlow(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups")
    suspend fun getAllGroups(): List<GroupEntity>

    @Query("SELECT * FROM groups WHERE groupId = :groupId LIMIT 1")
    suspend fun getGroupById(groupId: String): GroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGroups(groups: List<GroupEntity>)

    @Update
    suspend fun updateGroup(group: GroupEntity)

    @Query("DELETE FROM groups WHERE groupId = :groupId AND isSystem = 0")
    suspend fun deleteGroup(groupId: String)

    // Group Members
    @Query("SELECT * FROM group_members WHERE pigeonId = :pigeonId")
    fun getMembershipsForPigeonFlow(pigeonId: String): Flow<List<GroupMemberEntity>>

    @Query("SELECT * FROM group_members WHERE pigeonId = :pigeonId")
    suspend fun getMembershipsForPigeon(pigeonId: String): List<GroupMemberEntity>

    @Query("SELECT pigeonId FROM group_members WHERE groupId = :groupId")
    fun getPigeonIdsInGroupFlow(groupId: String): Flow<List<String>>

    @Query("SELECT pigeonId FROM group_members WHERE groupId = :groupId")
    suspend fun getPigeonIdsInGroup(groupId: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: GroupMemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<GroupMemberEntity>)

    @Query("DELETE FROM group_members WHERE groupId = :groupId AND pigeonId = :pigeonId")
    suspend fun removeMember(groupId: String, pigeonId: String)

    @Query("DELETE FROM group_members WHERE groupId = :groupId AND pigeonId IN (:pigeonIds)")
    suspend fun removeMembersBulk(groupId: String, pigeonIds: List<String>)

    @Query("DELETE FROM group_members WHERE pigeonId = :pigeonId")
    suspend fun removePigeonFromAllGroups(pigeonId: String)
}

@Dao
interface EventDao {
    @Query("SELECT * FROM pigeon_events WHERE pigeonId = :pigeonId ORDER BY timestamp DESC")
    fun getEventsByPigeonFlow(pigeonId: String): Flow<List<PigeonEventEntity>>

    @Query("SELECT * FROM pigeon_events ORDER BY timestamp DESC LIMIT 50")
    fun getRecentEventsFlow(): Flow<List<PigeonEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: PigeonEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<PigeonEventEntity>)

    @Query("DELETE FROM pigeon_events WHERE pigeonId = :pigeonId")
    suspend fun deleteEventsByPigeon(pigeonId: String)
}

@Dao
interface HealthDao {
    @Query("SELECT * FROM health_records WHERE pigeonId = :pigeonId ORDER BY date DESC")
    fun getRecordsByPigeonFlow(pigeonId: String): Flow<List<HealthRecordEntity>>

    @Query("SELECT * FROM health_records WHERE pigeonId = :pigeonId ORDER BY date DESC")
    suspend fun getHealthRecordsForPigeonList(pigeonId: String): List<HealthRecordEntity>

    @Query("SELECT * FROM health_records ORDER BY date DESC")
    fun getAllRecordsFlow(): Flow<List<HealthRecordEntity>>

    @Query("SELECT * FROM health_records")
    suspend fun getAllHealthRecords(): List<HealthRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: HealthRecordEntity): Long

    @Update
    suspend fun updateRecord(record: HealthRecordEntity)

    @Query("DELETE FROM health_records WHERE id = :id")
    suspend fun deleteRecord(id: Long)
}

@Dao
interface BreedingDao {
    @Query("SELECT * FROM breeding_records WHERE pigeonId = :pigeonId OR partnerId = :pigeonId ORDER BY date DESC")
    fun getRecordsByPigeonFlow(pigeonId: String): Flow<List<BreedingRecordEntity>>

    @Query("SELECT * FROM breeding_records WHERE pigeonId = :pigeonId OR partnerId = :pigeonId ORDER BY date DESC")
    suspend fun getBreedingRecordsForPigeonList(pigeonId: String): List<BreedingRecordEntity>

    @Query("SELECT * FROM breeding_records ORDER BY date DESC")
    fun getAllRecordsFlow(): Flow<List<BreedingRecordEntity>>

    @Query("SELECT * FROM breeding_records")
    suspend fun getAllBreedingRecords(): List<BreedingRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: BreedingRecordEntity): Long

    @Update
    suspend fun updateRecord(record: BreedingRecordEntity)

    @Query("DELETE FROM breeding_records WHERE id = :id")
    suspend fun deleteRecord(id: Long)
}

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos WHERE pigeonId = :pigeonId ORDER BY dateAdded DESC, createdAt DESC")
    fun getPhotosByPigeonFlow(pigeonId: String): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE pigeonId = :pigeonId ORDER BY dateAdded DESC, createdAt DESC")
    suspend fun getPhotosByPigeon(pigeonId: String): List<PhotoEntity>

    @Query("SELECT * FROM photos ORDER BY dateAdded DESC, createdAt DESC")
    suspend fun getAllPhotos(): List<PhotoEntity>

    @Query("SELECT * FROM photos WHERE id = :photoId")
    suspend fun getPhotoById(photoId: Long): PhotoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<PhotoEntity>)

    @Update
    suspend fun updatePhoto(photo: PhotoEntity)

    @Query("UPDATE photos SET caption = :caption, category = :category WHERE id = :photoId")
    suspend fun updateCaptionAndCategory(photoId: Long, caption: String, category: String)

    @Query("UPDATE photos SET isProfilePhoto = 0 WHERE pigeonId = :pigeonId")
    suspend fun clearProfilePhoto(pigeonId: String)

    @Query("UPDATE photos SET isProfilePhoto = 1 WHERE id = :photoId")
    suspend fun setProfilePhoto(photoId: Long)

    @Query("DELETE FROM photos WHERE id = :photoId")
    suspend fun deletePhoto(photoId: Long)

    @Query("DELETE FROM photos WHERE pigeonId = :pigeonId")
    suspend fun deletePhotosByPigeon(pigeonId: String)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE pigeonId = :pigeonId ORDER BY updatedAt DESC")
    fun getNotesByPigeonFlow(pigeonId: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNote(noteId: Long)
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements WHERE pigeonId = :pigeonId ORDER BY unlockedAt DESC")
    fun getAchievementsByPigeonFlow(pigeonId: String): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE pigeonId = :pigeonId ORDER BY unlockedAt DESC")
    suspend fun getAchievementsForPigeonList(pigeonId: String): List<AchievementEntity>

    @Query("SELECT * FROM achievements ORDER BY unlockedAt DESC")
    fun getAllAchievementsFlow(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements")
    suspend fun getAllAchievements(): List<AchievementEntity>

    @Query("SELECT COUNT(*) FROM achievements WHERE pigeonId = :pigeonId AND achievementKey = :key")
    suspend fun hasAchievement(pigeonId: String, key: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementEntity): Long

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)

    @Query("DELETE FROM achievements WHERE id = :id")
    suspend fun deleteAchievement(id: Long)
}

@Dao
interface TournamentDao {
    // Seasons
    @Query("SELECT * FROM seasons ORDER BY startDate DESC")
    fun getAllSeasonsFlow(): Flow<List<SeasonEntity>>

    @Query("SELECT * FROM seasons WHERE status = 'Active' LIMIT 1")
    fun getActiveSeasonFlow(): Flow<SeasonEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeason(season: SeasonEntity)

    @Update
    suspend fun updateSeason(season: SeasonEntity)

    @Query("DELETE FROM seasons WHERE seasonId = :seasonId")
    suspend fun deleteSeason(seasonId: String)

    // Tournaments
    @Query("SELECT * FROM tournaments ORDER BY startDate DESC")
    fun getAllTournamentsFlow(): Flow<List<TournamentEntity>>

    @Query("SELECT * FROM tournaments WHERE tournamentId = :tournamentId LIMIT 1")
    fun getTournamentByIdFlow(tournamentId: String): Flow<TournamentEntity?>

    @Query("SELECT * FROM tournaments WHERE tournamentId = :tournamentId LIMIT 1")
    suspend fun getTournamentById(tournamentId: String): TournamentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournament(tournament: TournamentEntity)

    @Update
    suspend fun updateTournament(tournament: TournamentEntity)

    @Query("DELETE FROM tournaments WHERE tournamentId = :tournamentId")
    suspend fun deleteTournament(tournamentId: String)

    // Participants
    @Query("SELECT pigeonId FROM tournament_participants WHERE tournamentId = :tournamentId")
    fun getParticipantsFlow(tournamentId: String): Flow<List<String>>

    @Query("SELECT tournamentId FROM tournament_participants WHERE pigeonId = :pigeonId")
    fun getTournamentsForPigeonFlow(pigeonId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addParticipant(participant: TournamentParticipantEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addParticipants(participants: List<TournamentParticipantEntity>)

    @Query("DELETE FROM tournament_participants WHERE tournamentId = :tournamentId AND pigeonId = :pigeonId")
    suspend fun removeParticipant(tournamentId: String, pigeonId: String)

    // Results
    @Query("SELECT * FROM tournament_results WHERE tournamentId = :tournamentId ORDER BY rank ASC, points DESC, totalFlightHours DESC")
    fun getResultsByTournamentFlow(tournamentId: String): Flow<List<TournamentResultEntity>>

    @Query("SELECT * FROM tournament_results WHERE pigeonId = :pigeonId ORDER BY id DESC")
    fun getResultsByPigeonFlow(pigeonId: String): Flow<List<TournamentResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: TournamentResultEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResults(results: List<TournamentResultEntity>)

    @Query("DELETE FROM tournament_results WHERE tournamentId = :tournamentId")
    suspend fun deleteResultsByTournament(tournamentId: String)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM point_rules ORDER BY minHours DESC")
    fun getAllPointRulesFlow(): Flow<List<PointRuleEntity>>

    @Query("SELECT * FROM point_rules ORDER BY minHours DESC")
    suspend fun getAllPointRules(): List<PointRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPointRule(rule: PointRuleEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPointRules(rules: List<PointRuleEntity>)

    @Query("DELETE FROM point_rules WHERE id = :id")
    suspend fun deletePointRule(id: Long)

    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSettingsEntity)
}
