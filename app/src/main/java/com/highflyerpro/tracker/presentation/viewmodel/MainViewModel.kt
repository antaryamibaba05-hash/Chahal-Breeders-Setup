package com.highflyerpro.tracker.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.highflyerpro.tracker.data.local.AppDatabase
import com.highflyerpro.tracker.data.local.entities.BreedingRecordEntity
import com.highflyerpro.tracker.data.local.entities.FlightRecordEntity
import com.highflyerpro.tracker.data.local.entities.FlightSessionEntity
import com.highflyerpro.tracker.data.local.entities.GroupEntity
import com.highflyerpro.tracker.data.local.entities.HealthRecordEntity
import com.highflyerpro.tracker.data.local.entities.NoteEntity
import com.highflyerpro.tracker.data.local.entities.PhotoEntity
import com.highflyerpro.tracker.data.local.entities.PigeonEntity
import com.highflyerpro.tracker.data.local.entities.PigeonEventEntity
import com.highflyerpro.tracker.data.local.entities.PointRuleEntity
import com.highflyerpro.tracker.data.local.entities.SeasonEntity
import com.highflyerpro.tracker.data.local.entities.TournamentEntity
import com.highflyerpro.tracker.data.repository.HighFlyerRepository
import com.highflyerpro.tracker.data.repository.LandingMode
import com.highflyerpro.tracker.data.repository.LoftMasterRepository
import com.highflyerpro.tracker.data.repository.PreferencesRepository
import com.highflyerpro.tracker.data.repository.ThemeMode
import com.highflyerpro.tracker.domain.calculation.PerformanceEngine
import com.highflyerpro.tracker.domain.model.LeaderboardEntry
import com.highflyerpro.tracker.domain.model.PigeonWithStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class DisplayMode { GRID, LIST, COMPACT }

enum class PigeonSortOrder {
    NAME_AZ, NAME_ZA, NEWEST, OLDEST, RING_NUMBER, BEST_PERFORMANCE, MOST_ACTIVE, HIGHEST_HOURS
}

enum class LeaderboardTimeframe { TODAY, WEEK, MONTH, YEAR, LIFETIME }

enum class LeaderboardRankingMode {
    LONGEST_FLIGHT, LANDING_ORDER, AVERAGE_FLIGHT, TOTAL_FLIGHT_TIME, PERFORMANCE_SCORE, CONSISTENCY, CUSTOM_POINTS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val repository = HighFlyerRepository(database)
    val loftMasterRepository = LoftMasterRepository(database)
    val preferencesRepository = PreferencesRepository(application)

    // UI Preferences
    val themeMode: StateFlow<ThemeMode> = preferencesRepository.themeModeFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM
    )
    val landingMode: StateFlow<LandingMode> = preferencesRepository.landingModeFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), LandingMode.CONFIRMATION
    )

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch {
        preferencesRepository.setThemeMode(mode)
    }

    fun setLandingMode(mode: LandingMode) = viewModelScope.launch {
        preferencesRepository.setLandingMode(mode)
    }

    // Search, Filter, Sort, View Modes
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow<String?>(null)
    val selectedStatusFilter = _selectedStatusFilter.asStateFlow()

    private val _selectedGenderFilter = MutableStateFlow<String?>(null)
    val selectedGenderFilter = _selectedGenderFilter.asStateFlow()

    private val _selectedBreedFilter = MutableStateFlow<String?>(null)
    val selectedBreedFilter = _selectedBreedFilter.asStateFlow()

    private val _selectedGroupFilter = MutableStateFlow<String?>(null)
    val selectedGroupFilter = _selectedGroupFilter.asStateFlow()

    private val _sortOrder = MutableStateFlow(PigeonSortOrder.NAME_AZ)
    val sortOrder = _sortOrder.asStateFlow()

    private val _displayMode = MutableStateFlow(DisplayMode.LIST)
    val displayMode = _displayMode.asStateFlow()

    private val _selectedPigeonIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedPigeonIds = _selectedPigeonIds.asStateFlow()

    // Live Timer State (Calculated strictly from Release Timestamp!)
    private val _currentLiveTimeMillis = MutableStateFlow(System.currentTimeMillis())
    val currentLiveTimeMillis = _currentLiveTimeMillis.asStateFlow()

    // Active session recovery banner dismissal state
    private val _dismissedRecoverySessionId = MutableStateFlow<String?>(null)
    val dismissedRecoverySessionId = _dismissedRecoverySessionId.asStateFlow()

    // Landing Confirmation Dialog state
    private val _pendingLandingRecord = MutableStateFlow<FlightRecordEntity?>(null)
    val pendingLandingRecord = _pendingLandingRecord.asStateFlow()

    // Landing Correction Dialog state
    private val _recordToCorrect = MutableStateFlow<FlightRecordEntity?>(null)
    val recordToCorrect = _recordToCorrect.asStateFlow()

    // Leaderboard state
    private val _leaderboardTimeframe = MutableStateFlow(LeaderboardTimeframe.LIFETIME)
    val leaderboardTimeframe = _leaderboardTimeframe.asStateFlow()

    private val _leaderboardRankingMode = MutableStateFlow(LeaderboardRankingMode.LONGEST_FLIGHT)
    val leaderboardRankingMode = _leaderboardRankingMode.asStateFlow()

    // Comparison Selected Pigeons
    private val _comparisonPigeonIds = MutableStateFlow<List<String>>(emptyList())
    val comparisonPigeonIds = _comparisonPigeonIds.asStateFlow()

    // Repository Flows
    val allPigeons = repository.allPigeonsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allGroups = repository.allGroupsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allSessions = repository.allSessionsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val activeSession: StateFlow<FlightSessionEntity?> = repository.activeSessionFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    val allFlightRecords = repository.allFlightRecordsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val pointRules = repository.allPointRulesFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allSeasons = repository.allSeasonsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allTournaments = repository.allTournamentsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val recentEvents = repository.getRecentEventsFlow().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Combined Pigeons with Computed Statistics & Filtered/Sorted
    val filteredPigeonsWithStats: StateFlow<List<PigeonWithStats>> = combine(
        allPigeons,
        allFlightRecords,
        allGroups,
        searchQuery,
        selectedStatusFilter,
        selectedGenderFilter,
        selectedBreedFilter,
        selectedGroupFilter,
        sortOrder
    ) { args ->
        val pigeons = args[0] as List<PigeonEntity>
        val records = args[1] as List<FlightRecordEntity>
        val groups = args[2] as List<GroupEntity>
        val query = (args[3] as String).trim().lowercase()
        val statusF = args[4] as String?
        val genderF = args[5] as String?
        val breedF = args[6] as String?
        val groupF = args[7] as String?
        val sort = args[8] as PigeonSortOrder

        val recordsByPigeon = records.groupBy { it.pigeonId }

        val withStats = pigeons.map { p ->
            val pRecords = recordsByPigeon[p.pigeonId] ?: emptyList()
            val landed = pRecords.filter { it.status == "LANDED" && (it.durationMillis ?: 0L) > 0 }
            val totalFlights = landed.size
            val totalMillis = landed.sumOf { it.durationMillis ?: 0L }
            val avgMillis = if (totalFlights > 0) totalMillis / totalFlights else 0L
            val bestMillis = landed.maxOfOrNull { it.durationMillis ?: 0L } ?: 0L
            val worstMillis = landed.minOfOrNull { it.durationMillis ?: 0L } ?: 0L
            val lastFlight = pRecords.maxByOrNull { it.releaseTimestamp }

            val score = PerformanceEngine.calculatePerformanceScore(pRecords)
            val consistency = PerformanceEngine.calculateConsistencyScore(pRecords)
            val form = PerformanceEngine.calculateCurrentForm(pRecords)

            PigeonWithStats(
                pigeon = p,
                totalFlights = totalFlights,
                totalFlightMillis = totalMillis,
                averageFlightMillis = avgMillis,
                bestFlightMillis = bestMillis,
                worstFlightMillis = worstMillis,
                currentForm = form,
                performanceScore = score,
                consistencyScore = consistency,
                lastFlightTimestamp = lastFlight?.releaseTimestamp,
                lastFlightDurationMillis = lastFlight?.durationMillis
            )
        }

        // Apply filters
        var filtered = withStats.filter { !it.pigeon.isArchived }

        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.pigeon.name.lowercase().contains(query) ||
                        it.pigeon.nickname.lowercase().contains(query) ||
                        it.pigeon.ringNumber.lowercase().contains(query) ||
                        it.pigeon.color.lowercase().contains(query) ||
                        it.pigeon.breed.lowercase().contains(query) ||
                        it.pigeon.pigeonId.lowercase().contains(query)
            }
        }

        if (statusF != null) {
            filtered = filtered.filter { it.pigeon.status.equals(statusF, ignoreCase = true) }
        }
        if (genderF != null) {
            filtered = filtered.filter { it.pigeon.gender.equals(genderF, ignoreCase = true) }
        }
        if (breedF != null) {
            filtered = filtered.filter { it.pigeon.breed.equals(breedF, ignoreCase = true) }
        }

        // Sort
        when (sort) {
            PigeonSortOrder.NAME_AZ -> filtered.sortedBy { it.pigeon.name.lowercase() }
            PigeonSortOrder.NAME_ZA -> filtered.sortedByDescending { it.pigeon.name.lowercase() }
            PigeonSortOrder.NEWEST -> filtered.sortedByDescending { it.pigeon.createdAt }
            PigeonSortOrder.OLDEST -> filtered.sortedBy { it.pigeon.createdAt }
            PigeonSortOrder.RING_NUMBER -> filtered.sortedBy { it.pigeon.ringNumber }
            PigeonSortOrder.BEST_PERFORMANCE -> filtered.sortedByDescending { it.performanceScore }
            PigeonSortOrder.MOST_ACTIVE -> filtered.sortedByDescending { it.totalFlights }
            PigeonSortOrder.HIGHEST_HOURS -> filtered.sortedByDescending { it.totalFlightMillis }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Leaderboard Entries
    val leaderboardEntries: StateFlow<List<LeaderboardEntry>> = combine(
        allPigeons,
        allFlightRecords,
        pointRules,
        leaderboardTimeframe,
        leaderboardRankingMode
    ) { pigeons, records, rules, timeframe, mode ->
        val now = System.currentTimeMillis()
        val startTime = when (timeframe) {
            LeaderboardTimeframe.TODAY -> now - (24 * 3600 * 1000L)
            LeaderboardTimeframe.WEEK -> now - (7 * 24 * 3600 * 1000L)
            LeaderboardTimeframe.MONTH -> now - (30 * 24 * 3600 * 1000L)
            LeaderboardTimeframe.YEAR -> now - (365 * 24 * 3600 * 1000L)
            LeaderboardTimeframe.LIFETIME -> 0L
        }

        val filteredRecords = if (startTime == 0L) records else records.filter { it.releaseTimestamp >= startTime }
        val recordsByPigeon = filteredRecords.groupBy { it.pigeonId }

        val entries = pigeons.mapNotNull { p ->
            val pRecords = recordsByPigeon[p.pigeonId] ?: emptyList()
            val landed = pRecords.filter { it.status == "LANDED" && (it.durationMillis ?: 0L) > 0 }
            if (landed.isEmpty() && timeframe != LeaderboardTimeframe.LIFETIME) return@mapNotNull null

            val totalDuration = landed.sumOf { it.durationMillis ?: 0L }
            val avgDuration = if (landed.isNotEmpty()) totalDuration / landed.size else 0L
            val bestDuration = landed.maxOfOrNull { it.durationMillis ?: 0L } ?: 0L
            val points = landed.sumOf { PerformanceEngine.calculatePointsForDuration(it.durationMillis ?: 0L, rules) }
            val consistency = PerformanceEngine.calculateConsistencyScore(pRecords)
            val perfScore = PerformanceEngine.calculatePerformanceScore(pRecords)

            LeaderboardEntry(
                rank = 0,
                pigeonId = p.pigeonId,
                name = p.name,
                ringNumber = p.ringNumber,
                photoUri = p.photoUri,
                flightsCount = landed.size,
                totalDurationMillis = totalDuration,
                averageDurationMillis = avgDuration,
                bestDurationMillis = bestDuration,
                points = points,
                consistencyScore = consistency,
                performanceScore = perfScore
            )
        }

        val sorted = when (mode) {
            LeaderboardRankingMode.LONGEST_FLIGHT -> entries.sortedByDescending { it.bestDurationMillis }
            LeaderboardRankingMode.AVERAGE_FLIGHT -> entries.sortedByDescending { it.averageDurationMillis }
            LeaderboardRankingMode.TOTAL_FLIGHT_TIME -> entries.sortedByDescending { it.totalDurationMillis }
            LeaderboardRankingMode.PERFORMANCE_SCORE -> entries.sortedByDescending { it.performanceScore }
            LeaderboardRankingMode.CONSISTENCY -> entries.sortedByDescending { it.consistencyScore }
            LeaderboardRankingMode.CUSTOM_POINTS -> entries.sortedByDescending { it.points }
            LeaderboardRankingMode.LANDING_ORDER -> entries.sortedByDescending { it.bestDurationMillis }
        }

        sorted.mapIndexed { idx, entry -> entry.copy(rank = idx + 1) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Start persistent high-accuracy 1-second clock loop
        viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                _currentLiveTimeMillis.value = System.currentTimeMillis()
                delay(1000)
            }
        }

        // Ensure database defaults and seeds
        viewModelScope.launch(Dispatchers.IO) {
            repository.seedInitialDataIfEmpty()
        }
    }

    // Setters for UI controls
    fun setSearchQuery(q: String) { _searchQuery.value = q }
    fun setStatusFilter(s: String?) { _selectedStatusFilter.value = s }
    fun setGenderFilter(g: String?) { _selectedGenderFilter.value = g }
    fun setBreedFilter(b: String?) { _selectedBreedFilter.value = b }
    fun setSortOrder(s: PigeonSortOrder) { _sortOrder.value = s }
    fun setDisplayMode(m: DisplayMode) { _displayMode.value = m }
    fun setLeaderboardTimeframe(t: LeaderboardTimeframe) { _leaderboardTimeframe.value = t }
    fun setLeaderboardRankingMode(m: LeaderboardRankingMode) { _leaderboardRankingMode.value = m }

    fun togglePigeonSelection(id: String) {
        val set = _selectedPigeonIds.value.toMutableSet()
        if (set.contains(id)) set.remove(id) else set.add(id)
        _selectedPigeonIds.value = set
    }

    fun selectAllFilteredPigeons() {
        _selectedPigeonIds.value = filteredPigeonsWithStats.value.map { it.pigeon.pigeonId }.toSet()
    }

    fun clearPigeonSelection() {
        _selectedPigeonIds.value = emptySet()
    }

    fun toggleComparisonPigeon(id: String) {
        val list = _comparisonPigeonIds.value.toMutableList()
        if (list.contains(id)) {
            list.remove(id)
        } else if (list.size < 5) {
            list.add(id)
        }
        _comparisonPigeonIds.value = list
    }

    fun dismissRecoverySession(sessionId: String) {
        _dismissedRecoverySessionId.value = sessionId
    }

    // Landing Handlers
    fun onMarkLandedClicked(record: FlightRecordEntity) {
        if (landingMode.value == LandingMode.QUICK_ONE_TAP) {
            // Instant one-tap landing
            viewModelScope.launch {
                repository.markPigeonLanded(record.id, System.currentTimeMillis())
            }
        } else {
            // Open confirmation dialog
            _pendingLandingRecord.value = record
        }
    }

    fun confirmLanding(landingTimestamp: Long = System.currentTimeMillis(), notes: String = "") {
        val record = _pendingLandingRecord.value ?: return
        viewModelScope.launch {
            repository.markPigeonLanded(record.id, landingTimestamp, notes)
            _pendingLandingRecord.value = null
        }
    }

    fun cancelLanding() {
        _pendingLandingRecord.value = null
    }

    // Correction Handlers
    fun openCorrectionDialog(record: FlightRecordEntity) {
        _recordToCorrect.value = record
    }

    fun submitCorrection(newLandingTimestamp: Long?, newStatus: String, notes: String) {
        val record = _recordToCorrect.value ?: return
        viewModelScope.launch {
            repository.correctLandingRecord(record.id, newLandingTimestamp, newStatus, notes)
            _recordToCorrect.value = null
        }
    }

    fun closeCorrectionDialog() {
        _recordToCorrect.value = null
    }

    // Bulk actions
    fun bulkUpdateStatus(status: String) = viewModelScope.launch {
        val ids = _selectedPigeonIds.value.toList()
        if (ids.isNotEmpty()) {
            repository.bulkUpdateStatus(ids, status)
            clearPigeonSelection()
        }
    }

    fun bulkArchive() = viewModelScope.launch {
        val ids = _selectedPigeonIds.value.toList()
        if (ids.isNotEmpty()) {
            repository.bulkSetArchive(ids, true)
            clearPigeonSelection()
        }
    }

    fun bulkAddToGroup(groupId: String) = viewModelScope.launch {
        val ids = _selectedPigeonIds.value.toList()
        if (ids.isNotEmpty()) {
            repository.bulkAddToGroup(ids, groupId)
            clearPigeonSelection()
        }
    }

    // ----------------------------------------------------
    // PHOTO MANAGEMENT
    // ----------------------------------------------------
    fun getPhotosForPigeonFlow(pigeonId: String) = repository.getPhotosByPigeonFlow(pigeonId)

    suspend fun saveAndAddPigeonPhoto(
        pigeonId: String,
        sourceUri: android.net.Uri,
        category: String,
        caption: String,
        isProfile: Boolean
    ): Result<PhotoEntity> = withContext(Dispatchers.IO) {
        try {
            val app = getApplication<Application>()
            val processed = com.highflyerpro.tracker.util.PhotoStorageManager.savePigeonPhoto(
                context = app,
                sourceUri = sourceUri,
                pigeonId = pigeonId,
                isProfile = isProfile
            )
            val photoEntity = PhotoEntity(
                pigeonId = pigeonId,
                photoUri = processed.filePath,
                filePath = processed.filePath,
                thumbnailPath = processed.thumbnailPath,
                category = category,
                dateAdded = System.currentTimeMillis(),
                caption = caption,
                isProfilePhoto = isProfile
            )
            val insertedId = repository.addPhoto(photoEntity)
            Result.success(photoEntity.copy(id = insertedId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun setPigeonProfilePhoto(pigeonId: String, photoId: Long, filePath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setProfilePhoto(pigeonId, photoId, filePath)
        }
    }

    fun updatePhotoMetadata(photoId: Long, caption: String, category: String, isProfilePhoto: Boolean, pigeonId: String, filePath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updatePhotoCaptionAndCategory(photoId, caption, category)
            if (isProfilePhoto) {
                repository.setProfilePhoto(pigeonId, photoId, filePath)
            }
        }
    }

    fun deletePigeonPhoto(photo: PhotoEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePhoto(photo.id, deleteLocalFiles = true)
        }
    }
}
