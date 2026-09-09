package com.highflyerpro.tracker.presentation.screens.pigeons

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.highflyerpro.tracker.R
import com.highflyerpro.tracker.data.local.entities.BreedingRecordEntity
import com.highflyerpro.tracker.data.local.entities.HealthRecordEntity
import com.highflyerpro.tracker.data.local.entities.NoteEntity
import com.highflyerpro.tracker.data.local.entities.PhotoCategories
import com.highflyerpro.tracker.data.local.entities.PhotoEntity
import com.highflyerpro.tracker.data.local.entities.PigeonEntity
import com.highflyerpro.tracker.domain.calculation.PerformanceEngine
import com.highflyerpro.tracker.presentation.components.AddPhotoMetadataDialog
import com.highflyerpro.tracker.presentation.components.ConfirmDeletePhotoDialog
import com.highflyerpro.tracker.presentation.components.EditPhotoMetadataDialog
import com.highflyerpro.tracker.presentation.components.OutdoorStatCard
import com.highflyerpro.tracker.presentation.components.PhotoFullScreenViewerDialog
import com.highflyerpro.tracker.presentation.components.PhotoSourceSelectionDialog
import com.highflyerpro.tracker.presentation.components.PigeonAvatar
import com.highflyerpro.tracker.presentation.components.PigeonStatusChip
import com.highflyerpro.tracker.presentation.viewmodel.MainViewModel
import com.highflyerpro.tracker.util.PhotoStorageManager
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PigeonDetailScreen(
    viewModel: MainViewModel,
    pigeonId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToPigeonDetail: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var pigeon by remember { mutableStateOf<PigeonEntity?>(null) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Tab data flows
    val pigeonFlow = viewModel.repository.getPigeonByIdFlow(pigeonId).collectAsState(initial = null)
    val flightRecords by viewModel.repository.getRecordsByPigeonFlow(pigeonId).collectAsState(initial = emptyList())
    val events by viewModel.repository.getEventsByPigeonFlow(pigeonId).collectAsState(initial = emptyList())
    val healthRecords by viewModel.repository.getHealthRecordsByPigeonFlow(pigeonId).collectAsState(initial = emptyList())
    val breedingRecords by viewModel.repository.getBreedingRecordsByPigeonFlow(pigeonId).collectAsState(initial = emptyList())
    val achievements by viewModel.repository.getAchievementsByPigeonFlow(pigeonId).collectAsState(initial = emptyList())
    val photosList by viewModel.getPhotosForPigeonFlow(pigeonId).collectAsState(initial = emptyList())
    val notesList by viewModel.repository.getNotesByPigeonFlow(pigeonId).collectAsState(initial = emptyList())

    var familyMap by remember { mutableStateOf<Map<String, Any?>>(emptyMap()) }

    // Dialog States
    var showAddHealthDialog by remember { mutableStateOf(false) }
    var showAddBreedingDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showArchiveConfirmDialog by remember { mutableStateOf(false) }

    pigeon = pigeonFlow.value

    LaunchedEffect(pigeonId) {
        familyMap = viewModel.repository.getFamilyTree(pigeonId)
    }

    val tabs = listOf(
        "Overview", "Flights", "Performance", "Training",
        "Health", "Breeding", "Family", "Tournaments",
        "Achievements", "Photos", "Notes", "Timeline"
    )

    val landed = flightRecords.filter { it.status == "LANDED" && (it.durationMillis ?: 0L) > 0 }
    val totalFlightMillis = landed.sumOf { it.durationMillis ?: 0L }
    val avgMillis = if (landed.isNotEmpty()) totalFlightMillis / landed.size else 0L
    val bestFlightMillis = landed.maxOfOrNull { it.durationMillis ?: 0L } ?: 0L
    val consistencyScore = PerformanceEngine.calculateConsistencyScore(flightRecords)
    val performanceScore = PerformanceEngine.calculatePerformanceScore(flightRecords)
    val form = PerformanceEngine.calculateCurrentForm(flightRecords)
    val workload = PerformanceEngine.calculateTrainingWorkload(flightRecords)
    val insights = PerformanceEngine.generateInsights(flightRecords, pigeon?.name ?: "Pigeon")

    if (pigeon == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Pigeon Profile") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(padding)) {
                Text("Loading Pigeon Details...")
            }
        }
        return
    }

    val p = pigeon!!

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = p.name, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text(
                            text = p.pigeonId,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(p.pigeonId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                    }
                    IconButton(onClick = { showArchiveConfirmDialog = true }) {
                        Icon(Icons.Default.Archive, contentDescription = "Archive")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Profile Header Summary Card
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    PigeonAvatar(
                        photoUri = p.photoUri,
                        name = p.name,
                        gender = p.gender,
                        size = 56
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = p.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            if (p.nickname.isNotBlank()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${p.nickname})",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Ring: ${p.ringNumber.ifBlank { "Unbanded" }} • ${p.gender} • ${p.breed}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PigeonStatusChip(status = p.status)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Form: $form",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (form == "Excellent") Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$performanceScore",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "SCORE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // 12 Tabs Scrollable Row
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            // Tab Content
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTabIndex) {
                    0 -> OverviewTab(landed.size, totalFlightMillis, avgMillis, bestFlightMillis, consistencyScore, insights)
                    1 -> FlightsTab(flightRecords)
                    2 -> PerformanceTab(consistencyScore, performanceScore, form, workload, flightRecords)
                    3 -> TrainingTab(flightRecords, workload)
                    4 -> HealthTab(healthRecords, onAddHealth = { showAddHealthDialog = true })
                    5 -> BreedingTab(breedingRecords, onAddBreeding = { showAddBreedingDialog = true })
                    6 -> FamilyTab(familyMap, onNavigateToPigeonDetail)
                    7 -> TournamentsTab(p.pigeonId)
                    8 -> AchievementsTab(achievements)
                    9 -> PhotosTab(pigeon = p, photos = photosList, viewModel = viewModel)
                    10 -> NotesTab(notesList, onAddNote = { showAddNoteDialog = true })
                    11 -> TimelineTab(events)
                }
            }
        }
    }

    // Add Health Record Dialog
    if (showAddHealthDialog) {
        var hTitle by remember { mutableStateOf("") }
        var hType by remember { mutableStateOf("General Check") }
        var hNotes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddHealthDialog = false },
            title = { Text("Log Health Examination") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = hTitle, onValueChange = { hTitle = it }, label = { Text("Title (e.g. Feather Condition, Vitamin Dose)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = hNotes, onValueChange = { hNotes = it }, label = { Text("Clinical Notes & Observation") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (hTitle.isNotBlank()) {
                            coroutineScope.launch {
                                viewModel.repository.addHealthRecord(
                                    HealthRecordEntity(pigeonId = pigeonId, type = hType, title = hTitle, description = hNotes, notes = hNotes)
                                )
                                showAddHealthDialog = false
                            }
                        }
                    }
                ) { Text("Save Health Log") }
            },
            dismissButton = { TextButton(onClick = { showAddHealthDialog = false }) { Text("Cancel") } }
        )
    }

    // Add Note Dialog
    if (showAddNoteDialog) {
        var noteTitle by remember { mutableStateOf("") }
        var noteBody by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text("Add Loft Note") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = noteTitle, onValueChange = { noteTitle = it }, label = { Text("Subject (e.g. Feeding tweak, Loft behavior)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = noteBody, onValueChange = { noteBody = it }, label = { Text("Content") }, maxLines = 4, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteBody.isNotBlank()) {
                            coroutineScope.launch {
                                viewModel.repository.addNote(NoteEntity(pigeonId = pigeonId, title = noteTitle, content = noteBody))
                                showAddNoteDialog = false
                            }
                        }
                    }
                ) { Text("Add Note") }
            },
            dismissButton = { TextButton(onClick = { showAddNoteDialog = false }) { Text("Cancel") } }
        )
    }

    // Archive Confirmation Dialog
    if (showArchiveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showArchiveConfirmDialog = false },
            title = { Text("Archive Pigeon") },
            text = { Text("Are you sure you want to archive ${p.name}? Historical flight records will remain intact.") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.repository.setPigeonArchiveStatus(p.pigeonId, true)
                            showArchiveConfirmDialog = false
                            onNavigateBack()
                        }
                    }
                ) { Text("Confirm Archive") }
            },
            dismissButton = { TextButton(onClick = { showArchiveConfirmDialog = false }) { Text("Cancel") } }
        )
    }
}

// ----------------------------------------------------
// TAB IMPLEMENTATIONS
// ----------------------------------------------------

@Composable
fun OverviewTab(
    flightsCount: Int,
    totalFlightMillis: Long,
    avgMillis: Long,
    bestFlightMillis: Long,
    consistencyScore: Int,
    insights: List<String>
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text("LIFETIME METRICS", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutdoorStatCard("Total Flights", "$flightsCount", "Completed", icon = Icons.Default.FlightLand, modifier = Modifier.weight(1f))
                OutdoorStatCard("Lifetime Flight", PerformanceEngine.formatDurationLong(totalFlightMillis), "Accumulated", icon = Icons.Default.Timer, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutdoorStatCard("Best Flight", PerformanceEngine.formatDurationHMS(bestFlightMillis), "Personal Record", icon = Icons.Default.Star, modifier = Modifier.weight(1f))
                OutdoorStatCard("Avg Duration", PerformanceEngine.formatDurationHMS(avgMillis), "Per Session", icon = Icons.Default.DateRange, modifier = Modifier.weight(1f))
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("SMART LOFT INSIGHTS", fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    insights.forEach { tip ->
                        Text("• $tip", fontSize = 13.sp, modifier = Modifier.padding(vertical = 3.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FlightsTab(records: List<com.highflyerpro.tracker.data.local.entities.FlightRecordEntity>) {
    if (records.isEmpty()) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(32.dp)) {
            Text("No flight sessions logged for this pigeon yet.")
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(records) { r ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (r.landingPosition != null) "#${r.landingPosition}" else "-",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = PerformanceEngine.formatTimestamp(r.releaseTimestamp, "dd MMM yyyy, hh:mm a"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Status: ${r.status}${if (r.notes.isNotBlank()) " • ${r.notes}" else ""}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Text(
                            text = PerformanceEngine.formatDurationHMS(r.durationMillis ?: 0L),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PerformanceTab(
    consistencyScore: Int,
    performanceScore: Int,
    form: String,
    workload: String,
    records: List<com.highflyerpro.tracker.data.local.entities.FlightRecordEntity>
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutdoorStatCard("Consistency", "$consistencyScore / 100", "Duration Variance", icon = Icons.Default.Star, modifier = Modifier.weight(1f))
                OutdoorStatCard("Training Workload", workload, "7-day activity", icon = Icons.Default.Work, modifier = Modifier.weight(1f))
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("EVENT & FLIGHT PERFORMANCE METRICS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Composite Performance Score: $performanceScore / 100", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Calculated from endurance hours (40%), consistency index (20%), improvement trajectory (15%), best flight record (10%), and flight reliability.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
fun TrainingTab(records: List<com.highflyerpro.tracker.data.local.entities.FlightRecordEntity>, workload: String) {
    LazyColumn(contentPadding = PaddingValues(16.dp), modifier = Modifier.fillMaxSize()) {
        item {
            Text("Training Schedule & Conditioning", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Current Training Load: $workload", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Total Training Logs: ${records.size}", fontSize = 13.sp)
        }
    }
}

@Composable
fun HealthTab(records: List<HealthRecordEntity>, onAddHealth: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Health & Veterinary Records", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Button(onClick = onAddHealth) { Text("Log Health") }
        }
        Spacer(modifier = Modifier.height(10.dp))
        if (records.isEmpty()) {
            Text("No health concerns logged. Bird in prime condition.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(records) { h ->
                    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(h.title, fontWeight = FontWeight.Bold)
                            Text("${h.type} • Status: ${h.status}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            if (h.description.isNotBlank()) Text(h.description, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BreedingTab(records: List<BreedingRecordEntity>, onAddBreeding: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Pedigree & Breeding History", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(10.dp))
        if (records.isEmpty()) {
            Text("No breeding pairings logged.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(records) { b ->
                    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Pair with ${b.partnerName.ifBlank { "Unregistered Partner" }}", fontWeight = FontWeight.Bold)
                            Text("Eggs: ${b.eggCount} • Offspring: ${b.offspringCount}", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FamilyTab(familyMap: Map<String, Any?>, onNavigateToPigeonDetail: (String) -> Unit) {
    val father = familyMap["father"] as? PigeonEntity
    val mother = familyMap["mother"] as? PigeonEntity
    val paternalGrandfather = familyMap["paternalGrandfather"] as? PigeonEntity
    val maternalGrandfather = familyMap["maternalGrandfather"] as? PigeonEntity
    val siblings = (familyMap["siblings"] as? List<*>)?.filterIsInstance<PigeonEntity>() ?: emptyList()

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
        item {
            Text("PEDIGREE BLOODLINE TREE", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
        }
        item {
            Card(shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Father (Sire)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    if (father != null) {
                        Text(father.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.clickable { onNavigateToPigeonDetail(father.pigeonId) })
                        Text("Ring: ${father.ringNumber.ifBlank { "--" }} • Breed: ${father.breed}", fontSize = 12.sp)
                    } else {
                        Text("Unknown / Not assigned", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Mother (Dam)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    if (mother != null) {
                        Text(mother.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.clickable { onNavigateToPigeonDetail(mother.pigeonId) })
                        Text("Ring: ${mother.ringNumber.ifBlank { "--" }} • Breed: ${mother.breed}", fontSize = 12.sp)
                    } else {
                        Text("Unknown / Not assigned", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            }
        }
        if (siblings.isNotEmpty()) {
            item {
                Text("Full Blood Siblings (${siblings.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            items(siblings) { sib ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToPigeonDetail(sib.pigeonId) }
                ) {
                    Text("${sib.name} (${sib.ringNumber})", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun TournamentsTab(pigeonId: String) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("No tournament entries logged yet for this pigeon.")
    }
}

@Composable
fun AchievementsTab(achievements: List<com.highflyerpro.tracker.data.local.entities.AchievementEntity>) {
    if (achievements.isEmpty()) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Text("Complete endurance flights to unlock badges and club honors.")
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(achievements) { a ->
                Card(shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(14.dp)) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(a.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(a.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PhotosTab(
    pigeon: PigeonEntity,
    photos: List<PhotoEntity>,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedCategory by remember { mutableStateOf("All") }
    var isGridView by remember { mutableStateOf(true) }

    // Dialog & Action States
    var showSourceSelectionDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var pickedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showAddMetadataDialog by remember { mutableStateOf(false) }
    var isSavingPhoto by remember { mutableStateOf(false) }

    var viewerPhoto by remember { mutableStateOf<PhotoEntity?>(null) }
    var editPhoto by remember { mutableStateOf<PhotoEntity?>(null) }
    var deleteConfirmPhoto by remember { mutableStateOf<PhotoEntity?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            pickedImageUri = uri
            showAddMetadataDialog = true
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            val uri = tempCameraUri
            if (uri != null) {
                pickedImageUri = uri
                showAddMetadataDialog = true
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val tempUri = PhotoStorageManager.createTempCameraUri(context, pigeon.pigeonId)
            tempCameraUri = tempUri
            takePictureLauncher.launch(tempUri)
        }
    }

    // Filter photos based on category
    val filteredPhotos = remember(photos, selectedCategory) {
        if (selectedCategory == "All") {
            photos
        } else {
            photos.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontal Scrollable Category Chips
        val categories = listOf("All") + PhotoCategories.ALL
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val count = if (cat == "All") photos.size else photos.count { it.category.equals(cat, ignoreCase = true) }
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text("$cat ($count)", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Subheader: Count, Grid/List toggle, and "Add Photo" button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Text(
                text = "${filteredPhotos.size} Photo${if (filteredPhotos.size == 1) "" else "s"}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(onClick = { isGridView = true }) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = "Grid View",
                        tint = if (isGridView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                IconButton(onClick = { isGridView = false }) {
                    Icon(
                        imageVector = Icons.Default.ViewList,
                        contentDescription = "List View",
                        tint = if (!isGridView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                Button(
                    onClick = { showSourceSelectionDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_gallery_photo_button")
                ) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Photo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (filteredPhotos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    Text(
                        text = if (photos.isEmpty()) "No Photos in Gallery" else "No Photos in \"$selectedCategory\"",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Text(
                        text = if (photos.isEmpty())
                            "Add pigeon photos from camera or gallery to track eye signs, flight form, feathers, and achievements."
                        else "No photos match the selected filter category.",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = { showSourceSelectionDialog = true },
                        modifier = Modifier.testTag("add_first_photo_button")
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add First Photo")
                    }
                }
            }
        } else if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredPhotos, key = { it.photoId }) { photo ->
                    PhotoGridCard(
                        photo = photo,
                        onClick = { viewerPhoto = photo }
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredPhotos, key = { it.photoId }) { photo ->
                    PhotoListCard(
                        photo = photo,
                        onClick = { viewerPhoto = photo },
                        onDelete = { deleteConfirmPhoto = photo }
                    )
                }
            }
        }
    }

    // Photo Source Selection (Camera vs Gallery)
    if (showSourceSelectionDialog) {
        PhotoSourceSelectionDialog(
            onDismiss = { showSourceSelectionDialog = false },
            onCameraSelect = {
                showSourceSelectionDialog = false
                val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    val tempUri = PhotoStorageManager.createTempCameraUri(context, pigeon.pigeonId)
                    tempCameraUri = tempUri
                    takePictureLauncher.launch(tempUri)
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onGallerySelect = {
                showSourceSelectionDialog = false
                galleryLauncher.launch(
                    androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            title = "Add Pigeon Photo"
        )
    }

    // Metadata Dialog after photo picked/captured
    if (showAddMetadataDialog && pickedImageUri != null) {
        AddPhotoMetadataDialog(
            imageUri = pickedImageUri!!,
            isSaving = isSavingPhoto,
            onDismiss = {
                showAddMetadataDialog = false
                pickedImageUri = null
            },
            onSave = { category, caption, isProfile ->
                isSavingPhoto = true
                coroutineScope.launch {
                    val result = viewModel.saveAndAddPigeonPhoto(
                        pigeonId = pigeon.pigeonId,
                        sourceUri = pickedImageUri!!,
                        category = category,
                        caption = caption,
                        isProfile = isProfile
                    )
                    isSavingPhoto = false
                    showAddMetadataDialog = false
                    pickedImageUri = null
                }
            }
        )
    }

    // Full Screen Zoomable Viewer Dialog
    viewerPhoto?.let { photo ->
        PhotoFullScreenViewerDialog(
            photo = photo,
            onDismiss = { viewerPhoto = null },
            onSetAsProfile = {
                viewModel.setPigeonProfilePhoto(pigeon.pigeonId, photo.photoId, photo.filePath)
                viewerPhoto = photo.copy(isProfilePhoto = true)
            },
            onEditDetails = {
                editPhoto = photo
            },
            onDeletePhoto = {
                deleteConfirmPhoto = photo
            }
        )
    }

    // Edit Metadata Dialog
    editPhoto?.let { photo ->
        EditPhotoMetadataDialog(
            photo = photo,
            onDismiss = { editPhoto = null },
            onSave = { category, caption, isProfile ->
                viewModel.updatePhotoMetadata(
                    photoId = photo.photoId,
                    caption = caption,
                    category = category,
                    isProfilePhoto = isProfile,
                    pigeonId = pigeon.pigeonId,
                    filePath = photo.filePath
                )
                if (viewerPhoto?.photoId == photo.photoId) {
                    viewerPhoto = photo.copy(category = category, caption = caption, isProfilePhoto = isProfile)
                }
                editPhoto = null
            }
        )
    }

    // Delete Confirmation Dialog
    deleteConfirmPhoto?.let { photo ->
        ConfirmDeletePhotoDialog(
            onDismiss = { deleteConfirmPhoto = null },
            onConfirm = {
                viewModel.deletePigeonPhoto(photo)
                if (viewerPhoto?.photoId == photo.photoId) {
                    viewerPhoto = null
                }
                deleteConfirmPhoto = null
            }
        )
    }
}

@Composable
fun PhotoGridCard(
    photo: PhotoEntity,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable { onClick() }
            .testTag("photo_grid_item_${photo.photoId}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val imageModel = if (photo.filePath.isNotBlank()) File(photo.filePath) else photo.photoUri
            AsyncImage(
                model = imageModel,
                contentDescription = photo.caption.ifBlank { "Pigeon Photo" },
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.ic_pigeon_placeholder),
                modifier = Modifier.fillMaxSize()
            )

            // Top-right Profile Star badge
            if (photo.isProfilePhoto) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopEnd)
                        .size(26.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Profile Photo",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Bottom Gradient Scrim with details
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
                    .padding(8.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                        ) {
                            Text(
                                text = photo.category,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = PerformanceEngine.formatTimestamp(photo.dateAdded, "dd MMM"),
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    if (photo.caption.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = photo.caption,
                            fontSize = 11.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PhotoListCard(
    photo: PhotoEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("photo_list_item_${photo.photoId}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.05f))
            ) {
                val imageModel = if (photo.filePath.isNotBlank()) File(photo.filePath) else photo.photoUri
                AsyncImage(
                    model = imageModel,
                    contentDescription = photo.caption.ifBlank { "Pigeon Photo" },
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.ic_pigeon_placeholder),
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = photo.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (photo.isProfilePhoto) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Profile",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (photo.caption.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = photo.caption,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Added: ${PerformanceEngine.formatTimestamp(photo.dateAdded, "dd MMM yyyy, hh:mm a")}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Photo",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun NotesTab(notesList: List<NoteEntity>, onAddNote: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Loft Notes & Observations", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Button(onClick = onAddNote) { Text("Add Note") }
        }
        Spacer(modifier = Modifier.height(10.dp))
        if (notesList.isEmpty()) {
            Text("No notes recorded yet.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(notesList) { n ->
                    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (n.title.isNotBlank()) Text(n.title, fontWeight = FontWeight.Bold)
                            Text(n.content, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineTab(events: List<com.highflyerpro.tracker.data.local.entities.PigeonEventEntity>) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(events) { e ->
            Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                    Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(e.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(e.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                    Text(PerformanceEngine.formatTimeOnly(e.timestamp), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        }
    }
}
