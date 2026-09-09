package com.highflyerpro.tracker.presentation.screens.flight

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.highflyerpro.tracker.data.local.entities.FlightRecordEntity
import com.highflyerpro.tracker.domain.calculation.PerformanceEngine
import com.highflyerpro.tracker.presentation.components.AddEditManualFlightDialog
import com.highflyerpro.tracker.presentation.components.CreateEditEventDialog
import com.highflyerpro.tracker.presentation.components.LandingCorrectionDialog
import com.highflyerpro.tracker.presentation.components.OutdoorStatCard
import com.highflyerpro.tracker.presentation.components.PigeonAvatar
import com.highflyerpro.tracker.presentation.components.PigeonStatusChip
import com.highflyerpro.tracker.presentation.viewmodel.MainViewModel
import com.highflyerpro.tracker.ui.theme.GoldTrophy
import com.highflyerpro.tracker.ui.theme.SilverMedal
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightDetailScreen(
    viewModel: MainViewModel,
    sessionId: String,
    onNavigateBack: () -> Unit,
    onNavigateToPigeonDetail: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val sessionFlow = viewModel.repository.getSessionByIdFlow(sessionId).collectAsState(initial = null)
    val session = sessionFlow.value
    val records by viewModel.repository.getRecordsBySessionFlow(sessionId).collectAsState(initial = emptyList())
    val recordToCorrect by viewModel.recordToCorrect.collectAsState()
    val allPigeons by viewModel.allPigeons.collectAsState()
    val pigeonMap = remember(allPigeons) { allPigeons.associateBy { it.pigeonId } }

    var showEditEventDialog by remember { mutableStateOf(false) }
    var showAddManualRecordDialog by remember { mutableStateOf(false) }

    val landed = records.filter { it.status == "LANDED" && (it.durationMillis ?: 0L) > 0 }
    val totalFlightMillis = landed.sumOf { it.durationMillis ?: 0L }
    val avgFlightMillis = if (landed.isNotEmpty()) totalFlightMillis / landed.size else 0L
    val bestFlightMillis = landed.maxOfOrNull { it.durationMillis ?: 0L } ?: 0L

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = session?.name ?: "Flight Results", fontWeight = FontWeight.Bold)
                        if (session != null) {
                            Text(
                                text = "${session.eventType} • ${PerformanceEngine.formatTimestamp(session.releaseTimestamp)}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditEventDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Event Details")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddManualRecordDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Record")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutdoorStatCard("Total Flight Time", PerformanceEngine.formatDurationLong(totalFlightMillis), "${landed.size} Landed", icon = Icons.Default.Timer, modifier = Modifier.weight(1f))
                    OutdoorStatCard("Best Flight", PerformanceEngine.formatDurationHMS(bestFlightMillis), "Longest Duration", icon = Icons.Default.EmojiEvents, modifier = Modifier.weight(1f))
                }
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "FINAL LANDING CHRONOLOGICAL RESULTS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    TextButton(onClick = { showAddManualRecordDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add Record", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(records) { r ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (r.landingPosition) {
                                1 -> GoldTrophy
                                2 -> SilverMedal
                                3 -> Color(0xFFB45309)
                                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            },
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (r.landingPosition != null) "#${r.landingPosition}" else "-",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = if (r.landingPosition in 1..3) Color.White else MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        val pigeon = pigeonMap[r.pigeonId]
                        PigeonAvatar(
                            photoUri = pigeon?.photoUri ?: "",
                            name = r.pigeonName,
                            gender = pigeon?.gender ?: "Male",
                            size = 42
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = r.pigeonName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                text = "Ring: ${r.pigeonRingNumber.ifBlank { "Unbanded" }}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            if (r.landingTimestamp != null) {
                                Text(
                                    text = "Landed: ${PerformanceEngine.formatTimeOnly(r.landingTimestamp)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = PerformanceEngine.formatDurationHMS(r.durationMillis ?: 0L),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PigeonStatusChip(status = r.status)
                                IconButton(
                                    onClick = { viewModel.openCorrectionDialog(r) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditEventDialog && session != null) {
        CreateEditEventDialog(
            existingSession = session,
            onConfirm = { name, type, releaseTs, loc, rules, desc ->
                coroutineScope.launch {
                    viewModel.repository.updateFlightSession(
                        session.copy(
                            name = name,
                            eventType = type,
                            releaseTimestamp = releaseTs,
                            location = loc,
                            rules = rules,
                            description = desc
                        )
                    )
                    showEditEventDialog = false
                }
            },
            onDismiss = { showEditEventDialog = false }
        )
    }

    if (showAddManualRecordDialog) {
        AddEditManualFlightDialog(
            pigeons = allPigeons,
            sessionId = sessionId,
            onConfirm = { pigeonId, selSessionId, releaseTs, landingTs, directDur, status, notes ->
                coroutineScope.launch {
                    viewModel.repository.addManualFlightRecord(
                        sessionId = selSessionId ?: sessionId,
                        pigeonId = pigeonId,
                        releaseTimestamp = releaseTs,
                        landingTimestamp = landingTs,
                        directDurationMillis = directDur,
                        status = status,
                        notes = notes
                    )
                    showAddManualRecordDialog = false
                }
            },
            onDismiss = { showAddManualRecordDialog = false }
        )
    }

    if (recordToCorrect != null) {
        LandingCorrectionDialog(
            record = recordToCorrect!!,
            onConfirmCorrection = { newTime, newStatus, notes ->
                viewModel.submitCorrection(newTime, newStatus, notes)
            },
            onDismiss = { viewModel.closeCorrectionDialog() }
        )
    }
}
